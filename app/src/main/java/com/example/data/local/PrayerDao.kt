package com.example.data.local

import androidx.room.*
import com.example.data.model.PrayerRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayer_requests ORDER BY timestamp DESC")
    fun getAllPrayers(): Flow<List<PrayerRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayer(prayer: PrayerRequest)

    @Update
    suspend fun updatePrayer(prayer: PrayerRequest)

    @Query("SELECT * FROM prayer_requests WHERE id = :id")
    suspend fun getPrayerById(id: Int): PrayerRequest?

    @Query("DELETE FROM prayer_requests WHERE id = :id")
    suspend fun deletePrayerById(id: Int)

    @Query("SELECT COUNT(*) FROM prayer_requests")
    suspend fun getCount(): Int
}
