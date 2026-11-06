package one.muisnowdevs.apps.ndhucalendar.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
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

private sealed interface ScheduleContent {
    data object Loading : ScheduleContent
    data class Schedule(val sections: List<Section>) : ScheduleContent
    data object NoMatch : ScheduleContent
    data class Failed(val error: Throwable) : ScheduleContent
}

/**
 * The school calendar as a day-by-day schedule: one sticky header per day with something on, and
 * an event spanning several days repeated under each of them. Pull to refresh, tap a row for its
 * details, swipe it to star it.
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
            .let(::scheduleByDay)
    }

    val content = when {
        entries.isNotEmpty() ->
            if (sections.isEmpty()) ScheduleContent.NoMatch else ScheduleContent.Schedule(sections)
        // A failed refresh keeps the list it already had; only an empty one gives way.
        else -> error?.let(ScheduleContent::Failed) ?: ScheduleContent.Loading
    }

    var detailedKey by rememberSaveable { mutableStateOf<String?>(null) }

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = viewModel::reload,
        modifier = modifier
    ) {
        // Keyed by kind, so the outgoing state keeps drawing its own data while it fades out.
        AnimatedContent(
            targetState = content,
            modifier = Modifier.fillMaxSize(),
            contentKey = { it::class },
            label = "schedule content"
        ) { target ->
            when (target) {
                ScheduleContent.Loading -> Box(Modifier.fillMaxSize())
                ScheduleContent.NoMatch -> EmptyResult()
                is ScheduleContent.Failed -> LoadError(target.error, onRetry = viewModel::reload)
                is ScheduleContent.Schedule -> CalendarList(
                    sections = target.sections,
                    today = today,
                    favoriteKeys = favoriteKeys,
                    onFavoriteChange = favorites::setFavorite,
                    onEntryClick = { detailedKey = it.key },
                    scrollToTodayKey = entries
                )
            }
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

    // The sections from this one on are today or later; the line sits just above it.
    val firstUpcoming = sections.indexOfFirst { (date, _) -> date >= today }
        .let { if (it == -1) sections.size else it }

    // Opens on the now line, once per load rather than on every filter change,
    // so toggling a filter does not yank the list away from where the reader was.
    var scrolledFor by rememberSaveable { mutableStateOf<Int?>(null) }
    LaunchedEffect(scrollToTodayKey) {
        val loadId = scrollToTodayKey.hashCode()
        if (sections.isEmpty() || scrolledFor == loadId) return@LaunchedEffect
        state.scrollToItem(sections.take(firstUpcoming).sumOf { (_, entries) -> 1 + entries.size })
        scrolledFor = loadId
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = state,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for ((index, section) in sections.withIndex()) {
            val (date, sectionEntries) = section
            if (index == firstUpcoming) item(key = "now-line") { NowLine(Modifier.animateItem()) }

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

            items(sectionEntries, key = { "$date/${it.key}" }) { entry ->
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
                        day = date,
                        today = today,
                        isFavorite = isFavorite,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onEntryClick(entry) }
                    )
                }
            }
        }
        if (firstUpcoming == sections.size) item(key = "now-line") { NowLine(Modifier.animateItem()) }
    }
}

/** Marks where the past ends and today begins. */
@Composable
private fun NowLine(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    Row(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        HorizontalDivider(thickness = 2.dp, color = color)
    }
}

@Composable
private fun isHeaderFloating(state: LazyListState, headerIndex: Int) = remember(headerIndex) {
    derivedStateOf {
        state.firstVisibleItemIndex > headerIndex ||
                (state.firstVisibleItemIndex == headerIndex && state.firstVisibleItemScrollOffset > 0)
    }
}

/**
 * Every day with something on, each listing the events it falls within: those starting that day
 * first, then those carried over from earlier days.
 */
private fun scheduleByDay(entries: List<CalendarEntry>): List<Section> {
    val byDay = sortedMapOf<LocalDate, MutableList<CalendarEntry>>()
    for (entry in entries) {
        // do-while so an entry whose end comes before its start still shows on its start day.
        var day = entry.start
        do {
            byDay.getOrPut(day) { mutableListOf() }.add(entry)
            day = day.plusDays(1)
        } while (day <= entry.end)
    }
    return byDay.map { (day, dayEntries) -> day to dayEntries.sortedBy { it.start != day } }
}

/** Stands in for the list when the calendar will not load; the exception sits behind a sheet. */
@Composable
private fun LoadError(error: Throwable, onRetry: () -> Unit, modifier: Modifier = Modifier) {
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
            TextButton(onClick = { isDetailOpen = true }) { Text("詳細資訊") }
        }
    }

    if (isDetailOpen) {
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
