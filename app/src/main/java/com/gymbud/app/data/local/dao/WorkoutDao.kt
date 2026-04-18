package com.gymbud.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gymbud.app.data.local.entity.Workout
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {


    @Insert
    suspend fun insert(workout: Workout): Long

    @Update
    suspend fun update(workout: Workout): Int

    @Delete
    suspend fun delete(workout: Workout): Int


    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getById(id: Long): Workout?

    @Query(
        "SELECT * FROM workouts " +
                "WHERE isTemplate = 1 " +
                "ORDER BY name COLLATE NOCASE ASC"
    )
    fun observeTemplates(): Flow<List<Workout>>

    @Query(
        "SELECT * FROM workouts " +
                "WHERE isTemplate = 0 AND endedAt IS NOT NULL " +
                "ORDER BY endedAt DESC"
    )
    fun observeHistory(): Flow<List<Workout>>

    @Query(
        "SELECT * FROM workouts " +
                "WHERE isTemplate = 0 AND startedAt IS NOT NULL AND endedAt IS NULL " +
                "LIMIT 1"
    )
    fun observeActiveWorkout(): Flow<Workout?>
}