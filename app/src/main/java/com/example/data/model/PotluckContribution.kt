package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "potluck_contributions")
data class PotluckContribution(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contributorName: String,
    val dishName: String,
    val category: String, // "Main Dish", "Side Dish", "Salad", "Dessert", "Drinks"
    val notes: String = "",
    val servings: Int = 8
)
