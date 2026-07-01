package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.viewmodel.ChurchViewModel
import com.example.utils.LocationCoordinates
import com.example.utils.SunsetCalculator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Common Style Definitions so colors exist local to screen compilation
val SabbathGold = Color(0xFFFFD700)
val SoftIvoryText = Color(0xFFFDFBF7)

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChurchAppScreen(viewModel: ChurchViewModel, modifier: Modifier = Modifier) {
    var activeTab by remember { mutableStateOf(0) } // 0: Sabbath, 1: Potluck, 2: Bible Library, 3: Locations Map, 4: Prayer & Contacts

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                val tabs = listOf(
                    NavigationBarItemData("Sabbath", Icons.Default.Home, "sabbath_tab"),
                    NavigationBarItemData("Potluck", Icons.Default.ShoppingCart, "potluck_tab"),
                    NavigationBarItemData("Bible Library", Icons.Default.Book, "bible_tab"),
                    NavigationBarItemData("Locations", Icons.Default.LocationOn, "locations_tab"),
                    NavigationBarItemData("Prayer & Contact", Icons.Default.Favorite, "prayer_contact_tab")
                )

                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
                        modifier = Modifier.testTag(tab.testTag),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                0 -> SabbathTabScreen(viewModel)
                1 -> PotluckTabScreen(viewModel)
                2 -> BibleTabScreen(viewModel)
                3 -> LocationsMapTabScreen(viewModel)
                4 -> PrayerAndContactsTabScreen(viewModel)
            }
        }
    }
}

data class NavigationBarItemData(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)

// ==========================================
// SCREEN 1: SABBATH & HOME CENTRAL
// ==========================================

