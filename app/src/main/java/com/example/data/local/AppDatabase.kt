package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.PrayerRequest
import com.example.data.model.EventReminder
import com.example.data.model.PotluckContribution
import com.example.data.model.ChurchCampus
import com.example.utils.LocationCoordinates

@Database(
    entities = [
        PrayerRequest::class,
        EventReminder::class,
        PotluckContribution::class,
        LocationCoordinates::class,
        ChurchCampus::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prayerDao(): PrayerDao
    abstract fun eventDao(): EventDao
    abstract fun potluckDao(): PotluckDao
    abstract fun locationDao(): LocationDao
    abstract fun campusDao(): CampusDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "grace_covenant_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
