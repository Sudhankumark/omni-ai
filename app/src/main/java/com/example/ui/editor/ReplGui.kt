package com.example.ui.editor

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.CommitEntity
import com.example.database.DeploymentEntity
import com.example.database.FileEntity
import com.example.database.ProjectEntity
import com.example.database.ReplRepository

// Define semantic colors based on active custom editor theme state
data class VisualThemeTokens(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val text: Color,
    val textMuted: Color,
    val editorBg: Color,
    val editorText: Color,
    val lineHighlight: Color,
    val success: Color,
    val error: Color,
    val isDark: Boolean
)

@Composable
fun getThemeTokens(themeName: String): VisualThemeTokens {
    return when (themeName) {
        "Frosted Glass" -> VisualThemeTokens(
            primary = Color(0xFFD0BCFF),
            secondary = Color(0xFFEFB8C8),
            background = Color(0xFF141218),
            surface = Color(0xFF2B2930),
            text = Color(0xFFE6E1E5),
            textMuted = Color(0xFF938F99),
            editorBg = Color(0xFF1C1B1F),
            editorText = Color(0xFFE6E1E5),
            lineHighlight = Color(0xFF313033),
            success = Color(0xFF81C784),
            error = Color(0xFFF2B8B5),
            isDark = true
        )
        "Dracula Style" -> VisualThemeTokens(
            primary = Color(0xFFFF79C6),
            secondary = Color(0xFF8BE9FD),
            background = Color(0xFF1E1F29),
            surface = Color(0xFF282A36),
            text = Color(0xFFF8F8F2),
            textMuted = Color(0xFF6272A4),
            editorBg = Color(0xFF282A36),
            editorText = Color(0xFFF8F8F2),
            lineHighlight = Color(0xFF44475A),
            success = Color(0xFF50FA7B),
            error = Color(0xFFFF5555),
            isDark = true
        )
        "VS Code Dark" -> VisualThemeTokens(
            primary = Color(0xFF007ACC),
            secondary = Color(0xFF4EC9B0),
            background = Color(0xFF181818),
            surface = Color(0xFF1E1E1E),
            text = Color(0xFFD4D4D4),
            textMuted = Color(0xFF6A9955),
            editorBg = Color(0xFF1E1E1E),
            editorText = Color(0xFFD4D4D4),
            lineHighlight = Color(0xFF2D2D2D),
            success = Color(0xFF4EC9B0),
            error = Color(0xFFF44336),
            isDark = true
        )
        "One Dark Pro" -> VisualThemeTokens(
            primary = Color(0xFF61AFEF),
            secondary = Color(0xFFC678DD),
            background = Color(0xFF21252B),
            surface = Color(0xFF282C34),
            text = Color(0xFFABB2BF),
            textMuted = Color(0xFF5C6370),
            editorBg = Color(0xFF282C34),
            editorText = Color(0xFFABB2BF),
            lineHighlight = Color(0xFF3E4451),
            success = Color(0xFF98C379),
            error = Color(0xFFE06C75),
            isDark = true
        )
        "Github Light" -> VisualThemeTokens(
            primary = Color(0xFF0969DA),
            secondary = Color(0xFFCF222E),
            background = Color(0xFFF6F8FA),
            surface = Color(0xFFFFFFFF),
            text = Color(0xFF24292F),
            textMuted = Color(0xFF57606A),
            editorBg = Color(0xFFFFFFFF),
            editorText = Color(0xFF24292F),
            lineHighlight = Color(0xFFF6F8FA),
            success = Color(0xFF1A7F37),
            error = Color(0xFFCF222E),
            isDark = false
        )
        "Synthwave '84" -> VisualThemeTokens(
            primary = Color(0xFFF43F5E),
            secondary = Color(0xFFF472B6),
            background = Color(0xFF241B2F),
            surface = Color(0xFF2B213A),
            text = Color(0xFFFFEFFF),
            textMuted = Color(0xFF8B6B92),
            editorBg = Color(0xFF2B213A),
            editorText = Color(0xFFFFEFFF),
            lineHighlight = Color(0xFF3E3052),
            success = Color(0xFF2FE39F),
            error = Color(0xFFFE4A49),
            isDark = true
        )
        else -> VisualThemeTokens( // Nordic Frost
            primary = Color(0xFF88C0D0),
            secondary = Color(0xFF8FBCBB),
            background = Color(0xFF242933),
            surface = Color(0xFF2E3440),
            text = Color(0xFFE5E9F0),
            textMuted = Color(0xFF4C566A),
            editorBg = Color(0xFF2E3440),
            editorText = Color(0xFFE5E9F0),
            lineHighlight = Color(0xFF3B4252),
            success = Color(0xFFA3BE8C),
            error = Color(0xFFBF616A),
            isDark = true
        )
    }
}

@Composable
fun StudioForgeMainGui(viewModel: ReplViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val selectedThemeName by viewModel.selectedTheme.collectAsState()
    val tokens = getThemeTokens(selectedThemeName)

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = tokens.primary,
            secondary = tokens.secondary,
            background = tokens.background,
            surface = tokens.surface,
            onBackground = tokens.text,
            onSurface = tokens.text
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = tokens.background
        ) {
            if (!isLoggedIn) {
                GoogleSignInScreen(viewModel, tokens)
            } else {
                IdeWorkspaceLayout(viewModel, tokens)
            }
        }
    }
}

// --- CYBERNETIC MICROCHIP BRAND ACCENT COMPOSABLE ---
@Composable
fun OMNIAIMicrochip(
    modifier: Modifier = Modifier,
    glowColor: Color = Color(0xFF00E5FF),
    accentColor: Color = Color(0xFFE040FB)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Canvas(modifier = modifier.size(100.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        
        // 1. Glowing background radial aura
        drawCircle(
            color = glowColor.copy(alpha = 0.15f * pulseScale),
            radius = cx * 0.9f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = glowColor.copy(alpha = 0.05f),
            radius = cx * 1.3f,
            center = Offset(cx, cy)
        )

        // 2. Draw pin contacts (3 pins on each of the 4 edges of the microchip)
        val pinLength = 12.dp.toPx()
        val pinWidth = 2.5.dp.toPx()
        val chipSize = 48.dp.toPx()
        val halfChip = chipSize / 2f
        
        val pinSpacing = 10.dp.toPx()
        val offsets = listOf(-pinSpacing, 0f, pinSpacing)
        
        for (offset in offsets) {
            // Left pin
            drawRoundRect(
                color = glowColor.copy(alpha = 0.85f),
                topLeft = Offset(cx - halfChip - pinLength, cy + offset - pinWidth / 2f),
                size = androidx.compose.ui.geometry.Size(pinLength, pinWidth),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )
            // Right pin
            drawRoundRect(
                color = glowColor.copy(alpha = 0.85f),
                topLeft = Offset(cx + halfChip, cy + offset - pinWidth / 2f),
                size = androidx.compose.ui.geometry.Size(pinLength, pinWidth),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )
            // Top pin
            drawRoundRect(
                color = glowColor.copy(alpha = 0.85f),
                topLeft = Offset(cx + offset - pinWidth / 2f, cy - halfChip - pinLength),
                size = androidx.compose.ui.geometry.Size(pinWidth, pinLength),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )
            // Bottom pin
            drawRoundRect(
                color = glowColor.copy(alpha = 0.85f),
                topLeft = Offset(cx + offset - pinWidth / 2f, cy + halfChip),
                size = androidx.compose.ui.geometry.Size(pinWidth, pinLength),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )
        }

        // 3. Main Silicon Die Box
        drawRoundRect(
            color = Color(0xFF090E1A),
            topLeft = Offset(cx - halfChip, cy - halfChip),
            size = androidx.compose.ui.geometry.Size(chipSize, chipSize),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx(), 10.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Fill
        )
        
        // Cyber outline border
        drawRoundRect(
            color = glowColor,
            topLeft = Offset(cx - halfChip, cy - halfChip),
            size = androidx.compose.ui.geometry.Size(chipSize, chipSize),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx(), 10.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )

        // 4. Central Inner Processing Silicon Gate
        val innerSize = chipSize * 0.55f
        val halfInner = innerSize / 2f
        drawRoundRect(
            color = glowColor.copy(alpha = 0.25f),
            topLeft = Offset(cx - halfInner, cy - halfInner),
            size = androidx.compose.ui.geometry.Size(innerSize, innerSize),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx(), 5.dp.toPx())
        )
        drawRoundRect(
            color = glowColor.copy(alpha = 0.75f),
            topLeft = Offset(cx - halfInner, cy - halfInner),
            size = androidx.compose.ui.geometry.Size(innerSize, innerSize),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx(), 5.dp.toPx()),
            style = Stroke(width = 1.2f.dp.toPx())
        )
        
        // Inner core chip node
        drawCircle(
            color = accentColor,
            radius = 3.5.dp.toPx() * pulseScale,
            center = Offset(cx, cy)
        )
    }
}

