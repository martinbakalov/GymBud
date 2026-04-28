package com.gymbud.app.ui.screens.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymbud.app.GymBudApplication
import com.gymbud.app.R
import com.gymbud.app.domain.model.Equipment
import com.gymbud.app.domain.model.ExerciseType
import com.gymbud.app.domain.model.MuscleGroup
import com.gymbud.app.ui.util.labelRes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateExerciseScreen(
    onDone: () -> Unit
) {
    val app = LocalContext.current.applicationContext as GymBudApplication
    val viewModel: CreateExerciseViewModel = viewModel(
        factory = CreateExerciseViewModel.Factory(app.exerciseRepository)
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.exercises_new)) },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Name
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.exercises_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            SectionLabel(stringResource(R.string.exercises_type))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val types = ExerciseType.entries
                types.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = state.type == type,
                        onClick = { viewModel.onTypeChange(type) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                        label = {
                            Text(
                                when (type) {
                                    ExerciseType.WEIGHT_REPS -> stringResource(R.string.exercises_type_weight_reps)
                                    ExerciseType.TIME        -> stringResource(R.string.exercises_type_time)
                                }
                            )
                        }
                    )
                }
            }

            SectionLabel(stringResource(R.string.exercises_equipment))
            ChipSelectRow(
                options = Equipment.entries.toList(),
                isSelected = { it == state.equipment },
                onSelect = viewModel::onEquipmentChange,
                labelFor = { stringResource(it.labelRes()) }
            )

            SectionLabel(stringResource(R.string.exercises_primary_muscle))
            ChipSelectRow(
                options = MuscleGroup.entries.toList(),
                isSelected = { it == state.primaryMuscle },
                onSelect = viewModel::onPrimaryMuscleChange,
                labelFor = { stringResource(it.labelRes()) }
            )

            // Secondary muscles — multi-select, excludes the primary
            SectionLabel(stringResource(R.string.exercises_secondary_muscles))
            ChipMultiSelectRow(
                options = MuscleGroup.entries.filter { it != state.primaryMuscle },
                isSelected = { it in state.secondaryMuscles },
                onToggle = viewModel::onToggleSecondaryMuscle,
                labelFor = { stringResource(it.labelRes()) }
            )

            Button(
                onClick = { viewModel.save(onSaved = onDone) },
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun <T> ChipSelectRow(
    options: List<T>,
    isSelected: (T) -> Boolean,
    onSelect: (T) -> Unit,
    labelFor: @Composable (T) -> String
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = isSelected(option),
                onClick = { onSelect(option) },
                label = { Text(labelFor(option)) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun <T> ChipMultiSelectRow(
    options: List<T>,
    isSelected: (T) -> Boolean,
    onToggle: (T) -> Unit,
    labelFor: @Composable (T) -> String
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = isSelected(option),
                onClick = { onToggle(option) },
                label = { Text(labelFor(option)) }
            )
        }
    }
}