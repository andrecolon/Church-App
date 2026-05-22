package com.example.data.repository

import com.example.data.local.PrayerDao
import com.example.data.local.EventDao
import com.example.data.local.PotluckDao
import com.example.data.local.LocationDao
import com.example.data.local.CampusDao
import com.example.data.model.*
import com.example.utils.LocationCoordinates
import com.example.utils.SunsetCalculator
import kotlinx.coroutines.flow.Flow

class ChurchRepository(
    private val prayerDao: PrayerDao,
    private val eventDao: EventDao,
    private val potluckDao: PotluckDao,
    private val locationDao: LocationDao,
    private val campusDao: CampusDao
) {
    // Database Flow APIs
    val allPrayers: Flow<List<PrayerRequest>> = prayerDao.getAllPrayers()
    val allReminders: Flow<List<EventReminder>> = eventDao.getAllReminders()
    val allPotluckContributions: Flow<List<PotluckContribution>> = potluckDao.getAllContributions()
    val allLocations: Flow<List<LocationCoordinates>> = locationDao.getAllLocations()
    val allCampuses: Flow<List<ChurchCampus>> = campusDao.getAllCampuses()

    // Static content catalogs
    val events: List<CalendarEvent> = SeedData.events
    val holidays: List<BiblicalHoliday> = SeedData.holidays
    val bibleBooks: List<BibleBook> = SeedData.bibleBooks

    // Location operations
    suspend fun insertLocation(location: LocationCoordinates) {
        locationDao.insertLocation(location)
    }

    suspend fun updateLocation(location: LocationCoordinates) {
        locationDao.updateLocation(location)
    }

    suspend fun deleteLocation(location: LocationCoordinates) {
        locationDao.deleteLocation(location)
    }

    // Campus operations
    suspend fun insertCampus(campus: ChurchCampus) {
        campusDao.insertCampus(campus)
    }

    suspend fun updateCampus(campus: ChurchCampus) {
        campusDao.updateCampus(campus)
    }

    suspend fun deleteCampus(campus: ChurchCampus) {
        campusDao.deleteCampus(campus)
    }

    // Prayer requests operations
    suspend fun insertPrayer(prayer: PrayerRequest) {
        prayerDao.insertPrayer(prayer)
    }

    suspend fun updatePrayer(prayer: PrayerRequest) {
        prayerDao.updatePrayer(prayer)
    }

    suspend fun prayForRequest(prayerId: Int) {
        val prayer = prayerDao.getPrayerById(prayerId)
        if (prayer != null) {
            val updated = prayer.copy(
                prayerCount = prayer.prayerCount + 1,
                hasUserPrayed = true
            )
            prayerDao.updatePrayer(updated)
        }
    }

    suspend fun deletePrayer(prayerId: Int) {
        prayerDao.deletePrayerById(prayerId)
    }

    // Reminders operations
    suspend fun toggleEventAttendance(eventId: String, isAttending: Boolean) {
        val current = eventDao.getReminderById(eventId) ?: EventReminder(eventId = eventId)
        eventDao.insertReminder(current.copy(isAttending = isAttending))
    }

    suspend fun toggleEventReminder(eventId: String, reminderSet: Boolean) {
        val current = eventDao.getReminderById(eventId) ?: EventReminder(eventId = eventId)
        eventDao.insertReminder(current.copy(reminderSet = reminderSet))
    }

    suspend fun saveEventNote(eventId: String, notes: String) {
        val current = eventDao.getReminderById(eventId) ?: EventReminder(eventId = eventId)
        eventDao.insertReminder(current.copy(customNotes = notes))
    }

    // Potluck operations
    suspend fun insertPotluckContribution(contribution: PotluckContribution) {
        potluckDao.insertContribution(contribution)
    }

    suspend fun deletePotluckContribution(contribution: PotluckContribution) {
        potluckDao.deleteContribution(contribution)
    }

    // Seed initial prayers & potluck on startup
    suspend fun initializeSeedPrayersIfNeeded() {
        if (prayerDao.getCount() == 0) {
            val defaultPrayers = listOf(
                PrayerRequest(
                    title = "Healing for Sister Sarah",
                    content = "Please pray for our beloved Sister Sarah who is recovering from a major hip replacement surgery. May God restore her joints and strength rapidly.",
                    category = "Healing",
                    requester = "Brother Thomas",
                    prayerCount = 14,
                    isUserSubmitted = false
                ),
                PrayerRequest(
                    title = "Strength in New Beginnings",
                    content = "Starting a new career in healthcare next week. Praying for patience, understanding for the patients, and that I can carry a light of grace in the hospital.",
                    category = "Guidance",
                    requester = "Emily Johnson",
                    prayerCount = 9,
                    isUserSubmitted = false
                ),
                PrayerRequest(
                    title = "Restoration of Family Peace",
                    content = "Praying for reconciliation between siblings who have been estranged for four years. May hearts soften and pride yield to love this Sabbath season.",
                    category = "Family",
                    requester = "Anonymous",
                    prayerCount = 22,
                    isUserSubmitted = false
                ),
                PrayerRequest(
                    title = "Encountering God in the Wilderness",
                    content = "Struggling with a season of severe cloudiness and dryness in prayer. Praying for a renewal of spiritual discipline and hunger for the covenant Word.",
                    category = "Faith",
                    requester = "Michael S.",
                    prayerCount = 18,
                    isUserSubmitted = false
                )
            )
            for (p in defaultPrayers) {
                prayerDao.insertPrayer(p)
            }
        }
        if (potluckDao.getCount() == 0) {
            val defaultPotluck = listOf(
                PotluckContribution(
                    contributorName = "Sister Sarah",
                    dishName = "Unleavened Rosemary Flatbread",
                    category = "Side Dish",
                    servings = 20,
                    notes = "Warm whole wheat and rosemary, perfect with hummus or soup!"
                ),
                PotluckContribution(
                    contributorName = "Brother Thomas",
                    dishName = "Honey Garlic Roasted Chicken",
                    category = "Main Dish",
                    servings = 12,
                    notes = "Gluten free, prepared in parchment paper."
                ),
                PotluckContribution(
                    contributorName = "Emily Johnson",
                    dishName = "Fig, Pomegranate and Walnut Salad",
                    category = "Salad",
                    servings = 15,
                    notes = "Dressed in a lemon mustard vinaigrette."
                ),
                PotluckContribution(
                    contributorName = "Deacon Marcus",
                    dishName = "Chilled Hibiscus & Mint Tea",
                    category = "Drinks",
                    servings = 30,
                    notes = "Unsweetened, naturally refreshing."
                ),
                PotluckContribution(
                    contributorName = "Dorothy Vance",
                    dishName = "Stuffed Almond Butter Medjool Dates",
                    category = "Dessert",
                    servings = 24,
                    notes = "Topped with toasted organic sesame seeds."
                )
            )
            for (item in defaultPotluck) {
                potluckDao.insertContribution(item)
            }
        }
        if (locationDao.getCount() == 0) {
            val defaultLocs = listOf(
                LocationCoordinates("Jerusalem", "Israel", 31.7683, 35.2137, 3),
                LocationCoordinates("New York", "USA", 40.7128, -74.0060, -4),
                LocationCoordinates("Los Angeles", "USA", 34.0522, -118.2437, -7),
                LocationCoordinates("London", "UK", 51.5074, -0.1278, 1),
                LocationCoordinates("Sydney", "Australia", -33.8688, 151.2093, 10),
                LocationCoordinates("Toronto", "Canada", 43.6532, -79.3832, -4),
                LocationCoordinates("Cape Town", "South Africa", -33.9249, 18.4241, 2),
                LocationCoordinates("São Paulo", "Brazil", -23.5505, -46.6333, -3)
            )
            for (loc in defaultLocs) {
                locationDao.insertLocation(loc)
            }
        }
        if (campusDao.getCount() == 0) {
            val defaultCampuses = listOf(
                ChurchCampus(
                    name = "Grace Covenant - Central Sanctuary",
                    address = "500 Zion Way, City Center",
                    coordinates = "31.7719° N, 35.2170° E",
                    phone = "(555) 123-4567",
                    studyTime = "Sabbath Study: 10:00 AM",
                    worshipTime = "Sabbath Worship: 11:30 AM",
                    details = "Our main worship home featuring historical scroll archives and a spacious fellowship courtyard."
                ),
                ChurchCampus(
                    name = "Grace Covenant - North Ridge Chapel",
                    address = "12 Mount Hermon Road, Highland",
                    coordinates = "32.9642° N, 35.6983° E",
                    phone = "(555) 987-6543",
                    studyTime = "Friday Sunset Ingress: 6:30 PM",
                    worshipTime = "Sabbath Morning Blessing: 9:00 AM",
                    details = "A peaceful getaway location embedded in nature gardens, ideal for quiet retreats and sunset prayer."
                ),
                ChurchCampus(
                    name = "Grace Covenant - East Fellowship Cabin",
                    address = "88 Jordan Crossing, Valley Green",
                    coordinates = "31.9522° N, 35.9284° E",
                    phone = "(555) 555-7777",
                    studyTime = "Sabbath Outdoor Fellowship: 4:30 PM",
                    worshipTime = "Sunset Fellowship Service: 6:00 PM",
                    details = "Our rustic valley location hosting riverside baptism studies and communal outdoor potlucks."
                )
            )
            for (campus in defaultCampuses) {
                campusDao.insertCampus(campus)
            }
        }
    }
}
