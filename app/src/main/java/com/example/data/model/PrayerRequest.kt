package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_requests")
data class PrayerRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val category: String, // "Healing", "Faith", "Guidance", "Family", "General"
    val requester: String, // User's name or "Anonymous"
    val timestamp: Long = System.currentTimeMillis(),
    val prayerCount: Int = 0,
    val isUserSubmitted: Boolean = false,
    val hasUserPrayed: Boolean = false
)