// --- GOOGLE SIGN IN SCREEN (Sleek Creative Landing Page) ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GoogleSignInScreen(viewModel: ReplViewModel, tokens: VisualThemeTokens) {
    val isHindi by viewModel.isHindi.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var emailInput by remember { mutableStateOf("bhaisudhan035@gmail.com") }
    var nameInput by remember { mutableStateOf("Sudhan Bhai") }
    
    // Boot Splash tracking
    var bootProgress by remember { mutableStateOf(0f) }
    var bootPhaseComplete by remember { mutableStateOf(false) }
    var currentLogIndex by remember { mutableStateOf(0) }
    
    val logsEn = listOf(
        "Initializing Neural Pathways ...",
        "Loading Graphics Engines ...",
        "Spawning Cryptographic Shells ...",
        "Establishing Secure Gateway ...",
        "Kernel Operational. Enjoy!"
    )
    val logsHi = listOf(
        "तंत्रिका पथ प्रारंभ कर रहा है ...",
        "ग्राफिक्स इंजन लोड कर रहा है ...",
        "क्रिप्टोग्राफ़िक शेल्स स्पॉन कर रहा है ...",
        "सुरक्षित गेटवे स्थापित कर रहा है ...",
        "कर्नेल शुरू हो गया। आनंद लें!"
    )

    // Simulating boot progress from 0% to 100% just like the user's home screenshot
    LaunchedEffect(Unit) {
        if (!bootPhaseComplete) {
            val totalSteps = 100
            for (step in 1..totalSteps) {
                delay(18) // ~1.8 seconds loading experience
                bootProgress = step / 100f
                if (step == 20) currentLogIndex = 1
                if (step == 45) currentLogIndex = 2
                if (step == 70) currentLogIndex = 3
                if (step == 92) currentLogIndex = 4
            }
            delay(400)
            bootPhaseComplete = true
        }
    }

    var showGoogleAuthSelector by remember { mutableStateOf(false) }
    var isAuthenticatingGoogle by remember { mutableStateOf(false) }
    var authLogString by remember { mutableStateOf("") }

    val darkBackgroundThemeColor = Color(0xFF000000)

    if (!bootPhaseComplete) {
        // --- BOOT SPLASH DESIGN (STRICT MATCH TO SCREENSHOT) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(darkBackgroundThemeColor)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Glowing cyan-blue chip
            OMNIAIMicrochip(
                modifier = Modifier.size(110.dp),
                glowColor = Color(0xFF40C4FF), // Cyan glow
                accentColor = Color(0xFFE040FB)  // Magenta core
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Beautiful "OMNI-AI" with customized colors
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "OMNI",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00E5FF), // Cyan/Blue
                    style = TextStyle(letterSpacing = 2.sp)
                )
                Text(
                    text = "-AI",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE040FB), // Magenta/Purple
                    style = TextStyle(letterSpacing = 2.sp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "SUPREME INTELLIGENCE KERNEL V9.9",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF64748B),
                style = TextStyle(letterSpacing = 1.sp)
            )

            Spacer(modifier = Modifier.weight(0.4f))

            // Progress Bar Loader section
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Background bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(Color(0xFF1E293B))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(bootProgress)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF00E5FF), Color(0xFFE040FB))
                                )
                            )
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Status text matching the user's screenshot
                Text(
                    text = if (isHindi) logsHi[currentLogIndex] else logsEn[currentLogIndex],
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF00E5FF),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${(bootProgress * 100).toInt()}% Initialized",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFE040FB).copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.weight(0.6f))
            
            // Bypass button
            Text(
                text = viewModel.translate("Click to skip boot...", "बूट छोड़ें..."),
                fontSize = 10.sp,
                color = Color.DarkGray,
                modifier = Modifier
                    .clickable { bootPhaseComplete = true }
                    .padding(8.dp)
            )
        }
    } else {
        // --- SECURE GOOGLE PORTAL ACCREDITATION (PREMIUM GLOW EDITION) ---
        var loginTabSelected by remember { mutableStateOf("phone") } // "phone", "gmail", "google", "token"
        var passwordInput by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var rememberMeSelected by remember { mutableStateOf(true) }

        // Phone Authentication state variables
        var phoneNumberInput by remember { mutableStateOf("") }
        var phoneOtpInput by remember { mutableStateOf("") }
        var isPhoneOtpSent by remember { mutableStateOf(false) }
        var phoneOtpTimer by remember { mutableStateOf(60) }
        var isPhoneTimerActive by remember { mutableStateOf(false) }
        var phoneGeneratedOtp by remember { mutableStateOf("") }
        var phoneAuthError by remember { mutableStateOf("") }

        // Gmail Authentication state variables
        var gmailInput by remember { mutableStateOf("bhaisudhan035@gmail.com") }
        var gmailPasswordInput by remember { mutableStateOf("") }
        var gmailOtpInput by remember { mutableStateOf("") }
        var isGmailOtpSent by remember { mutableStateOf(false) }
        var gmailOtpTimer by remember { mutableStateOf(60) }
        var isGmailTimerActive by remember { mutableStateOf(false) }
        var gmailGeneratedOtp by remember { mutableStateOf("") }
        var gmailAuthError by remember { mutableStateOf("") }
        var gmailUseOtpMode by remember { mutableStateOf(false) }

        // Google Verification with OTP state variables
        var showGoogleAuthSelector by remember { mutableStateOf(false) }
        var isAuthenticatingGoogle by remember { mutableStateOf(false) }
        var authLogString by remember { mutableStateOf("") }
        
        var selectedGoogleEmail by remember { mutableStateOf("") }
        var selectedGoogleName by remember { mutableStateOf("") }
        var selectedGoogleAvatar by remember { mutableStateOf("") }
        var showGoogleOtpDialog by remember { mutableStateOf(false) }
        var googleOtpInput by remember { mutableStateOf("") }
        var googleGeneratedOtp by remember { mutableStateOf("") }
        var googleOtpError by remember { mutableStateOf("") }
        var googleOtpTimer by remember { mutableStateOf(60) }
        var isGoogleTimerActive by remember { mutableStateOf(false) }

        // Timers for security SMS & Email simulation
        LaunchedEffect(isPhoneTimerActive) {
            if (isPhoneTimerActive) {
                while (phoneOtpTimer > 0) {
                    delay(1000)
                    phoneOtpTimer--
                }
                isPhoneTimerActive = false
            }
        }

        LaunchedEffect(isGmailTimerActive) {
            if (isGmailTimerActive) {
                while (gmailOtpTimer > 0) {
                    delay(1000)
                    gmailOtpTimer--
                }
                isGmailTimerActive = false
            }
        }

        LaunchedEffect(isGoogleTimerActive) {
            if (isGoogleTimerActive) {
                while (googleOtpTimer > 0) {
                    delay(1000)
                    googleOtpTimer--
                }
                isGoogleTimerActive = false
            }
        }

        val infiniteTransition = rememberInfiniteTransition(label = "portal_glow")
        val glowPhase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2f * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(14000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "phase"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Base vertical space-grade background gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF03000C), Color(0xFF09061C), Color(0xFF010104))
                        )
                    )
                    
                    // Draw flowing breathing ambient neon particles (Star-Dust Core)
                    val starCoordinates = listOf(
                        Offset(0.15f, 0.20f) to Color(0xFF00E5FF),
                        Offset(0.85f, 0.25f) to Color(0xFFE040FB),
                        Offset(0.30f, 0.45f) to Color(0xFF38BDF8),
                        Offset(0.70f, 0.60f) to Color(0xFF818CF8),
                        Offset(0.20f, 0.75f) to Color(0xFFE040FB),
                        Offset(0.80f, 0.85f) to Color(0xFF00E5FF),
                        Offset(0.50f, 0.10f) to Color(0xFF00E5FF),
                        Offset(0.40f, 0.90f) to Color(0xFFE040FB)
                    )
                    
                    starCoordinates.forEachIndexed { index, (relOffset, color) ->
                        val phaseOffset = index * 1.5f
                        val currentGlowAlpha = (Math.sin((glowPhase + phaseOffset).toDouble()).toFloat() + 1f) / 2f
                        val sizeRadius = (4f + 3f * Math.cos((glowPhase + phaseOffset).toDouble()).toFloat()) * density
                        
                        // Outer neon bloom halo
                        drawCircle(
                            color = color.copy(alpha = currentGlowAlpha * 0.22f),
                            radius = sizeRadius * 2.8f,
                            center = Offset(relOffset.x * size.width, relOffset.y * size.height)
                        )
                        // Inner hot core
                        drawCircle(
                            color = Color.White.copy(alpha = currentGlowAlpha * 0.95f),
                            radius = sizeRadius * 0.9f,
                            center = Offset(relOffset.x * size.width, relOffset.y * size.height)
                        )
                    }

                    // Large deep background nebula gradients
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.08f), Color.Transparent),
                            center = Offset(size.width * 0.85f, size.height * 0.25f),
                            radius = (size.width * 0.8f).coerceAtLeast(1f)
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFE040FB).copy(alpha = 0.07f), Color.Transparent),
                            center = Offset(size.width * 0.15f, size.height * 0.75f),
                            radius = (size.width * 0.8f).coerceAtLeast(1f)
                        )
                    )
                }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(15.dp))

            // Neon floating chip portal accent with breathing glow
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(95.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E5FF).copy(alpha = 0.08f))
                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.15f), CircleShape)
                )
                OMNIAIMicrochip(
                    modifier = Modifier.size(75.dp),
                    glowColor = Color(0xFF00E5FF),
                    accentColor = Color(0xFFE040FB)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Styled Header with high-contrast dual neon accent
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "OMNI",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00E5FF),
                    style = TextStyle(letterSpacing = 1.sp)
                )
                Text(
                    text = "-AI",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFE040FB),
                    style = TextStyle(letterSpacing = 1.sp)
                )
            }

            Text(
                text = viewModel.translate("SUPREME ENCRYPTED LOGON KERNEL", "सर्वोच्च एन्क्रिप्टेड सुरक्षा लॉगिन प्रवेश"),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF818CF8),
                modifier = Modifier.padding(top = 2.dp),
                style = TextStyle(letterSpacing = 2.sp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Matrix style card box with beautiful design outline
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0B14)),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(Color(0xFF00E5FF).copy(alpha = 0.6f), Color(0xFFE040FB).copy(alpha = 0.2f))))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = viewModel.translate("IDENTITY VERIFICATION", "पहचान सत्यापन क्रेडेंशियल"),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        style = TextStyle(letterSpacing = 1.sp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = viewModel.translate("Secure authorization for professional IDE workflows", "प्रोफेशनल प्रोग्रामिंग वर्कफ़्लो के लिए सुरक्षित प्राधिकरण"),
                        fontSize = 10.5.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Premium Tab Bar selector inside Login card supporting 4 tabs with high-tech adaptive spacing
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF131525))
                            .padding(2.dp)
                    ) {
                        val tabs = listOf(
                            "phone" to viewModel.translate("Phone OTP", "फ़ोन OTP"),
                            "gmail" to viewModel.translate("Gmail", "जीमेल"),
                            "google" to viewModel.translate("Google Sync", "गूगल Sync"),
                            "token" to viewModel.translate("Pin/Key", "गोपनीय कुंजी")
                        )
                        tabs.forEach { (tabId, label) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (loginTabSelected == tabId) Color(0xFF1E293B) else Color.Transparent)
                                    .clickable { 
                                        loginTabSelected = tabId 
                                        phoneAuthError = ""
                                        gmailAuthError = ""
                                        googleOtpError = ""
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (loginTabSelected == tabId) Color(0xFF00E5FF) else Color(0xFF94A3B8),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (loginTabSelected == "phone") {
                        // --- PHONE NUMBER OTP AUTH BLOCK ---
                        if (!isPhoneOtpSent) {
                            Text(
                                text = viewModel.translate("ENTER PHONE NUMBER", "अपना मोबाइल नंबर डालें"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF),
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            OutlinedTextField(
                                value = phoneNumberInput,
                                onValueChange = { input -> 
                                    if (input.all { it.isDigit() } && input.length <= 10) {
                                        phoneNumberInput = input
                                    }
                                },
                                label = { Text(viewModel.translate("10-Digit Mobile Number", "10-अंकों का मोबाइल नंबर दर्ज करें"), color = Color(0xFF64748B), fontSize = 11.sp) },
                                placeholder = { Text("9876543210", color = Color.Gray.copy(alpha = 0.4f)) },
                                modifier = Modifier.fillMaxWidth().testTag("phone_number_field"),
                                leadingIcon = { 
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 12.dp, end = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+91 🇮🇳", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF00E5FF),
                                    unfocusedBorderColor = Color(0xFF131525)
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )

                            if (phoneAuthError.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(phoneAuthError, color = Color.Red, fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (phoneNumberInput.length != 10) {
                                        phoneAuthError = viewModel.translate("Please enter a valid 10-digit mobile number", "कृपया एक सही 10-अंकों का मोबाइल नंबर दर्ज करें")
                                    } else {
                                        phoneAuthError = ""
                                        // Generate random OTP code
                                        phoneGeneratedOtp = (1000..9999).random().toString()
                                        isPhoneOtpSent = true
                                        phoneOtpTimer = 60
                                        isPhoneTimerActive = true
                                        phoneOtpInput = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = viewModel.translate("Send Security OTP code ⚡", "सुरक्षा कोड (OTP) भेजें ⚡"),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        } else {
                            // OTP Input Fields when already sent
                            // SMS Notification simulation box (for testing)
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF07111A)),
                                border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = viewModel.translate("SIMULATED SMS GATEWAY RECEIPT", "सिमुलेटेड सुरक्षित एसएमएस गेटवे"),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF00E5FF)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "To: +91 $phoneNumberInput >> OMNI OTP Code is : $phoneGeneratedOtp",
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = viewModel.translate("Offline sandbox auto-generated system packet", "सैंडबॉक्स परीक्षण वातावरण के लिए बनाया गया स्वचालित पैकेट"),
                                        fontSize = 8.5.sp,
                                        color = Color.Gray,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Text(
                                text = viewModel.translate("ENTER SECONDARY SMS CODE", "एसएमएस सत्यापन ओटीपी कोड भरें"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE040FB),
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = phoneOtpInput,
                                onValueChange = { input -> 
                                    if (input.all { it.isDigit() } && input.length <= 4) {
                                        phoneOtpInput = input
                                    }
                                },
                                label = { Text(viewModel.translate("4-Digit OTP Code", "4-अंकीय ओटीपी कोड दर्ज करें"), color = Color(0xFF64748B), fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("phone_otp_field"),
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFE040FB), modifier = Modifier.size(18.dp)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFE040FB),
                                    unfocusedBorderColor = Color(0xFF131525)
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )

                            if (phoneAuthError.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(phoneAuthError, color = Color.Red, fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Timer counter / Resend Trigger
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isPhoneTimerActive) {
                                    Text(
                                        text = viewModel.translate("Resend code in ${phoneOtpTimer}s", "ओटीपी पुन: भेजें ${phoneOtpTimer}s में"),
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                } else {
                                    Text(
                                        text = viewModel.translate("Resend OTP Code", "ओटीपी कोड पुनः भेजें"),
                                        fontSize = 11.sp,
                                        color = Color(0xFF00E5FF),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable {
                                            phoneGeneratedOtp = (1000..9999).random().toString()
                                            phoneOtpTimer = 60
                                            isPhoneTimerActive = true
                                            phoneAuthError = viewModel.translate("A new OTP simulation was pushed! Check SMS receipt above.", "एक नया ओटीपी कोड सिमुलेशन पुश किया गया है! ऊपर देखें।")
                                        }
                                    )
                                }

                                Text(
                                    text = viewModel.translate("Change Number", "नंबर बदलें"),
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable {
                                        isPhoneOtpSent = false
                                        isPhoneTimerActive = false
                                        phoneAuthError = ""
                                        phoneOtpInput = ""
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (phoneOtpInput != phoneGeneratedOtp) {
                                        phoneAuthError = viewModel.translate("Invalid OTP matching failure. Attempt denied!", "अवैध ओटीपी सुरक्षा सिंक विफल। प्रवेश अस्वीकृत!")
                                    } else {
                                        phoneAuthError = ""
                                        isAuthenticatingGoogle = true
                                        authLogString = "Authenticating Phone Verification Gateway +91-$phoneNumberInput..."
                                        coroutineScope.launch {
                                            delay(700)
                                            authLogString = "Handshaking with OTP registrar signatures..."
                                            delay(600)
                                            viewModel.loginWithGoogle(
                                                viewModel.translate("Phone Verified Bhai", "फ़ोन सत्यापित यूजर"),
                                                "phone.user-$phoneNumberInput@omni-ai.io",
                                                "https://api.dicebear.com/7.x/pixel-art/svg?seed=PhoneUser$phoneNumberInput"
                                            )
                                            isAuthenticatingGoogle = false
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = viewModel.translate("Verify OTP & Open Workspace 🛡️", "ओटीपी सत्यापित करें और प्रवेश करें 🛡️"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    } else if (loginTabSelected == "gmail") {
                        // --- GMAIL / EMAIL AUTH BLOCK ---
                        Text(
                            text = viewModel.translate("PRIMARY GMAIL ACCOUNT ID", "प्राथमिक जीमेल/ईमेल दर्ज करें"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE040FB),
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = gmailInput,
                            onValueChange = { gmailInput = it },
                            label = { Text(viewModel.translate("Enter Google Email", "जीमेल आईडी दर्ज करें"), color = Color(0xFF64748B), fontSize = 11.sp) },
                            placeholder = { Text("username@gmail.com", color = Color.Gray.copy(alpha = 0.4f)) },
                            modifier = Modifier.fillMaxWidth().testTag("gmail_email_field"),
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFE040FB), modifier = Modifier.size(18.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFE040FB),
                                unfocusedBorderColor = Color(0xFF131525)
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Modern selector inside Gmail to toggle Password vs Code OTP
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF070914))
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (!gmailUseOtpMode) Color(0xFF1E293B) else Color.Transparent)
                                    .clickable { gmailUseOtpMode = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(viewModel.translate("Standard Password", "पासवर्ड विधि"), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (gmailUseOtpMode) Color(0xFF1E293B) else Color.Transparent)
                                    .clickable { gmailUseOtpMode = true; gmailAuthError = "" },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(viewModel.translate("Gmail verification OTP", "जीमेल ओटीपी कोड"), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (!gmailUseOtpMode) {
                            // Password input UI
                            OutlinedTextField(
                                value = gmailPasswordInput,
                                onValueChange = { gmailPasswordInput = it },
                                label = { Text(viewModel.translate("Account Password Structure", "पासवर्ड दर्ज करें"), color = Color(0xFF64748B), fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("gmail_password_field"),
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp)) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "toggle password visibility",
                                            tint = Color(0xFF00E5FF),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF00E5FF),
                                    unfocusedBorderColor = Color(0xFF131525)
                                ),
                                singleLine = true
                            )

                            if (gmailAuthError.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(gmailAuthError, color = Color.Red, fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (gmailInput.isEmpty() || !gmailInput.contains("@")) {
                                        gmailAuthError = viewModel.translate("Please enter a valid Gmail / Email address", "कृपया एक सही जीमेल आईडी दर्ज करें")
                                    } else if (gmailPasswordInput.length < 4) {
                                        gmailAuthError = viewModel.translate("Password must be at least 4 characters", "पासवर्ड कम से कम 4 अक्षरों का होना चाहिए")
                                    } else {
                                        gmailAuthError = ""
                                        isAuthenticatingGoogle = true
                                        authLogString = "Authenticating secret nodes for $gmailInput..."
                                        coroutineScope.launch {
                                            delay(600)
                                            authLogString = "Access Approved! Spawning developer session profiles..."
                                            delay(500)
                                            val namePart = gmailInput.split("@").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "User"
                                            viewModel.loginWithGoogle(
                                                namePart,
                                                gmailInput,
                                                "https://api.dicebear.com/7.x/pixel-art/svg?seed=${namePart}"
                                            )
                                            isAuthenticatingGoogle = false
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE040FB)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = viewModel.translate("Sign In Securely 🚀", "पासवर्ड से प्रवेश करें 🚀"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        } else {
                            // OTP verification sub-flow for Gmail
                            if (!isGmailOtpSent) {
                                Button(
                                    onClick = {
                                        if (gmailInput.isEmpty() || !gmailInput.contains("@")) {
                                            gmailAuthError = viewModel.translate("Please enter a valid Gmail / Email address", "कृपया सही ईमेल एड्रेस दर्ज करें")
                                        } else {
                                            gmailAuthError = ""
                                            gmailGeneratedOtp = (1000..9999).random().toString()
                                            isGmailOtpSent = true
                                            gmailOtpTimer = 60
                                            isGmailTimerActive = true
                                            gmailOtpInput = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE040FB)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(viewModel.translate("Request Gmail Security OTP Code 📧", "जीमेल पर सुरक्षा कोड (OTP) भेजें 📧"), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                }
                            } else {
                                // OTP Sent Gmail verification Box
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF170E1A)),
                                    border = BorderStroke(1.dp, Color(0xFFE040FB).copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFE040FB), modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = viewModel.translate("INBOUND GMAIL CLOUD INBOX MOCKUP", "सुरक्षित इनबाउंड क्लाउड ईमेल संदेश"),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color(0xFFE040FB)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Subject: Omni AI Portal Login code - code is $gmailGeneratedOtp",
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Recipient: $gmailInput",
                                            fontSize = 9.sp,
                                            color = Color.Gray,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                OutlinedTextField(
                                    value = gmailOtpInput,
                                    onValueChange = { input -> 
                                        if (input.all { it.isDigit() } && input.length <= 4) {
                                            gmailOtpInput = input
                                        }
                                    },
                                    label = { Text(viewModel.translate("4-Digit Gmail Verification OTP", "4-अंकीय ईमेल ओटीपी सुरक्षा कोड"), color = Color(0xFF64748B), fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("gmail_otp_field"),
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF00E5FF),
                                        unfocusedBorderColor = Color(0xFF131525)
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )

                                if (gmailAuthError.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(gmailAuthError, color = Color.Red, fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isGmailTimerActive) {
                                        Text(
                                            text = viewModel.translate("Resend key in ${gmailOtpTimer}s", "ओटीपी पुन: भेजें ${gmailOtpTimer}s में"),
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    } else {
                                        Text(
                                            text = viewModel.translate("Resend OTP Code", "ओटीपी पुनः भेजें"),
                                            fontSize = 11.sp,
                                            color = Color(0xFFE040FB),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable {
                                                gmailGeneratedOtp = (1000..9999).random().toString()
                                                gmailOtpTimer = 60
                                                isGmailTimerActive = true
                                                gmailAuthError = viewModel.translate("A new simulated activation mail was pushed! Check inbox above.", "एक नया जीमेल संदेश ऊपर पुश किया गया है! चेक करें।")
                                            }
                                        )
                                    }

                                    Text(
                                        text = viewModel.translate("Change Gmail", "ईमेल बदलें"),
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.clickable {
                                            isGmailOtpSent = false
                                            isGmailTimerActive = false
                                            gmailAuthError = ""
                                            gmailOtpInput = ""
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (gmailOtpInput != gmailGeneratedOtp) {
                                            gmailAuthError = viewModel.translate("OTP confirmation mismatch! Authorization denied.", "ओटीपी मिलान विफल! प्रवेश सुरक्षा अस्वीकार।")
                                        } else {
                                            gmailAuthError = ""
                                            isAuthenticatingGoogle = true
                                            authLogString = "Verifying secure OTP channel verification for digital nodes..."
                                            coroutineScope.launch {
                                                delay(600)
                                                val namePart = gmailInput.split("@").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "User"
                                                viewModel.loginWithGoogle(
                                                    namePart,
                                                    gmailInput,
                                                    "https://api.dicebear.com/7.x/pixel-art/svg?seed=${namePart}"
                                                )
                                                isAuthenticatingGoogle = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE040FB)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = viewModel.translate("Verify OTP & Open Portal 🛡️", "ओटीपी सत्यापित करें & प्रवेश करें 🛡️"),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    } else if (loginTabSelected == "google") {
                        // --- GOOGLE SIGN IN PORTAL ---
                        // Prominent Google ID selection button with futuristic glow borders
                        Button(
                            onClick = {
                                showGoogleAuthSelector = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("google_login_btn")
                                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Google logo representation
                                Canvas(modifier = Modifier.size(16.dp)) {
                                    val r = size.width / 2f
                                    drawCircle(color = Color(0xFF4285F4), radius = r)
                                    drawCircle(color = Color(0xFFEA4335), radius = r * 0.7f)
                                    drawCircle(color = Color(0xFFFBBC05), radius = r * 0.45f)
                                    drawCircle(color = Color(0xFF34A853), radius = r * 0.22f)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = viewModel.translate("Select Google Developer Account", "गूगल खाता चुनकर प्रवेश करें"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = viewModel.translate("🛡️ Double security activation enabled. Select Google account to trigger your registered security OTP authentication screen.", "🛡️ दोहरी सुरक्षा सक्रिय। अपने गूगल अकाउंट को चुनने के बाद सुरक्षा ओटीपी सत्यापन स्क्रीन सक्रिय हो जाएगी।"),
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // --- SECRET SECURITY PIN TAB ---
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text(viewModel.translate("Secure Dev Key Node (Password)", "सुरक्षित डेवलपर पासवर्ड दर्ज करें"), color = Color(0xFF818CF8), fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("dev_pin_field"),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFE040FB), modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "toggle password visibility",
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF1E293B)
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (passwordInput.isEmpty()) {
                                    phoneAuthError = viewModel.translate("Security Key PIN cannot be empty", "सुरक्षा कुंजी/पासवर्ड खाली नहीं होना चाहिए")
                                } else {
                                    isAuthenticatingGoogle = true
                                    authLogString = "Synthesizing Security PIN validity..."
                                    coroutineScope.launch {
                                        delay(600)
                                        viewModel.loginWithGoogle(
                                            "Dev Administrator",
                                            "admin.kernel@omni-ai.io",
                                            "https://api.dicebear.com/7.x/pixel-art/svg?seed=AdminOmni"
                                        )
                                        isAuthenticatingGoogle = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = viewModel.translate("Sign In Admin Console 🛡️", "एडमिन कंसोल लॉगिन करें 🛡️"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Remember Me Custom Toggle Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { rememberMeSelected = !rememberMeSelected }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberMeSelected,
                            onCheckedChange = { rememberMeSelected = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF00E5FF),
                                uncheckedColor = Color(0xFF64748B)
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = viewModel.translate("Keep credentials cached in secure storage", "इस डिवाइस पर क्रेडेंशियल सुरक्षित रूप से याद रखें"),
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Real-time Auth progress ticker logs
            if (isAuthenticatingGoogle) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(0.95f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101323)),
                    border = BorderStroke(1.dp, Color(0xFFE040FB).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF00E5FF), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(authLogString, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Premium Visual additions: Fast bypass profiles
            Text(
                text = viewModel.translate("⚡ FAST DEPLOYER SESSIONS (QUICK LOGON)", "⚡ त्वरित डेवलपर प्रोफाइल प्रवेश"),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                style = TextStyle(letterSpacing = 1.sp),
                modifier = Modifier.align(Alignment.Start).padding(horizontal = 6.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Profile 1: Sudhan Bhai
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedGoogleName = "Sudhan Bhai"
                        selectedGoogleEmail = "bhaisudhan035@gmail.com"
                        selectedGoogleAvatar = "https://api.dicebear.com/7.x/pixel-art/svg?seed=SudhanBhai"
                        googleGeneratedOtp = (1000..9999).random().toString()
                        googleOtpInput = ""
                        googleOtpError = ""
                        googleOtpTimer = 60
                        isGoogleTimerActive = true
                        showGoogleOtpDialog = true
                    },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0F1B)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E5FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("SB", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sudhan Bhai", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Text("bhaisudhan035@gmail.com", fontSize = 11.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                    }
                    Text(
                        text = "Keyring Active ✅",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF00E5FF).copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Profile 2: Admin Guest
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedGoogleName = "Guest Administrator"
                        selectedGoogleEmail = "guest.administrator@omni-ai.io"
                        selectedGoogleAvatar = "https://api.dicebear.com/7.x/pixel-art/svg?seed=AdministratorOmni"
                        googleGeneratedOtp = (1000..9999).random().toString()
                        googleOtpInput = ""
                        googleOtpError = ""
                        googleOtpTimer = 60
                        isGoogleTimerActive = true
                        showGoogleOtpDialog = true
                    },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0F1B)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE040FB).copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE040FB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("GA", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Guest Administrator", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Text("guest.administrator@omni-ai.io", fontSize = 11.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                    }
                    Text(
                        text = "Sandbox Node ✅",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE040FB),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE040FB).copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bilingual Switcher
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                    .background(Color(0xFF0E111F))
                    .clickable { viewModel.toggleLanguage() }
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Language",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isHindi) "👉 English Language" else "👉 हिंदी (Hindi) भाषा चुनें",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Replay loading boot sequence option
            Text(
                text = viewModel.translate("Show OMNI Boot Simulation", "ओम्नी बूट सिमुलेशन फिर से दिखाएं"),
                fontSize = 11.sp,
                color = Color.DarkGray,
                modifier = Modifier
                    .clickable {
                        bootProgress = 0f
                        bootPhaseComplete = false
                    }
                    .padding(6.dp)
            )
        }

        // Beautiful Interactive Dialog portraying Google Native selection accounts
        if (showGoogleAuthSelector) {
            AlertDialog(
                onDismissRequest = { showGoogleAuthSelector = false },
                containerColor = Color(0xFF0A0F1D),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (isHindi) "गूगल से अकाउंट चुनें" else "Choose a Google account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = "To sync and authorize with OMNI-AI.io",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Account 1: bhaisudhan035@gmail.com (From user metadata)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showGoogleAuthSelector = false
                                    selectedGoogleName = "Sudhan Bhai"
                                    selectedGoogleEmail = "bhaisudhan035@gmail.com"
                                    selectedGoogleAvatar = "https://api.dicebear.com/7.x/pixel-art/svg?seed=SudhanBhai"
                                    googleGeneratedOtp = (1000..9999).random().toString()
                                    googleOtpInput = ""
                                    googleOtpError = ""
                                    googleOtpTimer = 60
                                    isGoogleTimerActive = true
                                    showGoogleOtpDialog = true
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF171B26)),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.25f))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00E5FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("SB", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Sudhan Bhai", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    Text("bhaisudhan035@gmail.com", fontSize = 11.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }

                        // Account 2: Administrator Guest
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showGoogleAuthSelector = false
                                    selectedGoogleName = "Guest Administrator"
                                    selectedGoogleEmail = "guest.administrator@omni-ai.io"
                                    selectedGoogleAvatar = "https://api.dicebear.com/7.x/pixel-art/svg?seed=AdministratorOmni"
                                    googleGeneratedOtp = (1000..9999).random().toString()
                                    googleOtpInput = ""
                                    googleOtpError = ""
                                    googleOtpTimer = 60
                                    isGoogleTimerActive = true
                                    showGoogleOtpDialog = true
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF171B26))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE040FB)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("GA", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Guest Administrator", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    Text("guest.administrator@omni-ai.io", fontSize = 11.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showGoogleAuthSelector = false }) {
                        Text(if (isHindi) "रद्द करें" else "Cancel", color = Color(0xFFE040FB), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // --- SECURE GOOGLE SMART VERIFICATION OTP DIALOG ---
        if (showGoogleOtpDialog) {
            AlertDialog(
                onDismissRequest = { showGoogleOtpDialog = false },
                containerColor = Color(0xFF0A0F1D),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.translate("Google Smart OTP Verification", "गूगल स्मार्ट सुरक्षा OTP सत्यापन"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = viewModel.translate("Enter verification code sent to your Google Authenticator email: $selectedGoogleEmail", "ईमेल $selectedGoogleEmail पर भेजे गए कोड को यहाँ दर्ज करें"),
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Simulated Inboxes
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF05171C)),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "GOOGLE AUTHENTICATOR SECURE BOX",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF00E5FF)
                                    )
                                }
                                Text(
                                    text = "2-FA Verification Code is : $googleGeneratedOtp",
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = googleOtpInput,
                            onValueChange = { input -> 
                                if (input.all { it.isDigit() } && input.length <= 4) {
                                    googleOtpInput = input
                                }
                            },
                            label = { Text(viewModel.translate("Enter 4-Digit Security Code", "4-अंकीय सुरक्षा ओटीपी कोड डालें"), color = Color(0xFF64748B), fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("google_otp_dialog_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF1E293B)
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        if (googleOtpError.isNotEmpty()) {
                            Text(googleOtpError, color = Color.Red, fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isGoogleTimerActive) {
                                Text(
                                    text = viewModel.translate("Get new code in ${googleOtpTimer}s", "नया कोड ${googleOtpTimer}s में"),
                                    fontSize = 10.5.sp,
                                    color = Color.Gray
                                )
                            } else {
                                Text(
                                    text = viewModel.translate("Resend Auth Code", "ओटीपी कोड पुनः भेजें"),
                                    fontSize = 11.sp,
                                    color = Color(0xFF00E5FF),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        googleGeneratedOtp = (1000..9999).random().toString()
                                        googleOtpTimer = 60
                                        isGoogleTimerActive = true
                                        googleOtpError = viewModel.translate("Resent successfully! Use the code above.", "पुन: भेजा गया! ऊपर दिया कोड इस्तेमाल करें।")
                                    }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (googleOtpInput != googleGeneratedOtp) {
                                googleOtpError = viewModel.translate("Security OTP verification signature match failed!", "ओटीपी सुरक्षा हस्ताक्षरित मिलान विफल रहा!")
                            } else {
                                googleOtpError = ""
                                showGoogleOtpDialog = false
                                isAuthenticatingGoogle = true
                                authLogString = "Authenticating Google credentials and token registers..."
                                coroutineScope.launch {
                                    delay(600)
                                    viewModel.loginWithGoogle(selectedGoogleName, selectedGoogleEmail, selectedGoogleAvatar)
                                    isAuthenticatingGoogle = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                    ) {
                        Text(viewModel.translate("Confirm Verification 🔓", "सुरक्षा प्रमाणित करें 🔓"), color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGoogleOtpDialog = false }) {
                        Text(viewModel.translate("Cancel", "रद्द करें"), color = Color.Gray)
                    }
                }
            )
        }
    }
}

// --- WORSPACE LAYOUT PANELS (Fully responsive layout optimized for mobile views) ---
@Composable
fun IdeWorkspaceLayout(viewModel: ReplViewModel, tokens: VisualThemeTokens) {
    val isHindi by viewModel.isHindi.collectAsState()
    val isOffline by viewModel.isOfflineMode.collectAsState()
    val isBackingUp by viewModel.isBackingUp.collectAsState()
    val syncText by viewModel.backupSyncedTime.collectAsState()
    val userDetails by viewModel.userDetails.collectAsState()
    val selectedProject by viewModel.selectedProject.collectAsState()
 
    val activeTabFlow by viewModel.activeTabFlow.collectAsState()
    var activeTab by remember { mutableStateOf("playground") }

    LaunchedEffect(activeTabFlow) {
        if (activeTab != activeTabFlow) {
            activeTab = activeTabFlow
        }
    }
 
    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(tokens.surface)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Logo & Translation Switcher
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "OMNI-AI",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = tokens.primary
                        )
                        Text(
                            text = if (isOffline) "⚠️ OFFLINE" else "☁️ ONLINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOffline) tokens.error else tokens.success,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isOffline) tokens.error.copy(alpha=0.15f) else tokens.success.copy(alpha=0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
 
                    // Cloud state indicators and Google user icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Translation switch
                        IconButton(
                            onClick = { viewModel.toggleLanguage() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(tokens.background)
                        ) {
                            Text(
                                text = if (isHindi) "EN" else "हिन",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = tokens.primary
                            )
                        }
 
                        // Sync indicator
                        Icon(
                            imageVector = if (isBackingUp) Icons.Default.CloudSync else Icons.Default.CloudQueue,
                            contentDescription = "Sync state icon",
                            tint = if (isBackingUp) tokens.primary else tokens.textMuted,
                            modifier = Modifier.size(20.dp)
                        )
 
                        // Google user profile avatar simulation
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(tokens.secondary)
                                .clickable { viewModel.logout() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (userDetails?.get("name") ?: "C").take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = tokens.background,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
 
                // Sub-status line
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(tokens.surface.copy(alpha = 0.8f))
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedProject?.let { "${it.templateIcon} ${it.name} (${it.languageType})" } ?: viewModel.translate("No Project Active", "कोई प्रोजेक्ट सक्रिय नहीं"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = tokens.text
                    )
                    Text(
                        text = "Backup: $syncText",
                        fontSize = 10.sp,
                        color = tokens.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 180.dp)
                    )
                }
            }
        },
        bottomBar = {
            // Horizontal scroll responsive IDE workspaces switcher specifically styled for smaller screens
            TabToolbar(
                activeTab = activeTab,
                isHindi = isHindi,
                tokens = tokens,
                onTabSelected = { 
                    activeTab = it
                    viewModel.setActiveTab(it)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "playground" -> PlaygroundTab(viewModel, tokens)
                "files" -> ProjectFilesTab(viewModel, tokens)
                "editor" -> EditorTab(viewModel, tokens)
                "console" -> ConsoleTab(viewModel, tokens)
                "git" -> GitCommitTab(viewModel, tokens)
                "deploy" -> DeploymentTab(viewModel, tokens)
                "collab" -> CollaborationTab(viewModel, tokens)
                "themes" -> StyleCustomizerTab(viewModel, tokens)
                "ai" -> GhostwriterChatTab(viewModel, tokens)
                "labs" -> LabsTab(viewModel, tokens)
            }
        }
    }
}
 
// --- Horizontal Scroll Navigation Bar for IDE options ---
@Composable
fun TabToolbar(
    activeTab: String,
    isHindi: Boolean,
    tokens: VisualThemeTokens,
    onTabSelected: (String) -> Unit
) {
    val items = listOf(
        Triple("playground", Icons.Default.Science, if (isHindi) "प्लेग्राउंड" else "Playground"),
        Triple("files", Icons.Default.Folder, if (isHindi) "फ़ाइलें" else "Files"),
        Triple("editor", Icons.Default.Edit, if (isHindi) "संपादक" else "Editor"),
        Triple("console", Icons.Default.Terminal, if (isHindi) "कंसोल" else "Console"),
        Triple("git", Icons.Default.History, if (isHindi) "गिट" else "Git"),
        Triple("deploy", Icons.Default.CloudUpload, if (isHindi) "तैनाती" else "Deploy"),
        Triple("collab", Icons.Default.Groups, if (isHindi) "सहयोग" else "Team"),
        Triple("themes", Icons.Default.Palette, if (isHindi) "थीम" else "Themes"),
        Triple("ai", Icons.Default.AutoAwesome, if (isHindi) "AI" else "AI Chat"),
        Triple("labs", Icons.Default.DeveloperMode, if (isHindi) "लैब्स" else "Labs")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(tokens.surface)
            .drawBehind {
                // Subtle glowing frosted border on top of the tab bar
                drawLine(
                    color = tokens.primary.copy(alpha = 0.2f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2f
                )
            }
            .horizontalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (tabId, icon, label) ->
            val isActive = activeTab == tabId
            
            // Soft spring animation for active selection scaling
            val activeScale by animateFloatAsState(
                targetValue = if (isActive) 1.05f else 0.95f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "scale"
            )
            
            val activeBgColor by animateColorAsState(
                targetValue = if (isActive) tokens.primary.copy(alpha = 0.12f) else Color.Transparent,
                animationSpec = tween(250),
                label = "bgColor"
            )

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = activeScale
                        scaleY = activeScale
                    }
                    .clip(RoundedCornerShape(14.dp))
                    .background(activeBgColor)
                    .border(
                        width = 1.dp,
                        color = if (isActive) tokens.primary.copy(alpha = 0.25f) else Color.Transparent,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onTabSelected(tabId) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .widthIn(min = 50.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isActive) tokens.primary else tokens.textMuted,
                        modifier = Modifier.size(21.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = label,
                        fontSize = 10.5.sp,
                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isActive) tokens.primary else tokens.textMuted
                    )
                    
                    // Elegant micro active-dot at the very bottom
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 4.dp else 0.dp)
                            .clip(CircleShape)
                            .background(tokens.primary)
                    )
                }
            }
        }
    }
}

// --- TAB 1: FILES EXPLORER & 56 TEMPLATES SELECTOR ---
@Composable
fun ProjectFilesTab(viewModel: ReplViewModel, tokens: VisualThemeTokens) {
    val isHindi by viewModel.isHindi.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val selectedProject by viewModel.selectedProject.collectAsState()
    val projectFiles by viewModel.projectFiles.collectAsState()
    val activeFile by viewModel.activeFile.collectAsState()

    var showNewProjDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Frontend") } // "Frontend", "Backend", "System", "Creative"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Active Project Selection and deletion Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = viewModel.translate("My Active Projects", "मेरे सक्रिय प्रोजेक्ट्स"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = tokens.primary
            )
            Row {
                IconButton(onClick = { showNewProjDialog = true }, modifier = Modifier.testTag("create_proj_btn")) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add project icon", tint = tokens.primary)
                }
                if (selectedProject != null) {
                    IconButton(onClick = { viewModel.deleteSelectedProject() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Delete project icon", tint = tokens.error)
                    }
                }
            }
        }

        // Project selector chips
        if (projects.isEmpty()) {
            Text(
                text = viewModel.translate(
                    "No projects loaded. Click '+' to create a new workspace from templates.",
                    "कोई प्रोजेक्ट उपलब्ध नहीं है। नया कार्यक्षेत्र बनाने के लिए '+' पर क्लिक करें।"
                ),
                fontSize = 13.sp,
                color = tokens.textMuted,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                projects.forEach { proj ->
                    val isSel = proj.id == selectedProject?.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) tokens.primary.copy(alpha = 0.2f) else tokens.surface)
                            .border(1.dp, if (isSel) tokens.primary else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable { viewModel.selectProjectById(proj.id) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(proj.templateIcon, modifier = Modifier.padding(end = 4.dp))
                            Text(proj.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isSel) tokens.primary else tokens.text)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Files inside the selected project structure
        if (selectedProject != null) {
            Text(
                text = viewModel.translate("Project Source Files", "प्रोजेक्ट सोर्स फ़ाइलें"),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = tokens.text
            )

            // Dynamic File actions and direct file adding
            var nameFileToCreate by remember { mutableStateOf("") }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = nameFileToCreate,
                    onValueChange = { nameFileToCreate = it },
                    placeholder = { Text(viewModel.translate("e.g. style.css", "जैसे style.css")) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("new_filename_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = tokens.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (nameFileToCreate.isNotBlank()) {
                            viewModel.createNewFileInProject(nameFileToCreate)
                            nameFileToCreate = ""
                        }
                    },
                    modifier = Modifier.testTag("create_file_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.primary)
                ) {
                    Text(viewModel.translate("Add", "जोड़ें"), color = tokens.background)
                }
            }

            // Existing files list inside selected project
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = tokens.surface)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    projectFiles.forEach { file ->
                        val isAct = file.id == activeFile?.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAct) tokens.primary.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable { viewModel.selectFile(file) }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (file.path.endsWith(".html")) Icons.Default.Html else Icons.Default.Code,
                                    contentDescription = null,
                                    tint = if (isAct) tokens.primary else tokens.textMuted,
                                    modifier = Modifier.padding(end = 10.dp)
                                )
                                Text(
                                    text = file.path,
                                    fontWeight = if (isAct) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isAct) tokens.primary else tokens.text,
                                    fontSize = 14.sp
                                )
                            }
                            IconButton(onClick = { viewModel.deleteFileFromProject(file.path) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete file key", tint = tokens.textMuted.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Template catalog describing all 56 templates for quick project clones!
        Text(
            text = viewModel.translate("Browse 56 Language Templates", "56 भाषा और फ्रेमवर्क टेम्पलेट एक्सप्लोर करें"),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = tokens.primary
        )

        // Category toggles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val categorLabels = listOf("Frontend", "Backend", "System", "Creative")
            categorLabels.forEach { cat ->
                val isSel = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSel) tokens.primary else tokens.surface)
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = viewModel.translate(cat, when(cat) {
                            "Frontend" -> "वेब फ्रंटएंड"
                            "Backend" -> "बैकएंड सेवाएं"
                            "System" -> "सिस्टम कोड"
                            else -> "क्रिएटिव डेटा"
                        }),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isSel) tokens.background else tokens.text
                    )
                }
            }
        }

        // Grid cards displaying templates of active category
        val categoryTemplates = ReplRepository.templates.filter { it.category == selectedCategory }
        categoryTemplates.forEach { temp ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable {
                        viewModel.createProject(temp.name, temp.id)
                    },
                colors = CardDefaults.cardColors(containerColor = tokens.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(tokens.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(temp.icon, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(temp.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = tokens.text)
                        Text(temp.description, fontSize = 11.sp, color = tokens.textMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Lang: ${temp.lang}", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = tokens.primary)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = "Clone template", tint = tokens.primary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }

    // Modal dialog to create project manually
    if (showNewProjDialog) {
        var projName by remember { mutableStateOf("") }
        var selectedTempId by remember { mutableStateOf("html5") }

        AlertDialog(
            onDismissRequest = { showNewProjDialog = false },
            title = { Text(viewModel.translate("Create New Project", "नया प्रोजेक्ट बनाएं"), color=tokens.text) },
            containerColor = tokens.surface,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = projName,
                        onValueChange = { projName = it },
                        label = { Text(viewModel.translate("Project Name", "प्रोजेक्ट का नाम")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_proj_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = tokens.primary)
                    )

                    Text(viewModel.translate("Choose Starter Template", "बेस टेम्पलेट चुनें"), fontSize=12.sp, fontWeight=FontWeight.Bold, color=tokens.primary)
                    
                    // Simple list of first 5 templates
                    ReplRepository.templates.take(5).forEach { temp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedTempId == temp.id) tokens.primary.copy(alpha=0.15f) else Color.Transparent)
                                .clickable { selectedTempId = temp.id }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(temp.icon, modifier = Modifier.padding(end=10.dp))
                            Text(temp.name, fontSize=12.sp, color=tokens.text, modifier = Modifier.weight(1f))
                            if (selectedTempId == temp.id) {
                                Icon(Icons.Default.Check, contentDescription = "Checked icon", tint = tokens.primary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createProject(projName, selectedTempId)
                        showNewProjDialog = false
                    },
                    modifier = Modifier.testTag("confirm_create_proj_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.primary)
                ) {
                    Text(viewModel.translate("Bootstrap", "सृजन करें"), color = tokens.background)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjDialog = false }) {
                    Text(viewModel.translate("Cancel", "रद्द करें"), color = tokens.primary)
                }
            }
        )
    }
}

// --- TAB 2: ADVANCED TEXT EDITOR (With collaborative simulated multiplayer cursors) ---
@Composable
fun EditorTab(viewModel: ReplViewModel, tokens: VisualThemeTokens) {
    val isHindi by viewModel.isHindi.collectAsState()
    val activeFile by viewModel.activeFile.collectAsState()
    val codeByRef by viewModel.codeBuffer.collectAsState()
    val teammates by viewModel.teammates.collectAsState()
    val collabActive by viewModel.collaborationActive.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        CyborgChatbotBackground(modifier = Modifier.fillMaxSize())
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = activeFile?.let { "✏️ editing: ${it.path}" } ?: viewModel.translate("No File Selected to edit", "कोई फ़ाइल चुनी नहीं गई है"),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = tokens.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.saveCurrentBuffer() },
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.primary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp).padding(end=4.dp))
                        Text(
                            text = viewModel.translate("Save", "सहेजें"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = tokens.background
                        )
                    }
                }

                Button(
                    onClick = { viewModel.executeActiveCode() },
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.success),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("run_code_editor_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp).padding(end=4.dp))
                        Text(
                            text = viewModel.translate("Run", "चलाएं"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = tokens.background
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Multi-line responsive text workspace editor
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(tokens.editorBg.copy(alpha = 0.45f))
                .border(1.dp, tokens.lineHighlight.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Interactive lined index editor matching compiler styles
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Line counts sidebar
                    Column(
                        modifier = Modifier.padding(end = 12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        for (i in 1..maxOf(15, codeByRef.split("\n").size + 3)) {
                            Text(
                                text = "$i",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = tokens.textMuted.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Raw basic text field allowing live typing
                    Box(modifier = Modifier.weight(1f)) {
                        BasicTextField(
                            value = codeByRef,
                            onValueChange = { viewModel.updateCodeBuffer(it) },
                            textStyle = TextStyle(
                                color = tokens.editorText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            ),
                            cursorBrush = SolidColor(tokens.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("code_editor_textarea"),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Default
                            )
                        )

                        // Collaborative floating teammate typing indicator
                        if (collabActive && teammates.isNotEmpty()) {
                            teammates.forEachIndexed { index, mate ->
                                val parsedColor = try { Color(android.graphics.Color.parseColor(mate.colorHex)) } catch (e: Exception) { Color.Gray }
                                Box(
                                    modifier = Modifier
                                        .offset(y = (mate.lineIndex * 18).dp, x = (mate.charIndex * 6).dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(parsedColor)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = mate.name,
                                        fontSize = 8.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (collabActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(tokens.primary.copy(alpha = 0.1f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(tokens.success))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = viewModel.translate("Real-time team editing syncing instantly...", "टीम संपादन वास्तविक समय में सिंक हो रहा है..."),
                    fontSize = 11.sp,
                    color = tokens.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = tokens.surface.copy(alpha = 0.65f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, tokens.lineHighlight.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewModel.translate("🛠️ Dev Productivity Toolbox", "🛠️ देव उत्पादकता उपकरण"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = tokens.primary
                    )
                    Text(
                        text = viewModel.translate("Instant Injectors & Formatters", "इंजेक्टर और फॉर्मेटर्स"),
                        fontSize = 9.sp,
                        color = tokens.textMuted
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val original = codeByRef
                            var currentIndent = 0
                            val indented = original.split("\n").map { line ->
                                val trimmed = line.trim()
                                if (trimmed.startsWith("}") || trimmed.startsWith("</") || trimmed.startsWith("]")) {
                                    currentIndent = maxOf(0, currentIndent - 1)
                                }
                                val out = "    ".repeat(currentIndent) + trimmed
                                if (trimmed.endsWith("{") || trimmed.endsWith("<div") || trimmed.endsWith("<body") || trimmed.endsWith("<main>") || trimmed.endsWith(":[") || (trimmed.startsWith("def ") && trimmed.endsWith(":"))) {
                                    currentIndent++
                                }
                                out
                            }.joinToString("\n")
                            
                            viewModel.updateCodeBuffer(indented)
                            viewModel.saveCurrentBuffer()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.secondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(32.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(viewModel.translate("💅 Clean / Format", "💅 फॉर्मेट करें"), fontSize = 10.sp, color = tokens.background)
                    }
                    
                    Button(
                        onClick = {
                            val original = codeByRef
                            val minified = original.split("\n")
                                .map { it.trim() }
                                .filter { line -> 
                                    !(line.startsWith("//") || line.startsWith("#") || (line.startsWith("/*") && line.endsWith("*/")))
                                }
                                .filter { it.isNotBlank() }
                                .joinToString(" ") { it.replace("\\s+".toRegex(), " ") }
                            viewModel.updateCodeBuffer(minified)
                            viewModel.saveCurrentBuffer()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.secondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(32.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(viewModel.translate("🤐 Minify Code", "🤐 मिनीफ़ाई करें"), fontSize = 10.sp, color = tokens.background)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = viewModel.translate("Select Template Boilerplate snippet to insert:", "सम्मिलित करने के लिए कोड स्निपेट चुनें:"),
                    fontSize = 10.sp,
                    color = tokens.text,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val snippets = listOf(
                        "HTML5 CSS Grid" to """<!-- Responsive Layout -->
<div class="app-layout" style="display: grid; grid-template-columns: 200px 1fr; gap: 15px; color: #fff; font-family: sans-serif;">
  <aside style="background: rgba(255,255,255,0.05); padding: 15px; border-radius: 8px;">
    <h3>Sidebar Menu</h3>
    <ul style="list-style: none; padding: 0;">
      <li>📂 Files</li>
      <li>⚙️ Settings</li>
    </ul>
  </aside>
  <main style="background: rgba(255,255,255,0.02); padding: 15px; border-radius: 8px;">
    <h2>Dashboard View Container</h2>
    <p>Responsive design injected dynamically via DevRepl toolbox.</p>
  </main>
</div>""",
                        "JSON Fetch API Call" to """// Safe fetch API call pattern
async function fetchData(url = "https://api.github.com/users") {
    try {
        console.log("Dispatching network query target...");
        const response = await fetch(url);
        if (!response.ok) throw new Error("Status code match failure!");
        const data = await response.json();
        console.log("Success! Extracted response dataset:", data);
        return data;
    } catch (error) {
        console.error("Endpoint transaction issue occurred:", error.message);
    }
}""",
                        "CSS Glassmorphic Box" to """.glass-card {
    background: rgba(255, 255, 255, 0.05);
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 16px;
    box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.3);
    padding: 24px;
    transition: transform 0.2s ease-in-out;
}
.glass-card:hover {
    transform: translateY(-4px);
}""",
                        "Python Serverless Route" to """# Rapid routing template for Python sandbox
import json

def routing_handler(request_body):
    try:
        data = json.loads(request_body)
        print("Handling route transaction dynamically context...")
        return {
            "statusCode": 200,
            "body": {
                "status": "success",
                "message": f"Processed item {data.get('id', 'N/A')}"
            }
        }
    except Exception as e:
        return {
            "statusCode": 400,
            "error": str(e)
        }"""
                    )
                    
                    snippets.forEach { (label, codeText) ->
                        Button(
                            onClick = {
                                val current = codeByRef
                                val spacer = if (current.isBlank()) "" else "\n\n"
                                viewModel.updateCodeBuffer(current + spacer + codeText)
                                viewModel.saveCurrentBuffer()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = tokens.primary.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(label, fontSize = 9.sp, color = tokens.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
}

// --- TAB 3: CONSOLE & SIMULATION PREVIEW (Editor + Console seamless integration) ---
data class PreviewPart(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val radius: Float,
    val color: Color
)

@Composable
fun ConsoleTab(viewModel: ReplViewModel, tokens: VisualThemeTokens) {
    val isHindi by viewModel.isHindi.collectAsState()
    val logs by viewModel.consoleLogs.collectAsState()
    val isRunning by viewModel.isCodeRunning.collectAsState()
    val previewOutput by viewModel.runPreviewOutput.collectAsState()

    var inputCommand by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // --- INTERACTIVE PREVIEW CONTROLS ---
    var physicsGravity by remember { mutableStateOf(0.15f) }
    var physicsSpeed by remember { mutableStateOf(1.0f) }
    var particleThemeColor by remember { mutableStateOf("Neon Matrix Theme") }
    
    // Core particles list
    var particleList by remember {
        mutableStateOf(List(22) {
            val angle = Math.random() * 2 * Math.PI
            val speed = (2..5).random().toFloat()
            PreviewPart(
                x = (40..260).random().toFloat(),
                y = (30..100).random().toFloat(),
                vx = (Math.cos(angle) * speed).toFloat(),
                vy = (Math.sin(angle) * speed).toFloat(),
                radius = (5..11).random().toFloat(),
                color = if (Math.random() > 0.5) Color(0xFF00E5FF) else Color(0xFFE040FB)
            )
        })
    }

    // Trigger running real-time simulation updates
    LaunchedEffect(physicsSpeed, physicsGravity, particleThemeColor) {
        while (true) {
            delay(16) // ~60fps ticks
            particleList = particleList.map { p ->
                var newX = p.x + p.vx * physicsSpeed
                var newY = p.y + p.vy * physicsSpeed + physicsGravity * 3.5f
                var newVx = p.vx
                var newVy = p.vy

                // Boundary collision controls (elastic bounce)
                if (newX < p.radius || newX > 320f - p.radius) {
                    newVx = -p.vx * 0.95f
                    newX = newX.coerceIn(p.radius, 320f - p.radius)
                }
                if (newY < p.radius || newY > 150f - p.radius) {
                    newVy = -p.vy * 0.85f // dampened bounce
                    newY = newY.coerceIn(p.radius, 150f - p.radius)
                }

                // Inject a small brownian drift to avoid static lock
                newVx += ((Math.random() - 0.5) * 0.08).toFloat()

                // Update customized colors on theme modification
                val updatedClr = when (particleThemeColor) {
                    "Neon Matrix Theme" -> if (p.radius > 8f) Color(0xFF00E5FF) else Color(0xFFE040FB)
                    "Solar Flare Theme" -> if (p.radius > 8f) Color(0xFFFFD700) else Color(0xFFFF4500)
                    "Golden Hour Theme" -> if (p.radius > 8f) Color(0xFFEAB308) else Color(0xFFF97316)
                    else -> if (p.radius > 8f) Color(0xFF38BDF8) else Color(0xFF818CF8)
                }

                // Absolute sanity check to prevent NaN/Infinite or extreme values
                if (!newX.isFinite() || newX < 0f || newX > 320f) {
                    newX = (30..290).random().toFloat()
                }
                if (!newY.isFinite() || newY < 0f || newY > 150f) {
                    newY = (20..130).random().toFloat()
                }
                if (!newVx.isFinite()) {
                    newVx = ((Math.random() - 0.5) * 4).toFloat()
                }
                if (!newVy.isFinite()) {
                    newVy = ((Math.random() - 0.5) * 4).toFloat()
                }

                p.copy(x = newX, y = newY, vx = newVx, vy = newVy, color = updatedClr)
            }
        }
    }

    // --- REAL-TIME PERFORMANCE DATA STREAM ---
    var cpuUsageSim by remember { mutableStateOf(12) }
    var memoryMegaSim by remember { mutableStateOf(42.5f) }
    var benchmarkLatencySim by remember { mutableStateOf(145) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2800)
            cpuUsageSim = (8..34).random()
            memoryMegaSim = ((410..525).random() / 10.0).toFloat()
            benchmarkLatencySim = (115..210).random()
        }
    }

    // --- LOGS FILTER CORE ---
    var activeLogFilter by remember { mutableStateOf("ALL") } // "ALL", "SUCCESS", "ERROR", "INPUT"
    val filteredLogs = remember(logs, activeLogFilter) {
        if (activeLogFilter == "ALL") logs
        else logs.filter { it.type == activeLogFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💻 Terminal Console",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = tokens.primary
            )

            if (isRunning) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = tokens.primary)
            } else {
                IconButton(onClick = { viewModel.executeActiveCode() }, modifier = Modifier.testTag("run_workspace_console_btn")) {
                    Icon(Icons.Default.Refresh, contentDescription = "Rerun code icon", tint = tokens.success)
                }
            }
        }

        // --- POWER INTEGRITY MONITOR CORE METRICS ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = tokens.surface.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, tokens.primary.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // CPU Graph Line representation
                Column {
                    Text(text = viewModel.translate("SANDBOX CORE LOAD", "सैंडबॉक्स कोर लोड"), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = tokens.textMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "$cpuUsageSim%", fontSize = 11.5.sp, fontWeight = FontWeight.Black, color = tokens.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.size(45.dp, 4.dp).clip(CircleShape).background(Color(0xFF1E293B))) {
                            Box(modifier = Modifier.fillMaxWidth(cpuUsageSim / 100f).fillMaxHeight().background(tokens.primary))
                        }
                    }
                }

                // Memory Allocation
                Column {
                    Text(text = viewModel.translate("BUFFER ALLOC", "मेमोरी बफर आवंटन"), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = tokens.textMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "${memoryMegaSim}MB", fontSize = 11.5.sp, fontWeight = FontWeight.Black, color = tokens.secondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.size(45.dp, 4.dp).clip(CircleShape).background(Color(0xFF1E293B))) {
                            Box(modifier = Modifier.fillMaxWidth(memoryMegaSim / 100f).fillMaxHeight().background(tokens.secondary))
                        }
                    }
                }

                // Response speed Benchmark telemetry
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = viewModel.translate("LATENCY THREAD", "थ्रेड विलंबता बेंचमार्क"), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = tokens.textMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "${benchmarkLatencySim}ms", fontSize = 11.5.sp, fontWeight = FontWeight.Black, color = tokens.success)
                }
            }
        }

        // Live Rendered visual outputs map sandbox (Web Browser Mockup)
        if (previewOutput.startsWith("SUCCESS_")) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                colors = CardDefaults.cardColors(containerColor = tokens.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.2.dp, tokens.success.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (previewOutput == "SUCCESS_BROWSER") Icons.Default.Launch else Icons.Default.Brush,
                                contentDescription = null,
                                tint = tokens.success,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (previewOutput == "SUCCESS_BROWSER") viewModel.translate("🌍 ACTIVE SANDBOX WEB SERVER LIVE", "🌍 सक्रिय सैंडबॉक्स लाइव यूआई सर्वर") else viewModel.translate("⚙️ PYTHON COMPILER OUT FRAME", "⚙️ पायथन कंपाइलर विजुअल फ्रेम"),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = tokens.success
                            )
                        }
                        
                        // Animated pulsing active indicator
                        val pulseTransition = rememberInfiniteTransition(label = "pulse_trans")
                        val pulseAlpha by pulseTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulseAlpha"
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(tokens.success.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .graphicsLayer { alpha = pulseAlpha }
                                        .clip(CircleShape)
                                        .background(tokens.success)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("LIVE", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = tokens.success)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (previewOutput == "SUCCESS_BROWSER") {
                        // Interactive controls layout
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val colorThemes = listOf("Neon Matrix Theme", "Solar Flare Theme", "Golden Hour Theme")
                            colorThemes.forEach { theme ->
                                val isSelected = particleThemeColor == theme
                                Card(
                                    modifier = Modifier
                                        .clickable { particleThemeColor = theme }
                                        .padding(vertical = 2.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) tokens.success.copy(alpha = 0.15f) else tokens.background
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (isSelected) tokens.success else tokens.textMuted.copy(alpha = 0.1f))
                                ) {
                                    Text(
                                        text = theme,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) tokens.success else tokens.textMuted,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Sliders Core
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = viewModel.translate("GRAVITY CONST: ${String.format("%.2f", physicsGravity)}", "गुरुत्वाकर्षण: ${String.format("%.2f", physicsGravity)}"),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = tokens.text
                                )
                                Slider(
                                    value = physicsGravity,
                                    onValueChange = { physicsGravity = it },
                                    valueRange = 0f..0.8f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = tokens.success,
                                        activeTrackColor = tokens.success.copy(alpha = 0.5f)
                                    )
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = viewModel.translate("SPEED FACTOR: ${String.format("%.1f", physicsSpeed)}x", "सिम्युलेटर गति: ${String.format("%.1f", physicsSpeed)}x"),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = tokens.text
                                )
                                Slider(
                                    value = physicsSpeed,
                                    onValueChange = { physicsSpeed = it },
                                    valueRange = 0.2f..3.0f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = tokens.success,
                                        activeTrackColor = tokens.success.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Dynamic Interactive Node canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF03050C))
                                .border(1.dp, tokens.success.copy(alpha = 0.2f))
                                .clickable {
                                    // Inject sudden scatter thrust action on tap!
                                    particleList = particleList.map { p ->
                                        p.copy(
                                            vx = ((Math.random() - 0.5) * 14).toFloat(),
                                            vy = ((Math.random() - 0.5) * 14 - 4).toFloat()
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // High performance GPU Draw scope matrix grid
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw elegant scanning laser wireframes
                                val lineSteps = 10
                                val gw = size.width / lineSteps
                                val gh = size.height / lineSteps
                                for (i in 0..lineSteps) {
                                    drawLine(
                                        color = tokens.success.copy(alpha = 0.04f),
                                        start = Offset(0f, i * gh),
                                        end = Offset(size.width, i * gh),
                                        strokeWidth = 1f
                                    )
                                    drawLine(
                                        color = tokens.success.copy(alpha = 0.04f),
                                        start = Offset(i * gw, 0f),
                                        end = Offset(i * gw, size.height),
                                        strokeWidth = 1f
                                    )
                                }

                                // Render the interactive particles
                                particleList.forEach { p ->
                                    val mappedX = if (p.x.isFinite()) (p.x / 320f) * size.width else 160f
                                    val mappedY = if (p.y.isFinite()) (p.y / 150f) * size.height else 75f
                                    val mappedR = if (p.radius.isFinite()) (p.radius * density).coerceAtLeast(0.1f) else 5f

                                    // Outer neon glow
                                    drawCircle(
                                        color = p.color.copy(alpha = 0.35f),
                                        radius = mappedR * 2.5f,
                                        center = Offset(mappedX, mappedY)
                                    )
                                    // Inner bright core
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.9f),
                                        radius = mappedR * 0.9f,
                                        center = Offset(mappedX, mappedY)
                                    )
                                }
                            }

                            // Tap to scatter prompt
                            Text(
                                text = viewModel.translate("👆 Tap Canvas to Inject Spark Shockwave Kinetic Force", "👆 गतिमान वेक्टर दबाव के लिए टैप करें"),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = tokens.textMuted.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 6.dp)
                            )
                        }
                    } else if (previewOutput == "SUCCESS_PYTHON") {
                        // Rotation transition state
                        val infPython = rememberInfiniteTransition(label = "py_spiral")
                        val baseRotationSpeed by infPython.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(9000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "base_rot"
                        )

                        var recursiveDepth by remember { mutableStateOf(5.0f) }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = viewModel.translate("Fractal Branch Complexity: ${recursiveDepth.toInt()}", "शाखा जटिलता गुणांक: ${recursiveDepth.toInt()}"),
                                    fontSize = 10.sp,
                                    color = tokens.text
                                )
                                Slider(
                                    value = recursiveDepth,
                                    onValueChange = { recursiveDepth = it },
                                    valueRange = 3.0f..7.0f,
                                    modifier = Modifier.width(160.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = tokens.success,
                                        activeTrackColor = tokens.success.copy(alpha = 0.5f)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF02040A))
                                    .border(1.dp, tokens.success.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    // Recursive tree renderer in Jetpack Compose DrawScope
                                    fun drawPyBranch(startX: Float, startY: Float, branchLen: Float, curAngle: Float, currentDepth: Int) {
                                        if (!startX.isFinite() || !startY.isFinite() || !branchLen.isFinite() || !curAngle.isFinite()) return
                                        if (currentDepth <= 0 || branchLen < 5f) return
                                        
                                        val angleRad = Math.toRadians(curAngle.toDouble())
                                        val endX = startX + (Math.cos(angleRad) * branchLen).toFloat()
                                        val endY = startY - (Math.sin(angleRad) * branchLen).toFloat()
                                        if (!endX.isFinite() || !endY.isFinite()) return

                                        drawLine(
                                            color = Color(0xFF00E5FF).copy(alpha = if (recursiveDepth > 0f) { val ratio = currentDepth / recursiveDepth; if (ratio.isFinite()) ratio.coerceIn(0f, 1f) else 1f } else 1f),
                                            start = Offset(startX, startY),
                                            end = Offset(endX, endY),
                                            strokeWidth = (currentDepth * 1.5f).coerceIn(1f, 8f)
                                        )

                                        // Left recurse branching
                                        drawPyBranch(
                                            startX = endX,
                                            startY = endY,
                                            branchLen = branchLen * 0.72f,
                                            curAngle = curAngle + 24f + (Math.sin(Math.toRadians(baseRotationSpeed.toDouble())) * 10f).toFloat(),
                                            currentDepth = currentDepth - 1
                                        )
                                        // Right recurse branching
                                        drawPyBranch(
                                            startX = endX,
                                            startY = endY,
                                            branchLen = branchLen * 0.72f,
                                            curAngle = curAngle - 24f - (Math.cos(Math.toRadians(baseRotationSpeed.toDouble())) * 10f).toFloat(),
                                            currentDepth = currentDepth - 1
                                        )
                                    }

                                    // Initiate recursive Golden spiral fractal tree base
                                    drawPyBranch(
                                        startX = size.width / 2f,
                                        startY = size.height - 10f,
                                        branchLen = size.height * 0.27f,
                                        curAngle = 90f,
                                        currentDepth = recursiveDepth.toInt()
                                    )
                                }
                            }
                        }
                    } else {
                        // Generic compiler standard visualizer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF070B14))
                                .border(1.dp, tokens.success.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✓ LOCAL EXECUTOR CONTAINER CLOSED SUCCESSFULLY WITH CODE 0",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = tokens.success,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // --- FILTER CHIPS ROW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val filterOptions = listOf(
                "ALL" to viewModel.translate("All Logs", "सभी लॉग्स"),
                "SUCCESS" to viewModel.translate("Success ✅", "सफलता"),
                "ERROR" to viewModel.translate("Errors ❌", "त्रुटियां"),
                "INPUT" to viewModel.translate("Inputs ⌨️", "इनपुट")
            )

            filterOptions.forEach { (filterType, label) ->
                val isSelected = activeLogFilter == filterType
                Card(
                    modifier = Modifier
                        .clickable { activeLogFilter = filterType }
                        .padding(vertical = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) tokens.primary.copy(alpha = 0.2f) else tokens.surface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSelected) tokens.primary else Color.Transparent)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) tokens.primary else tokens.text,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Terminal text streams container with retro coding header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F141C))
                .border(1.dp, Color(0xFF283244), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                reverseLayout = false
            ) {
                if (filteredLogs.isEmpty()) {
                    item {
                        Text(
                            text = viewModel.translate(
                                "Console Idle. Tap the 'Run' FAB above to compile the code.",
                                "कंसोल खाली है। कंपाइल करने के लिए 'Run' (चलाएं) पर क्लिक करें।"
                            ),
                            fontSize = 12.sp,
                            color = tokens.textMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    items(filteredLogs) { log ->
                        val logClr = when (log.type) {
                            "SUCCESS" -> tokens.success
                            "ERROR" -> tokens.error
                            "INPUT" -> tokens.secondary
                            else -> tokens.text
                        }
                        Text(
                            text = log.message,
                            color = logClr,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Live shell commands text input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$ ", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = tokens.primary)
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedTextField(
                value = inputCommand,
                onValueChange = { inputCommand = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("console_command_input"),
                placeholder = { Text(viewModel.translate("Type 'clear' or command...", "निर्देश दर्ज करें..."), fontSize = 11.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputCommand.isNotBlank()) {
                        viewModel.submitConsoleInputCommand(inputCommand)
                        inputCommand = ""
                        focusManager.clearFocus()
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = tokens.primary,
                    unfocusedBorderColor = tokens.surface
                ),
                textStyle = TextStyle(fontSize = 13.sp, fontFamily = FontFamily.Monospace, color = tokens.text)
            )
        }
    }
}

// --- TAB 4: VERSION CONTROL GIT PANEL ---
@Composable
fun GitCommitTab(viewModel: ReplViewModel, tokens: VisualThemeTokens) {
    val isHindi by viewModel.isHindi.collectAsState()
    val commits by viewModel.commits.collectAsState()

    var commitMsg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "🌲 Git Branch: [main]",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = tokens.primary
        )

        Text(
            text = viewModel.translate("Incremental snapshots local commits", "स्थानीय वृद्धिशील कोड स्नैपशॉट सहेजें"),
            fontSize = 12.sp,
            color = tokens.textMuted
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Create new commit Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = tokens.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = viewModel.translate("Assemble Snapshot Commit", "नया कमिट व्यवस्थित करें"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = tokens.text
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = commitMsg,
                    onValueChange = { commitMsg = it },
                    label = { Text(viewModel.translate("Commit Message (e.g. fix UI bounds)", "कमिट संदेश दर्ज करें")) },
                    modifier = Modifier.fillMaxWidth().testTag("git_commit_msg_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = tokens.primary)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        viewModel.pushGitCommit(commitMsg)
                        commitMsg = ""
                    },
                    modifier = Modifier.fillMaxWidth().testTag("git_commit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.primary)
                ) {
                    Text(viewModel.translate("Push & Commit Changes", "पुश और बदलाव कमिट करें"), color = tokens.background, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = viewModel.translate("Commit Chronological Timeline", "कमिट हिस्ट्री टाइमलाइन"),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = tokens.text
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Render previous commits list
        if (commits.isEmpty()) {
            Text(text = "No snapshots recorded yet.", color = tokens.textMuted, fontSize = 12.sp)
        } else {
            commits.forEachIndexed { index, commit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Vertical visual timeline dots
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(tokens.primary)
                        )
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(40.dp)
                                .background(tokens.textMuted.copy(alpha = 0.3f))
                        )
                    }

                    Column {
                        Text(commit.commitMessage, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = tokens.text)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Commit Hash: devrepl_c${commit.id}abc • Author: ${commit.author} • ${commit.timestamp / 1000}", fontSize = 10.sp, color = tokens.textMuted)
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 5: DEPLOYMENT HUB (Real time building, microservice outputs) ---
@Composable
fun DeploymentTab(viewModel: ReplViewModel, tokens: VisualThemeTokens) {
    val isHindi by viewModel.isHindi.collectAsState()
    val isDeploying by viewModel.isDeploying.collectAsState()
    val deployments by viewModel.deployments.collectAsState()
    val selectedProject by viewModel.selectedProject.collectAsState()
    val activeFile by viewModel.activeFile.collectAsState()
    val codeByRef by viewModel.codeBuffer.collectAsState()

    val activeDeploy = deployments.firstOrNull()

    val coroutineScope = rememberCoroutineScope()

    // Interactive Publisher Forms State
    var subdomainInput by remember { mutableStateOf("") }
    var versionInput by remember { mutableStateOf("1.0.0") }
    var selectedEnv by remember { mutableStateOf("edge") } // "edge" or "router"
    var showSharePortal by remember { mutableStateOf(false) }

    // Live Sandbox Local Simulator State
    var sandboxLogString by remember { mutableStateOf("") }
    var isSandboxRunning by remember { mutableStateOf(false) }
    var sandboxAppOutput by remember { mutableStateOf("") }
    var lastInteractedAction by remember { mutableStateOf("") }

    // Sync default subdomain with project name upon discovery
    LaunchedEffect(selectedProject) {
        if (selectedProject != null && subdomainInput.isEmpty()) {
            subdomainInput = selectedProject!!.name.lowercase().replace(" ", "-").replace(Regex("[^a-z0-9-]"), "")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // App title header section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = viewModel.translate("🚀 OMNI Cloud Portal & Publisher", "🚀 ओम्नी क्लाउड पोर्टल और पब्लिशर"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = tokens.primary
                )
                Text(
                    text = viewModel.translate(
                        "Manage elite serverless hosting & live sandbox simulation nodes",
                        "एलीट सर्वरलेस होस्टिंग और लाइव सैंडबॉक्स सिमुलेशन नोड्स प्रबंधित करें"
                    ),
                    fontSize = 11.sp,
                    color = tokens.textMuted
                )
            }
            
            // Neon Status indicator of main OMNI network
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF091414)),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E5FF))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ONLINE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF00E5FF)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedProject == null) {
            // Friendly prompt if no active project is found
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = tokens.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, tokens.textMuted.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "No project",
                        tint = tokens.textMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = viewModel.translate("Select a Project First", "पहले एक प्रोजेक्ट चुनें"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = tokens.text
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = viewModel.translate(
                            "Deployments require an active file structure workspace. Choose or create a project in the 'Files' workspace tab.",
                            "परिनियोजन के लिए एक सक्रिय फ़ाइल संरचना कार्यक्षेत्र की आवश्यकता होती है। 'फ़ाइलें' टैब में एक प्रोजेक्ट चुनें।"
                        ),
                        fontSize = 12.sp,
                        color = tokens.textMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Main content for active project
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = tokens.surface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, tokens.textMuted.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = viewModel.translate("1. Configure Publication Credentials ✨", "1. प्रकाशन साख (Credentials) सेट करें ✨"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = tokens.primary
                    )
                    Text(
                        text = viewModel.translate(
                            "Customize your public OMNI presence and edge router addresses",
                            "अपने सार्वजनिक ओम्नी प्रेजेंस और एज राउटर एड्रेस को कस्टमाइज़ करें"
                        ),
                        fontSize = 10.sp,
                        color = tokens.textMuted
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))

                    // Subdomain selection row
                    Text(
                        text = viewModel.translate("Production URL Alias prefix:", "उत्पादन यूआरएल उपनाम (Alias):"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = tokens.text
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = subdomainInput,
                            onValueChange = { subdomainInput = it.lowercase().replace(" ", "-") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, tint = tokens.primary, modifier = Modifier.size(16.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = tokens.text,
                                unfocusedTextColor = tokens.text,
                                focusedBorderColor = tokens.primary,
                                unfocusedBorderColor = tokens.textMuted.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = ".omni-ai.live",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = tokens.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Version & Environment selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = viewModel.translate("Release Tag:", "रिलीज़ टैग:"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = tokens.text
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = versionInput,
                                onValueChange = { versionInput = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = tokens.text,
                                    unfocusedTextColor = tokens.text,
                                    focusedBorderColor = tokens.primary,
                                    unfocusedBorderColor = tokens.textMuted.copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(
                                text = viewModel.translate("Routing Environment:", "रूटिंग वातावरण:"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = tokens.text
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(tokens.background)
                                    .border(1.dp, tokens.textMuted.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selectedEnv == "edge") tokens.primary.copy(alpha = 0.15f) else Color.Transparent)
                                        .clickable { selectedEnv = "edge" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Edge CDN",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedEnv == "edge") tokens.primary else tokens.textMuted
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selectedEnv == "router") tokens.secondary.copy(alpha = 0.15f) else Color.Transparent)
                                        .clickable { selectedEnv = "router" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "OMNI Core",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedEnv == "router") tokens.secondary else tokens.textMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Deploy and Publish Live Trigger Button
                    Button(
                        onClick = {
                            viewModel.deployProject()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("deploy_trigger_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDeploying) tokens.textMuted else tokens.primary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isDeploying
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isDeploying) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = tokens.background
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = viewModel.translate("Publishing Core Files...", "प्रकाशन चल रहा है..."),
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.background
                                )
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = tokens.background, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = viewModel.translate("DEPLOY & PUBLISH LIVE 🚀", "लाइव तैनात और प्रकाशित करें 🚀"),
                                    fontWeight = FontWeight.Bold,
                                    color = tokens.background
                                )
                            }
                        }
                    }

                    // Release URL display block if deployed is live
                    if (activeDeploy != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(tokens.success.copy(alpha = 0.08f))
                                .border(1.dp, tokens.success.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = viewModel.translate("🌐 Live Published Link Active", "🌐 लाइव प्रकाशित लिंक सक्रिय है"),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = tokens.success
                                    )
                                    Text(
                                        text = "SSL CERTIFIED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = tokens.success
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "https://$subdomainInput.omni-ai.live",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    color = tokens.secondary,
                                    modifier = Modifier
                                        .clickable { showSharePortal = true }
                                        .padding(vertical = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Version: $versionInput",
                                        fontSize = 10.sp,
                                        color = tokens.textMuted
                                    )
                                    Text(
                                        text = "Env: " + selectedEnv.uppercase(),
                                        fontSize = 10.sp,
                                        color = tokens.textMuted
                                    )
                                    Text(
                                        text = "Active: SSL TLS 1.3",
                                        fontSize = 10.sp,
                                        color = tokens.success
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 2. THE LIVE RUN SMARTPHONE SIMULATOR WORKSPACE ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = tokens.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = viewModel.translate("2. Live Sandbox Smartphone Emulator 📱", "2. लाइव सैंडबॉक्स स्मार्टफोन एमुलेटर 📱"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF00E5FF)
                            )
                            Text(
                                text = viewModel.translate(
                                    "Simulate active files code compiled in real time",
                                    "वास्तविक समय में लिंक किए गए सक्रिय फ़ाइल कोड का अनुकरण करें"
                                ),
                                fontSize = 10.sp,
                                color = tokens.textMuted
                            )
                        }
                        
                        // Icon Indicator
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // THE PHYSICAL PHONE MOCKUP FRAME DESIGNED INSIDE COMPOSE
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color(0xFF000000)) // Black phone physical body
                            .border(3.dp, Color(0xFF1E293B), RoundedCornerShape(32.dp)) // Chrome bezel
                            .border(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.6f), RoundedCornerShape(32.dp)) // Cyan cyber trim
                            .padding(8.dp)
                    ) {
                        // Top Camera Notch and status bar indicator row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                                .padding(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left screen time
                            Text(
                                text = "06:00",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            // Central punch hole notch
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Color(0xFF0F172A))
                            )

                            // Right icons: Network signal bar, Battery indicator
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Wifi, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "100%",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Sandbox Simulator Live Canvas Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xFF090E1A)) // Dark space screen depth
                                .border(1.dp, Color(0xFF334155).copy(alpha = 0.4f), RoundedCornerShape(22.dp))
                        ) {
                            Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                                // Sub headers
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = (selectedProject?.templateIcon ?: "⚙️") + " OMNI-SANDBOX V9.9",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = Color(0xFFE2E8F0)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSandboxRunning) Color(0xFFEF4444) else Color(0xFF10B981))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isSandboxRunning) "EXECUTING" else "IDLE GATE",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Simulator Viewport main stream code/outcome
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF030712))
                                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                                        .padding(8.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Current File: " + (activeFile?.path ?: "N/A"),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color(0xFF00E5FF)
                                            )
                                            Text(
                                                text = "Lang: " + (selectedProject?.languageType ?: "N/A"),
                                                fontSize = 9.sp,
                                                color = Color(0xFFE040FB)
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        // Visual simulation of running output
                                        if (isSandboxRunning) {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                CircularProgressIndicator(modifier = Modifier.size(14.dp).align(Alignment.CenterHorizontally), color = Color(0xFF00E5FF), strokeWidth = 1.6.dp)
                                                Text(
                                                    text = sandboxLogString,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 10.sp,
                                                    color = Color.LightGray
                                                )
                                            }
                                        } else if (sandboxAppOutput.isNotEmpty()) {
                                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                                Text(
                                                    text = sandboxAppOutput,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF4ADE80)
                                                )
                                                if (lastInteractedAction.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Action log: $lastInteractedAction",
                                                        fontSize = 9.sp,
                                                        color = Color(0xFFFAB914),
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            }
                                        } else {
                                            Column(
                                                modifier = Modifier.fillMaxSize(),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(Icons.Default.Smartphone, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(30.dp))
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "Simulator Screen Off",
                                                    fontSize = 11.sp,
                                                    color = Color.DarkGray
                                                )
                                                Text(
                                                    text = "Tap 'LIVE RUN' below to fire up engines",
                                                    fontSize = 9.sp,
                                                    color = Color.DarkGray
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Real-time virtual hardware interactive phone buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            lastInteractedAction = "Clicked Home Screen"
                                            sandboxAppOutput = ">>> Navigated back to virtual home of applet.\n>>> Memory buffered state is safe.\n>>> Loaded dynamic resources."
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        modifier = Modifier.weight(1.2f).height(32.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Virtual Home", fontSize = 9.sp, color = Color.White)
                                    }

                                    Button(
                                        onClick = {
                                            lastInteractedAction = "Dispatched Intent Event ID #${System.currentTimeMillis() % 1000}"
                                            sandboxAppOutput = sandboxAppOutput + "\n>>> Triggered custom Action intent!"
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Send Intent", fontSize = 9.sp, color = Color.White)
                                    }
                                }
                            }
                        }

                        // Bottom Navigation home bar Pill on physical phone frame
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.DarkGray)
                                .align(Alignment.CenterHorizontally)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Buttons to operationalize the Smartphone Emulator ("Live Run")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                isSandboxRunning = true
                                sandboxLogString = "Constructing simulated environment..."
                                coroutineScope.launch {
                                    delay(500)
                                    sandboxLogString = "Synthesizing AST for file code..."
                                    delay(500)
                                    sandboxLogString = "Executing sandbox virtual memory compiler..."
                                    delay(400)
                                    isSandboxRunning = false
                                    val currentSnippet = if (codeByRef.length > 200) codeByRef.take(200) + "\n..." else codeByRef
                                    sandboxAppOutput = """
                                        [🚀 LIVE RUN SUCCESSFUL]
                                        Compiled Language: ${selectedProject?.languageType}
                                        Output Dump:
                                        ------------------------------
                                        $currentSnippet
                                        ------------------------------
                                        Process completed successfully.
                                    """.trimIndent()
                                }
                            },
                            modifier = Modifier.weight(1.3f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("LIVE RUN SMARTPHONE 📱", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Black)
                            }
                        }

                        Button(
                            onClick = {
                                sandboxAppOutput = ""
                                sandboxLogString = ""
                                lastInteractedAction = ""
                            },
                            modifier = Modifier.weight(0.7f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = tokens.background),
                            border = BorderStroke(1.dp, tokens.textMuted.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(viewModel.translate("Clear Screen", "स्क्रीन साफ करें"), fontSize = 10.sp, color = tokens.text)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- 3. LIVE COMPILER DEPLOYMENT LOGS IN REAL TIME ---
            Text(
                text = viewModel.translate("Live Edge Compiling & Release Logs", "लाइव एज कंपाइलिंग और रिलीज़ लॉग्स"),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = tokens.text
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF070B14))
                    .border(1.dp, tokens.textMuted.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                val logsScrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(logsScrollState)
                ) {
                    if (activeDeploy == null) {
                        Text(
                            text = "System Release Kernel Idle.\nPress 'DEPLOY & PUBLISH LIVE 🚀' above to dispatch static node instances to edge router gates.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = tokens.textMuted
                        )
                    } else {
                        Text(
                            text = activeDeploy.logs,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = tokens.text
                        )
                    }
                }
            }
        }

        // SHARE AND PUBLICATION REGISTRY DIALOG IF REQUESTED
        if (showSharePortal) {
            AlertDialog(
                onDismissRequest = { showSharePortal = false },
                containerColor = Color(0xFF0F121C),
                title = {
                    Text("OMNI-AI Developer Registry", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Your app is published globally at the stable URL below. You can share this node link with collaborators contextually.",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )

                        OutlinedTextField(
                            value = "https://$subdomainInput.omni-ai.live",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF1E293B)
                            )
                        )

                        Text(
                            text = "✔️ HTTPS Encryption (SSL) Registered\n✔️ Content Delivery Network (CDN) Status: ACTIVE\n✔️ Geo-replicated globally across edge zones",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF10B981)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSharePortal = false }) {
                        Text("Copy Link & Close", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

// --- TAB 6: COLLABORATION SWARMS AND TEAM CONTROL ---
@Composable
fun CollaborationTab(viewModel: ReplViewModel, tokens: VisualThemeTokens) {
    val isHindi by viewModel.isHindi.collectAsState()
    val isCollabOn by viewModel.collaborationActive.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "👥 Team Collaboration",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = tokens.primary
        )

        Text(
            text = viewModel.translate("Real-time collaborative editing for team projects syncing instantly", "टीम प्रोजेक्ट के लिए तत्काल रीयल-टाइम बहु-अकाउंट कोडिंग सिंक"),
            fontSize = 12.sp,
            color = tokens.textMuted
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = tokens.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewModel.translate("Sync Collaboration Swarms", "टीम सहयोग नेटवर्क चालू करें"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = tokens.text
                    )

                    Switch(
                        checked = isCollabOn,
                        onCheckedChange = { viewModel.setCollaboration(it) },
                        modifier = Modifier.testTag("collab_toggle_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isCollabOn) {
                        viewModel.translate("Swarms Active! Multi-user team editing enabled. Coloured floating bubbles will draw online active inputs.", "सक्रिय सहयोग! कई सदस्य एक साथ लिख रहे हैं। संपादन बॉक्स में गतिशील रंगीन कर्सर दिखेंगे।")
                    } else {
                        viewModel.translate("Swarms Idle. Toggle the switch above to link with remote development partners.", "सहयोग बंद है। दूरस्थ डेवलपर सहयोगियों को जोड़ने के लिए ऊपर स्विच ऑन करें।")
                    },
                    fontSize = 12.sp,
                    color = tokens.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isCollabOn) {
            Text(
                text = viewModel.translate("Online Workspace Members", "ऑनलाइन कोडर सहयोगी"),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = tokens.text
            )
            Spacer(modifier = Modifier.height(10.dp))

            val mates = listOf(
                Pair("Priya Sen (UI Expert)", "#FF61D2"),
                Pair("Rajesh Verma (Backend Eng)", "#FFE83F"),
                Pair("Carlos Gomez (Db Architect)", "#3FFFF8")
            )

            mates.forEach { (name, color) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(tokens.surface)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val parsedBgColor = try { Color(android.graphics.Color.parseColor(color)) } catch (e: Exception) { Color.Gray }
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(parsedBgColor)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = tokens.text)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(viewModel.translate("Sync Active", "सक्रिय रूप से सिंक"), fontSize = 10.sp, color = tokens.success)
                }
            }
        }
    }
}

// --- TAB 7: STYLES AND OPTIONAL THEMES CUSTOMIZATION ---
@Composable
fun StyleCustomizerTab(viewModel: ReplViewModel, tokens: VisualThemeTokens) {
    val isHindi by viewModel.isHindi.collectAsState()
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "🎨 Themes & Customization",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = tokens.primary
        )

        Text(
            text = viewModel.translate("Customize your mobile IDE style and parameters", "अपने मोबाइल कंपाइलर शैलियों और मापदंडों को अनुकूलित करें"),
            fontSize = 12.sp,
            color = tokens.textMuted
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Visual theme lists
        Text(
            text = viewModel.translate("Select Syntax Theme Preset", "सिंटैक्स हाइलाइट थीम Preset"),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = tokens.text
        )

        val themePresets = listOf("Frosted Glass", "Dracula Style", "VS Code Dark", "One Dark Pro", "Github Light", "Synthwave '84")
        themePresets.forEach { tm ->
            val isSel = selectedTheme == tm
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { viewModel.setTheme(tm) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSel) tokens.primary.copy(alpha = 0.15f) else tokens.surface
                ),
                border = BorderStroke(1.dp, if (isSel) tokens.primary else Color.Transparent)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = tm,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isSel) tokens.primary else tokens.text
                    )
                    if (isSel) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Active preset icon", tint = tokens.primary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Offline and safe backup preferences
        Text(
            text = viewModel.translate("Workflow and Backup Defaults", "ऑफ़लाइन कार्यप्रणाली और क्लाउड बैकअप सेटिंग्स"),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = tokens.text
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = tokens.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(viewModel.translate("Offline Development", "ऑफ़लाइन कार्यक्षेत्र"), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = tokens.text)
                        Text(viewModel.translate("Prevents auto backups, save offgrid", "ऑटो-मेमोरी बैकअप रोकें, पूरी तरह स्थानीय फ़ाइलें पढ़ें"), fontSize = 11.sp, color = tokens.textMuted)
                    }
                    Switch(
                        checked = isOfflineMode,
                        onCheckedChange = { viewModel.toggleOfflineMode() }
                    )
                }
            }
        }
    }
}

