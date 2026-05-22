package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event_reminders")
data class EventReminder(
    @PrimaryKey val eventId: String,
    val isAttending: Boolean = false,
    val reminderSet: Boolean = false,
    val customNotes: String = ""
)
