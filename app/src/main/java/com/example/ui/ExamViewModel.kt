package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DefaultDb
import com.example.data.ExamRepository
import com.example.data.ExamSession
import com.example.data.InfractionLog
import com.example.data.Student
import com.example.data.TokenInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class ExamViewModel(private val repository: ExamRepository) : ViewModel() {

    // Input States for Session creation
    private val _inputTitle = MutableStateFlow("Ujian Standard Anti-Cheat")
    val inputTitle: StateFlow<String> = _inputTitle.asStateFlow()

    private val _inputUrl = MutableStateFlow("https://docs.google.com/forms/d/e/1FAIpQLSfBpxZ9sR3k1vKuhwUWhsS1w86vE5Bih-19-iU8XWff9-N2ug/viewform")
    val inputUrl: StateFlow<String> = _inputUrl.asStateFlow()

    private val _examinerPassword = MutableStateFlow("12345")
    val examinerPassword: StateFlow<String> = _examinerPassword.asStateFlow()

    private val _useScreenPinning = MutableStateFlow(true)
    val useScreenPinning: StateFlow<Boolean> = _useScreenPinning.asStateFlow()

    private val _requirePasswordToUnlock = MutableStateFlow(true)
    val requirePasswordToUnlock: StateFlow<Boolean> = _requirePasswordToUnlock.asStateFlow()

    // Navigation and screen management
    private val _currentScreen = MutableStateFlow("LOGIN") // LOGIN, LOBBY, EXAM, SESSION_DETAIL
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _selectedSession = MutableStateFlow<ExamSession?>(null)
    val selectedSession: StateFlow<ExamSession?> = _selectedSession.asStateFlow()

    private val _selectedSessionLogs = MutableStateFlow<List<InfractionLog>>(emptyList())
    val selectedSessionLogs: StateFlow<List<InfractionLog>> = _selectedSessionLogs.asStateFlow()

    // Active session security states
    private val _activeSessionId = MutableStateFlow<Int?>(null)
    val activeSessionId: StateFlow<Int?> = _activeSessionId.asStateFlow()

    private val _examLocked = MutableStateFlow(false)
    val examLocked: StateFlow<Boolean> = _examLocked.asStateFlow()

    private val _lockReason = MutableStateFlow("")
    val lockReason: StateFlow<String> = _lockReason.asStateFlow()

    private val _isWebviewLoaded = MutableStateFlow(false)
    val isWebviewLoaded: StateFlow<Boolean> = _isWebviewLoaded.asStateFlow()

    // Database integrations
    val allSessions: StateFlow<List<ExamSession>> = repository.allSessions
         .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSession: StateFlow<ExamSession?> = repository.activeSessionFlow
         .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Log tracking for active exam
    private val _activeSessionLogs = MutableStateFlow<List<InfractionLog>>(emptyList())
    val activeSessionLogs: StateFlow<List<InfractionLog>> = _activeSessionLogs.asStateFlow()

    // Student & Token Google Sheet database states
    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _tokens = MutableStateFlow<List<TokenInfo>>(emptyList())
    val tokens: StateFlow<List<TokenInfo>> = _tokens.asStateFlow()

    private val _loggedInStudent = MutableStateFlow<Student?>(null)
    val loggedInStudent: StateFlow<Student?> = _loggedInStudent.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _tokenError = MutableStateFlow<String?>(null)
    val tokenError: StateFlow<String?> = _tokenError.asStateFlow()

    private val _activeTokenInfo = MutableStateFlow<TokenInfo?>(null)
    val activeTokenInfo: StateFlow<TokenInfo?> = _activeTokenInfo.asStateFlow()

    private val _isLoadingSheets = MutableStateFlow(false)
    val isLoadingSheets: StateFlow<Boolean> = _isLoadingSheets.asStateFlow()

    init {
        // Load fallback storage initially so offline testing excels immediately
        _students.value = DefaultDb.parseStudents(DefaultDb.FALLBACK_DB_CSV)
        _tokens.value = DefaultDb.parseTokens(DefaultDb.FALLBACK_TOKEN_CSV)

        // Asynchronously fetch Google Sheets DB and TOKEN dynamic spreadsheets
        fetchSpreadsheets()

        // Automatically sync activeSessionId if database records an active uncompleted session
        viewModelScope.launch {
            repository.activeSessionFlow.collect { session ->
                if (session != null) {
                    _activeSessionId.value = session.id
                    // Start observing logs for this session
                    repository.getLogsForSession(session.id).collect { logs ->
                        _activeSessionLogs.value = logs
                    }
                } else {
                    _activeSessionId.value = null
                    _activeSessionLogs.value = emptyList()
                }
            }
        }
    }

    fun fetchSpreadsheets() {
        viewModelScope.launch {
            _isLoadingSheets.value = true
            try {
                val dbCsv = withContext(Dispatchers.IO) {
                    URL("https://docs.google.com/spreadsheets/d/1cZ5IAB8MZcI8sZAO1xs_QzwFpzzeB_gAmj4duRJKwrY/export?format=csv&gid=0")
                        .readText()
                }
                val parsedStudents = DefaultDb.parseStudents(dbCsv)
                if (parsedStudents.isNotEmpty()) {
                    _students.value = parsedStudents
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                val tokenCsv = withContext(Dispatchers.IO) {
                    URL("https://docs.google.com/spreadsheets/d/1cZ5IAB8MZcI8sZAO1xs_QzwFpzzeB_gAmj4duRJKwrY/export?format=csv&gid=566010147")
                        .readText()
                }
                val parsedTokens = DefaultDb.parseTokens(tokenCsv)
                if (parsedTokens.isNotEmpty()) {
                    _tokens.value = parsedTokens
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isLoadingSheets.value = false
        }
    }

    fun login(username: String, passwordEntered: String): Boolean {
        _loginError.value = null
        val found = _students.value.find { student ->
            student.username.equals(username.trim(), ignoreCase = true) &&
                    student.passwordInput.equals(passwordEntered.trim(), ignoreCase = true)
        }
        if (found != null) {
            _loggedInStudent.value = found
            _currentScreen.value = "LOBBY"
            return true
        } else {
            _loginError.value = "Username atau password salah! Sila periksa lembar DB."
            return false
        }
    }

    fun checkToken(tokenCode: String): Boolean {
        _tokenError.value = null
        _activeTokenInfo.value = null
        val trimmedCode = tokenCode.trim()
        val found = _tokens.value.find { info ->
            info.tokenCode.equals(trimmedCode, ignoreCase = true)
        }
        if (found != null) {
            _activeTokenInfo.value = found
            // Sync with current exam configurations
            _inputTitle.value = "Ujian ${found.subject}"
            
            // Format external target URL correctly
            var finalUrl = found.examUrl.trim()
            if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
                finalUrl = "https://$finalUrl"
            }
            _inputUrl.value = finalUrl
            return true
        } else {
            _tokenError.value = "Token tidak valid! Hubungi pengawas ujian."
            return false
        }
    }

    fun logout() {
        _loggedInStudent.value = null
        _activeTokenInfo.value = null
        _loginError.value = null
        _tokenError.value = null
        _currentScreen.value = "LOGIN"
    }

    fun setInputTitle(title: String) {
        _inputTitle.value = title
    }

    fun setInputUrl(url: String) {
        _inputUrl.value = url
    }

    fun setExaminerPassword(password: String) {
        _examinerPassword.value = password
    }

    fun setUseScreenPinning(enable: Boolean) {
        _useScreenPinning.value = enable
    }

    fun setRequirePasswordToUnlock(enable: Boolean) {
        _requirePasswordToUnlock.value = enable
    }

    fun selectSession(session: ExamSession) {
        _selectedSession.value = session
        _currentScreen.value = "SESSION_DETAIL"
        // Load details for selected session
        viewModelScope.launch {
            repository.getLogsForSession(session.id).collect { logs ->
                _selectedSessionLogs.value = logs
            }
        }
    }

    fun deleteSession(sessionId: Int) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_selectedSession.value?.id == sessionId) {
                _selectedSession.value = null
                _currentScreen.value = "LOBBY"
            }
        }
    }

    fun goBackToLobby() {
        _selectedSession.value = null
        _currentScreen.value = "LOBBY"
    }

    fun startExam() {
        viewModelScope.launch {
            val title = _inputTitle.value.ifBlank { "Ujian Tanpa Nama" }
            val url = _inputUrl.value.ifBlank { "https://forms.gle/" }
            val id = repository.startNewSession(title, url)
            _activeSessionId.value = id.toInt()
            _examLocked.value = false
            _lockReason.value = ""
            _isWebviewLoaded.value = false
            _currentScreen.value = "EXAM"
        }
    }

    fun forceRegisterActiveSession(sessionId: Int) {
        _activeSessionId.value = sessionId
    }

    fun triggerInfraction(type: String, details: String) {
        val sessionId = _activeSessionId.value ?: return
        
        // Prevent duplicate spam of same infraction in a short window
        val existingLogs = _activeSessionLogs.value
        if (existingLogs.isNotEmpty()) {
            val lastLog = existingLogs.first()
            if (lastLog.type == type && (System.currentTimeMillis() - lastLog.timestamp) < 3000) {
                // If it was logged within last 3 seconds of the exact same type, don't double log
                return
            }
        }

        viewModelScope.launch {
            repository.logInfraction(sessionId, type, details)
            // Lock the exam immediately to prevent cheating
            _examLocked.value = true
            _lockReason.value = details
        }
    }

    fun unlockExam(passwordEntered: String): Boolean {
        if (passwordEntered == _examinerPassword.value) {
            _examLocked.value = false
            _lockReason.value = ""
            return true
        }
        return false
    }

    fun completeExam() {
        val sessionId = _activeSessionId.value ?: return
        viewModelScope.launch {
            repository.completeActiveSession(sessionId)
            _activeSessionId.value = null
            _examLocked.value = false
            _lockReason.value = ""
            _currentScreen.value = "LOBBY"
        }
    }
}

class ExamViewModelFactory(private val repository: ExamRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExamViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExamViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