// --- TAB 8: GHOSTWRITER AI ASSISTANT CHAT ---
@Composable
fun GhostwriterChatTab(viewModel: ReplViewModel, tokens: VisualThemeTokens) {
    val isHindi by viewModel.isHindi.collectAsState()
    val chatMessages by viewModel.aiMessages.collectAsState()
    val isThinking by viewModel.isAiThinking.collectAsState()
    val sharedPrompt by viewModel.sharedPrompt.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(sharedPrompt) {
        if (sharedPrompt.isNotEmpty()) {
            textInput = sharedPrompt
            viewModel.setSharedPrompt("") // Consume shared state
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CyborgChatbotBackground(modifier = Modifier.fillMaxSize())
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = tokens.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "🤖 Ghostwriter AI Helper",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = tokens.text
                )
                Text(
                    text = viewModel.translate("Bilingual companion with Hindi support", "हिंदी सपोर्ट के साथ द्विभाषी कोडिंग सहायक"),
                    fontSize = 11.sp,
                    color = tokens.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Collapsible Model & Credentials Config Card
        var showSettings by remember { mutableStateOf(false) }
        
        val aiProvider by viewModel.aiProvider.collectAsState()
        val sessionGeminiKey by viewModel.sessionGeminiKey.collectAsState()
        val sessionOpenaiKey by viewModel.sessionOpenaiKey.collectAsState()
        val sessionDeepseekKey by viewModel.sessionDeepseekKey.collectAsState()
        val systemInstructionPreset by viewModel.systemInstructionPreset.collectAsState()
        val modelTemperature by viewModel.modelTemperature.collectAsState()

        Card(
            modifier = Modifier.fillMaxWidth().animateContentSize(),
            colors = CardDefaults.cardColors(containerColor = tokens.surface.copy(alpha = 0.65f)),
            border = BorderStroke(1.dp, tokens.lineHighlight.copy(alpha = 0.25f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showSettings = !showSettings },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = tokens.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHindi) "AI मॉडल और कुंजियाँ (${aiProvider})" else "AI Model & Key Settings (${aiProvider})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = tokens.text
                        )
                    }
                    Icon(
                        imageVector = if (showSettings) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = tokens.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                if (showSettings) {
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Chips Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Gemini AI", "ChatGPT (OpenAI)", "DeepSeek").forEach { prov ->
                            val isSel = aiProvider == prov
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) tokens.primary.copy(alpha = 0.15f) else tokens.background)
                                    .border(1.dp, if (isSel) tokens.primary else tokens.textMuted.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setAiProvider(prov) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (prov == "Gemini AI") "Gemini" else if (prov == "ChatGPT (OpenAI)") "ChatGPT" else "DeepSeek",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) tokens.primary else tokens.text
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Password Field for current selected model key
                    val currentKeyVal = when (aiProvider) {
                        "Gemini AI" -> sessionGeminiKey
                        "ChatGPT (OpenAI)" -> sessionOpenaiKey
                        else -> sessionDeepseekKey
                    }
                    
                    val keyLabel = when (aiProvider) {
                        "Gemini AI" -> if (isHindi) "जेमिनी सत्र कुंजी रिप्लेसमेंट (वैकल्पिक)" else "Session Gemini API Key (Optional Override)"
                        "ChatGPT (OpenAI)" -> if (isHindi) "ओपनएआई एपीआई कुंजी दर्ज करें" else "Enter OpenAI API Key (Required for ChatGPT)"
                        else -> if (isHindi) "डीपसीक एपीआई कुंजी दर्ज करें" else "Enter DeepSeek API Key (Required for reasoning)"
                    }
                    
                    var keyVisible by remember { mutableStateOf(false) }
                    
                    OutlinedTextField(
                        value = currentKeyVal,
                        onValueChange = { nv ->
                            when (aiProvider) {
                                "Gemini AI" -> viewModel.setSessionGeminiKey(nv)
                                "ChatGPT (OpenAI)" -> viewModel.setSessionOpenaiKey(nv)
                                else -> viewModel.setSessionDeepseekKey(nv)
                            }
                        },
                        label = { Text(keyLabel, fontSize = 10.sp) },
                        visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { keyVisible = !keyVisible }) {
                                Icon(
                                    imageVector = if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "toggle key visibility",
                                    modifier = Modifier.size(16.dp),
                                    tint = tokens.primary
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("ai_session_key_field"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = tokens.primary)
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Slider and Mode options
                    Text(
                        text = "${if (isHindi) "सिस्टम निर्देश शैली: " else "System Instruction Preset: "} $systemInstructionPreset",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = tokens.textMuted
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Code Expert", "Debugger Partner", "Creative Builder").forEach { prs ->
                            val isPrsSel = systemInstructionPreset == prs
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isPrsSel) tokens.primary.copy(alpha = 0.1f) else tokens.background)
                                    .border(1.dp, if (isPrsSel) tokens.primary.copy(alpha=0.6f) else tokens.textMuted.copy(alpha=0.15f), RoundedCornerShape(6.dp))
                                    .clickable { viewModel.setSystemInstructionPreset(prs) }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(prs, fontSize = 8.5.sp, color = if (isPrsSel) tokens.primary else tokens.textMuted)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (isHindi) "मॉडल तापमान (Temperature)" else "Model Temperature", fontSize = 10.sp, color = tokens.textMuted)
                            Text(String.format("%.1f", modelTemperature), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tokens.primary)
                        }
                        Slider(
                            value = modelTemperature,
                            onValueChange = { viewModel.setModelTemperature(it) },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = tokens.primary,
                                activeTrackColor = tokens.primary,
                                inactiveTrackColor = tokens.lineHighlight
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scrollable logs chat
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(tokens.surface.copy(alpha = 0.42f))
                .border(1.dp, tokens.lineHighlight.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            val listState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(listState)
            ) {
                chatMessages.forEach { msg ->
                    val isUsr = msg.role == "user"
                    val bubbleBg = if (isUsr) tokens.primary.copy(alpha = 0.35f) else Color(0xDD0A0F25)
                    val align = if (isUsr) Alignment.End else Alignment.Start

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = align
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isUsr) 12.dp else 2.dp,
                                        bottomEnd = if (isUsr) 2.dp else 12.dp
                                    )
                                )
                                .background(bubbleBg)
                                .border(1.dp, if (isUsr) tokens.primary.copy(alpha = 0.3f) else Color(0xFF00E5FF).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                                .widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = msg.text,
                                fontSize = 12.sp,
                                color = if (isUsr) Color.White else Color(0xFFF1F5F9),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = if (isUsr) "You" else "Ghostwriter AI",
                            fontSize = 8.sp,
                            color = tokens.textMuted,
                            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
                        )
                    }
                }

                if (isThinking) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(tokens.background)
                                .padding(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = tokens.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Thinking...", fontSize = 11.sp, color = tokens.textMuted)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chat query text input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_assistant_chat_input"),
                placeholder = { Text(viewModel.translate("Ask Ghostwriter (e.g. fix syntax)...", "प्रश्न पूछें (जैसे बफर कोड समझाएं)..."), fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (textInput.isNotBlank()) {
                        viewModel.sendAiMessage(textInput)
                        textInput = ""
                        focusManager.clearFocus()
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = tokens.primary)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.sendAiMessage(textInput)
                        textInput = ""
                        focusManager.clearFocus()
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(tokens.primary)
                    .testTag("send_ai_message_btn")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send prompt button", tint = Color.White)
            }
        }
    }
}
}

