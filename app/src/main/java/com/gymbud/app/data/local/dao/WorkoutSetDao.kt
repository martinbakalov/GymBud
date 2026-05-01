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

    @Update
    suspend fun updateAll(sets: List<WorkoutSet>)

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
    SELECT ws.* FROM workout_sets ws
    INNER JOIN workout_exercises we ON ws.workoutExerciseId = we.id
    INNER JOIN workouts w ON we.workoutId = w.id
    WHERE we.exerciseId = :exerciseId
      AND ws.position = :position
      AND ws.isCompleted = 1
      AND w.isTemplate = 0
      AND w.endedAt IS NOT NULL
      AND w.id != :excludingWorkoutId
    ORDER BY w.endedAt DESC
    LIMIT 1
    """
    )
    suspend fun findPreviousSet(
        exerciseId: Long,
        position: Int,
        excludingWorkoutId: Long
    ): WorkoutSet?
}