package com.example.data.local

import androidx.room.*
import com.example.data.model.PotluckContribution
import kotlinx.coroutines.flow.Flow

@Dao
interface PotluckDao {
    @Query("SELECT * FROM potluck_contributions ORDER BY id DESC")
    fun getAllContributions(): Flow<List<PotluckContribution>>

    @Query("SELECT COUNT(*) FROM potluck_contributions")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contribution: PotluckContribution)

    @Delete
    suspend fun deleteContribution(contribution: PotluckContribution)
}
