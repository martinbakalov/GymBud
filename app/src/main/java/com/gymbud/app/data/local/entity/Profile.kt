package com.gymbud.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gymbud.app.model.Sex


@Entity(tableName = "profile")
data class Profile(
    @PrimaryKey
    val id: Long = 1L,
    val displayName: String? = null,
    val bio: String? = null,
    val sex: Sex? = null,
    val birthdayMillis: Long? = null,
    val avatarPath: String? = null
)