package com.gymbud.app.ui.screens.exercises

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymbud.app.GymBudApplication
import com.gymbud.app.R
import com.gymbud.app.data.local.entity.Exercise
import com.gymbud.app.domain.model.Equipment
import com.gymbud.app.domain.model.MuscleGroup
import com.gymbud.app.ui.util.labelRes
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import com.gymbud.app.ui.util.displayName
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(onCreateExerciseClick: () -> Unit) {

    val app = LocalContext.current.applicationContext as GymBudApplication
    val viewModel: ExerciseListViewModel = viewModel(
        factory = ExerciseListViewModel.Factory(app.exerciseRepository)
    )

    var pendingDelete by remember { mutableStateOf<Exercise?>(null) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.exercises_title)) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateExerciseClick) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.exercises_new))
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text(stringResource(R.string.exercises_search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            FilterChipsRow(
                allLabel = stringResource(R.string.exercises_filter_all_muscles),
                selected = uiState.muscleFilter,
                options = MuscleGroup.entries.toList(),
                labelFor = { stringResource(it.labelRes()) },
                onSelect = viewModel::onMuscleFilterChange
            )

            FilterChipsRow(
                allLabel = stringResource(R.string.exercises_filter_all_equipment),
                selected = uiState.equipmentFilter,
                options = Equipment.entries.toList(),
                labelFor = { stringResource(it.labelRes()) },
                onSelect = viewModel::onEquipmentFilterChange
            )

            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))

            if (exercises.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.exercises_no_results),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items = exercises, key = { it.id }) { exercise ->
                        ExerciseRow(
                            exercise = exercise,
                            onLongPress = if (exercise.isCustom) {
                                { pendingDelete = exercise }
                            } else null
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
    pendingDelete?.let { exercise ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.exercise_delete_title)) },
            text = { Text(stringResource(R.string.exercise_delete_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCustom(exercise)
                        pendingDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> FilterChipsRow(
    allLabel: String,
    selected: T?,
    options: List<T>,
    labelFor: @Composable (T) -> String,
    onSelect: (T?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text(allLabel) }
        )
        options.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(if (selected == option) null else option) },
                label = { Text(labelFor(option)) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExerciseRow(
    exercise: Exercise,
    onLongPress: (() -> Unit)?
) {
    val rowModifier = if (onLongPress != null) {
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onLongPress
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    }

    Column(modifier = rowModifier) {
        Text(
            text = exercise.displayName(),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${stringResource(exercise.primaryMuscle.labelRes())} · ${stringResource(exercise.equipment.labelRes())}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}