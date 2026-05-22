package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "campuses")
data class ChurchCampus(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val address: String,
    val coordinates: String,
    val phone: String,
    val studyTime: String,
    val worshipTime: String,
    val details: String
)
