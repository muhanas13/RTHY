package com.example.ui

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ExamSession
import com.example.data.InfractionLog
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// HTML offline secure self-test quiz
const val OFFLINE_MOCK_HTML = """
<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body { 
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; 
            background-color: #121212; 
            color: #E0E0E0; 
            padding: 20px; 
            line-height: 1.6; 
            margin: 0;
        }
        .container {
            max-width: 600px;
            margin: 0 auto;
        }
        .header {
            text-align: center;
            border-bottom: 2px solid #333333;
            padding-bottom: 15px;
            margin-bottom: 25px;
        }
        .header h1 {
            color: #00E676;
            font-size: 24px;
            margin: 0 0 8px 0;
            letter-spacing: 0.5px;
        }
        .header p {
            color: #888888;
            margin: 0;
            font-size: 14px;
        }
        .card { 
            background-color: #1E1E1E; 
            border-radius: 12px; 
            padding: 18px; 
            margin-bottom: 20px; 
            border: 1px solid #2A2A2A; 
            box-shadow: 0 4px 6px rgba(0,0,0,0.3);
        }
        h3 { 
            margin-top: 0; 
            color: #FFB300; 
            font-size: 16px;
            font-weight: 600;
        }
        .btn { 
            display: block; 
            width: 100%; 
            background-color: #00E676; 
            color: #000000; 
            border: none; 
            padding: 14px; 
            border-radius: 8px; 
            font-size: 16px; 
            font-weight: bold; 
            cursor: pointer; 
            margin-top: 25px; 
            transition: all 0.2s ease;
        }
        .btn:hover { 
            background-color: #00B0FF; 
            transform: translateY(-1px);
        }
        .option { 
            display: block; 
            background: #252525; 
            border: 1px solid #333333; 
            padding: 12px; 
            margin: 10px 0; 
            border-radius: 8px; 
            cursor: pointer; 
            transition: background 0.15s;
        }
        .option:hover {
            background: #2D2D2D;
            border-color: #444444;
        }
        .option input { 
            margin-right: 12px; 
            transform: scale(1.1);
            vertical-align: middle;
        }
        .option span {
            vertical-align: middle;
        }
        .results {
            display: none;
            text-align: center;
            background: #1B5E20;
            border: 1px solid #2E7D32;
            padding: 18px;
            border-radius: 8px;
            color: #FFFFFF;
            margin-bottom: 20px;
        }
    </style>
    <title>Ujian Mandiri Integrasi Sistem</title>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>UJIAN CETAK INTEGRITAS AKADEMIK</h1>
            <p>Simulasi Ujian Mandiri - Gunakan mode offline ini untuk menguji seluruh sistem proteksi</p>
        </div>
        
        <div id="results" class="results">
            <h2>Ujian Selesai! Score Anda: 100/100</h2>
            <p>Sistem mendeteksi integritas Anda sempurna selama pengerjaan.</p>
        </div>

        <div class="card">
            <h3>Soal 1. Mengapa memotong/membagi layar (split-screen) dilarang selama pelaksanaan ujian akademik digital?</h3>
            <label class="option">
                <input type="radio" name="q1" value="A"> 
                <span>A. Mempercepat masa pakai baterai perangkat mobile</span>
            </label>
            <label class="option">
                <input type="radio" name="q1" value="B"> 
                <span>B. Mencegah siswa membuka text book, catatan penting, atau browser sekunder secara langsung</span>
            </label>
            <label class="option">
                <input type="radio" name="q1" value="C"> 
                <span>C. Menjamin sinyal wifi tetap stabil</span>
            </label>
        </div>

        <div class="card">
            <h3>Soal 2. Apa akibat teknis apabila Anda menurunkan laci notifikasi atau membuka menu melayang saat ujian berlangsung?</h3>
            <label class="option">
                <input type="radio" name="q2" value="A"> 
                <span>A. Halaman ujian akan ter-lock otomatis demi melindungi integritas konten</span>
            </label>
            <label class="option">
                <input type="radio" name="q2" value="B"> 
                <span>B. Mematikan layar handphone secara mendadak</span>
            </label>
            <label class="option">
                <input type="radio" name="q2" value="C"> 
                <span>C. Mematikan sambungan bluetooth</span>
            </label>
        </div>

        <div class="card">
            <h3>Soal 3. Bagaimana cara keluar dan menyerahkan hasil ujian secara legal dalam Exam Browser ini?</h3>
            <label class="option">
                <input type="radio" name="q3" value="A"> 
                <span>A. Melakukan restart paksa perangkat</span>
            </label>
            <label class="option">
                <input type="radio" name="q3" value="B"> 
                <span>B. Menggeser layar dari samping (gestur kembali) secara terus menerus</span>
            </label>
            <label class="option">
                <input type="radio" name="q3" value="C"> 
                <span>C. Meminta Pengawas/Examiner menginput sandi pengawas yang sah untuk melepaskan penguncian penuh</span>
            </label>
        </div>

        <button class="btn" onclick="submitExam()">KIRIM JAWABAN SEKARANG</button>
    </div>

    <script>
        function submitExam() {
            var q1 = document.querySelector('input[name="q1"]:checked');
            var q2 = document.querySelector('input[name="q2"]:checked');
            var q3 = document.querySelector('input[name="q3"]:checked');
            
            if (!q1 || !q2 || !q3) {
                alert("Mohon jawab seluruh pertanyaan sebelum menekan tombol Selesai.");
                return;
            }
            
            document.getElementById('results').style.display = 'block';
            window.scrollTo({top: 0, behavior: 'smooth'});
            alert("Hasil Ujian Terkirim dengan Sukses!");
        }
    </script>
</body>
</html>
"""

