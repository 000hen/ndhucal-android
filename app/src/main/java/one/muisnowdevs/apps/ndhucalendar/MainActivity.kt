package one.muisnowdevs.apps.ndhucalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import one.muisnowdevs.apps.ndhucalendar.ui.component.CategoryFilterSheet
import one.muisnowdevs.apps.ndhucalendar.ui.component.FavoriteFilterButton
import one.muisnowdevs.apps.ndhucalendar.ui.screen.CalendarScreen
import one.muisnowdevs.apps.ndhucalendar.ui.theme.NDHUCalendarTheme
import one.muisnowdevs.apps.ndhucalendar.viewmodel.CalendarViewModel
import uniffi.ndhucal.Category

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NDHUCalendarTheme {
                // Hoisted here: the controls live in the app bar, the list they filter below it.
                var showFavoritesOnly by rememberSaveable { mutableStateOf(false) }
                var selectedCategories by rememberSaveable { mutableStateOf(emptySet<Category>()) }
                var isFilterOpen by rememberSaveable { mutableStateOf(false) }

                val viewModel: CalendarViewModel = viewModel()
                val entries by viewModel.entries.collectAsState()

                // Only categories the calendar actually uses, named once per load.
                val categories = remember(entries) {
                    entries.map { it.category to it.categoryName }.distinct().sortedBy { it.first }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text(stringResource(R.string.app_name)) },
                            navigationIcon = {
                                FavoriteFilterButton(
                                    isEnabled = showFavoritesOnly,
                                    onEnabledChange = { showFavoritesOnly = it }
                                )
                            },
                            actions = {
                                IconButton(onClick = { isFilterOpen = true }) {
                                    BadgedBox(
                                        badge = {
                                            if (selectedCategories.isNotEmpty()) {
                                                Badge { Text("${selectedCategories.size}") }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Rounded.FilterList,
                                            contentDescription = "篩選分類"
                                        )
                                    }
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    CalendarScreen(
                        viewModel = viewModel,
                        showFavoritesOnly = showFavoritesOnly,
                        selectedCategories = selectedCategories,
                        modifier = Modifier.padding(innerPadding)
                    )

                    if (isFilterOpen) {
                        CategoryFilterSheet(
                            categories = categories,
                            selected = selectedCategories,
                            onSelectedChange = { selectedCategories = it },
                            onDismissRequest = { isFilterOpen = false }
                        )
                    }
                }
            }
        }
    }
}
