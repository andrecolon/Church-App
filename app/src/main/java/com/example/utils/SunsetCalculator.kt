package com.example.utils

import java.util.Calendar
import kotlin.math.*
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationCoordinates(
    val cityName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneOffsetHours: Int,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)

object SunsetCalculator {
    val locations = listOf(
        LocationCoordinates("Yerushalayim", "Israel", 31.7683, 35.2137, 3),
        LocationCoordinates("New York", "USA", 40.7128, -74.0060, -4),
        LocationCoordinates("Los Angeles", "USA", 34.0522, -118.2437, -7),
 )

    fun getSabbathDays(calendar: Calendar = Calendar.getInstance()): Pair<Calendar, Calendar> {
        val friday = calendar.clone() as Calendar
        while (friday.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
            friday.add(Calendar.DAY_OF_YEAR, 1)
        }
        val saturday = friday.clone() as Calendar
        saturday.add(Calendar.DAY_OF_YEAR, 1)
        return Pair(friday, saturday)
    }

    fun calculateSunset(lat: Double, lng: Double, timezoneOffset: Int, calendar: Calendar): String {
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        
        // Convert latitude and longitude to radians
        val latRad = Math.toRadians(lat)
        
        // Calculate solar declination (radians)
        // declination = 23.45 * sin(360/365 * (284 + dayOfYear))
        val decDegrees = 23.45 * sin(Math.toRadians(360.0 / 365.0 * (284 + dayOfYear)))
        val decRad = Math.toRadians(decDegrees)
        
        // Sunset hour angle: cos(H) = (sin(-0.833 degrees) - sin(lat)*sin(dec)) / (cos(lat)*dec)
        val sinMinus083 = sin(Math.toRadians(-0.833))
        
        val denom = cos(latRad) * cos(decRad)
        if (denom == 0.0) return "6:00 PM" // Fallback safety
        
        val cosH = (sinMinus083 - sin(latRad) * sin(decRad)) / denom
        
        if (cosH > 1.0) {
            return "No Sunset (Polar Night)"
        } else if (cosH < -1.0) {
            return "No Sunrise (Polar Day)"
        }
        
        val hRad = acos(cosH)
        val hDegrees = Math.toDegrees(hRad)
        val localHourAngleOffset = hDegrees / 15.0 // Convert degrees to solar hours
        
        // Solar noon time estimates standard 12:00
        val localNoonGmt = 12.0 - (lng / 15.0)
        val sunsetGmt = localNoonGmt + localHourAngleOffset
        
        // Convert to local timezone
        var localSunsetHour = sunsetGmt + timezoneOffset
        while (localSunsetHour < 0) localSunsetHour += 24.0
        while (localSunsetHour >= 24) localSunsetHour -= 24.0
        
        val hour = localSunsetHour.toInt()
        val minutesDecimal = (localSunsetHour - hour) * 60
        val minutes = round(minutesDecimal).toInt()
        
        val finalHour: Int
        val finalMin: Int
        if (minutes >= 60) {
            finalHour = (hour + 1) % 24
            finalMin = minutes - 60
        } else {
            finalHour = hour
            finalMin = minutes
        }
        
        val ampm = if (finalHour >= 12) "PM" else "AM"
        val displayHr = when {
            finalHour == 0 -> 12
            finalHour > 12 -> finalHour - 12
            else -> finalHour
        }
        
        return String.format("%d:%02d %s", displayHr, finalMin, ampm)
    }
}