@Composable
fun SabbathTabScreen(viewModel: ChurchViewModel) {
    val location by viewModel.selectedLocation.collectAsStateWithLifecycle()
    val selectedCalendar by viewModel.selectedCalendar.collectAsStateWithLifecycle()
    val dynamicHolidays = remember(selectedCalendar) { getHolidaysForSystem(selectedCalendar, viewModel.holidays) }
    var showLocationSelector by remember { mutableStateOf(false) }

    val currentTime = Date()
    val cal = Calendar.getInstance().apply {
        time = currentTime
    }

    val sabbathDays = SunsetCalculator.getSabbathDays(cal)
    val fridayCal = sabbathDays.first
    val saturdayCal = sabbathDays.second

    val fridaySunsetStr = SunsetCalculator.calculateSunset(
        location.latitude, location.longitude, location.timezoneOffsetHours, fridayCal
    )
    val saturdaySunsetStr = SunsetCalculator.calculateSunset(
        location.latitude, location.longitude, location.timezoneOffsetHours, saturdayCal
    )

    val dateFormatter = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())

    val sdfSunsetTime = SimpleDateFormat("h:mm a", Locale.getDefault())
    val parsedFridaySunset = try { sdfSunsetTime.parse(fridaySunsetStr) } catch(e: Exception) { null }
    val parsedSaturdaySunset = try { sdfSunsetTime.parse(saturdaySunsetStr) } catch(e: Exception) { null }

    val friSunsetCal = fridayCal.clone() as Calendar
    if (parsedFridaySunset != null) {
        val sunsetHourMin = Calendar.getInstance().apply { time = parsedFridaySunset }
        friSunsetCal.set(Calendar.HOUR_OF_DAY, sunsetHourMin.get(Calendar.HOUR_OF_DAY))
        friSunsetCal.set(Calendar.MINUTE, sunsetHourMin.get(Calendar.MINUTE))
        friSunsetCal.set(Calendar.SECOND, 0)
    } else {
        friSunsetCal.set(Calendar.HOUR_OF_DAY, 19)
        friSunsetCal.set(Calendar.MINUTE, 0)
    }

    val satSunsetCal = saturdayCal.clone() as Calendar
    if (parsedSaturdaySunset != null) {
        val sunsetHourMin = Calendar.getInstance().apply { time = parsedSaturdaySunset }
        satSunsetCal.set(Calendar.HOUR_OF_DAY, sunsetHourMin.get(Calendar.HOUR_OF_DAY))
        satSunsetCal.set(Calendar.MINUTE, sunsetHourMin.get(Calendar.MINUTE))
        satSunsetCal.set(Calendar.SECOND, 0)
    } else {
        satSunsetCal.set(Calendar.HOUR_OF_DAY, 19)
        satSunsetCal.set(Calendar.MINUTE, 0)
    }

    val isSabbathActive = cal.after(friSunsetCal) && cal.before(satSunsetCal)

    val diffMillis = if (cal.before(friSunsetCal)) {
        friSunsetCal.timeInMillis - cal.timeInMillis
    } else if (isSabbathActive) {
        satSunsetCal.timeInMillis - cal.timeInMillis
    } else {
        val nextFriday = friSunsetCal.clone() as Calendar
        nextFriday.add(Calendar.DAY_OF_YEAR, 7)
        nextFriday.timeInMillis - cal.timeInMillis
    }

    val hoursLeft = diffMillis / (1000 * 60 * 60)
    val minsLeft = (diffMillis % (1000 * 60 * 60)) / (1000 * 60)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "T.O.B.Y.",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = "Grace and Peace • " + SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(currentTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                
                IconButton(
                    onClick = { showLocationSelector = true },
                    modifier = Modifier.testTag("change_location_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Select Location",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        item {
            SabbathWidget(
                isSabbathActive = isSabbathActive,
                locationName = location.cityName,
                country = location.country,
                fridaySunset = fridaySunsetStr,
                saturdaySunset = saturdaySunsetStr,
                hoursLeft = hoursLeft,
                minsLeft = minsLeft,
                onLocationClick = { showLocationSelector = true }
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About Sabbath",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "A Sanctuary in Time",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "From Friday sunset to Saturday sunset, the Sabbath calls us to lay aside spatial labor and dwell together in sacred, quiet time. Connect with your community and your Maker.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        item {
            CalendarSystemSelector(
                selectedSystem = selectedCalendar,
                onSystemSelect = { viewModel.selectCalendar(it) }
            )
        }

        item {
            CalendarInfoCard(selectedSystem = selectedCalendar)
        }

        item {
            InteractiveCalendarMonthGrid(selectedSystem = selectedCalendar)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sacred Biblical Moedim (Holidays)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Year 2026",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Black
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, SabbathGold)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Star, "Holy day alert", tint = SabbathGold)
                            Text(
                                text = "Moed Highlight: Feast of Weeks (Shavuot)",
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val shavuotHighlight = when (selectedCalendar) {
                            CalendarSystem.HILLEL -> "Shavuot begins TODAY (May 22, 2026)! This celebrates the Sinai Torah giving and Acts 2 Pentecost Spirit outpouring. Read the Book of Ruth and study the Bible Scroll library!"
                            CalendarSystem.ZADOK -> "Shavuot is upcoming on Sunday, June 7, 2026 (Month 3, Day 15)! This solar date represents the historic oath covenants in Qumran-Essene records. Study the scrolls in our Bible Library!"
                            CalendarSystem.CONJUNCTION -> "Shavuot has passed on Monday, May 18, 2026! Computed exactly 50 days from Wave Sheaf after the astronomical dark moon conjunction. Dive into study of astronomical cycles!"
                        }
                        
                        Text(
                            text = shavuotHighlight,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(dynamicHolidays) { holiday ->
                        HolidayCard(holiday = holiday)
                    }
                }
            }
        }
    }

    if (showLocationSelector) {
        LocationSelectorDialog(
            viewModel = viewModel,
            currentSelected = location,
            onSelect = {
                viewModel.selectLocation(it)
                showLocationSelector = false
            },
            onDismiss = { showLocationSelector = false }
        )
    }
}

@Composable
fun SabbathWidget(
    isSabbathActive: Boolean,
    locationName: String,
    country: String,
    fridaySunset: String,
    saturdaySunset: String,
    hoursLeft: Long,
    minsLeft: Long,
    onLocationClick: () -> Unit
) {
    val gradientBrush = if (isSabbathActive) {
        Brush.verticalGradient(listOf(Color(0xFF0F1A30), Color(0xFF1E2F5A)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF2E1C0C), Color(0xFF4E3015)))
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLocationClick() }
            .testTag("sabbath_tracking_widget")
    ) {
        Box(
            modifier = Modifier
                .background(gradientBrush)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isSabbathActive) Icons.Default.CheckCircle else Icons.Default.Star,
                            contentDescription = "Status",
                            tint = SabbathGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (isSabbathActive) "Holy Sabbath Rest Active" else "Countdown to Sabbath",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$locationName, $country",
                            color = SoftIvoryText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isSabbathActive) "Time Remaining in Rest:" else "Time until Sabbath begins:",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format("%02d", hoursLeft),
                                color = SabbathGold,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "h ",
                                color = Color.White,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Text(
                                text = String.format("%02d", minsLeft),
                                color = SabbathGold,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "m",
                                color = Color.White,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.size(76.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val sweepRate = if (isSabbathActive) {
                            (hoursLeft * 60 + minsLeft).toFloat() / (24 * 60)
                        } else {
                            (hoursLeft * 60 + minsLeft).toFloat() / (6 * 24 * 60)
                        }
                        
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.1f),
                                style = Stroke(width = 6.dp.toPx())
                            )
                            drawArc(
                                color = SabbathGold,
                                startAngle = -90f,
                                sweepAngle = sweepRate * 360f,
                                useCenter = false,
                                style = Stroke(width = 6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                            )
                        }
                        Icon(
                            imageVector = if (isSabbathActive) Icons.Default.Favorite else Icons.Default.Refresh,
                            contentDescription = "Timer Icon",
                            tint = SabbathGold,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.12f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "FRI SUNSET",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = fridaySunset,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SAT SUNSET",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = saturdaySunset,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HolidayCard(holiday: BiblicalHoliday) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = holiday.name,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = holiday.hebrewName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontStyle = FontStyle.Italic
                    )
                }
                
                Box(
                    modifier = Modifier
                        .background(
                            if (holiday.season == "Spring") Color(0xFFC8E6C9) else Color(0xFFFFCC80),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = holiday.season,
                        color = if (holiday.season == "Spring") Color(0xFF1B5E20) else Color(0xFFE65100),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Text(
                text = "${holiday.biblicalDate} | 2026: ${holiday.dateRange2026}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Text(
                text = holiday.significance,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Practice",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = holiday.practice,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun LocationSelectorDialog(
    viewModel: ChurchViewModel,
    currentSelected: LocationCoordinates,
    onSelect: (LocationCoordinates) -> Unit,
    onDismiss: () -> Unit
) {
    val locations by viewModel.locationsList.collectAsStateWithLifecycle()

    var customCityName by remember { mutableStateOf("") }
    var customCountry by remember { mutableStateOf("") }
    var customLat by remember { mutableStateOf("") }
    var customLng by remember { mutableStateOf("") }
    var customTz by remember { mutableStateOf("") }

    var editingLocation by remember { mutableStateOf<LocationCoordinates?>(null) }
    var showError by remember { mutableStateOf(false) }
    var deleteConfirmLocation by remember { mutableStateOf<LocationCoordinates?>(null) }

    val startEditing: (LocationCoordinates) -> Unit = { loc ->
        editingLocation = loc
        customCityName = loc.cityName
        customCountry = loc.country
        customLat = loc.latitude.toString()
        customLng = loc.longitude.toString()
        customTz = loc.timezoneOffsetHours.toString()
        showError = false
    }

    val clearFields = {
        editingLocation = null
        customCityName = ""
        customCountry = ""
        customLat = ""
        customLng = ""
        customTz = ""
        showError = false
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Global Sunset Meridian",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Serif
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_location_dialog")
                    ) {
                        Icon(Icons.Default.Close, "Dismiss")
                    }
                }

                Text(
                    text = "Select, add, modify, or delete coordinates to recalculate Sabbath sunset rest times dynamically:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )

                // Deletion Confirmation Banner
                if (deleteConfirmLocation != null) {
                    val target = deleteConfirmLocation!!
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Delete ${target.cityName} permanently?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { deleteConfirmLocation = null }) {
                                    Text("Cancel", color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    onClick = {
                                        viewModel.deleteLocation(target)
                                        deleteConfirmLocation = null
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Confirm Delete", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Scrollable List of locations
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .heightIn(max = 220.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    locations.forEach { location ->
                        val isSelected = location.cityName == currentSelected.cityName && location.country == currentSelected.country
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                    ),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelect(location) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "${location.cityName}, ${location.country}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Lat: ${location.latitude}° | Lng: ${location.longitude}° | UTC Offset: ${location.timezoneOffsetHours}h",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            
                            // Edit modifier & delete control row
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { startEditing(location) },
                                    modifier = Modifier.size(28.dp).testTag("edit_location_${location.cityName}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Location",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { deleteConfirmLocation = location },
                                    modifier = Modifier.size(28.dp).testTag("delete_location_${location.cityName}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Location",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Heading for editor section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (editingLocation != null) "Modify Location Coordinates:" else "Add Custom Location:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if (editingLocation != null) {
                        TextButton(
                            onClick = { clearFields() },
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Cancel Edit", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = customCityName,
                            onValueChange = { customCityName = it },
                            label = { Text("City Name", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("custom_city_name"),
                            singleLine = true
                        )
                        TextField(
                            value = customCountry,
                            onValueChange = { customCountry = it },
                            label = { Text("Country", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("custom_country"),
                            singleLine = true
                        )
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = customLat,
                            onValueChange = { customLat = it },
                            label = { Text("Lat (-90..90)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("custom_lat"),
                            singleLine = true
                        )
                        TextField(
                            value = customLng,
                            onValueChange = { customLng = it },
                            label = { Text("Lng (-180..180)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("custom_lng"),
                            singleLine = true
                        )
                    }

                    TextField(
                        value = customTz,
                        onValueChange = { customTz = it },
                        label = { Text("UTC Offset Hours (-12..14)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("custom_tz"),
                        singleLine = true
                    )
                }

                if (showError) {
                    Text(
                        text = "Validation failed! Confirm non-empty name/country, and valid ranges: Lat (-90 to 90), Lng (-180 to 180), UTC offset (-12 to 14).",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 14.sp
                    )
                }

                Button(
                    onClick = {
                        val latVal = customLat.toDoubleOrNull()
                        val lngVal = customLng.toDoubleOrNull()
                        val tzVal = customTz.toIntOrNull()
                        val city = customCityName.trim()
                        val country = if (customCountry.trim().isEmpty()) "Custom" else customCountry.trim()

                        if (latVal != null && lngVal != null && tzVal != null && city.isNotEmpty() &&
                            latVal in -90.0..90.0 && lngVal in -180.0..180.0 && tzVal in -12..14
                        ) {
                            showError = false
                            val toSave = editingLocation?.copy(
                                cityName = city,
                                country = country,
                                latitude = latVal,
                                longitude = lngVal,
                                timezoneOffsetHours = tzVal
                            ) ?: LocationCoordinates(
                                cityName = city,
                                country = country,
                                latitude = latVal,
                                longitude = lngVal,
                                timezoneOffsetHours = tzVal
                            )

                            if (editingLocation != null) {
                                viewModel.updateLocation(toSave)
                            } else {
                                viewModel.addLocation(city, country, latVal, lngVal, tzVal)
                            }
                            clearFields()
                        } else {
                            showError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("apply_custom_coordinates")
                ) {
                    Text(
                        text = if (editingLocation != null) "Save Modifications" else "Add New Coordinate Marker",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 2: PILGRIM POTLUCK REGISTRATION (NEW)
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PotluckTabScreen(viewModel: ChurchViewModel) {
    val contributions by viewModel.potluckContributions.collectAsStateWithLifecycle()
    val categories = listOf("Main Dish", "Side Dish", "Salad", "Dessert", "Drinks")

    var isFormExpanded by remember { mutableStateOf(false) }
    var contributorInput by remember { mutableStateOf("") }
    var dishInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Main Dish") }
    var servingsInput by remember { mutableStateOf(12) }
    var notesInput by remember { mutableStateOf("") }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Fellowship Potluck",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "Register details of dishes you will share during Sabbath study gatherings:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        // Expandable Registration Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isFormExpanded = !isFormExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.AddCircle, "Add", tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Sign Up to Bring a Dish",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            imageVector = if (isFormExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (isFormExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        TextField(
                            value = contributorInput,
                            onValueChange = { contributorInput = it },
                            label = { Text("Your Name", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("potluck_contributor_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        TextField(
                            value = dishInput,
                            onValueChange = { dishInput = it },
                            label = { Text("Dish Name (e.g. Unleavened Barley Bread)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("potluck_dish_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Select Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            items(categories) { cat ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        Text("Servings: $servingsInput people", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Slider(
                            value = servingsInput.toFloat(),
                            onValueChange = { servingsInput = it.toInt() },
                            valueRange = 2f..100f,
                            steps = 49,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        TextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text("Notes (Ingredients, allergens, details...)", fontSize = 11.sp) },
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth().testTag("potluck_notes_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (dishInput.isNotBlank()) {
                                    viewModel.addPotluckContribution(
                                        contributor = contributorInput,
                                        dish = dishInput,
                                        category = selectedCategory,
                                        servings = servingsInput,
                                        notes = notesInput
                                    )
                                    Toast.makeText(context, "Dish registered successfully!", Toast.LENGTH_SHORT).show()
                                    // Reset & collapse
                                    contributorInput = ""
                                    dishInput = ""
                                    notesInput = ""
                                    servingsInput = 12
                                    isFormExpanded = false
                                } else {
                                    Toast.makeText(context, "Please enter a dish name!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("potluck_submit_button")
                        ) {
                            Text("Submit Contribution", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Render sections grouped by Category
        categories.forEach { category ->
            val matchingItems = contributions.filter { it.category == category }
            if (matchingItems.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${matchingItems.size}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Divider(modifier = Modifier.weight(2f))
                        }

                        matchingItems.forEach { dish ->
                            PotluckDishCard(dish = dish, onDelete = {
                                viewModel.deletePotluckContribution(dish)
                                Toast.makeText(context, "Dish removed from list.", Toast.LENGTH_SHORT).show()
                            })
                        }
                    }
                }
            }
        }

        if (contributions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recorded potluck offerings yet. Tap 'Sign Up' above to contribute!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun PotluckDishCard(dish: PotluckContribution, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .testTag("potluck_dish_card_${dish.id}")
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dish.dishName,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Brought by: ${dish.contributorName} | Serves: ${dish.servings} people",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
                if (dish.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dish.notes,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontStyle = FontStyle.Italic
                    )
                }
            }
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("potluck_delete_${dish.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Dish",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// ==========================================
// SCREEN 3: BIBLICAL SCROLLS & APOCRYPHA
// ==========================================

@Composable
fun BibleTabScreen(viewModel: ChurchViewModel) {
    val books = viewModel.bibleBooks
    val selectedBook by viewModel.selectedBook.collectAsStateWithLifecycle()
    val selectedChapterIndex by viewModel.selectedChapterIndex.collectAsStateWithLifecycle()

    var activeCategoryFilter by remember { mutableStateOf(BookCategory.OLD_TESTAMENT) }
    var fontScale by remember { mutableStateOf(16f) }
    var contrastMode by remember { mutableStateOf(0) } // 0: Sepia Light, 1: Pure White, 2: Cyber Dark

    var showBookSelectorDropdown by remember { mutableStateOf(false) }
    var isReadingAreaExpanded by remember { mutableStateOf(false) }

    val readBgColor = when (contrastMode) {
        0 -> Color(0xFFF3EFE0)
        1 -> Color(0xFFFFFFFF)
        else -> Color(0xFF131110)
    }

    val readTextColor = when (contrastMode) {
        0 -> Color(0xFF322A21)
        1 -> Color(0xFF121212)
        else -> Color(0xFFEFECE4)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (!isReadingAreaExpanded) {
            Text(
                text = "Biblical Library",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Serif
            )

            // Scrollable horizontal category filters supporting Pseudepigrapha
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val categorizers = listOf(
                    Pair(BookCategory.OLD_TESTAMENT, "Tanakh (OT)"),
                    Pair(BookCategory.NEW_TESTAMENT, "Apostles (NT)"),
                    Pair(BookCategory.APOCRYPHA, "Apocrypha"),
                    Pair(BookCategory.DEAD_SEA_SCROLLS, "Dead Sea Scrolls"),
                    Pair(BookCategory.PSEUDEPIGRAPHA, "Pseudepigrapha")
                )

                items(categorizers) { cat ->
                    val isSelected = activeCategoryFilter == cat.first
                    Button(
                        onClick = { activeCategoryFilter = cat.first },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) SoftIvoryText else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = cat.second,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Current Resource selectors
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("book_selector_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CURRENT RESOURCE",
                                fontSize = 8.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedBook.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Serif,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = { showBookSelectorDropdown = !showBookSelectorDropdown },
                            elevation = ButtonDefaults.buttonElevation(1.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("change_book_button")
                        ) {
                            Text("Change", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Dropdown")
                        }
                    }

                    AnimatedVisibility(visible = showBookSelectorDropdown) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Divider()
                            Text(
                                text = "Select book from category criteria:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )

                            val matchingBooksOfActiveCategory = books.filter { it.category == activeCategoryFilter }
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(matchingBooksOfActiveCategory) { book ->
                                    val isCur = book.id == selectedBook.id
                                    InputChip(
                                        selected = isCur,
                                        onClick = {
                                            viewModel.selectBook(book)
                                            showBookSelectorDropdown = false
                                        },
                                        label = { Text(book.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        colors = InputChipDefaults.inputChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        modifier = Modifier.testTag("book_chip_${book.id}")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            ExpandableIntroCard(introText = selectedBook.introduction)
        } else {
            // Expanded Focus Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = selectedBook.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = "History & Covenant Focus Reader",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        .clickable { isReadingAreaExpanded = false }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Close, "Exit Focus", tint = SoftIvoryText, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Exit Focus", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SoftIvoryText)
                }
            }
        }

        // Adjusters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Size:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                IconButton(
                    onClick = { if (fontScale > 11f) fontScale -= 2f },
                    modifier = Modifier.size(24.dp)
                ) {
                    Text("A-", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
                IconButton(
                    onClick = { if (fontScale < 30f) fontScale += 2f },
                    modifier = Modifier.size(24.dp)
                ) {
                    Text("A+", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val contrastLabels = listOf("Sepia", "Light", "Dark")
                contrastLabels.forEachIndexed { i, label ->
                    val isSel = i == contrastMode
                    ElevatedButton(
                        onClick = { contrastMode = i },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = if (isSel) MaterialTheme.colorScheme.primary else Color.White,
                            contentColor = if (isSel) Color.White else Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Chapter selections Row
        if (selectedBook.chapters.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedBook.chapters.forEachIndexed { index, ch ->
                    val isSelectedCh = index == selectedChapterIndex
                    Button(
                        onClick = { viewModel.selectChapterIndex(index) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelectedCh) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelectedCh) SoftIvoryText else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("chapter_selector_${index}")
                    ) {
                        Text(ch.label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Reading Area Workspace Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val curCh = selectedBook.chapters.getOrNull(selectedChapterIndex)
            Text(
                text = "${selectedBook.name} — ${curCh?.label ?: "Reading"}",
                style = MaterialTheme.typography.titleSmall,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { isReadingAreaExpanded = !isReadingAreaExpanded }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("toggle_reading_area_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isReadingAreaExpanded) Icons.Default.Close else Icons.Default.KeyboardArrowUp,
                    contentDescription = if (isReadingAreaExpanded) "Minimize Reading Area" else "Expand Reading Area",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isReadingAreaExpanded) "Compact" else "Focus Mode",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Reading Content Pane
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                .testTag("bible_reading_container"),
            color = readBgColor
        ) {
            val scroll = rememberScrollState()
            val currentChapterText = if (selectedChapterIndex < selectedBook.chapters.size) {
                selectedBook.chapters[selectedChapterIndex].text
            } else {
                "Text unavailable."
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(16.dp)
            ) {
                Text(
                    text = currentChapterText,
                    fontSize = fontScale.sp,
                    lineHeight = (fontScale * 1.5).sp,
                    fontFamily = FontFamily.Serif,
                    fontStyle = if (selectedBook.category == BookCategory.DEAD_SEA_SCROLLS) FontStyle.Italic else FontStyle.Normal,
                    color = readTextColor,
                    modifier = Modifier.testTag("bible_chapter_text_display")
                )
            }
        }
    }
}

@Composable
fun ExpandableIntroCard(introText: String) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Info, "Intro", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Historical Introduction",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = introText,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ==========================================
// SCREEN 4: INTERACTIVE LOCATIONS & VECTOR MAPS (NEW)
// ==========================================

@Composable
fun LocationsMapTabScreen(viewModel: ChurchViewModel) {
    val campuses by viewModel.campusesList.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedCampusIndex by remember { mutableStateOf(0) }
    val safeSelectedCampusIndex = if (campuses.isEmpty()) -1 else selectedCampusIndex.coerceIn(0, campuses.size - 1)
    val activeCampus = if (safeSelectedCampusIndex != -1) campuses[safeSelectedCampusIndex] else null

    var highlightedFeatureName by remember { mutableStateOf("Main Sanctuary Entry") }
    var highlightedFeatureDesc by remember { mutableStateOf("The main foyer and access point. Tap the campus blueprint to inspect sectors.") }

    var showEditor by remember { mutableStateOf(false) }
    var editTargetCampus by remember { mutableStateOf<ChurchCampus?>(null) } // null means add, not-null means edit

    var campusName by remember { mutableStateOf("") }
    var campusAddress by remember { mutableStateOf("") }
    var campusCoordinates by remember { mutableStateOf("") }
    var campusPhone by remember { mutableStateOf("") }
    var campusStudyTime by remember { mutableStateOf("") }
    var campusWorshipTime by remember { mutableStateOf("") }
    var campusDetails by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf(false) }

    var campusToDelete by remember { mutableStateOf<ChurchCampus?>(null) }

    val openAddForm = {
        campusName = ""
        campusAddress = ""
        campusCoordinates = ""
        campusPhone = ""
        campusStudyTime = ""
        campusWorshipTime = ""
        campusDetails = ""
        editTargetCampus = null
        formError = false
        showEditor = true
    }

    val openEditForm: (ChurchCampus) -> Unit = { campus ->
        campusName = campus.name
        campusAddress = campus.address
        campusCoordinates = campus.coordinates
        campusPhone = campus.phone
        campusStudyTime = campus.studyTime
        campusWorshipTime = campus.worshipTime
        campusDetails = campus.details
        editTargetCampus = campus
        formError = false
        showEditor = true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Church Locations & Map",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = "Detailed directions and blueprints of T.O.B.Y. worship campuses:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                
                Button(
                    onClick = openAddForm,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_campus_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Campus", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Campus scrollable chips
        if (campuses.isNotEmpty()) {
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(campuses) { index, c ->
                        val isSelected = index == safeSelectedCampusIndex
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCampusIndex = index
                                // Reset blueprint focus
                                highlightedFeatureName = "Main Sanctuary Entry"
                                highlightedFeatureDesc = "The main foyer and access point. Tap the campus blueprint to inspect sectors."
                            },
                            label = { Text(c.name.split(" - ").last(), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("campus_chip_${c.id}")
                        )
                    }
                }
            }
        } else {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No worship campuses registered. Tap 'Add' to declare one!",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Active campus card
        if (activeCampus != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("active_campus_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activeCampus.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Serif,
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Edit & Delete operations for active campus
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { openEditForm(activeCampus) },
                                    modifier = Modifier.size(32.dp).testTag("edit_campus_${activeCampus.id}")
                                ) {
                                    Icon(Icons.Default.Edit, "Edit Campus", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { campusToDelete = activeCampus },
                                    modifier = Modifier.size(32.dp).testTag("delete_campus_${activeCampus.id}")
                                ) {
                                    Icon(Icons.Default.Delete, "Delete Campus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.clickable {
                                try {
                                    val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(activeCampus.address)}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Map application not found", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(Icons.Default.LocationOn, "Address", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "${activeCampus.address} | Coordinates: ${activeCampus.coordinates}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.clickable {
                                try {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${activeCampus.phone}"))
                                    context.startActivity(dialIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Dialer not found", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Phone, "Phone", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            Text(
                                text = activeCampus.phone,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.DateRange, "Schedule", tint = SabbathGold, modifier = Modifier.size(16.dp))
                            Text(
                                text = "${activeCampus.studyTime} • ${activeCampus.worshipTime}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Divider()

                        Text(
                            text = activeCampus.details,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Vector Blueprint floor map drawer
            item {
                Text(
                    text = "Interactive Campus Blueprint Layout Map:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A25)), // deep map background
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val isCentral = activeCampus.name.lowercase().contains("central")
                        val isNorth = activeCampus.name.lowercase().contains("north")

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw architectural grid lines
                            val gridSpacing = 25.dp.toPx()
                            for (x in 0..w.toInt() step gridSpacing.toInt()) {
                                drawLine(
                                    color = Color(0xFF1E3A52),
                                    start = Offset(x.toFloat(), 0f),
                                    end = Offset(x.toFloat(), h),
                                    strokeWidth = 1f
                                )
                            }
                            for (y in 0..h.toInt() step gridSpacing.toInt()) {
                                drawLine(
                                    color = Color(0xFF1E3A52),
                                    start = Offset(0f, y.toFloat()),
                                    end = Offset(w, y.toFloat()),
                                    strokeWidth = 1f
                                )
                            }

                            // Outer border walls
                            drawRoundRect(
                                color = Color(0xFF42A5F5),
                                topLeft = Offset(40f, 40f),
                                size = Size(w - 80f, h - 80f),
                                style = Stroke(width = 4f),
                                cornerRadius = CornerRadius(12f)
                            )

                            // Divider Walls mapping different sectors based on selected campus
                            if (isCentral) {
                                // Sanctuary Pew blocks
                                drawRect(Color(0xFF2E7D32).copy(alpha = 0.3f), Offset(60f, 60f), Size(w/2 - 80f, h - 120f))
                                // Pulpit / Ark scroll Room
                                drawRect(Color(0xFFE65100).copy(alpha = 0.4f), Offset(w/2 + 20f, 60f), Size(w/2 - 80f, h/2 - 40f))
                                // Fellowship kitchen tables
                                drawRect(Color(0xFF00bcd4).copy(alpha = 0.3f), Offset(w/2 + 20f, h/2 + 40f), Size(w/2 - 80f, h/2 - 100f))
                            } else if (isNorth) {
                                // Nature Chapel outdoor benches
                                drawRect(Color(0xFF2E7D32).copy(alpha = 0.4f), Offset(60f, 60f), Size(w - 120f, h/3))
                                // Indoor altar prayer closets
                                drawRect(Color(0xFFE65100).copy(alpha = 0.3f), Offset(60f, h/2), Size(w/2 - 80f, h/2 - 40f))
                                // Garden scroll cabins
                                drawRect(Color(0xFF673ab7).copy(alpha = 0.3f), Offset(w/2 + 20f, h/2), Size(w/2 - 80f, h/2 - 40f))
                            } else {
                                // Outdoor baptistry pool focus
                                drawCircle(Color(0xFF0288d1).copy(alpha = 0.5f), center = Offset(w/3, h/2), radius = 50f)
                                // Potluck courtyard benches
                                drawRect(Color(0xFF00bcd4).copy(alpha = 0.3f), Offset(w/2 + 30f, 60f), Size(w/2 - 90f, h - 120f))
                            }
                        }

                        // Interactive touch overlay labels
                        if (isCentral) {
                            BlueprintTextButton(
                                label = "Worship Pews",
                                modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp),
                                onClick = {
                                    highlightedFeatureName = "Sanctuary Seating (Pews)"
                                    highlightedFeatureDesc = "Pew layouts configured with generous spacing. This seats 150 participants for Sabbath readings."
                                }
                            )
                            BlueprintTextButton(
                                label = "Scroll Chest",
                                modifier = Modifier.align(Alignment.TopEnd).padding(end = 40.dp, top = 24.dp),
                                onClick = {
                                    highlightedFeatureName = "Scroll Ark & Library Chamber"
                                    highlightedFeatureDesc = "Secure climate chamber housing biblical papyri, Apocrypha, and detailed Pseudepigrapha leather scroll replicas."
                                }
                            )
                            BlueprintTextButton(
                                label = "Potluck Court",
                                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 40.dp, bottom = 24.dp),
                                onClick = {
                                    highlightedFeatureName = "Courtyard Fellowship Table"
                                    highlightedFeatureDesc = "Outdoor covered space where sharing dishes and refreshing hibiscus teas are served after Sabbath services."
                                }
                            )
                        } else if (isNorth) {
                            BlueprintTextButton(
                                label = "Nature Benches",
                                modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp),
                                onClick = {
                                    highlightedFeatureName = "Highland Nature Benches"
                                    highlightedFeatureDesc = "Surrounded by olive branches. Direct view of Israel sunrise / sunsets during our prayer groups."
                                }
                            )
                            BlueprintTextButton(
                                label = "Altar Chapel",
                                modifier = Modifier.align(Alignment.BottomStart).padding(start = 24.dp, bottom = 24.dp),
                                onClick = {
                                    highlightedFeatureName = "Indoor Altar"
                                    highlightedFeatureDesc = "Quiet sanctuary chapel containing a stone pillar memorial and private devotion kneeling cushions."
                                }
                            )
                        } else {
                            BlueprintTextButton(
                                label = "Baptistry Pool",
                                modifier = Modifier.align(Alignment.CenterStart).padding(start = 28.dp),
                                onClick = {
                                    highlightedFeatureName = "Jordan River Baptistry"
                                    highlightedFeatureDesc = "Fresh-water outdoor immersion pool supplied by local thermal springs, dedicated to covenant initiations."
                                }
                            )
                            BlueprintTextButton(
                                label = "Gathering Green",
                                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 28.dp),
                                onClick = {
                                    highlightedFeatureName = "Valley Assembly Pavilion"
                                    highlightedFeatureDesc = "Covered wooden cabin pavilion hosting rustic river-side biblical discussions and local farmer bread fellowships."
                                }
                            )
                        }
                    }
                }
            }

            // Selected Blueprint Sector card details
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Info, "Sector Details", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text(
                                text = highlightedFeatureName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = highlightedFeatureDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }

    // Modal Editor Dialog for adding/modifying campuses
    if (showEditor) {
        Dialog(onDismissRequest = { showEditor = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val isEdit = editTargetCampus != null
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEdit) "Edit Campus Record" else "Add Campus Record",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Serif
                        )
                        IconButton(onClick = { showEditor = false }) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    }

                    TextField(
                        value = campusName,
                        onValueChange = { campusName = it },
                        label = { Text("Campus Name", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("campus_name_input")
                    )

                    TextField(
                        value = campusAddress,
                        onValueChange = { campusAddress = it },
                        label = { Text("Address", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("campus_address_input")
                    )

                    TextField(
                        value = campusCoordinates,
                        onValueChange = { campusCoordinates = it },
                        label = { Text("Coordinates (e.g. 31.7719° N, 35.2170° E)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("campus_coordinates_input")
                    )

                    TextField(
                        value = campusPhone,
                        onValueChange = { campusPhone = it },
                        label = { Text("Phone Number", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("campus_phone_input")
                    )

                    TextField(
                        value = campusStudyTime,
                        onValueChange = { campusStudyTime = it },
                        label = { Text("Sabbath Study Time (e.g. 10:00 AM)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("campus_study_input")
                    )

                    TextField(
                        value = campusWorshipTime,
                        onValueChange = { campusWorshipTime = it },
                        label = { Text("Worship Service Time (e.g. 11:30 AM)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("campus_worship_input")
                    )

                    TextField(
                        value = campusDetails,
                        onValueChange = { campusDetails = it },
                        label = { Text("Campus/Assembly Details", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("campus_details_input"),
                        minLines = 3
                    )

                    if (formError) {
                        Text(
                            text = "Validation failed! Campus Name and Address cannot be blank.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showEditor = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (campusName.trim().isNotBlank() && campusAddress.trim().isNotBlank()) {
                                    val finalPhone = if (campusPhone.trim().isBlank()) "(No Phone Listed)" else campusPhone.trim()
                                    val finalCoords = if (campusCoordinates.trim().isBlank()) "0.0° N, 0.0° E" else campusCoordinates.trim()
                                    val finalStudy = if (campusStudyTime.trim().isBlank()) "Sabbath Study: 10:00 AM" else campusStudyTime.trim()
                                    val finalWorship = if (campusWorshipTime.trim().isBlank()) "Sabbath Worship: 11:30 AM" else campusWorshipTime.trim()
                                    val finalDetails = if (campusDetails.trim().isBlank()) "T.O.B.Y. Assembly of Holy Covenants" else campusDetails.trim()
                                    
                                    val target = editTargetCampus
                                    if (target != null) {
                                        viewModel.updateCampus(
                                            target.copy(
                                                name = campusName.trim(),
                                                address = campusAddress.trim(),
                                                coordinates = finalCoords,
                                                phone = finalPhone,
                                                studyTime = finalStudy,
                                                worshipTime = finalWorship,
                                                details = finalDetails
                                            )
                                        )
                                    } else {
                                        viewModel.addCampus(
                                            name = campusName.trim(),
                                            address = campusAddress.trim(),
                                            coordinates = finalCoords,
                                            phone = finalPhone,
                                            studyTime = finalStudy,
                                            worshipTime = finalWorship,
                                            details = finalDetails
                                        )
                                    }
                                    showEditor = false
                                } else {
                                    formError = true
                                }
                            },
                            modifier = Modifier.testTag("submit_campus_form")
                        ) {
                            Text(if (isEdit) "Save Edits" else "Create Campus")
                        }
                    }
                }
            }
        }
    }

    // AlertDialog for confirming campus deletions
    if (campusToDelete != null) {
        val target = campusToDelete!!
        AlertDialog(
            onDismissRequest = { campusToDelete = null },
            title = { Text("Delete Campus Record?", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
            text = { Text("Are you absolutely sure you want to delete ${target.name}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCampus(target)
                        campusToDelete = null
                        selectedCampusIndex = 0
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_campus_button")
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { campusToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BlueprintTextButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

// ==========================================
// SCREEN 5: PRAYER FEED & MINISTRY CONTACTS
// ==========================================

@Composable
fun PrayerAndContactsTabScreen(viewModel: ChurchViewModel) {
    var directoryActiveTab by remember { mutableStateOf(0) } // 0: Prayer Board, 1: Leadership Directory

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(
            selectedTabIndex = directoryActiveTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = directoryActiveTab == 0,
                onClick = { directoryActiveTab = 0 },
                text = { Text("Prayer Wall Support", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = directoryActiveTab == 1,
                onClick = { directoryActiveTab = 1 },
                text = { Text("Leadership Directory", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (directoryActiveTab == 0) {
                PrayerWallSubScreen(viewModel)
            } else {
                MinistryDirectorySubScreen()
            }
        }
    }
}

@Composable
fun MinistryDirectorySubScreen() {
    val context = LocalContext.current
    val contacts = listOf(
        Pair(
            "Pastor David Sterling",
            "Lead Covenant Shepherd | Coordinates spiritual counseling, Biblical law, and family covenants. Available for prayer counseling and direct support."
        ),
        Pair(
            "Elder Johnathan Vance",
            "Sabbath Study Dean | Directs advanced scriptural translation panels, Qumran exhibits, and the Sabbath biblical libraries."
        ),
        Pair(
            "Dr. Rachel Chen",
            "Archaeologist & Scribe Archivist | Specializes in Qumran scroll restorations, Apocrypha historical context studies, and literature publications."
        ),
        Pair(
            "Sister Sarah Brooks",
            "Hospitality Coordinator | Oversees global sabbath potluck structures, bread fellowships, and care counseling for recovering patients."
        ),
        Pair(
            "Deacon Marcus Vance",
            "Campuses Facilities Director | Guides sanctuary blueprints, chapel facilities scheduling, and local geographic locator services."
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Covenant Leadership Directory",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Direct communications panel with administrators, clergy, and study helpers:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        items(contacts) { leader ->
            val cleanedPrefix = leader.first.replace("Pastor ", "").replace("Elder ", "").replace("Dr. ", "").replace("Sister ", "").replace("Deacon ", "")
            val slug = cleanedPrefix.split(" ").first().lowercase()
            val fakeEmail = "${slug}@gracecovenant.org"
            val fakePhone = "+1-555-covenant-${slug.take(4)}"

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth().testTag("leader_card_${slug}")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = leader.first,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Serif
                    )

                    Text(
                        text = leader.second,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Email: $fakeEmail",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.clickable {
                                    try {
                                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:$fakeEmail")
                                        }
                                        context.startActivity(emailIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Email client not found", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                            Text(
                                text = "Admin Direct: $fakePhone",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.clickable {
                                    try {
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$fakePhone"))
                                        context.startActivity(dialIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Dialer not found", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Copy button for email
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Pastor Email", fakeEmail)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Email copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp).testTag("copy_email_$slug")
                            ) {
                                Icon(Icons.Default.Share, "Copy", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }

                            // Trigger phone dial intent
                            IconButton(
                                onClick = {
                                    try {
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$fakePhone"))
                                        context.startActivity(dialIntent)
                                    } catch(e: Exception) {
                                        Toast.makeText(context, "Dialer not found", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(32.dp).testTag("call_$slug")
                            ) {
                                Icon(Icons.Default.Phone, "Call", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerWallSubScreen(viewModel: ChurchViewModel) {
    val prayers by viewModel.prayers.collectAsStateWithLifecycle()
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var showSubmitDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Healing", "Guidance", "Family", "Faith", "General")

    val filteredPrayers = remember(prayers, selectedCategoryFilter) {
        if (selectedCategoryFilter == "All") prayers else prayers.filter { it.category == selectedCategoryFilter }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Intercessory Prayer Requests",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "We are commanded to carry each others' burdens in unity:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Category select chips for filtering prayers
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { category ->
                        val isSel = category == selectedCategoryFilter
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedCategoryFilter = category },
                            label = { Text(category, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("filter_chip_$category")
                        )
                    }
                }
            }

            items(filteredPrayers) { prayer ->
                PrayerWallCard(
                    prayer = prayer,
                    onPrayClick = { viewModel.intercedeForPrayer(prayer.id) },
                    onDelete = { viewModel.deletePrayerRequest(prayer.id) }
                )
            }

            if (filteredPrayers.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No prayers submitted in this criteria yet.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) } // Spacer so fab doesn't block content
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showSubmitDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = SoftIvoryText,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_prayer_fab")
        ) {
            Icon(Icons.Default.Add, "Post Prayer Query")
        }

        if (showSubmitDialog) {
            SubmitPrayerDialog(
                onSubmit = { title, content, cat, requester ->
                    viewModel.submitPrayerRequest(title, content, cat, requester)
                    showSubmitDialog = false
                },
                onDismiss = { showSubmitDialog = false }
            )
        }
    }
}

@Composable
fun PrayerWallCard(
    prayer: PrayerRequest,
    onPrayClick: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = when (prayer.category) {
        "Healing" -> Color(0xFFC8E6C9)
        "Guidance" -> Color(0xFFBBDEFB)
        "Family" -> Color(0xFFFFE0B2)
        "Faith" -> Color(0xFFD1C4E9)
        else -> Color(0xFFCFD8DC)
    }

    val labelColor = when (prayer.category) {
        "Healing" -> Color(0xFF1B5E20)
        "Guidance" -> Color(0xFF0D47A1)
        "Family" -> Color(0xFFE65100)
        "Faith" -> Color(0xFF4A148C)
        else -> Color(0xFF37474F)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prayer_card_${prayer.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(categoryColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = prayer.category,
                        color = labelColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Submitted by: ${prayer.requester}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (prayer.isUserSubmitted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp).testTag("delete_prayer_${prayer.id}")) {
                            Icon(Icons.Default.Delete, "Delete User Submission", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Text(
                text = prayer.title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Serif
            )

            Text(
                text = prayer.content,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "${prayer.prayerCount} intercessions offered",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = onPrayClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (prayer.hasUserPrayed) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary,
                        contentColor = if (prayer.hasUserPrayed) MaterialTheme.colorScheme.primary else SoftIvoryText
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("pray_button_${prayer.id}")
                ) {
                    Icon(
                        imageVector = if (prayer.hasUserPrayed) Icons.Default.Check else Icons.Default.FavoriteBorder,
                        contentDescription = "Offer prayers icon",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (prayer.hasUserPrayed) "Prayed!" else "Pray",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SubmitPrayerDialog(
    onSubmit: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }

    val categories = listOf("Healing", "Guidance", "Family", "Faith", "General", "Unspoken")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Submit Prayer Request",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Dismiss dialog")
                    }
                }

                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Name (Optional)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_prayer_name"),
                    placeholder = { Text("Anonymous", fontSize = 11.sp) }
                )

                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Request Title", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_prayer_title")
                )

                TextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Detailed Details & Burdens To Lift", fontSize = 11.sp) },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth().testTag("add_prayer_content_field")
                )

                Text(
                    text = "Category:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isChosen = category == cat
                        FilterChip(
                            selected = isChosen,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 10.sp) },
                            modifier = Modifier.testTag("dialog_cat_chip_$cat")
                        )
                    }
                }

                Button(
                    onClick = {
                        if (title.trim().isNotEmpty() && content.trim().isNotEmpty()) {
                            onSubmit(title.trim(), content.trim(), category, name.trim())
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_submit_prayer_button")
                ) {
                    Text("Submit to Assembly Wall", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