// --- TAB: GEMINI STUDIO PLAYGROUND ---
@Composable
fun GeminiSparkleStar(modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 22.dp) {
    val path = remember { androidx.compose.ui.graphics.Path() }
    androidx.compose.foundation.Canvas(modifier = modifier.size(size)) {
        if (this.size.width < 1f || this.size.height < 1f) return@Canvas
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        
        // Outer ambient glow disk
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x77A259FF), Color(0x00A259FF)),
                center = Offset(cx, cy),
                radius = (cx * 1.6f).coerceAtLeast(1f)
            )
        )
        
        // Organic curved 4-pointed sparkle star path (pulls inward towards center)
        path.reset()
        path.apply {
            moveTo(cx, 0f)
            quadraticBezierTo(cx, cy, w, cy)
            quadraticBezierTo(cx, cy, cx, h)
            quadraticBezierTo(cx, cy, 0f, cy)
            quadraticBezierTo(cx, cy, cx, 0f)
            close()
        }
        
        val gradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFF5D8FFC), // Gemini cyan-blue
                Color(0xFFA259FF), // Sparkling violet
                Color(0xFFE052FF), // Pink-blue
                Color(0xFFFF9E2C)  // Warm amber
            ),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )
        
        drawPath(path, brush = gradient)
    }
}

