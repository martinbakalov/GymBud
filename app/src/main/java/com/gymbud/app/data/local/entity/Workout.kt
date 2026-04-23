package com.gymbud.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val isTemplate: Boolean = false,
    val startedAt: Long? = null,
    val endedAt: Long? = null,
    val notes: String? = null,
    val photoPath: String? = null
)