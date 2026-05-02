package com.gymbud.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.gymbud.app.GymBudApplication
import com.gymbud.app.R
import com.gymbud.app.data.local.entity.Profile
import com.gymbud.app.data.local.entity.Workout
import com.gymbud.app.domain.model.WeightUnit
import com.gymbud.app.domain.model.WorkoutStats
import com.gymbud.app.ui.util.formatDurationCompact
import com.gymbud.app.ui.util.formatSessionDateTime
import com.gymbud.app.ui.util.formatVolumeNumber
import java.io.File

private val prAmber = Color(0xFFFFB300)
private val profileBlue = Color(0xFF3B82F6)

@Composable
fun ProfileScreen(
    onWorkoutClick: (Long) -> Unit,
    onOpenSettings: () -> Unit
) {
    val app = LocalContext.current.applicationContext as GymBudApplication
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(
            profileRepository = app.profileRepository,
            workoutRepository = app.workoutRepository
        )
    )

    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val workoutCount by viewModel.workoutCount.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val globalUnit by app.preferences.weightUnit
        .collectAsStateWithLifecycle(initialValue = WeightUnit.KG)

    val totalPrs = remember(history) { history.sumOf { it.second.prCount } }

    var pendingDelete by remember { mutableStateOf<Workout?>(null) }

    Scaffold(contentWindowInsets = WindowInsets(0)) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item(key = "header") {
                ProfileHeader(profile = profile, onOpenSettings = onOpenSettings)
            }

            item(key = "stats_label") {
                Text(
                    text = stringResource(R.string.profile_overview_label).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }

            item(key = "stats") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OverviewStatCard(
                        value = workoutCount.toString(),
                        label = stringResource(R.string.profile_workouts_count_label),
                        accentColor = profileBlue,
                        iconContent = {
                            Icon(
                                painter = painterResource(R.drawable.ic_nav_exercises),
                                contentDescription = null,
                                tint = profileBlue.copy(alpha = 0.35f),
                                modifier = Modifier.size(32.dp)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    OverviewStatCard(
                        value = totalPrs.toString(),
                        label = stringResource(R.string.stats_prs),
                        accentColor = prAmber,
                        iconContent = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = prAmber.copy(alpha = 0.35f),
                                modifier = Modifier.size(32.dp)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item(key = "history_label") {
                SectionLabel(
                    icon = Icons.AutoMirrored.Filled.List,
                    text = stringResource(R.string.profile_workout_history),
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 16.dp)
                )
            }

            if (history.isEmpty()) {
                item(key = "empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = stringResource(R.string.profile_empty_history),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(items = history, key = { _, pair -> pair.first.id }) { index, (workout, stats) ->
                    WorkoutHistoryCard(
                        workout = workout,
                        stats = stats,
                        unit = globalUnit,
                        onClick = { onWorkoutClick(workout.id) },
                        onDelete = { pendingDelete = workout },
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(bottom = if (index < history.size - 1) 12.dp else 0.dp)
                    )
                }
            }
        }
    }

    pendingDelete?.let { workout ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.history_delete_title)) },
            text = { Text(stringResource(R.string.history_delete_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWorkout(workout)
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

@Composable
private fun ProfileHeader(
    profile: Profile?,
    onOpenSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            profileBlue.copy(alpha = 0.10f),
                            Color(0xFF6366F1).copy(alpha = 0.05f),
                            Color(0xFFA855F7).copy(alpha = 0.10f)
                        )
                    )
                )
        )

        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.settings_title),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 52.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val avatarPath = profile?.avatarPath
            if (avatarPath != null && File(avatarPath).exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(File(avatarPath))
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .diskCachePolicy(CachePolicy.DISABLED)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = profile?.displayName?.ifBlank { null }
                    ?: stringResource(R.string.profile_default_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            val bio = profile?.bio?.ifBlank { null }
            if (bio != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = bio,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 36.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun OverviewStatCard(
    value: String,
    label: String,
    accentColor: Color,
    iconContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 12.dp, y = 12.dp)
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                iconContent()
            }

            Column(
                modifier = Modifier.align(Alignment.TopStart),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = label.uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkoutHistoryCard(
    workout: Workout,
    stats: WorkoutStats,
    unit: WeightUnit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unitSuffix = stringResource(
        if (unit == WeightUnit.KG) R.string.workout_kg else R.string.workout_lbs
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val photoPath = workout.photoPath
            if (photoPath != null && File(photoPath).exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(File(photoPath))
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .diskCachePolicy(CachePolicy.DISABLED)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = workout.name.ifBlank { stringResource(R.string.workout_empty_name) },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = formatSessionDateTime(workout.endedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }

                Spacer(Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MiniStatChip(
                        icon = Icons.Default.AccessTime,
                        text = formatDurationCompact(stats.durationMillis)
                    )
                    MiniStatChip(
                        icon = Icons.Default.FitnessCenter,
                        text = "${formatVolumeNumber(stats.totalVolumeKg, unit)} $unitSuffix"
                    )
                    MiniStatChip(
                        icon = Icons.AutoMirrored.Filled.List,
                        text = "${stats.totalSets} ${stringResource(R.string.stats_sets)}"
                    )
                    if (stats.prCount > 0) {
                        MiniStatChip(
                            icon = Icons.Default.Star,
                            text = "${stats.prCount} ${stringResource(R.string.stats_prs)}",
                            isPr = true
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun MiniStatChip(
    icon: ImageVector,
    text: String,
    isPr: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = if (isPr) prAmber else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (isPr) prAmber else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}