@Composable
fun GeminiOrbitControls(
    viewModel: ReplViewModel,
    tokens: VisualThemeTokens,
    modifier: Modifier = Modifier
) {
    val playSearchGrounding by viewModel.playSearchGrounding.collectAsState()
    val playMapsGrounding by viewModel.playMapsGrounding.collectAsState()
    val playCodeExecution by viewModel.playCodeExecution.collectAsState()
    val playStructuredOutputs by viewModel.playStructuredOutputs.collectAsState()
    val playFunctionCalling by viewModel.playFunctionCalling.collectAsState()
    val playUrlContext by viewModel.playUrlContext.collectAsState()

    var highlightedIndex by remember { mutableStateOf(0) }

    val options = listOf(
        Triple(Icons.Default.List, "Structured", playStructuredOutputs),
        Triple(Icons.Default.Code, "Code Run", playCodeExecution),
        Triple(Icons.Default.Build, "Functions", playFunctionCalling),
        Triple(Icons.Default.Language, "Search", playSearchGrounding),
        Triple(Icons.Default.Place, "Maps SDK", playMapsGrounding),
        Triple(Icons.Default.Link, "URL Input", playUrlContext)
    )

    val descriptions = listOf(
        "Structured outputs: Formulates robust, strictly matching JSON schema responses.",
        "Code execution: Runs a safe sandbox interpreter directly inside the workspace.",
        "Function calling: Declares smart programming routines for the model to trigger.",
        "Search Grounding: Integrates real-time index searches on Google Search engines.",
        "Maps Grounding: Verifies geo-spatial references with live Google Places databases.",
        "URL Context: Lets model scraper crawl details & metadata on linked web links."
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing Orbital Circular Board
        Box(
            modifier = Modifier
                .size(260.dp)
                .drawBehind {
                    // Dotted orbital path circle matching orbit radius
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)
                    val r = 84.dp.toPx()
                    drawCircle(
                        color = Color(0x221A73E8), // Beautiful translucent path in light blue
                        radius = r,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                floatArrayOf(10f, 10f), 0f
                            )
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // CENTRAL CORE: Iconic Glowing Google Gemini Star
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F3F4)) // High gray core profile in light canvas
                    .border(1.dp, Color(0x331A73E8), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GeminiSparkleStar(size = 36.dp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "CORE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A73E8),
                        letterSpacing = 1.sp
                    )
                }
            }

            // Outer Orbit Nodes placed Trigonometrically
            options.forEachIndexed { index, (icon, label, isEnabled) ->
                // Angles partitioned evenly into 6 sectors (60 degrees each), offset by -90 for top layout origin
                val angleDeg = index * 60f - 90f
                val angleRad = java.lang.Math.toRadians(angleDeg.toDouble())
                
                // Polar to Cartesian layout coordinates
                val radiusDp = 86f
                val xOffset = (radiusDp * java.lang.Math.cos(angleRad)).toFloat().dp
                val yOffset = (radiusDp * java.lang.Math.sin(angleRad)).toFloat().dp

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .offset(x = xOffset, y = yOffset)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            highlightedIndex = index
                            // Play matching switch toggles
                            when (index) {
                                0 -> viewModel.togglePlayStructuredOutputs()
                                1 -> viewModel.togglePlayCodeExecution()
                                2 -> viewModel.togglePlayFunctionCalling()
                                3 -> viewModel.togglePlaySearchGrounding()
                                4 -> viewModel.togglePlayMapsGrounding()
                                5 -> viewModel.togglePlayUrlContext()
                            }
                        }
                        .width(68.dp)
                ) {
                    // Glowing active capsule
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isEnabled) Color(0x331A73E8) else Color(0x0C000000)
                            )
                            .border(
                                width = if (isEnabled) 1.5.dp else 1.dp,
                                color = if (isEnabled) Color(0xFF1A73E8) else Color(0x1F000000),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isEnabled) Color(0xFF1A73E8) else tokens.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Tag Caption text written alongside each orbit sphere
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) Color(0xFF1A73E8) else tokens.textMuted,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .background(Color(0xCCFFFFFF), RoundedCornerShape(4.dp))
                            .border(0.5.dp, Color(0x1F000000), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Parameter Interactive Tooltip & State Cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F4)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = options[highlightedIndex].second,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1A73E8)
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (options[highlightedIndex].third) Color(0xFF1A73E8) else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (options[highlightedIndex].third) "Toggled ON" else "Toggled OFF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (options[highlightedIndex].third) Color(0xFF1A73E8) else tokens.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = descriptions[highlightedIndex],
                    fontSize = 11.sp,
                    color = tokens.text.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "💡 Tap any orbital node above to toggle.",
                    fontSize = 10.sp,
                    color = Color(0xFF1A73E8),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PlaygroundTab(viewModel: ReplViewModel, tokens: VisualThemeTokens) {
    val isHindi by viewModel.isHindi.collectAsState()
    val subTab by viewModel.playgroundSubTab.collectAsState()
    val selectedCard by viewModel.playgroundSelectedCard.collectAsState()
    val messages by viewModel.playgroundMessages.collectAsState()
    val isThinking by viewModel.isPlaygroundThinking.collectAsState()

    val playTemporaryChat by viewModel.playTemporaryChat.collectAsState()

    var showControlsPanel by remember { mutableStateOf(false) }
    var showMoreDropdown by remember { mutableStateOf(false) }
    var showAttachmentsPopup by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Base background is elegant pure white ("backgaraund wait kar do")
                drawRect(color = Color(0xFFFFFFFF))
                
                // Translucent soft pastel Gemini blue glow (upper-right)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x181A73E8), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.15f),
                        radius = (size.width * 0.7f).coerceAtLeast(1f)
                    )
                )
                
                // Translucent soft pastel violet glow (lower-left)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x18A259FF), Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.85f),
                        radius = (size.width * 0.7f).coerceAtLeast(1f)
                    )
                )
            }
    ) {
        // 1. Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8F9FA)) // Google light styled bar
                .drawBehind {
                    // Thin subtle divider line under header
                    drawLine(
                        color = Color(0x11000000),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1f
                    )
                }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 3. Custom Google Gemini-style logo sparkle
                GeminiSparkleStar(size = 22.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Playground",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF1F1F1F)
                    )
                    if (selectedCard != null) {
                        Text(
                            text = selectedCard!!,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A73E8)
                        )
                    } else {
                        Text(
                            text = viewModel.translate("Explore Models", "मॉडल्स खोजें"),
                            fontSize = 11.sp,
                            color = tokens.textMuted
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (selectedCard != null) {
                    IconButton(
                        onClick = { viewModel.setPlaygroundSelectedCard(null) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart",
                            tint = Color(0xFF5F6368),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { showControlsPanel = !showControlsPanel },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (showControlsPanel) Color(0x1A1A73E8) else Color.Transparent, 
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Tuning Parameters Controls",
                        tint = if (showControlsPanel) Color(0xFF1A73E8) else Color(0xFF5F6368),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMoreDropdown = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options menu",
                            tint = Color(0xFF5F6368),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreDropdown,
                        onDismissRequest = { showMoreDropdown = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Share Playground", color = Color(0xFF1F1F1F), fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF5F6368), modifier = Modifier.size(16.dp)) },
                            onClick = { showMoreDropdown = false }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Temporary chat", color = Color(0xFF1F1F1F), fontSize = 13.sp)
                                    Switch(
                                        checked = playTemporaryChat,
                                        onCheckedChange = { viewModel.togglePlayTemporaryChat() }
                                    )
                                }
                            },
                            onClick = { }
                        )
                        Divider(color = Color(0x1F000000))
                        DropdownMenuItem(
                            text = { Text("No changes to save", color = tokens.textMuted, fontSize = 12.sp) },
                            onClick = { }
                        )
                        DropdownMenuItem(
                            text = { Text("Make a copy", color = Color(0xFF1F1F1F), fontSize = 13.sp) },
                            onClick = { showMoreDropdown = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Log Workspace", color = tokens.error, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = tokens.error, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                viewModel.setPlaygroundSelectedCard(null)
                                showMoreDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // 2. Active Body Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (selectedCard == null) {
                    // TAB SELECTOR PILLS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF1F3F4)) // Soft capsule background of Google tabs
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (subTab == "models") Color.White else Color.Transparent)
                                .clickable { viewModel.setPlaygroundSubTab("models") }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = viewModel.translate("Models", "मॉडल्स"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (subTab == "models") Color(0xFF1A73E8) else Color(0xFF5F6368)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (subTab == "agents") Color.White else Color.Transparent)
                                .clickable { viewModel.setPlaygroundSubTab("agents") }
                               .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = viewModel.translate("Agents", "एजेंट्स"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (subTab == "agents") Color(0xFF1A73E8) else Color(0xFF5F6368)
                            )
                        }
                    }

                    // GRID LIST OF EXPERIENCES
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (subTab == "models") {
                            item {
                                Text(
                                    text = "Explore Google models",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = Color(0xFF1F1F1F),
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }

                            val modelCards = listOf(
                                Triple("Featured", "✨ Pre-loaded reasoning", tokens.primary),
                                Triple("Code and Chat", "💬 Heavyweight developer chat", Color(0xFF1A73E8)),
                                Triple("Image Generation", "🎨 Text matching painter", Color(0xFFBA68C8)),
                                Triple("Video Generation", "🎬 Cinematic video scripts", Color(0xFF2E7D32)),
                                Triple("Speech and Music", "🎵 Text to audio analyzer", Color(0xFFC2185B)),
                                Triple("Real-time", "⚡ High speed audio/text websockets", Color(0xFFE65100))
                            )

                            items(modelCards) { (name, label, color) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.setPlaygroundSelectedCard(name) },
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(color.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val ic = when (name) {
                                                "Featured" -> Icons.Default.Star
                                                "Code and Chat" -> Icons.Default.Chat
                                                "Image Generation" -> Icons.Default.Image
                                                "Video Generation" -> Icons.Default.Movie
                                                "Speech and Music" -> Icons.Default.Mic
                                                else -> Icons.Default.FlashOn
                                            }
                                            Icon(ic, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        Column {
                                            Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1F1F1F))
                                            Text(label, fontSize = 12.sp, color = Color(0xFF5F6368))
                                        }
                                    }
                                }
                            }

                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Start building →",
                                        color = Color(0xFF1A73E8),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            item {
                                Text(
                                    text = "Build with Agents",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = Color(0xFF1F1F1F),
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }

                            val agentCards = listOf(
                                Triple("Antigravity Preview", "🛸 Code-swarm engine helper", tokens.primary),
                                Triple("AI Talk Radio", "🎙️ Interactive transcription generator", Color(0xFF0288D1)),
                                Triple("Customer Support", "🤝 Client semantic help center", Color(0xFF5E35B1)),
                                Triple("Data Analyst", "📊 Analyze spreadsheets & JSON parameters", Color(0xFF33691E)),
                                Triple("Document Processing", "📄 Multi-modal OCR reader", Color(0xFFD32F2F)),
                                Triple("Repo Maintainer", "🔧 Refactor dependencies scripts", Color(0xFFF57F17))
                            )

                            items(agentCards) { (name, label, color) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.setPlaygroundSelectedCard(name) },
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(color.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val ic = when (name) {
                                                "Antigravity Preview" -> Icons.Default.FlightTakeoff
                                                "AI Talk Radio" -> Icons.Default.Radio
                                                "Customer Support" -> Icons.Default.Face
                                                "Data Analyst" -> Icons.Default.ShowChart
                                                "Document Processing" -> Icons.Default.Description
                                                else -> Icons.Default.Build
                                            }
                                            Icon(ic, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        Column {
                                            Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1F1F1F))
                                            Text(label, fontSize = 12.sp, color = Color(0xFF5F6368))
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // CONVERSATIONAL ACTIVE CHAT ENGINE
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F3F4))
                                .clickable { viewModel.setPlaygroundSelectedCard(null) }
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Go back", tint = Color(0xFF1A73E8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(viewModel.translate("Model Selection", "मॉडल चयन पृष्ठ पर वापस जाएं"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A73E8))
                        }

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(messages) { msg ->
                                val isUsr = msg.role == "user"
                                val boxBg = if (isUsr) Color(0xFFE8F0FE) else Color(0xFFF1F3F4)
                                val align = if (isUsr) Alignment.End else Alignment.Start
 
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = align
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = boxBg),
                                        border = if (isUsr) BorderStroke(1.dp, Color(0xFFD2E3FC)) else null,
                                        modifier = Modifier.widthIn(max = 290.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(text = msg.text, fontSize = 13.sp, color = Color(0xFF1F1F1F))
                                            
                                            if (selectedCard != null && !isUsr) {
                                                if (selectedCard!!.contains("Image") && msg.text.contains("Successfully")) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(130.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(
                                                                Brush.linearGradient(
                                                                    listOf(Color(0xFFFFB74D), Color(0xFFBA68C8), Color(0xFF64B5F6))
                                                                )
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                            Icon(Icons.Default.Image, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text("Simulated High-Res Output", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                                if (selectedCard!!.contains("Video") && msg.text.contains("rendering")) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(130.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(
                                                                Brush.linearGradient(
                                                                    listOf(Color(0xFF81C784), Color(0xFF4FC3F7), Color(0xFFB39DDB))
                                                                )
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text("Play Veo 60FPS Video", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
 
                            if (isThinking) {
                                item {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFF1F3F4))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = Color(0xFF1A73E8))
                                        Text("AI Studio Reasoning...", fontSize = 11.sp, color = Color(0xFF5F6368))
                                    }
                                }
                            }
                        }
 
                        // BOTTOM ROW PROMPT BOX
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                BasicTextField(
                                    value = textInput,
                                    onValueChange = { textInput = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 6.dp),
                                    textStyle = TextStyle(color = Color(0xFF1F1F1F), fontSize = 14.sp),
                                    cursorBrush = SolidColor(Color(0xFF1A73E8)),
                                    decorationBox = { innerTextField ->
                                        if (textInput.isEmpty()) {
                                            Text(
                                                text = "Start typing a prompt",
                                                color = Color(0xFF9AA0A6),
                                                fontSize = 14.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
 
                                Spacer(modifier = Modifier.height(8.dp))
 
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        IconButton(onClick = { focusManager.clearFocus() }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Keyboard, contentDescription = "Toggle Keyboard", tint = Color(0xFF5F6368), modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Apps, contentDescription = "Layout grid", tint = Color(0xFF5F6368), modifier = Modifier.size(18.dp))
                                        }
                                    }
 
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Mic, contentDescription = "Voice prompt recorder", tint = Color(0xFF5F6368), modifier = Modifier.size(18.dp))
                                        }
 
                                        Box {
                                            IconButton(
                                                onClick = { showAttachmentsPopup = !showAttachmentsPopup },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0x1A1A73E8))
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Add attachments popup", tint = Color(0xFF1A73E8), modifier = Modifier.size(16.dp))
                                            }
 
                                            DropdownMenu(
                                                expanded = showAttachmentsPopup,
                                                onDismissRequest = { showAttachmentsPopup = false },
                                                modifier = Modifier.background(Color.White)
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Drive Link File", color = Color(0xFF1F1F1F), fontSize = 13.sp) },
                                                    onClick = { showAttachmentsPopup = false }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Upload files metadata", color = Color(0xFF1F1F1F), fontSize = 13.sp) },
                                                    onClick = { showAttachmentsPopup = false }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Record Audio Snippet", color = Color(0xFF1F1F1F), fontSize = 13.sp) },
                                                    onClick = { showAttachmentsPopup = false }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Camera Capture Snap", color = Color(0xFF1F1F1F), fontSize = 13.sp) },
                                                    onClick = { showAttachmentsPopup = false }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("YouTube Video Context", color = Color(0xFF1F1F1F), fontSize = 13.sp) },
                                                    onClick = { showAttachmentsPopup = false }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Sample Template Media", color = Color(0xFF1F1F1F), fontSize = 13.sp) },
                                                    onClick = { showAttachmentsPopup = false }
                                                )
                                            }
                                        }
 
                                        Spacer(modifier = Modifier.width(4.dp))
 
                                        Button(
                                            onClick = {
                                                if (textInput.isNotBlank()) {
                                                    viewModel.submitPlaygroundPrompt(textInput)
                                                    textInput = ""
                                                    focusManager.clearFocus()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("Run", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
 
            // 2. Add all the text written on the side in a circular shape controls console
            if (showControlsPanel) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(280.dp)
                        .align(Alignment.CenterEnd)
                        .background(Color(0xFAFAFAFA)) // Clean cockpit panel on light mode
                        .border(1.dp, Color(0xFFE2E8F0))
                        .clickable(enabled = false) {}
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                GeminiSparkleStar(size = 18.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Console Orbits",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1F1F1F)
                                )
                            }
                            IconButton(
                                onClick = { showControlsPanel = false },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close panel",
                                    tint = Color(0xFF5F6368),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
 
                        Divider(color = Color(0xFFE2E8F0))
 
                        // High fidelity interactive polar orbit!
                        GeminiOrbitControls(viewModel, tokens)
                    }
                }
            }
        }
    }
}

