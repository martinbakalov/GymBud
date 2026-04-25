package com.gymbud.app.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymbud.app.GymBudApplication
import com.gymbud.app.R
import com.gymbud.app.domain.model.ThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(onExit: () -> Unit) {
    val app = LocalContext.current.applicationContext as GymBudApplication
    val prefs = app.preferences
    val scope = rememberCoroutineScope()

    val current by prefs.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_theme_title)) },
                navigationIcon = {
                    IconButton(onClick = onExit) {
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
        ) {
            ThemeOptionRow(
                label = stringResource(R.string.settings_theme_system),
                selected = current == ThemeMode.SYSTEM,
                onClick = {
                    scope.launch { prefs.setThemeMode(ThemeMode.SYSTEM) }
                }
            )
            HorizontalDivider()

            ThemeOptionRow(
                label = stringResource(R.string.settings_theme_light),
                selected = current == ThemeMode.LIGHT,
                onClick = {
                    scope.launch { prefs.setThemeMode(ThemeMode.LIGHT) }
                }
            )
            HorizontalDivider()

            ThemeOptionRow(
                label = stringResource(R.string.settings_theme_dark),
                selected = current == ThemeMode.DARK,
                onClick = {
                    scope.launch { prefs.setThemeMode(ThemeMode.DARK) }
                }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun ThemeOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        androidx.compose.foundation.layout.Spacer(Modifier.padding(horizontal = 6.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}