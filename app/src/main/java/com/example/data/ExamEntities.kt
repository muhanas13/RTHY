package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_sessions")
data class ExamSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val infractionsCount: Int = 0,
    val isCompleted: Boolean = false
)

@Entity(tableName = "infraction_logs")
data class InfractionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val type: String, // "BACKGROUND", "LOST_FOCUS", "SPLIT_SCREEN", "UNPINNED"
    val timestamp: Long = System.currentTimeMillis(),
    val details: String
)
