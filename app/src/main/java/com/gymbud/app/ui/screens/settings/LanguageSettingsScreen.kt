package com.gymbud.app.ui.screens.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymbud.app.GymBudApplication
import com.gymbud.app.R
import com.gymbud.app.domain.model.AppLanguage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsScreen(onExit: () -> Unit) {
    val app = LocalContext.current.applicationContext as GymBudApplication
    val prefs = app.preferences
    val scope = rememberCoroutineScope()

    val current by prefs.language.collectAsStateWithLifecycle(initialValue = AppLanguage.SYSTEM)

    fun apply(lang: AppLanguage) {
        scope.launch {
            prefs.setLanguage(lang)
            val locales = if (lang == AppLanguage.SYSTEM) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(lang.tag)
            }
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_language_title)) },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            LanguageOptionRow(
                label = stringResource(R.string.settings_language_system),
                selected = current == AppLanguage.SYSTEM,
                onClick = { apply(AppLanguage.SYSTEM) }
            )
            HorizontalDivider()

            LanguageOptionRow(
                label = stringResource(R.string.settings_language_english),
                selected = current == AppLanguage.ENGLISH,
                onClick = { apply(AppLanguage.ENGLISH) }
            )
            HorizontalDivider()

            LanguageOptionRow(
                label = stringResource(R.string.settings_language_bulgarian),
                selected = current == AppLanguage.BULGARIAN,
                onClick = { apply(AppLanguage.BULGARIAN) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun LanguageOptionRow(
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
        RadioButton(selected = selected, onClick = null)
        androidx.compose.foundation.layout.Spacer(Modifier.padding(horizontal = 6.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}