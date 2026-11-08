package one.muisnowdevs.apps.ndhucalendar.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timelapse
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import one.muisnowdevs.apps.ndhucalendar.model.CalendarEntry
import one.muisnowdevs.apps.ndhucalendar.model.statusText
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import uniffi.ndhucal.Subcategory

private val FullDate = DateTimeFormatter.ofPattern("y/M/d（E）", Locale.TRADITIONAL_CHINESE)

/** Full detail of one event, each line marked by an icon rather than a label. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailSheet(entry: CalendarEntry, today: LocalDate, onDismissRequest: () -> Unit = {}) {
    val rangeText = remember(entry) {
        if (entry.dayCount == 1L) FullDate.format(entry.start)
        else "${FullDate.format(entry.start)} – ${FullDate.format(entry.end)}"
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryBadge(entry)
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider()

            DetailRow(entry.category.icon, "分類", stringResource(entry.category.nameRes))
            entry.subcategory.takeIf { it != Subcategory.OTHER }?.let {
                DetailRow(Icons.AutoMirrored.Rounded.Label, "類型", stringResource(it.nameRes))
            }
            DetailRow(Icons.Rounded.DateRange, "日期", rangeText)
            DetailRow(Icons.Rounded.Timelapse, "天數", "共 ${entry.dayCount} 天")
            DetailRow(Icons.Rounded.Schedule, "狀態", statusText(entry, today))
            if (entry.isImportant) DetailRow(Icons.Rounded.PriorityHigh, "重要性", "重要日程")
            if (entry.source.isNotBlank()) DetailRow(Icons.Rounded.Flag, "來源", entry.source)
        }
    }
}

/** One detail line. The icon names what the value is; [label] carries that to screen readers. */
@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = "$label：$value" },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}
