package com.gymbud.app.data.repository

import com.gymbud.app.data.local.dao.ProfileDao
import com.gymbud.app.data.local.entity.Profile
import kotlinx.coroutines.flow.Flow

class ProfileRepository(
    private val dao: ProfileDao
) {
    fun observe(): Flow<Profile?> = dao.observe()

    suspend fun get(): Profile? = dao.get()

    suspend fun upsert(profile: Profile): Long = dao.upsert(profile)

    fun observeFinishedWorkoutCount(): Flow<Int> = dao.observeFinishedWorkoutCount()
}