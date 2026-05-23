package com.example.data

import kotlinx.coroutines.flow.Flow

class ExamRepository(private val examDao: ExamDao) {
    val allSessions: Flow<List<ExamSession>> = examDao.getAllSessions()
    val activeSessionFlow: Flow<ExamSession?> = examDao.getActiveSessionFlow()

    suspend fun getActiveSession(): ExamSession? = examDao.getActiveSession()

    suspend fun getSessionById(id: Int): ExamSession? = examDao.getSessionById(id)

    fun getLogsForSession(sessionId: Int): Flow<List<InfractionLog>> = examDao.getLogsForSession(sessionId)

    suspend fun startNewSession(title: String, url: String): Long {
        val newSession = ExamSession(
            title = title,
            url = url,
            startTime = System.currentTimeMillis()
        )
        return examDao.insertSession(newSession)
    }

    suspend fun completeActiveSession(sessionId: Int) {
        val session = examDao.getSessionById(sessionId)
        if (session != null) {
            examDao.updateSession(
                session.copy(
                    isCompleted = true,
                    endTime = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun logInfraction(sessionId: Int, type: String, details: String) {
        val log = InfractionLog(
            sessionId = sessionId,
            type = type,
            details = details
        )
        examDao.addInfractionAndIncrementCounter(log)
    }

    suspend fun deleteSession(sessionId: Int) {
        examDao.deleteSessionAndLogs(sessionId)
    }
}
