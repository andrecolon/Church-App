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
                3 -> LocationsMapTabScreen()
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
    val holidays = viewModel.holidays
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
                        text = "Grace Covenant",
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
                        Text(
                            text = "Shavuot begins TODAY (May 22, 2026)! This celebrates the Sinai Torah giving and Acts 2 Pentecost Spirit outpouring. Read the Book of Ruth and study the Bible Scroll library!",
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
                    items(holidays) { holiday ->
                        HolidayCard(holiday = holiday)
                    }
                }
            }
        }
    }

    if (showLocationSelector) {
        LocationSelectorDialog(
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
    currentSelected: LocationCoordinates,
    onSelect: (LocationCoordinates) -> Unit,
    onDismiss: () -> Unit
) {
    var customCityName by remember { mutableStateOf("") }
    var customLat by remember { mutableStateOf("") }
    var customLng by remember { mutableStateOf("") }
    var customTz by remember { mutableStateOf("") }

    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Global Sunset Meridian",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Dismiss")
                    }
                }

                Text(
                    text = "Select a region coordinate marker below to dynamically recalculate Sabbath sunset ingress times:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .heightIn(max = 240.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SunsetCalculator.locations.forEach { location ->
                        val isSelected = location.cityName == currentSelected.cityName
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelect(location) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${location.cityName}, ${location.country}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Lat: ${location.latitude}° | Lng: ${location.longitude}° | UTC Offset: ${location.timezoneOffsetHours}h",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, "Selected", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                Divider()

                Text(
                    text = "Or Set Custom Coordinates:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextField(
                        value = customCityName,
                        onValueChange = { customCityName = it },
                        label = { Text("City Name", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("custom_city_name"),
                        singleLine = true
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = customLat,
                            onValueChange = { customLat = it },
                            label = { Text("Latitude", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("custom_lat"),
                            singleLine = true
                        )
                        TextField(
                            value = customLng,
                            onValueChange = { customLng = it },
                            label = { Text("Longitude", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("custom_lng"),
                            singleLine = true
                        )
                    }

                    TextField(
                        value = customTz,
                        onValueChange = { customTz = it },
                        label = { Text("Timezone Offset (e.g. -5 for EST, 2 for Israel)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("custom_tz"),
                        singleLine = true
                    )
                }

                if (showError) {
                    Text(
                        text = "Invalid format! Ensure correct Lat (-90 to 90), Lng (-180 to 180), and Tz offset (-12 to 14).",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        val latVal = customLat.toDoubleOrNull()
                        val lngVal = customLng.toDoubleOrNull()
                        val tzVal = customTz.toIntOrNull()
                        val city = customCityName.trim()

                        if (latVal != null && lngVal != null && tzVal != null && city.isNotEmpty() &&
                            latVal in -90.0..90.0 && lngVal in -180.0..180.0 && tzVal in -12..14
                        ) {
                            showError = false
                            onSelect(
                                LocationCoordinates(
                                    cityName = city,
                                    country = "Custom",
                                    latitude = latVal,
                                    longitude = lngVal,
                                    timezoneOffsetHours = tzVal
                                )
                            )
                        } else {
                            showError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("apply_custom_coordinates")
                ) {
                    Text("Apply Custom Meridian", fontWeight = FontWeight.Bold)
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

data class ChurchCampus(
    val id: String,
    val name: String,
    val address: String,
    val coordinates: String,
    val phone: String,
    val studyTime: String,
    val worshipTime: String,
    val details: String
)

@Composable
fun LocationsMapTabScreen() {
    val campuses = listOf(
        ChurchCampus(
            id = "central",
            name = "Grace Covenant - Central Sanctuary",
            address = "500 Zion Way, City Center",
            coordinates = "31.7719° N, 35.2170° E",
            phone = "(555) 123-4567",
            studyTime = "Sabbath Study: 10:00 AM",
            worshipTime = "Sabbath Worship: 11:30 AM",
            details = "Our main worship home featuring historical scroll archives and a spacious fellowship courtyard."
        ),
        ChurchCampus(
            id = "north",
            name = "Grace Covenant - North Ridge Chapel",
            address = "12 Mount Hermon Road, Highland",
            coordinates = "32.9642° N, 35.6983° E",
            phone = "(555) 987-6543",
            studyTime = "Friday Sunset Ingress: 6:30 PM",
            worshipTime = "Sabbath Morning Blessing: 9:00 AM",
            details = "A peaceful getaway location embedded in nature gardens, ideal for quiet retreats and sunset prayer."
        ),
        ChurchCampus(
            id = "east",
            name = "Grace Covenant - East Fellowship Cabin",
            address = "88 Jordan Crossing, Valley Green",
            coordinates = "31.9522° N, 35.9284° E",
            phone = "(555) 555-7777",
            studyTime = "Sabbath Outdoor Fellowship: 4:30 PM",
            worshipTime = "Sunset Fellowship Service: 6:00 PM",
            details = "Our rustic valley location hosting riverside baptism studies and communal outdoor potlucks."
        )
    )

    var selectedCampusIndex by remember { mutableStateOf(0) }
    val activeCampus = campuses[selectedCampusIndex]

    var highlightedFeatureName by remember { mutableStateOf("Main Sanctuary Entry") }
    var highlightedFeatureDesc by remember { mutableStateOf("The main foyer and access point. Tap the campus blueprint to inspect sectors.") }

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
                    text = "Church Locations & Map",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "Detailed directions and blueprints of Grace Covenant worship campuses:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        // Campus scrollable chips
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(campuses) { index, c ->
                    val isSelected = index == selectedCampusIndex
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
                        )
                    )
                }
            }
        }

        // Active campus card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = activeCampus.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Serif
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.LocationOn, "Address", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "${activeCampus.address} | Coordinates: ${activeCampus.coordinates}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Phone, "Phone", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                        Text(text = activeCampus.phone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
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
                        if (activeCampus.id == "central") {
                            // Sanctuary Pew blocks
                            drawRect(Color(0xFF2E7D32).copy(alpha = 0.3f), Offset(60f, 60f), Size(w/2 - 80f, h - 120f))
                            // Pulpit / Ark scroll Room
                            drawRect(Color(0xFFE65100).copy(alpha = 0.4f), Offset(w/2 + 20f, 60f), Size(w/2 - 80f, h/2 - 40f))
                            // Fellowship kitchen tables
                            drawRect(Color(0xFF00bcd4).copy(alpha = 0.3f), Offset(w/2 + 20f, h/2 + 40f), Size(w/2 - 80f, h/2 - 100f))
                        } else if (activeCampus.id == "north") {
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
                    if (activeCampus.id == "central") {
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
                    } else if (activeCampus.id == "north") {
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
                            Text(text = "Email: $fakeEmail", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Text(text = "Admin Direct: $fakePhone", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+15551234567"))
                                        context.startActivity(dialIntent)
                                    } catch(e: Exception) {
                                        Toast.makeText(context, "Initiating direct dial to $fakePhone", Toast.LENGTH_SHORT).show()
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

    val categories = listOf("Healing", "Guidance", "Family", "Faith", "General")

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
