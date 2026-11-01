package com.example.hashcheck.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

enum class HashMode {
    TEXT,
    FILE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeTabRow(
    selectedMode: HashMode,
    onModeSelected: (HashMode) -> Unit,
    modifier: Modifier = Modifier
) {
    PrimaryTabRow(
        selectedTabIndex = selectedMode.ordinal,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        Tab(
            selected = selectedMode == HashMode.TEXT,
            onClick = { onModeSelected(HashMode.TEXT) },
            text = { Text("Text Input") },
            icon = { Icon(Icons.Default.TextFields, contentDescription = null) }
        )
        Tab(
            selected = selectedMode == HashMode.FILE,
            onClick = { onModeSelected(HashMode.FILE) },
            text = { Text("File Input") },
            icon = { Icon(Icons.Default.Description, contentDescription = null) }
        )
    }
}
