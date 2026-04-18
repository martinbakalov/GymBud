package com.gymbud.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gymbud.app.data.local.entity.WorkoutSet
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSetDao {

    @Insert
    suspend fun insert(set: WorkoutSet): Long

    @Insert
    suspend fun insertAll(sets: List<WorkoutSet>): List<Long>

    @Update
    suspend fun update(set: WorkoutSet): Int

    @Delete
    suspend fun delete(set: WorkoutSet): Int

    @Query("SELECT * FROM workout_sets WHERE id = :id")
    suspend fun getById(id: Long): WorkoutSet?

    @Query(
        "SELECT * FROM workout_sets " +
                "WHERE workoutExerciseId = :workoutExerciseId " +
                "ORDER BY position ASC"
    )
    fun observeForWorkoutExercise(workoutExerciseId: Long): Flow<List<WorkoutSet>>

    @Query("SELECT COALESCE(MAX(position) + 1, 0) FROM workout_sets WHERE workoutExerciseId = :workoutExerciseId")
    suspend fun nextPosition(workoutExerciseId: Long): Int

    @Query(
        """
        SELECT s.* FROM workout_sets s
        INNER JOIN workout_exercises we ON s.workoutExerciseId = we.id
        INNER JOIN workouts w ON we.workoutId = w.id
        WHERE we.exerciseId = :exerciseId
          AND w.isTemplate = 0
          AND w.endedAt IS NOT NULL
          AND w.id != :excludeWorkoutId
          AND s.isCompleted = 1
        ORDER BY w.endedAt DESC, s.position ASC
        """
    )
    suspend fun findPreviousSetsForExercise(
        exerciseId: Long,
        excludeWorkoutId: Long
    ): List<WorkoutSet>
}