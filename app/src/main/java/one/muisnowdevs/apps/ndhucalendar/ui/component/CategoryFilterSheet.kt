package one.muisnowdevs.apps.ndhucalendar.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import uniffi.ndhucal.Category

/**
 * Picks which categories the list shows, two to a row like the other pickers: filled when
 * selected, outlined when not. An empty [selected] set means every category, which is what the
 * leading 全部 option stands for.
 *
 * [categories] pairs each category with its display name, in the order they are laid out.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterSheet(
    categories: List<Pair<Category, String>>,
    selected: Set<Category>,
    onSelectedChange: (Set<Category>) -> Unit,
    onDismissRequest: () -> Unit
) {
    val options: List<Pair<Category?, String>> = listOf(null to "全部") + categories

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (pair in options.chunked(2)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for ((category, name) in pair) {
                        val isSelected =
                            if (category == null) selected.isEmpty() else category in selected

                        FilterButton(
                            text = name,
                            icon = category?.icon ?: Icons.Rounded.SelectAll,
                            isSelected = isSelected,
                            onClick = {
                                onSelectedChange(
                                    when {
                                        category == null -> emptySet()
                                        isSelected -> selected - category
                                        else -> selected + category
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FilterButton(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val content: @Composable () -> Unit = {
        Icon(icon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(text)
    }
    val stateModifier = modifier.semantics { this.selected = isSelected }

    if (isSelected) {
        FilledTonalButton(onClick = onClick, modifier = stateModifier) { content() }
    } else {
        OutlinedButton(onClick = onClick, modifier = stateModifier) { content() }
    }
}
