package com.gymbud.app.ui.util

import androidx.compose.ui.graphics.Color
import com.gymbud.app.domain.model.MuscleGroup
import com.gymbud.app.ui.theme.AccentAmber
import com.gymbud.app.ui.theme.AccentBlue
import com.gymbud.app.ui.theme.AccentCyan
import com.gymbud.app.ui.theme.AccentEmerald
import com.gymbud.app.ui.theme.AccentIndigo
import com.gymbud.app.ui.theme.AccentLime
import com.gymbud.app.ui.theme.AccentOrange
import com.gymbud.app.ui.theme.AccentPink
import com.gymbud.app.ui.theme.AccentPurple
import com.gymbud.app.ui.theme.AccentRose
import com.gymbud.app.ui.theme.AccentSlate
import com.gymbud.app.ui.theme.AccentTeal
import com.gymbud.app.ui.theme.AccentViolet

fun MuscleGroup.accentColor(): Color = when (this) {
    MuscleGroup.CHEST -> AccentBlue
    MuscleGroup.BACK -> AccentEmerald
    MuscleGroup.SHOULDERS -> AccentPurple
    MuscleGroup.BICEPS -> AccentAmber
    MuscleGroup.TRICEPS -> AccentCyan
    MuscleGroup.FOREARMS -> AccentTeal
    MuscleGroup.QUADS -> AccentIndigo
    MuscleGroup.HAMSTRINGS -> AccentOrange
    MuscleGroup.GLUTES -> AccentPink
    MuscleGroup.CALVES -> AccentLime
    MuscleGroup.CORE -> AccentRose
    MuscleGroup.FULL_BODY -> AccentViolet
    MuscleGroup.OTHER -> AccentSlate
}
