package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarSystem
import com.example.data.model.BiblicalHoliday

// Color definitions that align with ChurchAppScreen's theme
val GoldenAmber = Color(0xFFFFD700)
val DeepCharcoal = Color(0xFF1E1E1E)
val LightSlate = Color(0xFF2C2C2C)
val CoralRed = Color(0xFFFF6B6B)
val CalmTeal = Color(0xFF20B2AA)

data class MayCalendarDay(
    val dayNumber: Int,
    val isSabbath: Boolean,
    val isFeast: Boolean,
    val isNewMoon: Boolean,
    val label: String,
    val hebrewDate: String,
    val notes: String
)

@Composable
fun CalendarSystemSelector(
    selectedSystem: CalendarSystem,
    onSystemSelect: (CalendarSystem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calendar_system_selector_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Biblical Calendar Selector",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = "The congregation highlights historical calendar reckonings for Moedim (appointed times) to encourage learning and research of historical Second Temple texts.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 16.sp
            )

            // Selector row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple(CalendarSystem.HILLEL, "Hillel (Traditional)", "calendar_hillel"),
                    Triple(CalendarSystem.ZADOK, "Zadok (Solar 364)", "calendar_zadok"),
                    Triple(CalendarSystem.CONJUNCTION, "Conjunction (Astronomical)", "calendar_conjunction")
                ).forEach { (system, label, tag) ->
                    val isSelected = selectedSystem == system
                    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    val strokeColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(backgroundColor, RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, strokeColor), RoundedCornerShape(12.dp))
                            .clickable { onSystemSelect(system) }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                            .testTag(tag),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = when (system) {
                                    CalendarSystem.HILLEL -> Icons.Default.DateRange
                                    CalendarSystem.ZADOK -> Icons.Default.Brightness5
                                    CalendarSystem.CONJUNCTION -> Icons.Default.Brightness3
                                },
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = contentColor,
                                textAlign = TextAlign.Center,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarInfoCard(selectedSystem: CalendarSystem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calendar_info_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "About Calendar",
                tint = GoldenAmber,
                modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val title = when (selectedSystem) {
                    CalendarSystem.HILLEL -> "Hillel II Calculated Calendar"
                    CalendarSystem.ZADOK -> "Zadok Solar Priestly Calendar"
                    CalendarSystem.CONJUNCTION -> "Celestial Lunar Conjunction"
                }

                val description = when (selectedSystem) {
                    CalendarSystem.HILLEL -> "Established around 358 CE by Hillel II. A calculated lunisolar calendar that uses the Metonic cycle to keep months in sync with seasons. It adds leap months in specific intervals and introduces postponement rules (like avoiding Sabbath back-to-back holidays)."
                    CalendarSystem.ZADOK -> "Drawn from Enoch & Jubilees as recorded in the Dead Sea Scrolls. A strictly solar calendar of 364 days. Each year starts on a Wednesday in Spring (after the Solstice), divided into 13-week quarters. Feasts and Sabbaths always fall on the exact same days of the week every year."
                    CalendarSystem.CONJUNCTION -> "Determines biblical month starts strictly based on direct astronomical alignment (moon conjunction point / dark moon phase) in Jerusalem, free from Rabbinic postponements. It is used by astronomical caretakers and Karaites who observe lunar-solar celestial clocks directly."
                }

                val rules = when (selectedSystem) {
                    CalendarSystem.HILLEL -> "• Year cycle: 19-year Metonic cycle\n• Month start: Calculated Molad (mean conjunction)\n• Postponements: Yes (avoid Sunday/Wednesday/Friday Rosh Teruah)"
                    CalendarSystem.ZADOK -> "• Year cycle: 364 days (fixed 52 weeks)\n• Month start: Solar quarterly markers (always Wed)\n• Postponements: None (Perfect structural constancy)"
                    CalendarSystem.CONJUNCTION -> "• Year cycle: Observed Spring barley (Aviv) start\n• Month start: Actual Astronomical Conjunction (Jerusalem)\n• Postponements: None (Celestial-solar raw readings)"
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = rules,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    lineHeight = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun InteractiveCalendarMonthGrid(
    selectedSystem: CalendarSystem
) {
    val days = remember(selectedSystem) { getMay2026DaysForSystem(selectedSystem) }
    var selectedDayNum by remember { mutableStateOf(22) } // Default to May 22 (Pentecost Eve)
    val selectedDay = days.firstOrNull { it.dayNumber == selectedDayNum } ?: days[21]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("interactive_calendar_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Calendar Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Interactive Study Grid",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Viewing: May 2026 (Gregorian Matching)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(10.dp).background(GoldenAmber, CircleShape))
                        Text("Feast", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(2.dp)).border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                        Text("Sabbath", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
            }

            // Days of the week headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach { dayName ->
                    Text(
                        text = dayName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dayName == "SAT") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Calendar Days Layout
            // May 1, 2026 is Friday. Empty count = 5.
            val emptyPrefixCount = 5
            val totalCells = emptyPrefixCount + 31
            val rowsCount = (totalCells + 6) / 7

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (rowIndex in 0 until rowsCount) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (colIndex in 0..6) {
                            val cellIndex = rowIndex * 7 + colIndex
                            val dayNumber = cellIndex - emptyPrefixCount + 1

                            if (dayNumber in 1..31) {
                                val dayData = days[dayNumber - 1]
                                val isSelected = dayNumber == selectedDayNum

                                val borderStroke = when {
                                    isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
                                    dayData.isSabbath -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                    dayData.isNewMoon -> BorderStroke(1.dp, CalmTeal.copy(alpha = 0.5f))
                                    else -> BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                }

                                val containerColor = when {
                                    dayData.isFeast -> GoldenAmber
                                    dayData.isSabbath -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    dayData.isNewMoon -> CalmTeal.copy(alpha = 0.12f)
                                    else -> Color.Transparent
                                }

                                val contentColor = when {
                                    dayData.isFeast -> Color.Black
                                    dayData.isSabbath -> MaterialTheme.colorScheme.primary
                                    dayData.isNewMoon -> CalmTeal
                                    else -> MaterialTheme.colorScheme.onSurface
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(containerColor, RoundedCornerShape(8.dp))
                                        .border(borderStroke, RoundedCornerShape(8.dp))
                                        .clickable { selectedDayNum = dayNumber }
                                        .testTag("calendar_day_$dayNumber"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = dayNumber.toString(),
                                            fontSize = 11.sp,
                                            fontWeight = if (dayData.isFeast || dayData.isSabbath || isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                            color = contentColor
                                        )
                                        
                                        // Specific subtle indicators
                                        if (dayData.isNewMoon && !dayData.isFeast) {
                                            Box(modifier = Modifier.size(3.dp).background(CalmTeal, CircleShape))
                                        } else if (dayData.isFeast) {
                                            Box(modifier = Modifier.size(3.dp).background(Color.Black, CircleShape))
                                        }
                                    }
                                }
                            } else {
                                // Empty spacer cell
                                Spacer(modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), thickness = 1.dp)

            // Day details expanded card
            AnimatedContent(
                targetState = selectedDay,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "DayDetailsAnim"
            ) { targetDay ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "May ${targetDay.dayNumber}, 2026",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Box(
                            modifier = Modifier
                                .background(
                                    when {
                                        targetDay.isFeast -> GoldenAmber.copy(alpha = 0.2f)
                                        targetDay.isSabbath -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        targetDay.isNewMoon -> CalmTeal.copy(alpha = 0.1f)
                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = targetDay.label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    targetDay.isFeast -> Color(0xFFE65100)
                                    targetDay.isSabbath -> MaterialTheme.colorScheme.primary
                                    targetDay.isNewMoon -> CalmTeal
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Hebrew date",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Reckoned Date: ${targetDay.hebrewDate}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = targetDay.notes,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

fun getMay2026DaysForSystem(system: CalendarSystem): List<MayCalendarDay> {
    val days = mutableListOf<MayCalendarDay>()
    for (day in 1..31) {
        val dayOfWeek = (day + 4) % 7 // May 1, 2026 is Friday (5).
        // 0: Sunday, 1: Monday, 2: Tuesday, 3: Wednesday, 4: Thursday, 5: Friday, 6: Saturday
        val isSabbath = (dayOfWeek == 6)

        var isFeast = false
        var isNewMoon = false
        var label = "Weekday"
        var hebrewDate = ""
        var notes = ""

        when (system) {
            CalendarSystem.ZADOK -> {
                // March 25 is Month 1 Day 1.
                // Month 2 Day 1 is April 24.
                // May 1 is Month 2 Day 8.
                // May 23 is Month 2 Day 30.
                // May 24 is Month 3 Day 1.
                val zadokMonth: Int
                val zadokDay: Int
                if (day <= 23) {
                    zadokMonth = 2
                    zadokDay = day + 7
                } else {
                    zadokMonth = 3
                    zadokDay = day - 23
                }

                hebrewDate = "Month $zadokMonth, Day $zadokDay"
                if (zadokDay == 1) {
                    isNewMoon = true
                    label = "Zadok New Month 3"
                    notes = "Monthly marker. In Zadok priestly solar calendar, the entrance of Month 3 is celebrated as a seasonal marker of the gate rotation courses."
                } else if (isSabbath) {
                    label = "Weekly Sabbath"
                    notes = "The seventh-day of rest. Under the solar 364 priestly order, this Sabbath is in direct constant week continuity."
                } else {
                    label = "Weekday"
                    notes = "A regular preparation or travel watch under the Second Temple Zadokite priestly courses."
                }
            }
            CalendarSystem.HILLEL -> {
                // Iyar ends May 16 (Iyar 29). May 17 is Sivan 1.
                // Sivan 6 is Shavuot: May 22 is Sivan 6. May 23 is Sivan 7. May 24 is Sivan 8.
                val isSivan = day >= 17
                val hillelMonth = if (isSivan) "Sivan" else "Iyar"
                val hillelDay = if (isSivan) day - 16 else day + 13

                hebrewDate = "$hillelMonth $hillelDay"

                if (hillelMonth == "Sivan" && hillelDay in 6..8) {
                    isFeast = true
                    label = "Shavuot (Feast of Weeks)"
                    notes = "Pentecost! Celebrating the Sinai Torah giving and Acts 2 Pentecost Holy Spirit outpouring. Staying up in Bible scroll study is direct traditional practice."
                } else if (hillelMonth == "Sivan" && hillelDay == 1) {
                    isNewMoon = true
                    label = "Rosh Chodesh Sivan"
                    notes = "The New Moon of Sivan, calculated using modern mathematical approximations of Hillel II's Metonic cycle rules."
                } else if (isSabbath) {
                    label = "Weekly Sabbath"
                    notes = "The holy 7th day Sabbath, sanctified from creation."
                } else {
                    label = "Weekday"
                    notes = "A standard biblical workday under lunar-observation-assisted weeks."
                }
            }
            CalendarSystem.CONJUNCTION -> {
                // Conjunction-based. Nissan is April 1st.
                // Iyar starts at celestial conjunction (approx April 17). Sivan starts celestial conjunction (approx May 16).
                // Sivan 1 is May 16. Sivan 6 is May 21 or May 22. Let's showcase Shavuot on Sivan 6 (May 21).
                val isSivan = day >= 16
                val lunarMonth = if (isSivan) "Sivan" else "Iyar"
                val lunarDay = if (isSivan) day - 15 else day + 14

                hebrewDate = "$lunarMonth $lunarDay"

                if (lunarMonth == "Sivan" && lunarDay == 6) {
                    isFeast = true
                    label = "Shavuot (Astronomical)"
                    notes = "Shavuot computed exactly 50 days from the Wave Sheaf (First Fruits) based strictly on astronomical moon alignment."
                } else if (lunarMonth == "Sivan" && lunarDay == 1) {
                    isNewMoon = true
                    label = "Celestial New Moon"
                    notes = "The lunar month starts exactly at the astronomical conjunction point in Jerusalem, free from Rabbinic mathematical postponing shifts."
                } else if (isSabbath) {
                    label = "Weekly Sabbath"
                    notes = "The weekly seventh-day Sabbath in continuous physical count."
                } else {
                    label = "Weekday"
                    notes = "A regular weekday under observed lunar calendars."
                }
            }
        }
        days.add(MayCalendarDay(day, isSabbath, isFeast, isNewMoon, label, hebrewDate, notes))
    }
    return days
}

fun getHolidaysForSystem(system: CalendarSystem, baseHolidays: List<BiblicalHoliday>): List<BiblicalHoliday> {
    return baseHolidays.map { holiday ->
        when (system) {
            CalendarSystem.ZADOK -> {
                val newDate = when (holiday.id) {
                    "h1" -> "April 7 - April 8, 2026"
                    "h2" -> "April 8 - April 14, 2026"
                    "h3" -> "June 7, 2026"
                    "h4" -> "September 23, 2026"
                    "h5" -> "October 2, 2026"
                    "h6" -> "October 7 - October 14, 2026"
                    else -> holiday.dateRange2026
                }
                val newBiblical = when (holiday.id) {
                    "h1" -> "14th of Month 1"
                    "h2" -> "15th - 21st of Month 1"
                    "h3" -> "15th of Month 3"
                    "h4" -> "1st of Month 7"
                    "h5" -> "10th of Month 7"
                    "h6" -> "15th - 22nd of Month 7"
                    else -> holiday.biblicalDate
                }
                holiday.copy(dateRange2026 = newDate, biblicalDate = newBiblical)
            }
            CalendarSystem.CONJUNCTION -> {
                val newDate = when (holiday.id) {
                    "h1" -> "April 1 - April 2, 2026"
                    "h2" -> "April 2 - April 8, 2026"
                    "h3" -> "May 18, 2026"
                    "h4" -> "September 11, 2026"
                    "h5" -> "September 20, 2026"
                    "h6" -> "September 25 - October 2, 2026"
                    else -> holiday.dateRange2026
                }
                val newBiblical = when (holiday.id) {
                    "h1" -> "14th of Nisan (Conjunction)"
                    "h2" -> "15th - 21st of Nisan (Conjunction)"
                    "h3" -> "6th of Sivan (Conjunction)"
                    "h4" -> "1st of Tishrei (Conjunction)"
                    "h5" -> "10th of Tishrei (Conjunction)"
                    "h6" -> "15th - 22nd of Tishrei (Conjunction)"
                    else -> holiday.biblicalDate
                }
                holiday.copy(dateRange2026 = newDate, biblicalDate = newBiblical)
            }
            CalendarSystem.HILLEL -> holiday
        }
    }
}
