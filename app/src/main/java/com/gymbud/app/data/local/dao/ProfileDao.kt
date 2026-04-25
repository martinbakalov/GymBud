package com.gymbud.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gymbud.app.data.local.entity.Profile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Upsert
    suspend fun upsert(profile: Profile): Long

    @Query("SELECT * FROM profile WHERE id = 1")
    fun observe(): Flow<Profile?>

    @Query("SELECT * FROM profile WHERE id = 1")
    suspend fun get(): Profile?

    @Query(
        "SELECT COUNT(*) FROM workouts " +
                "WHERE isTemplate = 0 AND endedAt IS NOT NULL"
    )
    fun observeFinishedWorkoutCount(): Flow<Int>
}