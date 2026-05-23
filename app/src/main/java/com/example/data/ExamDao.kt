package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Query("SELECT * FROM exam_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<ExamSession>>

    @Query("SELECT * FROM exam_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Int): ExamSession?

    @Query("SELECT * FROM exam_sessions WHERE isCompleted = 0 LIMIT 1")
    fun getActiveSessionFlow(): Flow<ExamSession?>

    @Query("SELECT * FROM exam_sessions WHERE isCompleted = 0 LIMIT 1")
    suspend fun getActiveSession(): ExamSession?

    @Query("SELECT * FROM infraction_logs WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getLogsForSession(sessionId: Int): Flow<List<InfractionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ExamSession): Long

    @Update
    suspend fun updateSession(session: ExamSession)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInfraction(log: InfractionLog)

    @Query("DELETE FROM exam_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Int)

    @Query("DELETE FROM infraction_logs WHERE sessionId = :sessionId")
    suspend fun deleteLogsBySessionId(sessionId: Int)

    @Transaction
    suspend fun deleteSessionAndLogs(sessionId: Int) {
        deleteLogsBySessionId(sessionId)
        deleteSessionById(sessionId)
    }

    @Transaction
    suspend fun addInfractionAndIncrementCounter(log: InfractionLog) {
        insertInfraction(log)
        val session = getSessionById(log.sessionId)
        if (session != null) {
            updateSession(session.copy(infractionsCount = session.infractionsCount + 1))
        }
    }
}
