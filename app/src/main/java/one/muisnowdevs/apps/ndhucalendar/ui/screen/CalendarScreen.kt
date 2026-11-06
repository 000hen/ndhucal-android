package one.muisnowdevs.apps.ndhucalendar.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import one.muisnowdevs.apps.ndhucalendar.EventFavorite
import one.muisnowdevs.apps.ndhucalendar.model.CalendarEntry
import one.muisnowdevs.apps.ndhucalendar.ui.component.DateHeader
import one.muisnowdevs.apps.ndhucalendar.ui.component.ErrorDetailSheet
import one.muisnowdevs.apps.ndhucalendar.ui.component.EventCard
import one.muisnowdevs.apps.ndhucalendar.ui.component.EventDetailSheet
import one.muisnowdevs.apps.ndhucalendar.ui.component.SwipeToFavoriteBox
import one.muisnowdevs.apps.ndhucalendar.viewmodel.CalendarViewModel
import uniffi.ndhucal.Category
import java.time.LocalDate

private typealias Section = Pair<LocalDate, List<CalendarEntry>>

/**
 * The school calendar grouped under one sticky header per start date. Pull to refresh, tap a row
 * for its details, swipe it to star it.
 *
 * The filters are the caller's, since the controls that change them sit in the app bar.
 * An empty [selectedCategories] means every category.
 */
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    showFavoritesOnly: Boolean,
    selectedCategories: Set<Category>,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.entries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val context = LocalContext.current
    val favorites = remember(context) { EventFavorite.getInstance(context) }
    val favoriteKeys by favorites.favorites.collectAsState()

    // Taken again on every load, so a refresh after midnight moves "today" along with it.
    val today = remember(entries) { LocalDate.now() }

    val sections = remember(entries, favoriteKeys, showFavoritesOnly, selectedCategories) {
        entries
            .filter { !showFavoritesOnly || it.key in favoriteKeys }
            .filter { selectedCategories.isEmpty() || it.category in selectedCategories }
            .groupBy { it.start }
            .toSortedMap()
            .toList()
    }

    var detailedKey by rememberSaveable { mutableStateOf<String?>(null) }

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = viewModel::reload,
        modifier = modifier
    ) {
        when {
            // A failed refresh keeps the list it already had; only an empty one gives way.
            error != null && entries.isEmpty() -> LoadError(error, onRetry = viewModel::reload)
            entries.isNotEmpty() && sections.isEmpty() -> EmptyResult()
            else -> CalendarList(
                sections = sections,
                today = today,
                favoriteKeys = favoriteKeys,
                onFavoriteChange = favorites::setFavorite,
                onEntryClick = { detailedKey = it.key },
                scrollToTodayKey = entries
            )
        }
    }

    // Held by key so the open sheet survives a rotation and a reload that rebuilds the entries.
    entries.firstOrNull { it.key == detailedKey }?.let { entry ->
        EventDetailSheet(entry, today) { detailedKey = null }
    }
}

@Composable
private fun CalendarList(
    sections: List<Section>,
    today: LocalDate,
    favoriteKeys: Set<String>,
    onFavoriteChange: (key: String, favorite: Boolean) -> Unit,
    onEntryClick: (CalendarEntry) -> Unit,
    scrollToTodayKey: Any,
    modifier: Modifier = Modifier
) {
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Opens on the first date from today on, once per load rather than on every filter change,
    // so toggling a filter does not yank the list away from where the reader was.
    var scrolledFor by rememberSaveable { mutableStateOf<Int?>(null) }
    LaunchedEffect(scrollToTodayKey) {
        val loadId = scrollToTodayKey.hashCode()
        if (sections.isEmpty() || scrolledFor == loadId) return@LaunchedEffect
        state.scrollToItem(indexOfFirstSectionFrom(sections, today))
        scrolledFor = loadId
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = state,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for ((date, sectionEntries) in sections) {
            stickyHeader(key = "header-$date") { headerIndex ->
                val isFloating by isHeaderFloating(state, headerIndex)

                DateHeader(
                    date = date,
                    today = today,
                    isFloating = isFloating,
                    onClick = { scope.launch { state.animateScrollToItem(headerIndex) } },
                    modifier = Modifier.animateItem()
                )
            }

            items(sectionEntries, key = { it.key }) { entry ->
                val isFavorite = entry.key in favoriteKeys

                SwipeToFavoriteBox(
                    isFavorite = isFavorite,
                    onFavoriteChange = { onFavoriteChange(entry.key, it) },
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .animateItem()
                ) {
                    EventCard(
                        entry = entry,
                        today = today,
                        isFavorite = isFavorite,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onEntryClick(entry) }
                    )
                }
            }
        }
    }
}

@Composable
private fun isHeaderFloating(state: LazyListState, headerIndex: Int) = remember(headerIndex) {
    derivedStateOf {
        state.firstVisibleItemIndex > headerIndex ||
                (state.firstVisibleItemIndex == headerIndex && state.firstVisibleItemScrollOffset > 0)
    }
}

/** Lazy-list index of the first header dated today or later, or of the last one if all are past. */
private fun indexOfFirstSectionFrom(sections: List<Section>, today: LocalDate): Int {
    var index = 0
    var lastHeader = 0
    for ((date, entries) in sections) {
        if (date >= today) return index
        lastHeader = index
        index += 1 + entries.size
    }
    return lastHeader
}

/** Stands in for the list when the calendar will not load; the exception sits behind a sheet. */
@Composable
private fun LoadError(error: Throwable?, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    var isDetailOpen by rememberSaveable { mutableStateOf(false) }

    // Scrollable so the pull-to-refresh gesture still reaches the box underneath.
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Rounded.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )

        Text(text = "無法載入行事曆", style = MaterialTheme.typography.titleMedium)

        Text(
            text = "可能是連線出了問題，或是學校網頁格式有變動。",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onRetry) { Text("重試") }
            if (error != null) {
                TextButton(onClick = { isDetailOpen = true }) { Text("詳細資訊") }
            }
        }
    }

    if (isDetailOpen && error != null) {
        ErrorDetailSheet(error) { isDetailOpen = false }
    }
}

@Composable
private fun EmptyResult(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Rounded.EventBusy,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Text(text = "沒有符合篩選的行程", style = MaterialTheme.typography.titleMedium)
    }
}
