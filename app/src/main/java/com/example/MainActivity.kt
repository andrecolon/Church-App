package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.local.AppDatabase
import com.example.data.repository.ChurchRepository
import com.example.ui.screens.ChurchAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ChurchViewModel
import com.example.ui.viewmodel.ChurchViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize DB components
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ChurchRepository(
            prayerDao = database.prayerDao(),
            eventDao = database.eventDao(),
            potluckDao = database.potluckDao()
        )

        // 2. Instantiate Church Viewmodel with factory
        val viewModel: ChurchViewModel by viewModels {
            ChurchViewModelFactory(repository)
        }

        // 3. Set content layout with custom theme
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChurchAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}
