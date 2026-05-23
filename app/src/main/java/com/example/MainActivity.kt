package com.example

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.ExamDatabase
import com.example.data.ExamRepository
import com.example.ui.ExamViewModel
import com.example.ui.ExamViewModelFactory
import com.example.ui.MainNavigationContent
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val db by lazy { ExamDatabase.getDatabase(applicationContext) }
    private val repository by lazy { ExamRepository(db.examDao()) }
    
    // Instantiate our securely wired ViewModel
    private val viewModel: ExamViewModel by viewModels {
        ExamViewModelFactory(repository)
    }

    private var examWasPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Prevent screenshots, screen recorders, and clear overlay injections
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        
        // Keep the screen on continuously during exam
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        enableEdgeToEdge()
        setupImmersiveMode()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigationContent(
                        viewModel = viewModel,
                        onRequestPin = { startKioskMode() },
                        onRequestUnpin = { stopKioskMode() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupImmersiveMode()

        // Check if we were during active exam and we regained focus from pause/stop background cheat
        val currentScreen = viewModel.currentScreen.value
        if (currentScreen == "EXAM") {
            // Check Multi-Window (Split Screen) Attempt
            if (isInMultiWindowMode) {
                viewModel.triggerInfraction(
                    "SPLIT_SCREEN",
                    "Siswa mencoba membuka split-screen untuk mencontek secara berdampingan."
                )
            } else if (examWasPaused) {
                // Return from background infraction
                viewModel.triggerInfraction(
                    "BACKGROUND",
                    "Siswa terdeteksi keluar dari aplikasi (membuka tab browser lain, asisten, atau home)."
                )
            }
        }
        examWasPaused = false
    }

    override fun onPause() {
        super.onPause()
        val currentScreen = viewModel.currentScreen.value
        if (currentScreen == "EXAM") {
            examWasPaused = true
        }
    }

    // Intercept when home button is pressed, or app leaves focus (e.g., assistant triggered)
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val currentScreen = viewModel.currentScreen.value
        if (currentScreen == "EXAM") {
            viewModel.triggerInfraction(
                "BACKGROUND",
                "Siswa mencoba keluar ke layar utama (Home button pressed)."
            )
        }
    }

    // Capture floating overlays, notification shade pulling down, notification banners, volume panels
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val currentScreen = viewModel.currentScreen.value
        
        if (currentScreen == "EXAM" && !hasFocus) {
            // Focus lost in exam screen implies notification panel or overlay drew something
            viewModel.triggerInfraction(
                "LOST_FOCUS",
                "Fokus layar hilang. Terdeteksi tarikan bilah status (notifikasi) atau adanya jendela mengambang aktif."
            )
        }
        
        if (hasFocus) {
            setupImmersiveMode()
        }
    }

    // Android Native Screen Pinning (Lock Task Mode) Support
    private fun startKioskMode() {
        try {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Check if already pinned to avoid security exceptions or repeating dialogs
                if (activityManager.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) {
                    startLockTask()
                }
            } else {
                startLockTask()
            }
        } catch (e: Exception) {
            // Log security pinning warning and let standard state lock handle user fallback
            viewModel.triggerInfraction(
                "LOST_FOCUS",
                "Gagal memulai Screen Pinning otomatis. Harap pastikan fitur ter-izinkan."
            )
        }
    }

    private fun stopKioskMode() {
        try {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (activityManager.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE) {
                    stopLockTask()
                }
            } else {
                stopLockTask()
            }
        } catch (e: Exception) {
            // Do nothing
        }
    }

    // Force hide status bars and navigation inputs (Immersive Full Screen)
    private fun setupImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        }
    }
}
