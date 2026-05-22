package com.example.data.local

import androidx.room.*
import com.example.data.model.ChurchCampus
import kotlinx.coroutines.flow.Flow

@Dao
interface CampusDao {
    @Query("SELECT * FROM campuses ORDER BY id ASC")
    fun getAllCampuses(): Flow<List<ChurchCampus>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampus(campus: ChurchCampus)

    @Update
    suspend fun updateCampus(campus: ChurchCampus)

    @Delete
    suspend fun deleteCampus(campus: ChurchCampus)

    @Query("SELECT COUNT(*) FROM campuses")
    suspend fun getCount(): Int
}
