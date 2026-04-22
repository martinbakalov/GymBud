package com.gymbud.app.data.repository

import com.gymbud.app.data.local.dao.ExerciseDao
import com.gymbud.app.data.local.dao.WorkoutDao
import com.gymbud.app.data.local.dao.WorkoutExerciseDao
import com.gymbud.app.data.local.dao.WorkoutSetDao
import com.gymbud.app.data.local.entity.Workout
import com.gymbud.app.data.local.entity.WorkoutExercise
import com.gymbud.app.data.local.entity.WorkoutSet
import com.gymbud.app.domain.model.PreviousSet
import com.gymbud.app.domain.model.WorkoutStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val workoutSetDao: WorkoutSetDao,
    private val exerciseDao: ExerciseDao
) {


    fun observeTemplates(): Flow<List<Workout>> = workoutDao.observeTemplates()
    fun observeHistory(): Flow<List<Workout>> = workoutDao.observeHistory()
    fun observeActiveWorkout(): Flow<Workout?> = workoutDao.observeActiveWorkout()

    fun observeExercisesForWorkout(workoutId: Long): Flow<List<WorkoutExercise>> =
        workoutExerciseDao.observeForWorkout(workoutId)

    fun observeSetsForWorkoutExercise(workoutExerciseId: Long): Flow<List<WorkoutSet>> =
        workoutSetDao.observeForWorkoutExercise(workoutExerciseId)

    fun observeHistoryWithStats(): Flow<List<Pair<Workout, WorkoutStats>>> =
        workoutDao.observeHistory().map { workouts ->
            workouts.map { w -> w to statsFor(w.id) }
        }

    suspend fun getWorkout(id: Long): Workout? = workoutDao.getById(id)


    suspend fun createTemplate(name: String): Long =
        workoutDao.insert(Workout(name = name, isTemplate = true))

    suspend fun renameWorkout(workoutId: Long, newName: String) {
        val workout = workoutDao.getById(workoutId) ?: return
        workoutDao.update(workout.copy(name = newName))
    }

    suspend fun deleteWorkout(workout: Workout): Int = workoutDao.delete(workout)


    suspend fun startEmptyWorkout(): Long =
        workoutDao.insert(
            Workout(
                name = "",
                isTemplate = false,
                startedAt = System.currentTimeMillis()
            )
        )


    suspend fun startFromTemplate(templateId: Long): Long {
        val template = workoutDao.getById(templateId)
            ?: error("Template $templateId not found")

        val sessionId = workoutDao.insert(
            Workout(
                name = template.name,
                isTemplate = false,
                startedAt = System.currentTimeMillis()
            )
        )

        val templateExercises = workoutExerciseDao
            .observeForWorkout(templateId)
            .firstValueOrEmpty()

        for (we in templateExercises) {
            val newWorkoutExerciseId = workoutExerciseDao.insert(
                we.copy(id = 0, workoutId = sessionId)
            )

            val templateSets = workoutSetDao
                .observeForWorkoutExercise(we.id)
                .firstValueOrEmpty()

            if (templateSets.isNotEmpty()) {
                workoutSetDao.insertAll(
                    templateSets.map {
                        it.copy(
                            id = 0,
                            workoutExerciseId = newWorkoutExerciseId,
                            isCompleted = false  // always reset
                        )
                    }
                )
            }
        }

        return sessionId
    }


    suspend fun addExerciseToWorkout(workoutId: Long, exerciseId: Long): Long {
        val position = workoutExerciseDao.nextPosition(workoutId)
        return workoutExerciseDao.insert(
            WorkoutExercise(
                workoutId = workoutId,
                exerciseId = exerciseId,
                position = position
            )
        )
    }

    suspend fun removeExerciseFromWorkout(workoutExercise: WorkoutExercise) {
        workoutExerciseDao.delete(workoutExercise)
    }

    suspend fun updateExerciseNotes(workoutExercise: WorkoutExercise, notes: String?) {
        workoutExerciseDao.update(workoutExercise.copy(notes = notes))
    }

    suspend fun addSet(workoutExerciseId: Long): Long {
        val position = workoutSetDao.nextPosition(workoutExerciseId)
        return workoutSetDao.insert(
            WorkoutSet(
                workoutExerciseId = workoutExerciseId,
                position = position
            )
        )
    }

    suspend fun updateSet(set: WorkoutSet) = workoutSetDao.update(set)

    suspend fun deleteSet(set: WorkoutSet) = workoutSetDao.delete(set)

    suspend fun finishWorkout(workoutId: Long) {
        val workout = workoutDao.getById(workoutId) ?: return
        if (workout.endedAt != null) return // already finished
        workoutDao.update(workout.copy(endedAt = System.currentTimeMillis()))
    }

    suspend fun statsFor(workoutId: Long): WorkoutStats {
        val workout = workoutDao.getById(workoutId)
        val exercises = workoutExerciseDao.observeForWorkout(workoutId).firstValueOrEmpty()

        var totalSets = 0
        var totalVolumeKg = 0f

        for (we in exercises) {
            val sets = workoutSetDao.observeForWorkoutExercise(we.id).firstValueOrEmpty()
            for (s in sets) {
                if (!s.isCompleted) continue
                totalSets += 1
                val w = s.weightKg
                val r = s.reps
                if (w != null && r != null) {
                    totalVolumeKg += w * r
                }
            }
        }

        val duration = if (workout?.startedAt != null && workout.endedAt != null) {
            workout.endedAt - workout.startedAt
        } else null

        return WorkoutStats(
            totalSets = totalSets,
            totalVolumeKg = totalVolumeKg,
            durationMillis = duration
        )
    }

    suspend fun previousSetsFor(
        exerciseId: Long,
        excludeWorkoutId: Long
    ): List<PreviousSet> {
        val sets = workoutSetDao.findPreviousSetsForExercise(exerciseId, excludeWorkoutId)
        if (sets.isEmpty()) return emptyList()

        val mostRecentWorkoutExerciseIds = sets
            .groupBy { it.workoutExerciseId }
            .entries
            .first()
            .value

        return mostRecentWorkoutExerciseIds
            .sortedBy { it.position }
            .map { PreviousSet(it.weightKg, it.reps, it.durationSeconds) }
    }
}
private suspend fun <T> Flow<List<T>>.firstValueOrEmpty(): List<T> = first()