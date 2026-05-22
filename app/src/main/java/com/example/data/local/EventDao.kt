package com.example.data.local

import androidx.room.*
import com.example.data.model.EventReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM event_reminders")
    fun getAllReminders(): Flow<List<EventReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: EventReminder)

    @Query("SELECT * FROM event_reminders WHERE eventId = :eventId")
    suspend fun getReminderById(eventId: String): EventReminder?
}
