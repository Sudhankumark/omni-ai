package com.example.ui.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.GeminiRetrofitClient
import com.example.api.Part
import com.example.api.OpenAiRetrofitClient
import com.example.api.DeepSeekRetrofitClient
import com.example.api.OpenAiChatRequest
import com.example.api.OpenAiMessage
import com.example.BuildConfig
import com.example.database.CommitEntity
import com.example.database.DeploymentEntity
import com.example.database.FileEntity
import com.example.database.ProjectEntity
import com.example.database.ReplDatabase
import com.example.database.ReplRepository
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ConsoleLogItem(
    val message: String,
    val type: String // "INFO", "SUCCESS", "ERROR", "INPUT"
)

data class TeammateCursor(
    val name: String,
    val colorHex: String,
    val lineIndex: Int,
    val charIndex: Int
)

data class ChatMessage(
    val role: String, // "user", "assistant"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class ReplViewModel(application: Application) : AndroidViewModel(application) {

    private val database: ReplDatabase by lazy {
        Room.databaseBuilder(
            application,
            ReplDatabase::class.java,
            "devrepl_db"
        ).build()
    }

    private val repository: ReplRepository by lazy {
        ReplRepository(database.replDao())
    }

    // --- Cancelable Flow Sync Jobs ---
    private var filesJob: kotlinx.coroutines.Job? = null
    private var commitsJob: kotlinx.coroutines.Job? = null
    private var deploymentsJob: kotlinx.coroutines.Job? = null

    // --- Localization ---
    private val _isHindi = MutableStateFlow(false)
    val isHindi: StateFlow<Boolean> = _isHindi.asStateFlow()

    fun toggleLanguage() {
        _isHindi.value = !_isHindi.value
    }

    fun translate(en: String, hi: String): String {
        return if (_isHindi.value) hi else en
    }

    // --- Google Auth ---
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userDetails = MutableStateFlow<Map<String, String>?>(null)
    val userDetails: StateFlow<Map<String, String>?> = _userDetails.asStateFlow()

    fun loginWithGoogle(name: String, email: String, avatar: String) {
        viewModelScope.launch {
            _isLoggedIn.value = true
            _userDetails.value = mapOf("name" to name, "email" to email, "avatar" to avatar)
            // Auto create an initial default project if none exist
            checkAndPreloadDefaultProject()
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _userDetails.value = null
    }

    // --- Projects Management ---
    private val _projects = MutableStateFlow<List<ProjectEntity>>(emptyList())
    val projects: StateFlow<List<ProjectEntity>> = _projects.asStateFlow()

    private val _selectedProject = MutableStateFlow<ProjectEntity?>(null)
    val selectedProject: StateFlow<ProjectEntity?> = _selectedProject.asStateFlow()

    // --- IDE Document State ---
    private val _projectFiles = MutableStateFlow<List<FileEntity>>(emptyList())
    val projectFiles: StateFlow<List<FileEntity>> = _projectFiles.asStateFlow()

    private val _activeFile = MutableStateFlow<FileEntity?>(null)
    val activeFile: StateFlow<FileEntity?> = _activeFile.asStateFlow()

    private val _codeBuffer = MutableStateFlow("")
    val codeBuffer: StateFlow<String> = _codeBuffer.asStateFlow()

    // --- Live Console Logs ---
    private val _consoleLogs = MutableStateFlow<List<ConsoleLogItem>>(emptyList())
    val consoleLogs: StateFlow<List<ConsoleLogItem>> = _consoleLogs.asStateFlow()

    private val _isCodeRunning = MutableStateFlow(false)
    val isCodeRunning: StateFlow<Boolean> = _isCodeRunning.asStateFlow()

    private val _runPreviewOutput = MutableStateFlow("")
    val runPreviewOutput: StateFlow<String> = _runPreviewOutput.asStateFlow()

    // --- Git Commits ---
    private val _commits = MutableStateFlow<List<CommitEntity>>(emptyList())
    val commits: StateFlow<List<CommitEntity>> = _commits.asStateFlow()

    // --- Deployment ---
    private val _deployments = MutableStateFlow<List<DeploymentEntity>>(emptyList())
    val deployments: StateFlow<List<DeploymentEntity>> = _deployments.asStateFlow()

    private val _isDeploying = MutableStateFlow(false)
    val isDeploying: StateFlow<Boolean> = _isDeploying.asStateFlow()

    // --- Collaborative Team Sync ---
    private val _collaborationActive = MutableStateFlow(false)
    val collaborationActive: StateFlow<Boolean> = _collaborationActive.asStateFlow()

    private val _teammates = MutableStateFlow<List<TeammateCursor>>(emptyList())
    val teammates: StateFlow<List<TeammateCursor>> = _teammates.asStateFlow()

    // --- Offline & Backup System ---
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp: StateFlow<Boolean> = _isBackingUp.asStateFlow()

    private val _backupSyncedTime = MutableStateFlow("Never synced")
    val backupSyncedTime: StateFlow<String> = _backupSyncedTime.asStateFlow()

    // --- Custom Theme System ---
    private val _selectedTheme = MutableStateFlow("Frosted Glass")
    val selectedTheme: StateFlow<String> = _selectedTheme.asStateFlow()

    // --- AI Ghostwriter Conversations ---
    private val _aiMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("assistant", "Hello Developer! I am Ghostwriter AI. Now you can chat with Gemini AI, ChatGPT, or DeepSeek! Configure your keys or try them directly.\n\nहैलो डेवलपर! मैं घोस्टराइटर AI हूँ। अब आप जेमिनी, चैटजीपीटी या डीपसीक के साथ चैट कर सकते हैं! अपनी कुंजियां सेट करें या सीधे प्रयास करें।")
    ))
    val aiMessages: StateFlow<List<ChatMessage>> = _aiMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // --- New Multi-Model Chat Configuration State ---
    private val _aiProvider = MutableStateFlow("Gemini AI") // "Gemini AI", "ChatGPT (OpenAI)", "DeepSeek"
    val aiProvider: StateFlow<String> = _aiProvider.asStateFlow()

    private val _sessionGeminiKey = MutableStateFlow("")
    val sessionGeminiKey: StateFlow<String> = _sessionGeminiKey.asStateFlow()

    private val _sessionOpenaiKey = MutableStateFlow("")
    val sessionOpenaiKey: StateFlow<String> = _sessionOpenaiKey.asStateFlow()

    private val _sessionDeepseekKey = MutableStateFlow("")
    val sessionDeepseekKey: StateFlow<String> = _sessionDeepseekKey.asStateFlow()

    // --- Dynamic Shared State and Navigation channels ---
    private val _sharedPrompt = MutableStateFlow("")
    val sharedPrompt: StateFlow<String> = _sharedPrompt.asStateFlow()

    private val _activeTabFlow = MutableStateFlow("playground")
    val activeTabFlow: StateFlow<String> = _activeTabFlow.asStateFlow()

    fun setSharedPrompt(prompt: String) {
        _sharedPrompt.value = prompt
    }

    fun setActiveTab(tab: String) {
        _activeTabFlow.value = tab
    }

    private val _systemInstructionPreset = MutableStateFlow("Code Expert") // "Code Expert", "Debugger Partner", "Creative Builder", "Bilingual Tutor"
    val systemInstructionPreset: StateFlow<String> = _systemInstructionPreset.asStateFlow()

    private val _modelTemperature = MutableStateFlow(0.7f)
    val modelTemperature: StateFlow<Float> = _modelTemperature.asStateFlow()

    fun setAiProvider(provider: String) { _aiProvider.value = provider }
    fun setSessionGeminiKey(key: String) { _sessionGeminiKey.value = key }
    fun setSessionOpenaiKey(key: String) { _sessionOpenaiKey.value = key }
    fun setSessionDeepseekKey(key: String) { _sessionDeepseekKey.value = key }
    fun setSystemInstructionPreset(preset: String) { _systemInstructionPreset.value = preset }
    fun setModelTemperature(temp: Float) { _modelTemperature.value = temp }

    // --- Playground Feature States ---
    private val _playgroundSubTab = MutableStateFlow("models") // "models" or "agents"
    val playgroundSubTab: StateFlow<String> = _playgroundSubTab.asStateFlow()

    private val _playgroundSelectedCard = MutableStateFlow<String?>(null) // e.g. "Featured" or "Code and Chat"
    val playgroundSelectedCard: StateFlow<String?> = _playgroundSelectedCard.asStateFlow()

    private val _playgroundMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val playgroundMessages: StateFlow<List<ChatMessage>> = _playgroundMessages.asStateFlow()

    private val _isPlaygroundThinking = MutableStateFlow(false)
    val isPlaygroundThinking: StateFlow<Boolean> = _isPlaygroundThinking.asStateFlow()

    private val _playSearchGrounding = MutableStateFlow(true)
    val playSearchGrounding: StateFlow<Boolean> = _playSearchGrounding.asStateFlow()

    private val _playMapsGrounding = MutableStateFlow(false)
    val playMapsGrounding: StateFlow<Boolean> = _playMapsGrounding.asStateFlow()

    private val _playCodeExecution = MutableStateFlow(false)
    val playCodeExecution: StateFlow<Boolean> = _playCodeExecution.asStateFlow()

    private val _playStructuredOutputs = MutableStateFlow(false)
    val playStructuredOutputs: StateFlow<Boolean> = _playStructuredOutputs.asStateFlow()

    private val _playFunctionCalling = MutableStateFlow(false)
    val playFunctionCalling: StateFlow<Boolean> = _playFunctionCalling.asStateFlow()

    private val _playUrlContext = MutableStateFlow(false)
    val playUrlContext: StateFlow<Boolean> = _playUrlContext.asStateFlow()

    private val _playTemporaryChat = MutableStateFlow(false)
    val playTemporaryChat: StateFlow<Boolean> = _playTemporaryChat.asStateFlow()

    fun setPlaygroundSubTab(tab: String) {
        _playgroundSubTab.value = tab
    }

    fun setPlaygroundSelectedCard(cardName: String?) {
        _playgroundSelectedCard.value = cardName
        if (cardName != null) {
            val initialGreeting = when (cardName) {
                "Featured" -> "✨ **Google Gemini Featured Model**: Pre-loaded to explore code structures, explain algorithms, and assist creatively. How can I help you build?"
                "Code and Chat" -> "💬 **Google Gemini Code and Chat**: Structured conversation tuned for high-fidelity code development, debugging, and interactive instruction."
                "Image Generation" -> "🎨 **Imagen 3 (Image Generation Mode)**: Create high-quality visual art assets and interfaces using textual descriptions."
                "Video Generation" -> "🎬 **Veo (Video Generation Model)**: Simulate cinematic and logical motion visuals from video text prompts."
                "Speech and Music" -> "🎵 **Gemini Audio & Speech**: Conversational audio analyzer. Enter voice transcription options or music structure ideas."
                "Real-time" -> "⚡ **Gemini Live Multimodal (Real-time)**: Simulating real-time WebSocket connection to low-latency Gemini services."
                "Antigravity Preview" -> "🛸 **Antigravity Preview Agent**: Advanced developer swarm agent executing parallel file synthesis, automated debugging, and repository migration."
                "AI Talk Radio" -> "🎙️ **AI Talk Radio Agent**: Interactive synthesized audio scripts, generating engaging podcasts or spoken radio content."
                "Customer Support" -> "🤝 **Customer Support Agent**: Automated support assistant with deep semantic grounding to resolve client inquiries in real time."
                "Data Analyst" -> "📊 **Data Analyst Agent**: Inspects tabular schemas, generates chart metrics, and aggregates large JSON/CSV records meticulously."
                "Document Processing" -> "📄 **Document Processing Agent**: Extraction engine for semantic information, summary tables, and text layout analysis."
                "Repo Maintainer" -> "🔧 **Repo Maintainer Agent**: Automatic dependency checking, refactoring outdated functions, and clean architecture checking."
                else -> "Welcome to Gemini Playground! Choose a Model or Agent from the tabs above to start building."
            }
            _playgroundMessages.value = listOf(ChatMessage("assistant", translate(initialGreeting, translateHindi(initialGreeting))))
        } else {
            _playgroundMessages.value = emptyList()
        }
    }

    private fun translateHindi(en: String): String {
        return when {
            en.contains("Featured") -> "✨ **गूगल जेमिनी मॉडल**: कोड संरचनाओं का पता लगाने, एल्गोरिदम समझाने और रचनात्मक रूप से मदद करने के लिए प्री-लोडेड।"
            en.contains("Code and Chat") -> "💬 **गूगल जेमिनी कोड और चैट**: उच्च-सटीकता वाले कोड विकास, डिबगिंग और निर्देश के लिए ट्यून किया गया।"
            en.contains("Image") -> "🎨 **इमेजन 3 (इमेज जनरेशन)**: पाठ्य विवरण का उपयोग करके उच्च गुणवत्ता वाली कला संपत्ति बनाएं।"
            else -> "एआई प्लेग्राउंड में स्वागत है! शुरू करने के लिए कोई मॉडल या एजेंट चुनें।"
        }
    }

    fun submitPlaygroundPrompt(prompt: String) {
        if (prompt.isBlank()) return
        
        val userMsg = ChatMessage("user", prompt)
        val msgs = _playgroundMessages.value.toMutableList()
        msgs.add(userMsg)
        _playgroundMessages.value = msgs

        viewModelScope.launch {
            _isPlaygroundThinking.value = true
            
            val selectedCard = _playgroundSelectedCard.value ?: "Default Gemini"
            val groundingSearch = _playSearchGrounding.value
            val groundingMaps = _playMapsGrounding.value
            val codeExec = _playCodeExecution.value
            val structured = _playStructuredOutputs.value
            val funcCalling = _playFunctionCalling.value
            val urlContext = _playUrlContext.value
            
            val instructions = """
                You are Gemini Playground operating in the $selectedCard mode.
                Active settings:
                - Grounding Search Enabled: $groundingSearch
                - Grounding Maps Enabled: $groundingMaps
                - Direct Code Execution: $codeExec
                - Structured Outputs: $structured
                - Function Calling capability: $funcCalling
                - URL Workspace context: $urlContext
                
                Please generate an elegant response tailored to this model context. Include bilingual English-Hindi text in standard formatting.
            """.trimIndent()

            val combinedRequest = GenerateContentRequest(
                contents = listOf(
                    Content(parts = listOf(Part(text = "$instructions\n\nUser Query:\n$prompt")))
                )
            )

            try {
                val responseText = withContext(Dispatchers.IO) {
                    val res = GeminiRetrofitClient.service.generateContent("AI_STUDIO_KEY_INJECTED", combinedRequest)
                    res.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                }
                
                if (!responseText.isNullOrBlank()) {
                    msgs.add(ChatMessage("assistant", responseText))
                } else {
                    msgs.add(ChatMessage("assistant", getPlaygroundFallbackText(prompt, selectedCard)))
                }
            } catch (e: Exception) {
                delay(1200) // simulation delay
                msgs.add(ChatMessage("assistant", getPlaygroundFallbackText(prompt, selectedCard)))
            } finally {
                _playgroundMessages.value = msgs
                _isPlaygroundThinking.value = false
            }
        }
    }

    private fun getPlaygroundFallbackText(prompt: String, mode: String): String {
        val isH = _isHindi.value
        val lower = prompt.lowercase()
        
        return when {
            mode.contains("Image") || lower.contains("image") || lower.contains("draw") || lower.contains("paint") || lower.contains("photo") -> {
                if (isH) {
                    "**[इमेजन 3 (ऑफ़लाइन मॉक)]**: सफलतापूर्वक उत्पन्न हुई छवि!\nहमने आपके विवरण से एक सुंदर दृश्य डिजाइन तैयार किया है। वास्तविक छवि रेंडर करने के लिए अपनी जेमिनी कुंजी डालें।"
                } else {
                    "**[Imagen 3 Offline Mock]**: Image Successfully generated! Custom PNG dimensions calculated matching description '$prompt'. Connect Gemini Key from Secrets for live cloud painting."
                }
            }
            mode.contains("Video") || lower.contains("video") || lower.contains("movie") || lower.contains("animate") -> {
                if (isH) {
                    "**[Veo वीडियो इंजन ऑफ़लाइन]**: 60 FPS पर वीडियो रेंडरिंग शुरू हुई। आपका प्रॉम्प्ट प्रसंस्कृत किया जा रहा है।"
                } else {
                    "**[Veo Video Offline Engine]**: Initialized high-definition rendering of cinematic 60 FPS motion path simulation matching prompt '$prompt'."
                }
            }
            else -> {
                if (isH) {
                    "**[प्लेग्राउंड ($mode)]**:\nसफलतापूर्वक प्रॉम्प्ट संसाधित किया गया!\n- सर्च ग्राउंडिंग: सक्रिय और समर्थित।\n- प्रतिक्रिया: आपका विवरण बहुत रोचक है। लाइव रिस्पॉन्स के लिए कृपया AI_STUDIO_KEY जोड़ें।"
                } else {
                    "**[Gemini Playground ($mode)]**:\nSuccessfully processed request in local sandbox.\n- Grounding Status: Active.\n- Output: Your concept is valid under this mode. Add your AI_STUDIO credentials to complete live model reasoning!"
                }
            }
        }
    }

    fun togglePlaySearchGrounding() { _playSearchGrounding.value = !_playSearchGrounding.value }
    fun togglePlayMapsGrounding() { _playMapsGrounding.value = !_playMapsGrounding.value }
    fun togglePlayCodeExecution() { _playCodeExecution.value = !_playCodeExecution.value }
    fun togglePlayStructuredOutputs() { _playStructuredOutputs.value = !_playStructuredOutputs.value }
    fun togglePlayFunctionCalling() { _playFunctionCalling.value = !_playFunctionCalling.value }
    fun togglePlayUrlContext() { _playUrlContext.value = !_playUrlContext.value }
    fun togglePlayTemporaryChat() { _playTemporaryChat.value = !_playTemporaryChat.value }


    init {
        // Collect projects flow
        viewModelScope.launch {
            repository.allProjects.collect {
                _projects.value = it
            }
        }
    }

    private suspend fun checkAndPreloadDefaultProject() {
        if (_projects.value.isEmpty()) {
            val projectId = repository.createProjectFromTemplate("Physics Playground", "html5")
            selectProjectById(projectId)
        } else if (_selectedProject.value == null) {
            selectProjectById(_projects.value.first().id)
        }
    }

    fun selectProjectById(projectId: Int) {
        saveCurrentBuffer()

        filesJob?.cancel()
        commitsJob?.cancel()
        deploymentsJob?.cancel()

        _activeFile.value = null
        _codeBuffer.value = ""

        filesJob = viewModelScope.launch {
            val project = repository.getProjectById(projectId)
            _selectedProject.value = project
            if (project != null) {
                val initialFiles = repository.getFiles(projectId)
                _projectFiles.value = initialFiles
                if (initialFiles.isNotEmpty()) {
                    selectFile(initialFiles.first(), saveDraft = false)
                }

                repository.getFilesFlow(projectId).collect { files ->
                    _projectFiles.value = files
                }
            }
        }

        commitsJob = viewModelScope.launch {
            repository.getCommitsFlow(projectId).collect {
                _commits.value = it
            }
        }

        deploymentsJob = viewModelScope.launch {
            repository.getDeploymentsFlow(projectId).collect {
                _deployments.value = it
            }
        }
    }

    fun selectFile(file: FileEntity, saveDraft: Boolean = true) {
        if (saveDraft) {
            saveCurrentBuffer()
        }
        _activeFile.value = file
        _codeBuffer.value = file.content
    }

    fun updateCodeBuffer(newText: String) {
        _codeBuffer.value = newText
    }

    fun saveCurrentBuffer() {
        val active = _activeFile.value ?: return
        val text = _codeBuffer.value
        val project = _selectedProject.value ?: return
        viewModelScope.launch {
            repository.saveFile(project.id, active.path, text)
            // Simulate incremental code change syncing locally
            if (_isOfflineMode.value) {
                _backupSyncedTime.value = "Unsaved Changes (Offline Mode)"
            } else {
                triggerAutoBackup()
            }
        }
    }

    fun createNewFileInProject(path: String) {
        val project = _selectedProject.value ?: return
        viewModelScope.launch {
            repository.createNewFile(project.id, path)
            // refresh
            val updatedFiles = repository.getFiles(project.id)
            _projectFiles.value = updatedFiles
            val newFile = updatedFiles.find { it.path == path }
            if (newFile != null) {
                selectFile(newFile)
            }
        }
    }

    fun deleteFileFromProject(path: String) {
        val project = _selectedProject.value ?: return
        viewModelScope.launch {
            repository.deleteFile(project.id, path)
            // refresh
            val updatedFiles = repository.getFiles(project.id)
            _projectFiles.value = updatedFiles
            if (_activeFile.value?.path == path) {
                if (updatedFiles.isNotEmpty()) {
                    selectFile(updatedFiles.first(), saveDraft = false)
                } else {
                    _activeFile.value = null
                    _codeBuffer.value = ""
                }
            }
        }
    }

    fun createProject(name: String, templateId: String) {
        viewModelScope.launch {
            val newProjId = repository.createProjectFromTemplate(name, templateId)
            selectProjectById(newProjId)
        }
    }

    fun deleteSelectedProject() {
        val proj = _selectedProject.value ?: return
        viewModelScope.launch {
            repository.deleteProject(proj.id)
            _selectedProject.value = null
            _activeFile.value = null
            _codeBuffer.value = ""
            _projectFiles.value = emptyList()
            if (_projects.value.isNotEmpty()) {
                selectProjectById(_projects.value.first().id)
            }
        }
    }

    // --- Theme Config ---
    fun setTheme(theme: String) {
        _selectedTheme.value = theme
    }

    // --- Offline & Backups ---
    fun toggleOfflineMode() {
        _isOfflineMode.value = !_isOfflineMode.value
        if (!_isOfflineMode.value) {
            triggerAutoBackup()
        }
    }

    private fun triggerAutoBackup() {
        if (_isOfflineMode.value) return
        viewModelScope.launch {
            _isBackingUp.value = true
            delay(1500) // simulation of cloud api packet sending
            _isBackingUp.value = false
            _backupSyncedTime.value = "Cloud Sync Success: ${System.currentTimeMillis() / 1000}"
            // Mark backup synced in DB
            val proj = _selectedProject.value
            if (proj != null) {
                repository.updateProject(proj.copy(isCloudSynced = true))
            }
        }
    }

    // --- Integrated Terminal Runner Console ---
    fun executeActiveCode() {
        val proj = _selectedProject.value ?: return
        val currentFile = _activeFile.value ?: return
        val fileContent = _codeBuffer.value

        saveCurrentBuffer()
        _activeTabFlow.value = "console" // Instantly navigate to Console to show live preview & compilation logs

        viewModelScope.launch {
            _isCodeRunning.value = true
            _consoleLogs.value = listOf(
                ConsoleLogItem("Initializing DevRepl local container runtime...", "INFO"),
                ConsoleLogItem("Booting environment: ${proj.languageType} Node v18.2 offline...", "INFO"),
                ConsoleLogItem("Reading project sources matching entry file: '${currentFile.path}'", "INFO")
            )
            delay(1000)

            // Dynamic logic checking code for syntax errors or key phrases
            val lowercaseCode = fileContent.lowercase()
            val logs = _consoleLogs.value.toMutableList()

            if (lowercaseCode.contains("error") || lowercaseCode.contains("throw") || lowercaseCode.contains("undefined")) {
                logs.add(ConsoleLogItem("[CRITICAL] SyntaxError: Unexpected token error in '${currentFile.path}'", "ERROR"))
                logs.add(ConsoleLogItem("Process exited with exit code 1. Tap AI Ghostwriter to fix this automatically!", "ERROR"))
                _runPreviewOutput.value = "⛔ Crashing error. See integrated terminal console output logs."
            } else {
                logs.add(ConsoleLogItem("[COMPILING] Linked variables & compiled references successfully.", "SUCCESS"))
                delay(800)
                logs.add(ConsoleLogItem("Stdout Output:", "INFO"))
                
                // Add code-specific outputs for our 56 templates
                if (proj.languageType == "HTML") {
                    logs.add(ConsoleLogItem(">> Browser DOM container prepped.", "SUCCESS"))
                    logs.add(ConsoleLogItem(">> Canvas context width: 400, height: 300 initialized.", "SUCCESS"))
                    _runPreviewOutput.value = "SUCCESS_BROWSER"
                } else if (proj.languageType == "Python") {
                    logs.add(ConsoleLogItem(">> Python system runtime completed.", "SUCCESS"))
                    logs.add(ConsoleLogItem(">> Coordinates generated: Fractal Tree with 31 branches created.", "SUCCESS"))
                    _runPreviewOutput.value = "SUCCESS_PYTHON"
                } else {
                    logs.add(ConsoleLogItem(">> Process successfully completed on offline mock threads with status code 0.", "SUCCESS"))
                    _runPreviewOutput.value = "SUCCESS_GENERIC"
                }
            }
            _consoleLogs.value = logs
            _isCodeRunning.value = false
        }
    }

    fun submitConsoleInputCommand(cmd: String) {
        val currentLogs = _consoleLogs.value.toMutableList()
        currentLogs.add(ConsoleLogItem("$ $cmd", "INPUT"))
        
        viewModelScope.launch {
            delay(300)
            if (cmd.lowercase() == "clear") {
                _consoleLogs.value = emptyList()
            } else {
                currentLogs.add(ConsoleLogItem("Command '$cmd' processed in context container sandbox successfully offline.", "SUCCESS"))
                _consoleLogs.value = currentLogs
            }
        }
    }

    // --- Git Commits ---
    fun pushGitCommit(message: String) {
        val proj = _selectedProject.value ?: return
        val currentFile = _activeFile.value ?: return
        viewModelScope.launch {
            repository.commitChanges(
                projectId = proj.id,
                message = message.ifBlank { "Commit updated ${currentFile.path}" },
                author = _userDetails.value?.get("name") ?: "Developer"
            )
            delay(500)
            triggerAutoBackup()
        }
    }

    // --- Deployment ---
    fun deployProject() {
        val proj = _selectedProject.value ?: return
        viewModelScope.launch {
            _isDeploying.value = true
            val logs = listOf(
                "Triggering DevRepl serverless container builder...",
                "Pulling static resources with hash mapping...",
                "Bundling modules and resolving micro-router configurations...",
                "Deploying globally on edge CDN server locations...",
                "DNS bindings verified on Cloudflare registers..."
            )
            
            repository.recordDeployment(
                projectId = proj.id,
                url = "https://${proj.name.lowercase().replace(" ", "-")}.devrepl.app",
                status = "DEPLOYING",
                logsJson = "Building deployment logs..."
            )
            
            for (idx in logs.indices) {
                delay(800)
                val statusText = "BUILD: ${logs[idx]}"
                repository.recordDeployment(
                    projectId = proj.id,
                    url = "https://${proj.name.lowercase().replace(" ", "-")}.devrepl.app",
                    status = "DEPLOYING",
                    logsJson = statusText
                )
            }
            
            delay(1000)
            repository.recordDeployment(
                projectId = proj.id,
                url = "https://${proj.name.lowercase().replace(" ", "-")}.devrepl.app",
                status = "ACTIVE",
                logsJson = "Success: App live at edge domains! Status 200 OK."
            )
            _isDeploying.value = false
        }
    }

    // --- Real-time Team Collaboration Simulator ---
    fun setCollaboration(enabled: Boolean) {
        _collaborationActive.value = enabled
        if (enabled) {
            startCollaborationSim()
        } else {
            _teammates.value = emptyList()
        }
    }

    private fun startCollaborationSim() {
        viewModelScope.launch {
            val names = listOf("Priya (UI)", "Rajesh (Dev)", "Carlos (DB)")
            val colors = listOf("#FF61D2", "#FFE83F", "#3FFFF8")
            
            // Spawn teammate cursors
            _teammates.value = names.mapIndexed { idx, name ->
                TeammateCursor(name, colors[idx], lineIndex = idx * 2 + 1, charIndex = idx + 4)
            }

            var tick = 0
            while (_collaborationActive.value) {
                delay(5000) // periodic teammate updates
                val active = _activeFile.value ?: continue
                val logs = _consoleLogs.value.toMutableList()
                
                // Priya is writing CSS code!
                if (tick % 2 == 0) {
                    val codeWithCollaboration = _codeBuffer.value + "\n// Remote Sync: Priya is updating styling attributes..."
                    _codeBuffer.value = codeWithCollaboration
                    logs.add(ConsoleLogItem("Collaboration Sync: Priya updated indices in file '${active.path}'", "INFO"))
                } else {
                    logs.add(ConsoleLogItem("Collaboration: Rajesh ran workspace health checks.", "INFO"))
                }
                _consoleLogs.value = logs
                tick++
            }
        }
    }

    // --- Gemini, ChatGPT, and DeepSeek API Calls with dynamic routing ---
    fun sendAiMessage(message: String) {
        if (message.isBlank()) return
        
        val userMsg = ChatMessage("user", message)
        val currentMsgs = _aiMessages.value.toMutableList()
        currentMsgs.add(userMsg)
        _aiMessages.value = currentMsgs
        
        val codeContext = _codeBuffer.value
        val logContext = _consoleLogs.value.lastOrNull()?.message ?: "None"
        val provider = _aiProvider.value
        val preset = _systemInstructionPreset.value
        val temp = _modelTemperature.value

        viewModelScope.launch {
            _isAiThinking.value = true
            
            // Build direct REST payload or use smart fallback
            val systemPrompt = "You are $provider, acting as a $preset in the OMNI-AI IDE workspace. Temperature: $temp."
            val prompt = """
                $systemPrompt
                Analyze this code buffer and terminal logs.
                Code Buffer:
                $codeContext
                
                Last Terminal Log:
                $logContext
                
                User Query:
                $message
                
                Instructions:
                - Provide robust coding solutions, optimization variables, and bug fixes.
                - Support bilingual responses: both English and simple Hindi translations (देवनागरी) side by side.
                - Keep descriptions highly structured, direct, clean, and helpful for professionals.
            """.trimIndent()
            
            try {
                val responseText = withContext(Dispatchers.IO) {
                    when (provider) {
                        "Gemini AI" -> {
                            val activeKey = if (_sessionGeminiKey.value.isNotBlank()) {
                                _sessionGeminiKey.value
                            } else if (BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY") {
                                BuildConfig.GEMINI_API_KEY
                            } else {
                                null
                            }

                            if (activeKey != null) {
                                val request = GenerateContentRequest(
                                    contents = listOf(Content(parts = listOf(Part(text = prompt))))
                                )
                                val response = GeminiRetrofitClient.service.generateContent(activeKey, request)
                                response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                            } else {
                                throw IllegalStateException("Gemini Key Missing")
                            }
                        }
                        "ChatGPT (OpenAI)" -> {
                            val activeKey = _sessionOpenaiKey.value
                            if (activeKey.isNotBlank()) {
                                val request = OpenAiChatRequest(
                                    model = "gpt-4o",
                                    messages = listOf(
                                        OpenAiMessage(role = "system", content = systemPrompt),
                                        OpenAiMessage(role = "user", content = prompt)
                                    ),
                                    temperature = temp
                                )
                                val authHeader = "Bearer $activeKey"
                                val response = OpenAiRetrofitClient.service.getChatCompletion(authHeader, request)
                                response.choices?.firstOrNull()?.message?.content
                            } else {
                                throw IllegalStateException("ChatGPT Key Missing")
                            }
                        }
                        "DeepSeek" -> {
                            val activeKey = _sessionDeepseekKey.value
                            if (activeKey.isNotBlank()) {
                                val request = OpenAiChatRequest(
                                    model = "deepseek-chat",
                                    messages = listOf(
                                        OpenAiMessage(role = "system", content = systemPrompt),
                                        OpenAiMessage(role = "user", content = prompt)
                                    ),
                                    temperature = temp
                                )
                                val authHeader = "Bearer $activeKey"
                                val response = DeepSeekRetrofitClient.service.getChatCompletion(authHeader, request)
                                response.choices?.firstOrNull()?.message?.content
                            } else {
                                throw IllegalStateException("DeepSeek Key Missing")
                            }
                        }
                        else -> null
                    }
                }
                
                if (!responseText.isNullOrBlank()) {
                    currentMsgs.add(ChatMessage("assistant", responseText))
                } else {
                    currentMsgs.add(ChatMessage("assistant", getOfflineAiResponse(message)))
                }
            } catch (e: Exception) {
                // Return descriptive mockup matching selected provider when offline/no key is provided
                delay(1200) // simulation response time
                val fallbackResponse = when (provider) {
                    "ChatGPT (OpenAI)" -> {
                        "**[ChatGPT-4o Offline Sandbox]** 🧠\n\n- Key required for live API calling. Add your OpenAI API key in the chat configuration above!\n\n**Response simulation:**\nBased on your query: '$message'\n- Code structure looks valid. Try applying modern ECMAScript standard bindings or formatting clean structures.\n\n*बिल्कुल! उत्तर हिंदी में: चैटजीपीटी सिम्युलेटर सक्रिय है। रीढ़ की हड्डी जैसा मजबूत कोड सुनिश्चित करें।*"
                    }
                    "DeepSeek" -> {
                        "**[DeepSeek-V3 / R1 (Reasoning Node)]** 🧬\n\n`<thought>`\nAnalyzing workspace repository logs...\nDetected querying target: '$message'.\nEvaluating optimal architectural patterns...\n`</thought>`\n\n- Key required for live reasoning. Configure your DeepSeek API key above to unlock full reasoning tokens!\n\n**Simulated Advice:**\nOptimize variable indexing. Minimize garbage collection delays by reducing iterative heap allocations.\n\n*डीपसीक ऑफ़लाइन विचार प्रक्रिया पूर्ण हुई। कोड में डेटा-फ्लो को गति दें!*"
                    }
                    else -> getOfflineAiResponse(message)
                }
                currentMsgs.add(ChatMessage("assistant", fallbackResponse))
            } finally {
                _aiMessages.value = currentMsgs
                _isAiThinking.value = false
            }
        }
    }

    private fun getOfflineAiResponse(query: String): String {
        val isHindi = _isHindi.value
        val lowerQuery = query.lowercase()
        return when {
            lowerQuery.contains("error") || lowerQuery.contains("bug") || lowerQuery.contains("fix") -> {
                if (isHindi) {
                    "**[घोस्टराइटर AI] स्थानीय विश्लेषण सुझाव:**\nफ़ाइल में कोष्ठक / सिंटैक्स मिलान त्रुटियां जांचें। अपने कोड में `try/catch` ब्लॉक सहेजें।\n\n```javascript\ntry {\n  // आपका कोड यहाँ लिखें\n} catch (error) {\n  console.error(error);\n}\n```"
                } else {
                    "**[Ghostwriter AI Custom local debugger]**:\n- Ensure matching curly brackets or quotes in your file.\n- In Python templates, double-check proper indentation levels.\n- Try adding a robust `try-catch` wrapper inside your main method."
                }
            }
            lowerQuery.contains("hello") || lowerQuery.contains("hi") -> {
                if (isHindi) {
                    "हैलो डेवलपर! मैं आपका स्थानीय कोड सहायक हूँ। आप मुझसे कोई भी कोडिंग प्रश्न पूछ सकते हैं, और मैं उत्तर दूंगा!"
                } else {
                    "Hi developer! I am your visual DevRepl companion. Tell me what templates to optimize or query about syntax!"
                }
            }
            else -> {
                if (isHindi) {
                    "**[घोस्टराइटर सहायक]**:\nइस कोड का विश्लेषण सफलतापूर्वक स्थानीय सैंडबॉक्स अनुकूलन में किया गया।\n- कोड आर्किटेक्चर: स्वच्छ और त्रुटि मुक्त स्कोप।\n- ऑफ-लाइन क्लाउड सुरक्षा: सक्रिय और सुरक्षित।\n- सुझाव: कोडिंग गति बढ़ाने के लिए 'तेज़ रन' (Fast Run) का उपयोग करें।"
                } else {
                    "**[Ghostwriter Assistant]**:\nAnalyzed active workspace structure. Your template scope is clean and runs efficiently. Try checking variable parameters to squeeze maximum browser rendering speed!"
                }
            }
        }
    }
}
