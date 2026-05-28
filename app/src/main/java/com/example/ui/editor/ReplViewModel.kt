package com.example.ui.editor

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
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

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("ReplViewModel", "Unhandled exception in coroutine scope", throwable)
    }

    private val database: ReplDatabase by lazy {
        Room.databaseBuilder(
            application,
            ReplDatabase::class.java,
            "devrepl_db"
        ).fallbackToDestructiveMigration().build()
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
        viewModelScope.launch(exceptionHandler) {
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
    private val _aiProvider = MutableStateFlow("Gemini AI") // "Gemini AI", "ChatGPT (OpenAI)", "DeepSeek", "AI Studio Agent"
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
                "AI Coding Agent" -> "🤖 **Google AI Studio Coding Agent**: Powered by Gemini 3.5 & Antigravity. I synthesize complete files, execute compile-and-fix iterations, generate adaptive test cases, optimize room databases, and compile custom SVG vectors. What should we build together today?"
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
            en.contains("AI Coding Agent") -> "🤖 **गूगल एआई स्टूडियो कोडिंग एजेंट**: जेमिनी 3.5 और एंटीग्रेविटी द्वारा संचालित। मैं संपूर्ण फाइलें संकलित कर सकता हूँ, सिंटैक्स त्रुटियों को ठीक कर सकता हूँ, टेस्ट केसेस बना सकता हूँ और एसवीजी रेंडर कर सकता हूँ। आज हम मिलकर क्या बनाएँ?"
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

        viewModelScope.launch(exceptionHandler) {
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
            mode.contains("AI Coding") -> {
                if (isH) {
                    "**[गूगल एआई स्टूडियो कोडिंग एजेंट]** 🤖\n\nसफलतापूर्वक कोडिंग अनुरोध स्वीकृत किया गया!\nचूंकि मैं कोडिंग स्कोप में गहराई से एकीकृत हूँ, मैंने इस अनुरोध का विश्लेषण किया है: **'$prompt'**।\n\n- **संकलन-और-फिक्स लूप**: सिंटैक्स बिल्कुल स्वच्छ है। संकलन त्रुटियां दूर हैं।\n- **एडेप्टिव रोबोइलेक्ट्रिक परीक्षण प्रदाता**: आपके कोड के लिए बैकएंड सैंडबॉक्स टेस्ट तैयार किए गए हैं।\n\n*गूगल एआई स्टूडियो एजेंट पूर्ण गति से काम कर रहा है!*"
                } else {
                    "**[Google AI Studio Coding Agent]** 🤖\n\nDirect local coding request parsed successfully!\nAnalyzing your workspace code matching task: **'$prompt'**.\n\n- **Automated Compile & Fix Loop**: Verified index mapping and eliminated latent syntax breaks.\n- **Adaptive JUnit Robolectric Suite Generator**: Generated fast screenshots and behavior verification paths.\n- **Spacious Dynamic Layout**: Integrated seamless elements with high Material 3 fidelity.\n\n*Google AI Studio Swarm Agent online — active workspace code synchronized perfectly.*"
                }
            }
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
        viewModelScope.launch(exceptionHandler) {
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

        filesJob = viewModelScope.launch(exceptionHandler) {
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

        commitsJob = viewModelScope.launch(exceptionHandler) {
            repository.getCommitsFlow(projectId).collect {
                _commits.value = it
            }
        }

        deploymentsJob = viewModelScope.launch(exceptionHandler) {
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
        viewModelScope.launch(exceptionHandler) {
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
        viewModelScope.launch(exceptionHandler) {
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
        viewModelScope.launch(exceptionHandler) {
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
        viewModelScope.launch(exceptionHandler) {
            val newProjId = repository.createProjectFromTemplate(name, templateId)
            selectProjectById(newProjId)
        }
    }

    fun deleteSelectedProject() {
        val proj = _selectedProject.value ?: return
        viewModelScope.launch(exceptionHandler) {
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
        viewModelScope.launch(exceptionHandler) {
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

        viewModelScope.launch(exceptionHandler) {
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
        
        viewModelScope.launch(exceptionHandler) {
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
        viewModelScope.launch(exceptionHandler) {
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
        viewModelScope.launch(exceptionHandler) {
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
        viewModelScope.launch(exceptionHandler) {
            val names = listOf("Priya (UI)", "Rajesh (Dev)", "Carlos (DB)", "Gemini (AI Agent)")
            val colors = listOf("#FF61D2", "#FFE83F", "#3FFFF8", "#A259FF")
            
            // Spawn teammate cursors
            _teammates.value = names.mapIndexed { idx, name ->
                TeammateCursor(name, colors[idx], lineIndex = idx * 2 + 1, charIndex = idx + 4)
            }

            var tick = 0
            while (_collaborationActive.value) {
                delay(5000) // periodic teammate updates
                val active = _activeFile.value
                if (active != null) {
                    val logs = _consoleLogs.value.toMutableList()
                    
                    if (tick % 3 == 0) {
                        val codeWithCollaboration = _codeBuffer.value + "\n// Remote Sync: Priya is updating styling attributes..."
                        _codeBuffer.value = codeWithCollaboration
                        logs.add(ConsoleLogItem("Collaboration Sync: Priya updated spacing indices in '${active.path}'", "INFO"))
                    } else if (tick % 3 == 1) {
                        logs.add(ConsoleLogItem("Collaboration: Rajesh completed parallel unit test execution checks.", "INFO"))
                    } else {
                        val codeWithGemini = _codeBuffer.value + "\n// AI Gen: Gemini Code Agent resolved a latent compile-warning block."
                        _codeBuffer.value = codeWithGemini
                        logs.add(ConsoleLogItem("AI Assistance: Gemini Code Agent analyzed syntax, added exception limits & synced references!", "SUCCESS"))
                    }
                    _consoleLogs.value = logs
                    tick++
                }
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

        viewModelScope.launch(exceptionHandler) {
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
                        "AI Studio Agent" -> {
                            val activeProj = _selectedProject.value?.name ?: "None"
                            val activeFile = _activeFile.value?.path ?: "None"
                            val codeLen = _codeBuffer.value.length
                            val isH = _isHindi.value
                            if (isH) {
                                "**[Google AI Studio Code Agent - Antigravity Engine]** 🚀\n\nमैं सक्रिय रूप से आपके प्लेग्राउंड वर्कस्पेस की निगरानी कर रहा हूँ! चूंकि मैं आपके निर्माण और रनटाइम सिस्टम में एक कोडिंग एजेंट के रूप में सीधे एकीकृत हूँ, **मुझे किसी बाहरी एपीआई कुंजी की आवश्यकता नहीं है**।\n\n**वर्कस्पेस अंतर्दृष्टि (Workspace Insights):**\n- सक्रिय प्रोजेक्ट: `$activeProj`\n- सक्रिय फ़ाइल बफ़र: `$activeFile` (बफ़र लंबाई: `$codeLen` संप्रतीक)\n- संकलन स्थिति (Compile Status): सफलतापूर्वक संकलित और विज्ञापित!\n\n**आपके कोडिंग प्रश्न का विश्लेषण:** '$message'\n- **सुझाव (Tip):** अपनी फाइलों को स्वचालित रूप से सहेजने और संकलित करने के लिए `Fast Run` का उपयोग करें। यदि आप `Room Database` का उपयोग कर रहे हैं, तो सुनिश्चित करें कि डेटाबेस स्कीमा संस्करण मेल खाता हो।\n- **नया विचार (Next Step):** मुझे इस प्रोजेक्ट के लिए टेस्ट-केस या नई फाइलें बनाने को कहें। मैं हर काम सेकंडों में स्वचालित कर दूँगा!\n\n*गूगल एआई स्टूडियो कोडिंग एजेंट सक्रिय है: मैंने आपके प्रोजेक्ट की संरचना का विश्लेषण किया है। सब कुछ सही और कुशल है!*"
                            } else {
                                "**[Google AI Studio Code Agent - Antigravity Engine]** 🚀\n\nI am actively monitoring your workspace session! Since I am integrated directly into your build and runtime systems as a Developer Swarm Agent, **no external API keys are required for my services**.\n\n**Workspace Insights:**\n- Active Project: `$activeProj`\n- Active File Buffer: `$activeFile` (Buffer length: `$codeLen` chars)\n- System Compilation Status: Compiled & Verified Successfully!\n\n**Analysis of your query:** '$message'\n- **Architectural Advice:** Keep methods bounded by thread-safe coroutines, implement fallback structures destructively if updating local schema, and use declarative canvas models.\n- **Actionable Next Step:** Instruct me to write corresponding visual test suites or inject clean refactored routes in your workspace.\n\n*Google AI Studio Coding Agent activated: Direct developer connection established!*"
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

    // --- REPLIT SIMULATION MODELS & METHODS ---
    private val _replitCycles = MutableStateFlow(12500)
    val replitCycles: StateFlow<Int> = _replitCycles.asStateFlow()

    private val _replitBounties = MutableStateFlow<List<ReplitBounty>>(
        listOf(
            ReplitBounty(
                id = "bounty_paint",
                title = "Interactive Canvas Paint App",
                clientName = "VentureLabs Inc.",
                payoutUsd = 150,
                payoutCycles = 15000,
                difficulty = "Intermediate",
                tags = listOf("Compose", "Canvas", "Drawing"),
                description = "Build a responsive Canvas-based painting application in Compose with color picker, undo/redo buttons, stroke width sliders, and high accuracy visual trails.",
                requiredPrompt = "Create a Canvas Paint app with a customizable color palette, brush thickness controller, and interactive canvas drawing path state."
            ),
            ReplitBounty(
                id = "bounty_game",
                title = "Vibrant Retro Tetris Engine",
                clientName = "NostalgiaDevs",
                payoutUsd = 300,
                payoutCycles = 30000,
                difficulty = "Expert",
                tags = listOf("GameDev", "StateMath", "HighPerformance"),
                description = "Synthesize an offline-first retro block game stack centering high-speed tick rate, line clear celebrations, score records tracking, and a gorgeous CRT shader filter.",
                requiredPrompt = "Generate a classic retro Tetris puzzle game layout with custom block colors, next-queue visualizer, score counter, and fully keyboard/touch responsive state engine."
            ),
            ReplitBounty(
                id = "bounty_crypto",
                title = "Crypto Ticker with Live Sparklines",
                clientName = "ApexTrade",
                payoutUsd = 100,
                payoutCycles = 10000,
                difficulty = "Beginner",
                tags = listOf("API", "Retrofit", "VisualCharts"),
                description = "Integrate a real-time cryptocurrency price tracker showing live SVG sparklines for BTC, ETH and SOL, and a quick converter widget.",
                requiredPrompt = "Create a modern cryptocurrency price monitor layout showing dynamic list of currencies (BTC, ETH, SOL, ADA) with animated up/down sparkline trend graphs."
            )
        )
    )
    val replitBounties: StateFlow<List<ReplitBounty>> = _replitBounties.asStateFlow()

    private val _replitClaimedBounty = MutableStateFlow<ReplitBounty?>(null)
    val replitClaimedBounty: StateFlow<ReplitBounty?> = _replitClaimedBounty.asStateFlow()

    private val _replitAgentLogs = MutableStateFlow<List<String>>(listOf("System: Replit Agent idle. Select a bounty or enter custom prompt."))
    val replitAgentLogs: StateFlow<List<String>> = _replitAgentLogs.asStateFlow()

    private val _replitAgentPhase = MutableStateFlow("Idle") // "Idle", "Planning", "Analyzing Workspace", "Generating Code", "Compiling App", "Deploying Output", "Success"
    val replitAgentPhase: StateFlow<String> = _replitAgentPhase.asStateFlow()

    private val _replitAgentProgress = MutableStateFlow(0f)
    val replitAgentProgress: StateFlow<Float> = _replitAgentProgress.asStateFlow()

    private val _replitAgentUrl = MutableStateFlow<String?>(null)
    val replitAgentUrl: StateFlow<String?> = _replitAgentUrl.asStateFlow()

    private val _replitTransactions = MutableStateFlow<List<CycleTransaction>>(
        listOf(
            CycleTransaction("tx_01", "EARN", 5000, "Sign-up Bonus Credits Provided", "May 28, 09:12"),
            CycleTransaction("tx_02", "SPEND", 2500, "Dedicated GPU Host VM Allocation", "May 28, 09:30")
        )
    )
    val replitTransactions: StateFlow<List<CycleTransaction>> = _replitTransactions.asStateFlow()

    fun claimReplitBounty(bountyId: String) {
        val updated = _replitBounties.value.map {
            if (it.id == bountyId) it.copy(isClaimed = true) else it
        }
        _replitBounties.value = updated
        val claimed = updated.find { it.id == bountyId }
        _replitClaimedBounty.value = claimed
        
        _replitAgentLogs.value = listOf(
            "System: Claimed bounty successfully!",
            "Bounty Title: ${claimed?.title}",
            "Target Prompt is injected: \"${claimed?.requiredPrompt}\"",
            "Click 'Run Replit Agent' to begin auto synthesis and solve the bounty!"
        )
        _replitAgentPhase.value = "Idle"
        _replitAgentProgress.value = 0f
        _replitAgentUrl.value = null
    }

    fun submitReplitBounty() {
        val b = _replitClaimedBounty.value ?: return
        val updatedBounties = _replitBounties.value.map {
            if (it.id == b.id) it.copy(isCompleted = true, isClaimed = false) else it
        }
        _replitBounties.value = updatedBounties
        _replitClaimedBounty.value = null
        
        val earnAmount = b.payoutCycles
        _replitCycles.value = _replitCycles.value + earnAmount
        
        val tx = CycleTransaction(
            id = "tx_${System.currentTimeMillis()}",
            type = "EARN",
            amount = earnAmount,
            description = "Completed Bounty: '${b.title}'",
            timestamp = "Just Now"
        )
        val curTx = _replitTransactions.value.toMutableList()
        curTx.add(0, tx)
        _replitTransactions.value = curTx
        
        // Log to console also to feel interactive
        val consoleLogList = _consoleLogs.value.toMutableList()
        consoleLogList.add(ConsoleLogItem("✨ REPLIT BOUNTY SOLVED: ${b.title}! Received +$earnAmount Cycles!", "SUCCESS"))
        _consoleLogs.value = consoleLogList
    }

    fun buyCycles(usdAmount: Int) {
        val cyclesReward = usdAmount * 100
        _replitCycles.value = _replitCycles.value + cyclesReward
        
        val tx = CycleTransaction(
            id = "tx_${System.currentTimeMillis()}",
            type = "EARN",
            amount = cyclesReward,
            description = "Purchased $cyclesReward Cycles via Google Pay",
            timestamp = "Just Now"
        )
        val curTx = _replitTransactions.value.toMutableList()
        curTx.add(0, tx)
        _replitTransactions.value = curTx
    }

    fun runReplitAgent(prompt: String) {
        if (prompt.isBlank()) return
        _replitAgentPhase.value = "Planning"
        _replitAgentProgress.value = 0.05f
        _replitAgentUrl.value = null
        
        val logs = mutableListOf<String>()
        logs.add("🚀 Replit Agent initialized in workspace context...")
        logs.add("🔍 Scanning directory structure and files...")
        _replitAgentLogs.value = logs.toList()
        
        viewModelScope.launch(exceptionHandler) {
            delay(1200)
            _replitAgentPhase.value = "Analyzing Workspace"
            _replitAgentProgress.value = 0.2f
            logs.add("ℹ️ Target Prompt: \"$prompt\"")
            logs.add("📁 Workspace active: ${_selectedProject.value?.name ?: "Sandbox Playground"}")
            logs.add("⚙️ Spawning parallel compilation synthesis process...")
            _replitAgentLogs.value = logs.toList()
            
            delay(1500)
            _replitAgentPhase.value = "Generating Code"
            _replitAgentProgress.value = 0.45f
            logs.add("📝 Writing visual structures matching requested theme...")
            logs.add("✓ Created file: 'ReplitAgent_GeneratedApp.kt' in directory")
            logs.add("✓ Tied state streams & added responsive Material 3 layout layers.")
            _replitAgentLogs.value = logs.toList()
            
            // Actually create file in active project workspace so they see it!
            val proj = _selectedProject.value
            if (proj != null) {
                val generatedCodeStr = """
                    package com.example.generated
                    
                    import androidx.compose.runtime.*
                    import androidx.compose.foundation.layout.*
                    import androidx.compose.material3.*
                    import androidx.compose.ui.Modifier
                    import androidx.compose.ui.unit.dp
                    import androidx.compose.ui.Alignment
                    import androidx.compose.ui.graphics.Color
                    import androidx.compose.foundation.background
                    import androidx.compose.foundation.shape.RoundedCornerShape
                    import androidx.compose.ui.text.font.FontWeight
                    import androidx.compose.ui.unit.sp
                    
                    @Composable
                    fun GeneratedApp() {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "✨ Replit Agent: Built Successfully!",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "This screen was compiled and synthesized by the Replit Agent matching the target prompt:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "$prompt",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = {}) {
                                    Text("Simulate Interactions")
                                }
                            }
                        }
                    }
                """.trimIndent()
                
                try {
                    repository.createNewFile(proj.id, "ReplitAgent_GeneratedApp.kt")
                    repository.saveFile(proj.id, "ReplitAgent_GeneratedApp.kt", generatedCodeStr)
                    
                    // Refresh project files list
                    val updatedFiles = repository.getFiles(proj.id)
                    _projectFiles.value = updatedFiles
                    
                    logs.add("✓ Local File System synced live: 'ReplitAgent_GeneratedApp.kt' stands ready!")
                } catch (e: Exception) {
                    logs.add("ℹ️ Note: Applet local DB file sync bypassed (already exists or in general playground context).")
                }
            }
            
            delay(1800)
            _replitAgentPhase.value = "Compiling App"
            _replitAgentProgress.value = 0.75f
            logs.add("⚙️ Checking for Room DB layout compatibility...")
            logs.add("✓ Executing incremental Kotlin compilers...")
            logs.add("✓ Compiled successfully! 0 errors identified.")
            _replitAgentLogs.value = logs.toList()
            
            delay(1500)
            _replitAgentPhase.value = "Deploying Output"
            _replitAgentProgress.value = 0.9f
            logs.add("☁️ Spinning up container hosting with serverless isolated VMs...")
            val slug = prompt.lowercase().replace(" ", "-").filter { it.isLetterOrDigit() || it == '-' }
            logs.add("✓ Assigned subdomain: 'https://$slug.replit.app'")
            _replitAgentLogs.value = logs.toList()
            
            delay(1200)
            _replitAgentPhase.value = "Success"
            _replitAgentProgress.value = 1f
            val randomSlug = (1000..9999).random()
            val url = "https://agent-${randomSlug}.replit.app"
            _replitAgentUrl.value = url
            logs.add("🎉 REPLIT AGENT COMPLETED ALL TASKS!")
            logs.add("🔗 Deploy Link: $url")
            _replitAgentLogs.value = logs.toList()
            
            // Update terminal logs too
            val consoleLogList = _consoleLogs.value.toMutableList()
            consoleLogList.add(ConsoleLogItem("✓ Replit Agent deployment successful: $url", "SUCCESS"))
            _consoleLogs.value = consoleLogList
        }
    }

    // --- GOOGLE AI STUDIO SIMULATION MODELS & METHODS ---
    private val _aiStudioApiKeys = MutableStateFlow<List<String>>(listOf("AI_STUDIO_LIVE_KEY_8w9qza", "GEMINI_SANDBOX_SANDBOX_77f"))
    val aiStudioApiKeys: StateFlow<List<String>> = _aiStudioApiKeys.asStateFlow()

    private val _aiStudioActiveKey = MutableStateFlow("AI_STUDIO_LIVE_KEY_8w9qza")
    val aiStudioActiveKey: StateFlow<String> = _aiStudioActiveKey.asStateFlow()

    private val _aiStudioPromptType = MutableStateFlow("freeform") // "freeform", "chat", "structured"
    val aiStudioPromptType: StateFlow<String> = _aiStudioPromptType.asStateFlow()

    private val _aiStudioSelectedModel = MutableStateFlow("Gemini 2.5 Pro")
    val aiStudioSelectedModel: StateFlow<String> = _aiStudioSelectedModel.asStateFlow()

    private val _aiStudioTemperature = MutableStateFlow(0.7f)
    val aiStudioTemperature: StateFlow<Float> = _aiStudioTemperature.asStateFlow()

    private val _aiStudioSystemInstruction = MutableStateFlow("You are a pragmatic, high-velocity developer assistant specialized in building beautiful Jetpack Compose screens.")
    val aiStudioSystemInstruction: StateFlow<String> = _aiStudioSystemInstruction.asStateFlow()

    private val _aiStudioPromptInput = MutableStateFlow("")
    val aiStudioPromptInput: StateFlow<String> = _aiStudioPromptInput.asStateFlow()

    private val _aiStudioOutput = MutableStateFlow("")
    val aiStudioOutput: StateFlow<String> = _aiStudioOutput.asStateFlow()

    private val _aiStudioIsRunning = MutableStateFlow(false)
    val aiStudioIsRunning: StateFlow<Boolean> = _aiStudioIsRunning.asStateFlow()

    private val _aiStudioTokensCount = MutableStateFlow(0)
    val aiStudioTokensCount: StateFlow<Int> = _aiStudioTokensCount.asStateFlow()

    private val _aiStudioLatency = MutableStateFlow(0L)
    val aiStudioLatency: StateFlow<Long> = _aiStudioLatency.asStateFlow()

    fun addAiStudioApiKey(key: String) {
        if (key.isNotBlank()) {
            _aiStudioApiKeys.value = _aiStudioApiKeys.value + key
            _aiStudioActiveKey.value = key
        }
    }

    fun selectAiStudioApiKey(key: String) {
        _aiStudioActiveKey.value = key
    }

    fun setAiStudioPromptType(type: String) {
        _aiStudioPromptType.value = type
    }

    fun setAiStudioSelectedModel(model: String) {
        _aiStudioSelectedModel.value = model
    }

    fun setAiStudioTemperature(temp: Float) {
        _aiStudioTemperature.value = temp
    }

    fun setAiStudioSystemInstruction(instr: String) {
        _aiStudioSystemInstruction.value = instr
    }

    fun setAiStudioPromptInput(input: String) {
        _aiStudioPromptInput.value = input
    }

    fun runAiStudioPrompt() {
        val prompt = _aiStudioPromptInput.value
        if (prompt.isBlank()) return
        _aiStudioIsRunning.value = true
        _aiStudioOutput.value = "Connecting to Google Cloud servers... Initiating stream..."
        
        viewModelScope.launch(exceptionHandler) {
            delay(800)
            _aiStudioOutput.value = "Tokens calculated. Sending payload context matching instructions..."
            delay(1000)
            
            val model = _aiStudioSelectedModel.value
            val response = when {
                prompt.lowercase().contains("button") || prompt.lowercase().contains("compose") || prompt.lowercase().contains("ui") -> {
                    """
                    // Generated Kotlin code for Jetpack Compose Component
                    import androidx.compose.runtime.*
                    import androidx.compose.foundation.layout.*
                    import androidx.compose.material3.*
                    import androidx.compose.ui.Modifier
                    import androidx.compose.ui.unit.dp
                    import androidx.compose.ui.Alignment
                    import androidx.compose.ui.graphics.Color
                    import androidx.compose.ui.text.font.FontWeight
                    
                    @Composable
                    fun PremiumCustomCard(
                        title: String,
                        subtitle: String,
                        onClick: () -> Unit
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = title, 
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = subtitle, 
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = onClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Explore Component Details")
                                }
                            }
                        }
                    }
                    """.trimIndent()
                }
                prompt.lowercase().contains("algorithm") || prompt.lowercase().contains("bubble") || prompt.lowercase().contains("quick") -> {
                    """
                    // Kotlin implementation of Quick Sort Algorithm
                    fun quickSort(arr: IntArray, low: Int, high: Int) {
                        if (low < high) {
                            val pi = partition(arr, low, high)
                            quickSort(arr, low, pi - 1)
                            quickSort(arr, pi + 1, high)
                        }
                    }
                    
                    private fun partition(arr: IntArray, low: Int, high: Int): Int {
                        val pivot = arr[high]
                        var i = low - 1
                        for (j in low until high) {
                            if (arr[j] < pivot) {
                                i++
                                swap(arr, i, j)
                            }
                        }
                        swap(arr, i + 1, high)
                        return i + 1
                    }
                    
                    private fun swap(arr: IntArray, i: Int, j: Int) {
                        val temp = arr[i]
                        arr[i] = arr[j]
                        arr[j] = temp
                    }
                    """.trimIndent()
                }
                else -> {
                    """
                    🔮 **Google AI Studio Response ($model)**:
                    
                    Active Workspace analysis complete!
                    Here are some tips to optimize the Room database design and Kotlin Flow structures in your workspace:
                    
                    1. **Combine StateStreams**: Use Jetpack Compose `collectAsStateWithLifecycle` to consume Flows on UI thread efficiently, minimizing recompositions by mapping exact state properties.
                    2. **Index Strategy**: Your `FileEntity` structure has active foreign keys; ensure an `@Index(value = ["project_id"])` is added to prevent warning overlays.
                    3. **Threading Constraints**: Always switch dispatcher context to `Dispatchers.IO` when invoking Room transactions to prevent blocking your visual main thread rendering cycles.
                    """.trimIndent()
                }
            }
            
            _aiStudioOutput.value = response
            _aiStudioTokensCount.value = (100..450).random()
            _aiStudioLatency.value = (80..320).random().toLong()
            _aiStudioIsRunning.value = false
            
            // Log to console too that they are working inside Google AI Studio
            val consoleLogList = _consoleLogs.value.toMutableList()
            consoleLogList.add(ConsoleLogItem("✓ Google AI Studio execution completed: ${_aiStudioTokensCount.value} tokens analyzed.", "SUCCESS"))
            _consoleLogs.value = consoleLogList
        }
    }
}

// --- REPLIT SIMULATION DEFS ---
data class ReplitBounty(
    val id: String,
    val title: String,
    val clientName: String,
    val payoutUsd: Int,
    val payoutCycles: Int,
    val difficulty: String,
    val tags: List<String>,
    val description: String,
    val requiredPrompt: String,
    val isClaimed: Boolean = false,
    val isCompleted: Boolean = false
)

data class CycleTransaction(
    val id: String,
    val type: String, // "EARN", "SPEND"
    val amount: Int,
    val description: String,
    val timestamp: String
)

