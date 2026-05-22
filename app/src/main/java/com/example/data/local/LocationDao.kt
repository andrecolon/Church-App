package com.example.data.local

import androidx.room.*
import com.example.utils.LocationCoordinates
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations ORDER BY cityName ASC")
    fun getAllLocations(): Flow<List<LocationCoordinates>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationCoordinates)

    @Update
    suspend fun updateLocation(location: LocationCoordinates)

    @Delete
    suspend fun deleteLocation(location: LocationCoordinates)

    @Query("SELECT COUNT(*) FROM locations")
    suspend fun getCount(): Int
}