@Composable
fun LabsTab(viewModel: ReplViewModel, tokens: VisualThemeTokens) {
    val isHindi by viewModel.isHindi.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var promptInput by remember { mutableStateOf("") }
    
    // Theme collection
    val selectedThemeName by viewModel.selectedTheme.collectAsState()
    
    // Live DB state flows for local Room inspection
    val dbProjects by viewModel.projects.collectAsState()
    val dbCommits by viewModel.commits.collectAsState()
    val dbDeployments by viewModel.deployments.collectAsState()
    val dbFiles by viewModel.projectFiles.collectAsState()
    val activeProject by viewModel.selectedProject.collectAsState()
    
    var selectedTableQuery by remember { mutableStateOf("projects") }
    var displayedQueryResults by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var queryMessage by remember { mutableStateOf("SELECT * FROM projects LIMIT 20;") }
    var isQueryExecuting by remember { mutableStateOf(false) }
    var codeSnippetInput by remember { mutableStateOf("// Paste code here to transform\nfun calculateTotal(list: List<Int>): Int {\n    var res = 0\n    for (item in list) {\n        res += item\n    }\n    return res\n}") }
    var transformedOutput by remember { mutableStateOf("") }
    var isOptimizing by remember { mutableStateOf(false) }
    var activeVoicePreset by remember { mutableStateOf("Normal Mode") }
    var soundwaveIntensity by remember { mutableStateOf(0.5f) }

    // --- SVG Vector Live Sandbox States ---
    var svgCodeInput by remember { mutableStateOf("<rect x='20' y='20' width='160' height='160' fill='#0C0F1B' stroke='#00E5FF' />\n<circle cx='100' cy='100' r='60' fill='none' stroke='#E040FB' />\n<line x1='100' y1='20' x2='100' y2='180' stroke='#38BDF8' stroke-width='2' />\n<line x1='20' y1='100' x2='180' y2='100' stroke='#38BDF8' stroke-width='2' />\n<circle cx='100' cy='100' r='8' fill='#00E5FF' />") }
    var selectedSvgPreset by remember { mutableStateOf("Quantum Grid Portal") }
    
    // --- AI System Prompt Architect States ---
    var architectPromptInput by remember { mutableStateOf("Create a beautiful calendar dashboard with weather integration") }
    var architectTargetModel by remember { mutableStateOf("DeepSeek-R1 (DeepThinking)") }
    var generatedArchitectPrompt by remember { mutableStateOf("") }
    var isFusingArchitect by remember { mutableStateOf(false) }
    
    // Wave animation tick
    var waveOffset by remember { mutableStateOf(0f) }
    val soundwavePath = remember { androidx.compose.ui.graphics.Path() }
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            waveOffset = (waveOffset + 0.05f) % (2f * Math.PI.toFloat())
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(tokens.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Branding header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = tokens.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, tokens.primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(tokens.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeveloperMode,
                                contentDescription = "Studio Labs Icon",
                                tint = tokens.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = viewModel.translate("AI OMNI-AI Labs 🧪", "AI ओम्नी-एआई लैब्स 🧪"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = tokens.text
                            )
                            Text(
                                text = viewModel.translate("Prototype advanced models, optimize prompts & forge code presets.", "उन्नत मॉडलों का प्रोटोटाइप बनाएं, प्रॉम्प्ट अनुकूलित करें और कोड रिफैक्टर करें।"),
                                fontSize = 11.sp,
                                color = tokens.textMuted
                            )
                        }
                    }
                }
            }
        }

        // Section 1: Live Prompt Optimizer & Analyzer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = tokens.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, tokens.textMuted.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = viewModel.translate("1. Space Prompt Optimizer 🧠", "1. स्पेस प्रॉम्प्ट ऑप्टिमाइज़र 🧠"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = tokens.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = viewModel.translate("Write your Gemini instructions below. Evaluates token estimate and design quality criteria.", "नीचे अपने जेमिनी निर्देश लिखें। टोकन प्रभाग और गुणवत्ता मापदंड का मूल्यांकन करें।"),
                        fontSize = 11.sp,
                        color = tokens.textMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = { promptInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        textStyle = TextStyle(fontSize = 13.sp, color = tokens.text),
                        placeholder = { Text(viewModel.translate("Type prompt here...", "यहाँ प्रॉम्प्ट लिखें..."), color = tokens.textMuted, fontSize = 13.sp) },
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = tokens.text,
                            unfocusedTextColor = tokens.text,
                            focusedBorderColor = tokens.primary,
                            unfocusedBorderColor = tokens.textMuted.copy(alpha = 0.3f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Token math & scoring
                    val charCount = promptInput.length
                    val estimatedTokens = (charCount / 4.0).toInt()
                    val grade = when {
                        charCount == 0 -> "Empty"
                        charCount < 15 -> "Too Minimalists (Needs Detail)"
                        charCount < 50 -> "Decent (Needs Context)"
                        else -> "Production Quality (Optimal) ✅"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(tokens.background)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Est. Tokens", fontSize = 10.sp, color = tokens.textMuted)
                            Text("$estimatedTokens tkn", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tokens.primary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Prompt Grade Score", fontSize = 10.sp, color = tokens.textMuted)
                            Text(grade, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (grade.contains("✅")) tokens.success else tokens.secondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(viewModel.translate("Quick Preset Injection Templates:", "त्वरित प्रीसेट इंजेक्शन टेम्पलेट:"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tokens.text)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Preset tags row
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val templates = listOf(
                            "Hinglish Expert 🇮🇳" to "Mera objective code optimize karna hai, fast runtime chahiye, aur bugs free parameters dhoondho.",
                            "Compose Wizard 🚀" to "You are Jetpack Compose system wizard. Refactor with dynamic color theme, edgeToEdge, clear state hoisting and 48dp minimum click targets.",
                            "Deep Debugger 🐞" to "Strictly audit class structures, check Kotlin syntax, and scan for duplicate unresolved reference signatures."
                        )

                        templates.forEach { (name, fullTxt) ->
                            Card(
                                modifier = Modifier
                                    .clickable { promptInput = fullTxt }
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = tokens.background),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, tokens.primary.copy(alpha = 0.15f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = tokens.primary, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(name, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = tokens.text)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Inline Code Optimizer & Beautifier
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = tokens.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, tokens.textMuted.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = viewModel.translate("2. Hinglish Code Mutator & Optimizer 🧪", "2. हिंग्लिश कोड म्यूटेटर और ऑप्टिमाइज़र 🧪"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = tokens.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = codeSnippetInput,
                        onValueChange = { codeSnippetInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        textStyle = TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 11.sp, color = tokens.text),
                        placeholder = { Text("Write/paste code snippet...", color = tokens.textMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = tokens.text,
                            unfocusedTextColor = tokens.text,
                            focusedBorderColor = tokens.primary,
                            unfocusedBorderColor = tokens.textMuted.copy(alpha = 0.3f)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                isOptimizing = true
                                coroutineScope.launch {
                                    delay(800)
                                    transformedOutput = """
                                        // Highly optimized using tailrec or inline structures
                                        inline fun calculateTotalFast(list: List<Int>): Int {
                                            return list.sum()
                                        }
                                    """.trimIndent()
                                    isOptimizing = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = tokens.primary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isOptimizing) "Optimizing..." else "Optimize speed ⚡", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                transformedOutput = """
                                    // Chalo check karte hai input variables ko list mein
                                    fun calculateTotalHinglish(list: List<Int>): Int {
                                        var totalNaamKaValue = 0 // shuruati value zero hai
                                        for (it in list) {
                                            totalNaamKaValue += it // har item ko jodte jao hamari gaddi doudti rahegi
                                        }
                                        return totalNaamKaValue // lo bhai total mil gaya!
                                    }
                                """.trimIndent()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = tokens.secondary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Hinglish comments 🇮🇳", fontSize = 11.sp, color = tokens.background)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            transformedOutput = """
                                class GaddiEngine {
                                    var chalRahiHai = false
                                    var gaddiKaNaam = "Maruti Core"
                                    
                                    fun startKardoBhai() {
                                        chalRahiHai = true
                                    }
                                    
                                    fun thokoBreak() {
                                        chalRahiHai = false
                                    }
                                }
                            """.trimIndent()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.background),
                        border = BorderStroke(1.dp, tokens.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = tokens.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Inject Hinglish variable names 🇮🇳", fontSize = 11.sp, color = tokens.text)
                    }

                    if (transformedOutput.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Transformed Output 💅", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tokens.success)
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(tokens.background)
                                .border(1.dp, tokens.success.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = transformedOutput,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = tokens.text
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Live Pulse Soundwave Generator
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = tokens.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, tokens.textMuted.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = viewModel.translate("3. AI Voice Pitch & Rhythm Soundwave 🎙️", "3. AI वॉयस पिच और रिदम साउंडवेव 🎙️"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = tokens.primary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = viewModel.translate("Interactive frequency synthesizer. Dynamic wave render class.", "इंटरैक्टिव आवृत्ति सिंथेसाइज़र। गतिशील वेव रेंडर क्लास।"),
                        fontSize = 11.sp,
                        color = tokens.textMuted,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Pulsating voice nodes
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val voiceModes = listOf("Normal Mode", "Hyperspeed Mode", "Robotic Synth", "Hinglish DJ")
                        voiceModes.forEach { mode ->
                            val isSel = activeVoicePreset == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) tokens.primary else tokens.background)
                                    .border(1.dp, if (isSel) tokens.primary else tokens.primary.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                    .clickable { 
                                        activeVoicePreset = mode
                                        soundwaveIntensity = when(mode) {
                                            "Normal Mode" -> 0.4f
                                            "Hyperspeed Mode" -> 0.85f
                                            "Robotic Synth" -> 0.2f
                                            "Hinglish DJ" -> 0.98f
                                            else -> 0.5f
                                        }
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) tokens.background else tokens.text,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gorgeous live math soundwave custom Canvas using drawBehind to avoid recomposition!
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(tokens.background)
                            .border(1.dp, tokens.primary.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            .drawBehind {
                                val width = size.width
                                val height = size.height
                                if (width < 1f || height < 1f) return@drawBehind
                                val midY = height / 2f
                                
                                soundwavePath.reset()
                                soundwavePath.moveTo(0f, midY)
                                
                                // Render trigonometric wave curves multiplied by intensity and animated offset
                                val pointsCount = width.toInt()
                                for (x in 0..pointsCount step 12) {
                                    val xf = x.toFloat()
                                    val angle = (xf / width) * 4f * Math.PI.toFloat() + waveOffset
                                    // Multi frequency sine waves
                                    val sineVal = Math.sin(angle.toDouble()).toFloat()
                                    val secondarySine = Math.sin((angle * 2.5f).toDouble()).toFloat() * 0.35f
                                    val y = midY + (sineVal + secondarySine) * 28.dp.toPx() * soundwaveIntensity
                                    soundwavePath.lineTo(xf, y)
                                }
                                
                                drawPath(
                                    path = soundwavePath,
                                    color = tokens.primary,
                                    style = Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                )

                                // Draw neat grid guide/nodes representing sound level bounds
                                drawLine(
                                    color = tokens.textMuted.copy(alpha = 0.15f),
                                    start = androidx.compose.ui.geometry.Offset(0f, midY),
                                    end = androidx.compose.ui.geometry.Offset(width, midY),
                                    strokeWidth = 1f,
                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                )
                            }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hz Modulation Frequency: ${ (soundwaveIntensity * 440).toInt() } Hz • Adaptive Amplitude Audio Buffer",
                        fontSize = 9.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = tokens.textMuted
                    )
                }
            }
        }

        // Section 4: Interactive SVG Vector Graphics Live Sandbox Compiler & Preview Canvas
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = tokens.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, tokens.textMuted.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = viewModel.translate("4. SVG Vector Graphics Live Sandbox 🎨", "4. एसवीजी वेक्टर विजुअल लाइव सैंडबॉक्स 🎨"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = tokens.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = viewModel.translate("Edit raw SVG tags in real-time or select presets to compile dynamic graphics onto the GPU Canvas.", "वास्तविक समय में कच्चे एसवीजी कोड को संपादित करें या सीधे जीपीयू कैनवास पर रेंडर करने के लिए प्रीसेट चुनें।"),
                        fontSize = 11.sp,
                        color = tokens.textMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Preset selection tags row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val presets = listOf(
                            "Quantum Grid Portal" to "<rect x='20' y='20' width='160' height='160' fill='#0C0F1B' stroke='#00E5FF' />\n<circle cx='100' cy='100' r='60' fill='none' stroke='#E040FB' />\n<line x1='100' y1='20' x2='100' y2='180' stroke='#38BDF8' stroke-width='2' />\n<line x1='20' y1='100' x2='180' y2='100' stroke='#38BDF8' stroke-width='2' />\n<circle cx='100' cy='100' r='8' fill='#00E5FF' />",
                            "Cyber Star Radar" to "<rect x='10' y='10' width='180' height='180' fill='#04050F' stroke='#00E5FF' />\n<circle cx='100' cy='100' r='80' fill='none' stroke='#E040FB' />\n<circle cx='100' cy='100' r='50' fill='none' stroke='#00E5FF' />\n<line x1='100' y1='10' x2='100' y2='190' stroke='#EAB308' stroke-width='1' />\n<circle cx='100' cy='100' r='10' fill='#00E5FF' />",
                            "Golden Radiant Spiral" to "<rect x='10' y='10' width='180' height='180' fill='#070E0D' stroke='#EAB308' />\n<circle cx='100' cy='100' r='75' fill='none' stroke='#EAB308' stroke-width='3' />\n<circle cx='100' cy='100' r='55' fill='none' stroke='#F59E0B' stroke-width='2' />\n<circle cx='100' cy='100' r='35' fill='none' stroke='#FFD700' stroke-width='1' />\n<circle cx='100' cy='100' r='15' fill='#FFD700' />"
                        )

                        presets.forEach { (name, code) ->
                            val isSelected = selectedSvgPreset == name
                            Card(
                                modifier = Modifier
                                    .clickable {
                                        selectedSvgPreset = name
                                        svgCodeInput = code
                                    }
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isSelected) tokens.primary.copy(alpha = 0.2f) else tokens.background),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isSelected) tokens.primary else tokens.primary.copy(alpha = 0.1f))
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) tokens.primary else tokens.text,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Text editor for SVG tags
                    OutlinedTextField(
                        value = svgCodeInput,
                        onValueChange = { svgCodeInput = it },
                        textStyle = TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 11.sp, color = tokens.text),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        label = { Text("SVG Compilation Tags Console", color = tokens.primary, fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = tokens.text,
                            unfocusedTextColor = tokens.text,
                            focusedBorderColor = tokens.primary,
                            unfocusedBorderColor = tokens.textMuted.copy(alpha = 0.3f)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Compiled Output Live Canvas Box
                    Text("LIVE INTERACTIVE GPU CANVAS PREVIEW:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tokens.text)
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF030514))
                            .border(1.dp, tokens.primary.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(200.dp)) {
                            // Draw an ambient scanner wireframe background grid
                            val gridSize = size.width / 10f
                            for (i in 0..10) {
                                drawLine(
                                    color = Color(0xFF00E5FF).copy(alpha = 0.05f),
                                    start = androidx.compose.ui.geometry.Offset(0f, i * gridSize),
                                    end = androidx.compose.ui.geometry.Offset(size.width, i * gridSize),
                                    strokeWidth = 1f
                                )
                                drawLine(
                                    color = Color(0xFF00E5FF).copy(alpha = 0.05f),
                                    start = androidx.compose.ui.geometry.Offset(i * gridSize, 0f),
                                    end = androidx.compose.ui.geometry.Offset(i * gridSize, size.height),
                                    strokeWidth = 1f
                                )
                            }

                            // Dynamic parsing & drawing of circle, rect, line, and polygons!
                            val lines = svgCodeInput.split("\n")
                            lines.forEach { line ->
                                val trimmed = line.trim()
                                try {
                                    if (trimmed.startsWith("<circle")) {
                                        val cx = trimmed.substringAfter("cx='").substringBefore("'").toFloatOrNull() ?: 100f
                                        val cy = trimmed.substringAfter("cy='").substringBefore("'").toFloatOrNull() ?: 100f
                                        val r = trimmed.substringAfter("r='").substringBefore("'").toFloatOrNull() ?: 50f
                                        val isNone = trimmed.contains("fill='none'")
                                        val fillColHex = if (isNone) "" else (trimmed.substringAfter("fill='").substringBefore("'"))
                                        val strokeColHex = trimmed.substringAfter("stroke='").substringBefore("'")
                                        val strokeW = trimmed.substringAfter("stroke-width='").substringBefore("'").toFloatOrNull() ?: 2f

                                        // Scaling for 200dp grid mapping
                                        val scaleX = size.width / 200f
                                        val scaleY = size.height / 200f

                                        if (fillColHex.isNotEmpty() && !fillColHex.startsWith("<circle")) {
                                            val col = try { Color(android.graphics.Color.parseColor(fillColHex)) } catch (e: Exception) { Color(0xFF00E5FF) }
                                            drawCircle(color = col, radius = r * scaleX, center = androidx.compose.ui.geometry.Offset(cx * scaleX, cy * scaleY))
                                        }
                                        if (strokeColHex.isNotEmpty() && !strokeColHex.startsWith("<circle")) {
                                            val sCol = try { Color(android.graphics.Color.parseColor(strokeColHex)) } catch (e: Exception) { Color(0xFFE040FB) }
                                            drawCircle(
                                                color = sCol,
                                                radius = r * scaleX,
                                                center = androidx.compose.ui.geometry.Offset(cx * scaleX, cy * scaleY),
                                                style = Stroke(width = strokeW * scaleX)
                                            )
                                        }
                                    } else if (trimmed.startsWith("<rect")) {
                                        val rx = trimmed.substringAfter("x='").substringBefore("'").toFloatOrNull() ?: 20f
                                        val ry = trimmed.substringAfter("y='").substringBefore("'").toFloatOrNull() ?: 20f
                                        val rw = trimmed.substringAfter("width='").substringBefore("'").toFloatOrNull() ?: 160f
                                        val rh = trimmed.substringAfter("height='").substringBefore("'").toFloatOrNull() ?: 160f
                                        val isNone = trimmed.contains("fill='none'")
                                        val fillColHex = if (isNone) "" else (trimmed.substringAfter("fill='").substringBefore("'"))
                                        val strokeColHex = trimmed.substringAfter("stroke='").substringBefore("'")
                                        val strokeW = trimmed.substringAfter("stroke-width='").substringBefore("'").toFloatOrNull() ?: 2f

                                        val scaleX = size.width / 200f
                                        val scaleY = size.height / 200f

                                        if (fillColHex.isNotEmpty() && !fillColHex.startsWith("<rect")) {
                                            val col = try { Color(android.graphics.Color.parseColor(fillColHex)) } catch (e: Exception) { Color(0xFF0C0F1B) }
                                            drawRect(
                                                color = col,
                                                topLeft = androidx.compose.ui.geometry.Offset(rx * scaleX, ry * scaleY),
                                                size = androidx.compose.ui.geometry.Size(rw * scaleX, rh * scaleY)
                                            )
                                        }
                                        if (strokeColHex.isNotEmpty() && !strokeColHex.startsWith("<rect")) {
                                            val sCol = try { Color(android.graphics.Color.parseColor(strokeColHex)) } catch (e: Exception) { Color(0xFF00E5FF) }
                                            drawRect(
                                                color = sCol,
                                                topLeft = androidx.compose.ui.geometry.Offset(rx * scaleX, ry * scaleY),
                                                size = androidx.compose.ui.geometry.Size(rw * scaleX, rh * scaleY),
                                                style = Stroke(width = strokeW * scaleX)
                                            )
                                        }
                                    } else if (trimmed.startsWith("<line")) {
                                        val x1 = trimmed.substringAfter("x1='").substringBefore("'").toFloatOrNull() ?: 0f
                                        val y1 = trimmed.substringAfter("y1='").substringBefore("'").toFloatOrNull() ?: 0f
                                        val x2 = trimmed.substringAfter("x2='").substringBefore("'").toFloatOrNull() ?: 200f
                                        val y2 = trimmed.substringAfter("y2='").substringBefore("'").toFloatOrNull() ?: 200f
                                        val strokeColHex = trimmed.substringAfter("stroke='").substringBefore("'")
                                        val strokeW = trimmed.substringAfter("stroke-width='").substringBefore("'").toFloatOrNull() ?: 2f

                                        val scaleX = size.width / 200f
                                        val scaleY = size.height / 200f

                                        val sCol = if (strokeColHex.isNotEmpty() && !strokeColHex.startsWith("<line")) {
                                            try { Color(android.graphics.Color.parseColor(strokeColHex)) } catch (e: Exception) { Color(0xFF38BDF8) }
                                        } else Color(0xFF38BDF8)

                                        drawLine(
                                            color = sCol,
                                            start = androidx.compose.ui.geometry.Offset(x1 * scaleX, y1 * scaleY),
                                            end = androidx.compose.ui.geometry.Offset(x2 * scaleX, y2 * scaleY),
                                            strokeWidth = strokeW * scaleX
                                        )
                                    }
                                } catch (e: Exception) {
                                    // Soft failure to ensure safe user edits
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 5: AI System Prompt Refiner & Architect Engine
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = tokens.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, tokens.textMuted.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = viewModel.translate("5. AI System Prompt Refiner 🧬", "5. एआई सिस्टम प्रॉम्प्ट रिफाइनर 🧬"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = tokens.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = viewModel.translate("Forge production-ready, bulletproof system instruction hierarchies for Gemini or DeepSeek models.", "जेमिनी या डीपसीक मॉडल के लिए उत्पादन-तैयार, अचूक सिस्टम निर्देश पदानुक्रम बनाएं।"),
                        fontSize = 11.sp,
                        color = tokens.textMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = architectPromptInput,
                        onValueChange = { architectPromptInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Describe application intent or logic", color = tokens.text, fontSize = 11.sp) },
                        placeholder = { Text("E.g., build offline calculator with logs...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = tokens.text,
                            unfocusedTextColor = tokens.text,
                            focusedBorderColor = tokens.primary,
                            unfocusedBorderColor = tokens.textMuted.copy(alpha = 0.3f)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Model Selector for architecting target
                    Text("Architect Engine Optimization Core:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tokens.text)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val architectures = listOf("DeepSeek-R1", "Gemini 1.5 Pro", "ChatGPT-4o")
                        architectures.forEach { arch ->
                            val isSelected = architectTargetModel.startsWith(arch)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) tokens.primary.copy(alpha = 0.2f) else tokens.background)
                                    .border(1.dp, if (isSelected) tokens.primary else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { architectTargetModel = arch }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(arch, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = if (isSelected) tokens.primary else tokens.text)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Fuse Prompt Action Button
                    Button(
                        onClick = {
                            isFusingArchitect = true
                            coroutineScope.launch {
                                delay(900)
                                generatedArchitectPrompt = """
                                    # SYSTEM INSTRUCTIONS FOR ${architectTargetModel.uppercase()}
                                    You are an elite Android Developer Specialised in high performance design patterns.
                                    
                                    ## APPLICATION GOALS:
                                    - User Goal: $architectPromptInput
                                    - State management: Use clean, reactive state hoisted in ViewModels with MutableStateFlow.
                                    - Localization: Fully localized bilingual resources using standard translation hooks.
                                    
                                    ## CODING DIRECTIVES:
                                    1. Strictly adhere to dynamic Material Design 3 templates.
                                    2. Ensure 48dp minimum click targets with beautiful interactive ripples.
                                    3. Write precise Kotlin code without any mock data structures. Include beautiful edge-to-edge layouts.
                                """.trimIndent()
                                isFusingArchitect = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.primary)
                    ) {
                        if (isFusingArchitect) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(viewModel.translate("Construct Professional System Prompt 🧬", "व्यावसायिक प्रणाली प्रॉम्प्ट तैयार करें 🧬"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (generatedArchitectPrompt.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))

                        // Output Display Window
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, tokens.success.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                            colors = CardDefaults.cardColors(containerColor = tokens.background),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("PROMPT FORGED SUCCESSFULLY ✅", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tokens.success)
                                    Icon(Icons.Default.Done, contentDescription = null, tint = tokens.success, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = generatedArchitectPrompt,
                                    fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = tokens.text
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Inject directly to Ghostwriter AI Chat Tab Button
                                Button(
                                    onClick = {
                                        viewModel.setSharedPrompt(generatedArchitectPrompt)
                                        viewModel.setActiveTab("ai") // Instantly redirect to the Chat Tab!
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = tokens.success),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(viewModel.translate("Inject to Ghostwriter Chat 💬", "घोस्टराइटर चैट में भेजें 💬"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 6: Live Room SQLite Database Inspector & Event Forge
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = tokens.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, tokens.textMuted.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = viewModel.translate("6. Live Room SQLite DB Inspector & Event Forge 🗄️", "6. लाइव रूम SQLite डेटाबेस इंस्पेक्टर और इवेंट फोर्ज 🗄️"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = tokens.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = viewModel.translate("Inspect direct Room database entities and forge live files, commits, and deployments on-the-fly.", "प्रत्यक्ष रूप से रूम डेटाबेस निकायों का निरीक्षण करें और तुरंत फाइलें, कमिट तथा परिनियोजन (डिप्लॉयमेंट) बनाएं।"),
                        fontSize = 11.sp,
                        color = tokens.textMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Raw Database Metrics Overview Grid
                    Text(viewModel.translate("Live Connected DB Tables:", "लाइव कनेक्टेड डेटाबेस टेबल्स:"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tokens.text)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Projects card
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = tokens.background),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("projects", fontSize = 10.sp, color = tokens.textMuted, maxLines = 1)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${dbProjects.size} rows", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = tokens.primary)
                            }
                        }

                        // Files card
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = tokens.background),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("project_files", fontSize = 10.sp, color = tokens.textMuted, maxLines = 1)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${dbFiles.size} rows", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = tokens.primary)
                            }
                        }

                        // Commits card
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = tokens.background),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("commits", fontSize = 10.sp, color = tokens.textMuted, maxLines = 1)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${dbCommits.size} rows", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = tokens.primary)
                            }
                        }

                        // Deployments card
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = tokens.background),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("deployments", fontSize = 10.sp, color = tokens.textMuted, maxLines = 1)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${dbDeployments.size} rows", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = tokens.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Row Creator/Forge Buttons (Mutates state of ROOM database in real-time)
                    Text(viewModel.translate("Instant DB Mutation Forge:", "त्वरित डेटाबेस म्यूटेशन फोर्ज:"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tokens.text)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Forge commit
                        Button(
                            onClick = {
                                viewModel.pushGitCommit("🤖 Labs Forge Event #${System.currentTimeMillis() % 1000} in ${selectedThemeName} theme")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = tokens.primary.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, tokens.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = tokens.primary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Forge Commit", fontSize = 9.sp, color = tokens.text, maxLines = 1)
                        }

                        // Forge deploy
                        Button(
                            onClick = {
                                viewModel.deployProject()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = tokens.success.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, tokens.success.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = tokens.success, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Forge Deploy", fontSize = 9.sp, color = tokens.text, maxLines = 1)
                        }

                        // Forge file
                        Button(
                            onClick = {
                                val testFileName = "forge_labs_${System.currentTimeMillis() % 1000}.kt"
                                viewModel.createNewFileInProject(testFileName)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = tokens.secondary.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, tokens.secondary.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = tokens.secondary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Forge File", fontSize = 9.sp, color = tokens.text, maxLines = 1)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Interactive Query Console Selector
                    Text(viewModel.translate("Live SQLite Query Simulator (Choose Table):", "लाइव SQLite क्वेरी सिम्युलेटर (तालिका चुनें):"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tokens.text)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val tables = listOf("projects", "project_files", "commits", "deployments")
                        tables.forEach { table ->
                            val isSel = selectedTableQuery == table
                            Card(
                                modifier = Modifier
                                    .clickable {
                                        selectedTableQuery = table
                                        queryMessage = "SELECT * FROM $table LIMIT 20;"
                                        displayedQueryResults = emptyList()
                                    }
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isSel) tokens.primary.copy(alpha = 0.1f) else tokens.background),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isSel) tokens.primary else tokens.primary.copy(alpha = 0.1f))
                            ) {
                                Text(
                                    text = table,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSel) tokens.primary else tokens.text,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Text Editor representing SQLite query Input
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(tokens.background)
                            .border(1.dp, tokens.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("sqlite>", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 12.sp, color = tokens.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(queryMessage, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 12.sp, color = tokens.text)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            isQueryExecuting = true
                            coroutineScope.launch {
                                delay(600)
                                isQueryExecuting = false
                                displayedQueryResults = when (selectedTableQuery) {
                                    "projects" -> dbProjects.map {
                                        mapOf(
                                            "id" to it.id.toString(),
                                            "name" to it.name,
                                            "lang" to it.languageType,
                                            "synced" to it.isCloudSynced.toString()
                                        )
                                    }
                                    "project_files" -> dbFiles.map {
                                        mapOf(
                                            "id" to it.id.toString(),
                                            "p_id" to it.projectId.toString(),
                                            "path" to it.path,
                                            "size" to "${it.content.length} chars"
                                        )
                                    }
                                    "commits" -> dbCommits.map {
                                        mapOf(
                                            "id" to it.id.toString(),
                                            "p_id" to it.projectId.toString(),
                                            "msg" to it.commitMessage,
                                            "author" to it.author,
                                            "time" to it.timestamp.toString()
                                        )
                                    }
                                    "deployments" -> dbDeployments.map {
                                        mapOf(
                                            "id" to it.id.toString(),
                                            "url" to it.domainUrl,
                                            "status" to it.status,
                                            "logs_len" to "${it.logs.length} chars"
                                        )
                                    }
                                    else -> emptyList()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.primary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = tokens.background, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isQueryExecuting) "Executing Simulator..." else "Execute Query Sim 🔥", fontSize = 11.sp, color = tokens.background)
                    }

                    if (displayedQueryResults.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Query Results (${displayedQueryResults.size} Rows):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tokens.success)
                            Text("Status: OK", fontSize = 10.sp, color = tokens.success, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(tokens.background)
                                .border(1.dp, tokens.success.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                                .horizontalScroll(rememberScrollState())
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                displayedQueryResults.forEachIndexed { idx, row ->
                                    if (idx == 0) {
                                        // Header row
                                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                            row.keys.forEach { key ->
                                                Text(
                                                    text = key.uppercase(),
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = tokens.primary,
                                                    modifier = Modifier.width(80.dp)
                                                )
                                            }
                                        }
                                        Spacer(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(tokens.textMuted.copy(alpha = 0.3f))
                                        )
                                    }
                                    
                                    // Data row
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        row.values.forEach { valStr ->
                                            Text(
                                                text = valStr,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                fontSize = 10.sp,
                                                color = tokens.text,
                                                modifier = Modifier.width(80.dp),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (!isQueryExecuting && selectedTableQuery.isNotEmpty() && displayedQueryResults.isEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Query returned 0 rows or has not been run yet.",
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = tokens.textMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CyborgChatbotBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "cyborg_pulse")
    val profilePath = remember { androidx.compose.ui.graphics.Path() }
    val cablePath = remember { androidx.compose.ui.graphics.Path() }
    
    val starPositions = remember {
        listOf(
            Offset(0.12f, 0.18f) to 4f,
            Offset(0.38f, 0.09f) to 3f,
            Offset(0.88f, 0.15f) to 5.5f,
            Offset(0.92f, 0.45f) to 3.5f,
            Offset(0.78f, 0.82f) to 4.5f,
            Offset(0.18f, 0.88f) to 3f,
            Offset(0.06f, 0.62f) to 4f,
            Offset(0.82f, 0.58f) to 4.5f
        )
    }
    
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val orbitalRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(26000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitalRotation"
    )

    val starPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "starPhase"
    )

    androidx.compose.foundation.layout.Box(
        modifier = modifier.drawBehind {
            if (size.width < 1f || size.height < 1f) return@drawBehind

            // 1. Beautiful Space Galaxy / Cybernetic Auroral background (Inspired by user's gradient)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF040614), // Dark deep slate blue
                        Color(0xFF0A0F24), // Glowing navy blue
                        Color(0xFF03040C)  // Pure deep cosmic blue
                    )
                )
            )

            // Light Blue / Cyan holographic flare aura (Left glowing section)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.25f), Color.Transparent),
                    center = Offset(size.width * 0.18f, size.height * 0.42f),
                    radius = (size.width * 0.9f).coerceAtLeast(1f)
                )
            )

            // Rich Lavender / Pink aura glow (Right glowing section)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFD946EF).copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(size.width * 0.82f, size.height * 0.35f),
                    radius = (size.width * 0.95f).coerceAtLeast(1f)
                )
            )

            // Lower glowing nebula cloud
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFEC4899).copy(alpha = 0.11f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.8f),
                    radius = (size.width * 0.75f).coerceAtLeast(1f)
                )
            )

            // 2. Light Cyber-Grid matrix scanning lines
            val columnsCount = 8
            val rowsCount = 12
            val colW = size.width / columnsCount
            val rowH = size.height / rowsCount
            for (i in 0..columnsCount) {
                drawLine(
                    color = Color(0xFF00E5FF).copy(alpha = 0.025f),
                    start = Offset(i * colW, 0f),
                    end = Offset(i * colW, size.height),
                    strokeWidth = 1f
                )
            }
            for (j in 0..rowsCount) {
                drawLine(
                    color = Color(0xFF00E5FF).copy(alpha = 0.025f),
                    start = Offset(0f, j * rowH),
                    end = Offset(size.width, j * rowH),
                    strokeWidth = 1f
                )
            }

            // 3. Glowing micro-stars/constellations (gpt-5.1 chatbot AI vibes)
            starPositions.forEachIndexed { index, (relOffset, rSize) ->
                val phase = index * 1.5f
                val alphaMultiplier = (Math.sin((starPhase + phase).toDouble()).toFloat() + 1f) / 2f * 0.65f + 0.35f
                val px = relOffset.x * size.width
                val py = relOffset.y * size.height
                
                // Halo neon glow
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = alphaMultiplier * 0.22f),
                    radius = rSize * 2.8f,
                    center = Offset(px, py)
                )
                // Spark center
                drawCircle(
                    color = Color.White.copy(alpha = alphaMultiplier * 0.95f),
                    radius = rSize,
                    center = Offset(px, py)
                )
            }

            // 4. Trace the sleek cybernetic Cyborg Princess/Android Profile head in vector lines (left side)
            val cx = size.width * 0.28f
            val cy = size.height * 0.48f
            val fScale = size.width * 0.25f

            // Soft backing bio-fluorescent aura behind head
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.22f * pulseGlow), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = (fScale * 1.7f).coerceAtLeast(1f)
                )
            )

            // Draw compass navigation gears (orbit rings)
            rotate(degrees = orbitalRotation, pivot = Offset(cx, cy)) {
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.08f),
                    radius = fScale * 1.25f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.2f)
                )
                drawArc(
                    color = Color(0xFFD946EF).copy(alpha = 0.15f),
                    startAngle = 30f,
                    sweepAngle = 100f,
                    useCenter = false,
                    topLeft = Offset(cx - fScale * 1.25f, cy - fScale * 1.25f),
                    size = androidx.compose.ui.geometry.Size(fScale * 2.5f, fScale * 2.5f),
                    style = Stroke(width = 3f)
                )
                drawArc(
                    color = Color(0xFF00E5FF).copy(alpha = 0.20f),
                    startAngle = 180f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(cx - fScale * 1.25f, cy - fScale * 1.25f),
                    size = androidx.compose.ui.geometry.Size(fScale * 2.5f, fScale * 2.5f),
                    style = Stroke(width = 3.5f)
                )
            }

            // Compose Vector lines Path for the robot face (extremely clean profile)
            profilePath.reset()
            profilePath.apply {
                moveTo(cx - fScale * 0.65f, cy - fScale * 0.55f)
                cubicTo(
                    cx - fScale * 0.85f, cy - fScale * 0.5f,
                    cx - fScale * 0.9f, cy + fScale * 0.25f,
                    cx - fScale * 0.55f, cy + fScale * 0.72f
                )
                lineTo(cx - fScale * 0.5f, cy + fScale * 0.92f)
                lineTo(cx - fScale * 0.2f, cy + fScale * 0.92f)
                cubicTo(
                    cx + fScale * 0.02f, cy + fScale * 0.8f,
                    cx + fScale * 0.08f, cy + fScale * 0.62f,
                    cx + fScale * 0.15f, cy + fScale * 0.52f
                )
                lineTo(cx + fScale * 0.2f, cy + fScale * 0.45f)
                lineTo(cx + fScale * 0.26f, cy + fScale * 0.24f)
                lineTo(cx + fScale * 0.18f, cy + fScale * 0.18f)
                cubicTo(
                    cx + fScale * 0.2f, cy - fScale * 0.12f,
                    cx + fScale * 0.08f, cy - fScale * 0.6f,
                    cx - fScale * 0.12f, cy - fScale * 0.65f
                )
                close()
            }

            // Fill background of cybernetic face
            drawPath(
                path = profilePath,
                color = Color(0xFF04091A).copy(alpha = 0.42f)
            )

            // Draw profile stroke outline
            drawPath(
                path = profilePath,
                color = Color(0xFF00E5FF).copy(alpha = 0.38f),
                style = Stroke(width = 2.5f)
            )

            // Beautiful micro chip details inside
            val chX = cx - fScale * 0.3f
            val chY = cy - fScale * 0.18f
            val chW = fScale * 0.42f
            val chH = fScale * 0.42f

            drawRect(
                color = Color(0xFF00E5FF).copy(alpha = 0.1f * pulseGlow),
                topLeft = Offset(chX, chY),
                size = androidx.compose.ui.geometry.Size(chW, chH)
            )
            drawRect(
                color = Color(0xFF00E5FF).copy(alpha = 0.32f),
                topLeft = Offset(chX, chY),
                size = androidx.compose.ui.geometry.Size(chW, chH),
                style = Stroke(width = 1.8f)
            )

            // Neuro connections paths
            drawLine(
                color = Color(0xFFD946EF).copy(alpha = 0.4f),
                start = Offset(chX + chW / 2, chY),
                end = Offset(chX + chW / 2, cy - fScale * 0.42f),
                strokeWidth = 2f
            )
            drawCircle(
                color = Color(0xFFD946EF),
                radius = 3f * density,
                center = Offset(chX + chW / 2, cy - fScale * 0.42f)
            )

            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = 0.42f),
                start = Offset(chX + chW, chY + chH / 2),
                end = Offset(cx + fScale * 0.1f, chY + chH / 2),
                strokeWidth = 1.8f
            )
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.8f * pulseGlow),
                radius = 4.5f * density,
                center = Offset(cx + fScale * 0.1f, chY + chH / 2)
            )

            // Neck cables glow
            cablePath.reset()
            cablePath.apply {
                moveTo(cx - fScale * 0.45f, cy + fScale * 0.78f)
                lineTo(cx - fScale * 0.28f, cy + fScale * 0.78f)
                lineTo(cx - fScale * 0.18f, cy + fScale * 0.88f)
            }
            drawPath(
                path = cablePath,
                color = Color(0xFF00E5FF).copy(alpha = 0.4f),
                style = Stroke(width = 2.2f)
            )
        }
    )
}

