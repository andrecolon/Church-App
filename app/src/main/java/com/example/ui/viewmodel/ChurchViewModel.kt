package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.ChurchRepository
import com.example.utils.LocationCoordinates
import com.example.utils.SunsetCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChurchViewModel(private val repository: ChurchRepository) : ViewModel() {

    // Database flows converted to hot StateFlows
    val prayers: StateFlow<List<PrayerRequest>> = repository.allPrayers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val eventReminders: StateFlow<List<EventReminder>> = repository.allReminders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val potluckContributions: StateFlow<List<PotluckContribution>> = repository.allPotluckContributions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Public lists from Repository
    val staticEvents: List<CalendarEvent> = repository.events
    val holidays: List<BiblicalHoliday> = repository.holidays
    val bibleBooks: List<BibleBook> = repository.bibleBooks

    // Selected state variables
    private val _selectedLocation = MutableStateFlow(SunsetCalculator.locations[0]) // Defaults to Jerusalem
    val selectedLocation: StateFlow<LocationCoordinates> = _selectedLocation.asStateFlow()

    private val _selectedCalendar = MutableStateFlow(CalendarSystem.HILLEL) // Defaults to Hillel Rabbinic
    val selectedCalendar: StateFlow<CalendarSystem> = _selectedCalendar.asStateFlow()

    private val _searchQueryBible = MutableStateFlow("")
    val searchQueryBible: StateFlow<String> = _searchQueryBible.asStateFlow()

    private val _selectedBook = MutableStateFlow(repository.bibleBooks[0]) // Genesis
    val selectedBook: StateFlow<BibleBook> = _selectedBook.asStateFlow()

    private val _selectedChapterIndex = MutableStateFlow(0)
    val selectedChapterIndex: StateFlow<Int> = _selectedChapterIndex.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeSeedPrayersIfNeeded()
        }
    }

    fun selectLocation(location: LocationCoordinates) {
        _selectedLocation.value = location
    }

    fun selectCalendar(system: CalendarSystem) {
        _selectedCalendar.value = system
    }

    fun updateSearchQueryBible(query: String) {
        _searchQueryBible.value = query
    }

    fun selectBook(book: BibleBook) {
        _selectedBook.value = book
        _selectedChapterIndex.value = 0
    }

    fun selectChapterIndex(index: Int) {
        _selectedChapterIndex.value = index
    }

    // Repository Actions delegated
    fun submitPrayerRequest(title: String, content: String, category: String, requester: String) {
        viewModelScope.launch {
            val finalRequester = if (requester.trim().isEmpty()) "Anonymous" else requester
            val prayer = PrayerRequest(
                title = title,
                content = content,
                category = category,
                requester = finalRequester,
                isUserSubmitted = true
            )
            repository.insertPrayer(prayer)
        }
    }

    fun intercedeForPrayer(prayerId: Int) {
        viewModelScope.launch {
            repository.prayForRequest(prayerId)
        }
    }

    fun toggleAttending(eventId: String, isAttending: Boolean) {
        viewModelScope.launch {
            repository.toggleEventAttendance(eventId, isAttending)
        }
    }

    fun toggleReminder(eventId: String, isReminderSet: Boolean) {
        viewModelScope.launch {
            repository.toggleEventReminder(eventId, isReminderSet)
        }
    }

    fun deletePrayerRequest(id: Int) {
        viewModelScope.launch {
            repository.deletePrayer(id)
        }
    }

    // Potluck actions
    fun addPotluckContribution(contributor: String, dish: String, category: String, servings: Int, notes: String) {
        viewModelScope.launch {
            val finalContributor = if (contributor.trim().isBlank()) "Guest" else contributor
            val finalDish = if (dish.trim().isBlank()) "Tasty Sharing Dish" else dish
            val finalNotes = if (notes.trim().isBlank()) "Looking forward to sharing!" else notes
            repository.insertPotluckContribution(
                PotluckContribution(
                    contributorName = finalContributor,
                    dishName = finalDish,
                    category = category,
                    servings = if (servings <= 0) 8 else servings,
                    notes = finalNotes
                )
            )
        }
    }

    fun deletePotluckContribution(contribution: PotluckContribution) {
        viewModelScope.launch {
            repository.deletePotluckContribution(contribution)
        }
    }
}

class ChurchViewModelFactory(private val repository: ChurchRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChurchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChurchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
