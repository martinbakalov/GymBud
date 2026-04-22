package com.gymbud.app.ui.util

import com.gymbud.app.data.local.entity.Exercise
import com.gymbud.app.domain.model.WeightUnit


fun effectiveUnit(exercise: Exercise?, globalDefault: WeightUnit): WeightUnit =
    exercise?.preferredUnit ?: globalDefault