@Composable
fun MainNavigationContent(
    viewModel: ExamViewModel,
    onRequestPin: () -> Unit,
    onRequestUnpin: () -> Unit
) {
    val screen by viewModel.currentScreen.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = screen,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "ScreenTransition"
    ) { currentScreen ->
        when (currentScreen) {
            "LOGIN" -> LoginScreen(viewModel)
            "LOBBY" -> LobbyScreen(viewModel, onRequestPin)
            "EXAM" -> ExamScreen(viewModel, onRequestUnpin)
            "SESSION_DETAIL" -> SessionDetailScreen(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: ExamViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginError by viewModel.loginError.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingSheets.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Shield Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "EXAM BROWSER SECURE",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 450.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Masuk Peserta Ujian",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Masukkan Username dan Password Anda",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        placeholder = { Text("Contoh: 0072743132") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("login_username_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        placeholder = { Text("Password Anda") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                        singleLine = true
                    )

                    if (loginError != null) {
                        Text(
                            text = loginError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.login(username, password)
                        },
                        enabled = username.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_submit_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("MASUK", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))

                    if (isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                        Text(
                            "Menyinkronkan basis data Google Sheets...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.fetchSpreadsheets() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SINKRONKAN DATA TERKINI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    viewModel: ExamViewModel,
    onRequestPin: () -> Unit
) {
    val loggedInStudent by viewModel.loggedInStudent.collectAsStateWithLifecycle()
    val activeTokenInfo by viewModel.activeTokenInfo.collectAsStateWithLifecycle()
    val tokenError by viewModel.tokenError.collectAsStateWithLifecycle()
    
    val inputTitle by viewModel.inputTitle.collectAsStateWithLifecycle()
    val inputUrl by viewModel.inputUrl.collectAsStateWithLifecycle()
    val examinerPassword by viewModel.examinerPassword.collectAsStateWithLifecycle()
    val useScreenPinning by viewModel.useScreenPinning.collectAsStateWithLifecycle()
    val requirePasswordToUnlock by viewModel.requirePasswordToUnlock.collectAsStateWithLifecycle()
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()

    var tokenValue by remember { mutableStateOf("") }
    var showSupervisorPanel by remember { mutableStateOf(false) }
    var supervisorPasswordInput by remember { mutableStateOf("") }
    var isSupervisorUnlocked by remember { mutableStateOf(false) }
    var supervisorPasswordError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Shield Safety",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "DASHBOARD EXAM BROWSER",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Keluar akun", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Student identity section
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Student Icon",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = loggedInStudent?.name ?: "N/A Student",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Kelas: ${loggedInStudent?.className ?: "N/A"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Token entry section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Aktivasi Token Ujian",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Silakan masukkan token pengerjaan yang dibagikan oleh pengawas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = tokenValue,
                            onValueChange = { tokenValue = it },
                            label = { Text("Token Ujian") },
                            placeholder = { Text("Contoh: CINTA") },
                            leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("token_input"),
                            singleLine = true
                        )

                        if (tokenError != null) {
                            Text(
                                text = tokenError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.checkToken(tokenValue)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("check_token_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PERIKSA TOKEN")
                        }
                    }
                }
            }

            // Confirmed Exam Info Card
            if (activeTokenInfo != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Exam Found",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Detail Soal Teraktivasi",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Mata Pelajaran:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text(activeTokenInfo!!.subject, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Waktu Pengerjaan:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text(activeTokenInfo!!.duration, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Kelas Sasaran:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text(activeTokenInfo!!.className, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = {
                                    if (useScreenPinning) {
                                        onRequestPin()
                                    }
                                    viewModel.startExam()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("start_exam_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2E7D32)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "MULAI SEKARANG",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Hidden Supervisor/Admin Panel for security configurations and infraction histories
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSupervisorPanel = !showSupervisorPanel },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Menu Khusus Pengawas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Icon(
                                imageVector = if (showSupervisorPanel) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }

                        if (showSupervisorPanel) {
                            Spacer(modifier = Modifier.height(12.dp))
                            if (!isSupervisorUnlocked) {
                                OutlinedTextField(
                                    value = supervisorPasswordInput,
                                    onValueChange = {
                                        supervisorPasswordInput = it
                                        supervisorPasswordError = false
                                    },
                                    label = { Text("Sandi Pengawas") },
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                if (supervisorPasswordError) {
                                    Text("Sandi Salah!", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                }
                                Button(
                                    onClick = {
                                        if (supervisorPasswordInput == examinerPassword) {
                                            isSupervisorUnlocked = true
                                            supervisorPasswordInput = ""
                                        } else {
                                            supervisorPasswordError = true
                                        }
                                    },
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Buka Panel")
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Pengaturan Proteksi:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                                    OutlinedTextField(
                                        value = inputUrl,
                                        onValueChange = { viewModel.setInputUrl(it) },
                                        label = { Text("Tautan Ujian Darurat / Manual") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = examinerPassword,
                                        onValueChange = { viewModel.setExaminerPassword(it) },
                                        label = { Text("Ubah Sandi Pengawas (Default: 12345)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    ListItem(
                                        headlineContent = { Text("Kunci Layar Penuh (Pinning)") },
                                        trailingContent = {
                                            Switch(
                                                checked = useScreenPinning,
                                                onCheckedChange = { viewModel.setUseScreenPinning(it) }
                                            )
                                        },
                                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                    )

                                    ListItem(
                                        headlineContent = { Text("Batasi dengan Sandi") },
                                        trailingContent = {
                                            Switch(
                                                checked = requirePasswordToUnlock,
                                                onCheckedChange = { viewModel.setRequirePasswordToUnlock(it) }
                                            )
                                        },
                                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                    )

                                    Divider()

                                    Text("Riwayat Penggunaan Sistem (${allSessions.size} Sesi)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    if (allSessions.isEmpty()) {
                                        Text("Belum ada riwayat terekam.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    } else {
                                        allSessions.forEach { session ->
                                            SessionItemCard(session = session, onClick = { viewModel.selectSession(session) })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionItemCard(
    session: ExamSession,
    onClick: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val formattedDate = formatter.format(Date(session.startTime))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = session.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (session.infractionsCount > 0) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${session.infractionsCount} Curang", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Badge(
                        containerColor = Color(0xFF2E7D32),
                        contentColor = Color.White
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Aman", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mulai: $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = if (session.isCompleted) "Selesai" else "Belum Selesai",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (session.isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ExamScreen(
    viewModel: ExamViewModel,
    onRequestUnpin: () -> Unit
) {
    val title by viewModel.inputTitle.collectAsStateWithLifecycle()
    val rawUrl by viewModel.inputUrl.collectAsStateWithLifecycle()
    val isLocked by viewModel.examLocked.collectAsStateWithLifecycle()
    val lockReason by viewModel.lockReason.collectAsStateWithLifecycle()
    val activeLogs by viewModel.activeSessionLogs.collectAsStateWithLifecycle()
    val requirePasswordToUnlock by viewModel.requirePasswordToUnlock.collectAsStateWithLifecycle()

    var elapsedSeconds by remember { mutableStateOf(0) }
    var inputUnlockPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var loadProgress by remember { mutableStateOf(0) }

    // Increment Timer while Exam is Active
    LaunchedEffect(isLocked) {
        if (!isLocked) {
            while (true) {
                delay(1000)
                elapsedSeconds++
            }
        }
    }

    // Auto-unlock features if Password config is disabled
    var cooldownSecondsRemaining by remember { mutableStateOf(0) }
    LaunchedEffect(isLocked) {
        if (isLocked && !requirePasswordToUnlock) {
            cooldownSecondsRemaining = 15 // 15 seconds cooldown sandbox mode
            while (cooldownSecondsRemaining > 0) {
                delay(1000)
                cooldownSecondsRemaining--
            }
            // Auto unlock
            viewModel.unlockExam("12345") // Default triggers bypass
        }
    }

    // Intercept hardware Back gesture during exam to prevent cheat escape
    BackHandler(enabled = true) {
        viewModel.triggerInfraction(
            "UNPINNED",
            "Siswa mencoba menggunakan gestur kembali (Back Button) untuk menavigasi keluar."
        )
    }

    // Custom dark gradient border under active countdown dashboard
    Scaffold(
        topBar = {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = {
                            viewModel.triggerInfraction(
                                "UNPINNED",
                                "Siswa mencoba menekan tombol Kembali di panel atas."
                            )
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            if (isLocked) MaterialTheme.colorScheme.error else Color(
                                                0xFF2E7D32
                                            ), CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isLocked) "SISTEM TERKUNCI" else "PROTEKSI AKTIF",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = if (isLocked) MaterialTheme.colorScheme.error else Color(
                                        0xFF2E7D32
                                    )
                                )
                            }
                        }

                        // Elapsed Time display
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            val minutes = elapsedSeconds / 60
                            val seconds = elapsedSeconds % 60
                            Text(
                                text = String.format("%02d:%02d", minutes, seconds),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Linear indicators
                    if (loadProgress < 100) {
                        LinearProgressIndicator(
                            progress = loadProgress / 100f,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    } else {
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                tonalElevation = 1.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Infraction warning counters
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (activeLogs.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${activeLogs.size} Percobaan Curang",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (activeLogs.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Finish exam action requiring examiner code confirmation
                    Button(
                        onClick = {
                            // Instantly lock screen and demand password for submission to guarantee examiner collection!
                            viewModel.triggerInfraction("EXIT_REQUEST", "Siswa meminta pengumpulan lembar ujian selesai.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("submit_exam_finished")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SELESAI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Secure Webview
            val context = LocalContext.current
            
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            supportZoom()
                            builtInZoomControls = true
                            displayZoomControls = false
                        }

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val destination = request?.url?.toString() ?: ""
                                if (destination.startsWith("http://") || destination.startsWith("https://")) {
                                    return false // Load inside self WebView
                                }
                                return true // Block external escape protocols
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                loadProgress = 100
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                loadProgress = newProgress
                            }
                        }

                        webViewInstance = this

                        // Determine Offline vs Online URLs
                        if (rawUrl.equals("offline", ignoreCase = true) || rawUrl.isBlank()) {
                            loadDataWithBaseURL("file:///android_asset/", OFFLINE_MOCK_HTML, "text/html", "UTF-8", null)
                        } else {
                            loadUrl(rawUrl)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("exam_webview"),
                update = {
                    // Update actions if necessary
                }
            )

            // Blur & Shroud overlay when the Exam is Locked (anti-cheat alert triggered!)
            AnimatedVisibility(
                visible = isLocked,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                // Intercept clicks on locked screen so user can't tap webview items behind it!
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.95f))
                        .clickable(enabled = true, onClick = {}),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                    CircleShape
                                )
                                .border(1.dp, MaterialTheme.colorScheme.error, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked alert",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Akses Ujian Terkunci!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Pemicu Penguncian:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = lockReason,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp),
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Passcode Unlocking inputs
                        if (requirePasswordToUnlock) {
                            Text(
                                "Masuk Sandi Pengawas Untuk Membuka PIN:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = inputUnlockPassword,
                                onValueChange = {
                                    inputUnlockPassword = it
                                    passwordError = false
                                },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                label = { Text("PIN Pengawas (Default: 12345)") },
                                isError = passwordError,
                                singleLine = true,
                                modifier = Modifier
                                    .width(260.dp)
                                    .testTag("unlock_pin_input")
                            )

                            if (passwordError) {
                                Text(
                                    "Sandi salah! Silakan coba lagi.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = {
                                        if (viewModel.unlockExam(inputUnlockPassword)) {
                                            inputUnlockPassword = ""
                                            passwordError = false
                                        } else {
                                            passwordError = true
                                        }
                                    },
                                    modifier = Modifier.testTag("submit_unlock_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Buka Kunci")
                                }

                                // Quick Exit/Force Unpin for supervisors if student cheats
                                OutlinedButton(
                                    onClick = {
                                        if (inputUnlockPassword == viewModel.examinerPassword.value) {
                                            onRequestUnpin()
                                            viewModel.completeExam()
                                        } else {
                                            passwordError = true
                                        }
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Paksa Selesai & Keluar")
                                }
                            }
                        } else {
                            // Cooldown message for sandbox evaluation
                            Text(
                                "Membuka kunci otomatis dalam:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "$cooldownSecondsRemaining detik",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "(Hanya untuk mode evaluasi / testing)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Text(
                            "Hubungi pengawas ruang untuk merilis penguncian ujian Anda.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    viewModel: ExamViewModel
) {
    val session by viewModel.selectedSession.collectAsStateWithLifecycle()
    val logs by viewModel.selectedSessionLogs.collectAsStateWithLifecycle()

    val formatter = remember { SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rincian Sesi Ujian") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBackToLobby() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { session?.let { viewModel.deleteSession(it.id) } }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Riwayat",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (session == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Memuat data...")
            }
        } else {
            val s = session!!
            val dateStr = formatter.format(Date(s.startTime))
            val durationStr = if (s.endTime != null) {
                val diffMs = s.endTime - s.startTime
                val diffMins = diffMs / (1000 * 60)
                val diffSecs = (diffMs / 1000) % 60
                String.format("%d Menit %d Detik", diffMins, diffSecs)
            } else {
                "Tidak terekam selesai"
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                // Header card details
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (s.infractionsCount > 0) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "STATUS DETAIL INTEGRITAS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (s.infractionsCount > 0) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = s.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = s.url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Waktu Mulai", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                Text(dateStr, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Durasi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                Text(durationStr, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Jumlah Pelanggaran", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                Text("${s.infractionsCount}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (s.infractionsCount > 0) MaterialTheme.colorScheme.error else Color(0xFF2E7D32))
                            }
                            Column {
                                Text("Integritas Skor", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                val baseIntegritas = if (s.infractionsCount == 0) 100 else (100 - (s.infractionsCount * 20)).coerceAtLeast(0)
                                Text("$baseIntegritas%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (baseIntegritas >= 80) Color(0xFF2E7D32) else if (baseIntegritas >= 50) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                Text(
                    "Daftar Kejadian Pelanggaran (${logs.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (logs.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "INTEGRITAS SEMPURNA!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                "Tidak ada aktivitas mencurigakan, split-screen, pemindahan latar belakang, ataupun penarikan bar notifikasi yang terdeteksi selama pengerjaan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(logs) { log ->
                            InfractionItemCard(log = log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfractionItemCard(log: InfractionLog) {
    val formatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val timeStr = formatter.format(Date(log.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val badgeTypeName = when (log.type) {
                        "BACKGROUND" -> "Latar Belakang / Switch App"
                        "LOST_FOCUS" -> "Kehilangan Fokus / Jendela Melayang"
                        "SPLIT_SCREEN" -> "Coba Split Screen"
                        "UNPINNED" -> "Gestur Keluar / Back Action"
                        "EXIT_REQUEST" -> "Permintaan Keluar"
                        else -> log.type
                    }
                    Text(
                        text = badgeTypeName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
