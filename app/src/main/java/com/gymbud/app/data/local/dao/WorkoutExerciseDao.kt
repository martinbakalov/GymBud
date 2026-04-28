package com.gymbud.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.gymbud.app.data.local.entity.WorkoutExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutExerciseDao {

    @Insert
    suspend fun insert(workoutExercise: WorkoutExercise): Long

    @Delete
    suspend fun delete(workoutExercise: WorkoutExercise): Int

    @Query("SELECT * FROM workout_exercises WHERE id = :id")
    suspend fun getById(id: Long): WorkoutExercise?

    @Query(
        "SELECT * FROM workout_exercises " +
                "WHERE workoutId = :workoutId " +
                "ORDER BY position ASC"
    )
    fun observeForWorkout(workoutId: Long): Flow<List<WorkoutExercise>>

    @Query("UPDATE workout_exercises SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String?): Int

    @Query("SELECT COALESCE(MAX(position) + 1, 0) FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun nextPosition(workoutId: Long): Int
}