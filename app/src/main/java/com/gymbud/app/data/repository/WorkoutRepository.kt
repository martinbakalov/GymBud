package com.gymbud.app.data.repository

import com.gymbud.app.data.local.dao.ExerciseDao
import com.gymbud.app.data.local.dao.WorkoutDao
import com.gymbud.app.data.local.dao.WorkoutExerciseDao
import com.gymbud.app.data.local.dao.WorkoutSetDao
import com.gymbud.app.data.local.entity.Workout
import com.gymbud.app.data.local.entity.WorkoutExercise
import com.gymbud.app.data.local.entity.WorkoutSet
import com.gymbud.app.domain.model.MuscleGroup
import com.gymbud.app.domain.model.PersonalBest
import com.gymbud.app.domain.model.PreviousSet
import com.gymbud.app.domain.model.WorkoutStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map


class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val workoutSetDao: WorkoutSetDao,
    private val exerciseDao: ExerciseDao,
) {


    fun observeTemplates(): Flow<List<Workout>> = workoutDao.observeTemplates()
    fun observeActiveWorkout(): Flow<Workout?> = workoutDao.observeActiveWorkout()

    fun observeExercisesForWorkout(workoutId: Long): Flow<List<WorkoutExercise>> =
        workoutExerciseDao.observeForWorkout(workoutId)

    fun observeSetsForWorkoutExercise(workoutExerciseId: Long): Flow<List<WorkoutSet>> =
        workoutSetDao.observeForWorkoutExercise(workoutExerciseId)

    fun observeHistoryWithStats(): Flow<List<Pair<Workout, WorkoutStats>>> =
        workoutDao.observeHistory().map { workouts ->
            workouts.map { w -> w to statsFor(w.id) }
        }

    fun observePrCount(workoutId: Long): Flow<Int> =
        workoutSetDao.observePrCount(workoutId)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun observeStats(workoutId: Long): Flow<WorkoutStats> =
        workoutExerciseDao.observeForWorkout(workoutId)
            .flatMapLatest { exercises ->
                if (exercises.isEmpty()) {
                    kotlinx.coroutines.flow.flowOf(emptyList())
                } else {
                    val setFlows = exercises.map { we ->
                        workoutSetDao.observeForWorkoutExercise(we.id)
                    }
                    combine(setFlows) { arrayOfLists -> arrayOfLists.toList() }
                }
            }
            .map { allSetLists ->
                var totalSets = 0
                var totalVolumeKg = 0f
                for (setList in allSetLists) {
                    for (s in setList) {
                        if (!s.isCompleted) continue
                        totalSets += 1
                        val w = s.weightKg
                        val r = s.reps
                        if (w != null && r != null) {
                            totalVolumeKg += w * r
                        }
                    }
                }
                WorkoutStats(
                    totalSets = totalSets,
                    totalVolumeKg = totalVolumeKg,
                    durationMillis = null
                )
            }
            .combine(observePrCount(workoutId)) { stats, prCount ->
                stats.copy(prCount = prCount)
            }

    suspend fun getWorkout(id: Long): Workout? = workoutDao.getById(id)

    suspend fun getSetCountForTemplate(templateId: Long): Int =
        workoutDao.getSetCountForTemplate(templateId)

    suspend fun getLastUsedAt(templateId: Long): Long? =
        workoutDao.getLastUsedAt(templateId)

    suspend fun createTemplate(name: String): Long =
        workoutDao.insert(Workout(name = name, isTemplate = true))

    suspend fun renameWorkout(workoutId: Long, newName: String) {
        workoutDao.updateName(workoutId, newName)
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
                startedAt = System.currentTimeMillis(),
                sourceTemplateId = templateId
            )
        )

        val templateExercises = workoutExerciseDao
            .observeForWorkout(templateId)
            .firstValueOrEmpty()

        for (we in templateExercises) {
            if (we.exerciseId == null) continue
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
                            isCompleted = false
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


    suspend fun updateExerciseNotes(workoutExerciseId: Long, notes: String?) {
        workoutExerciseDao.updateNotes(workoutExerciseId, notes)
    }

    suspend fun updateSet(set: WorkoutSet) = workoutSetDao.update(set)

    suspend fun addSetWithLastValues(workoutExerciseId: Long, exerciseId: Long?): Long {
        val position = workoutSetDao.nextPosition(workoutExerciseId)
        val lastSet = exerciseId?.let {
            workoutSetDao.findPreviousSet(exerciseId = it, position = position, excludingWorkoutId = 0L)
        }
        return workoutSetDao.insert(
            WorkoutSet(
                workoutExerciseId = workoutExerciseId,
                position = position,
                weightKg = lastSet?.weightKg,
                reps = lastSet?.reps,
                durationSeconds = lastSet?.durationSeconds
            )
        )
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

    suspend fun updateSetCheckPR(set: WorkoutSet, exerciseId: Long?, currentWorkoutId: Long) {
        val updatedSet = when {
            !set.isCompleted -> set.copy(isPR = false)
            exerciseId == null -> set
            else -> {
                val isPR = when {
                    set.weightKg != null && set.reps != null && set.reps > 0 -> {
                        val best1RM = computeBest1RM(exerciseId, currentWorkoutId)
                        best1RM != null && epley1RM(set.weightKg, set.reps) > best1RM
                    }
                    set.durationSeconds != null -> {
                        val best = workoutSetDao.getMaxDurationForExercise(exerciseId, currentWorkoutId)
                        best != null && set.durationSeconds > best
                    }
                    else -> false
                }
                set.copy(isPR = isPR)
            }
        }
        workoutSetDao.update(updatedSet)
    }

    suspend fun deleteSet(set: WorkoutSet) {
        workoutSetDao.delete(set)
        val remaining = workoutSetDao.observeForWorkoutExercise(set.workoutExerciseId).first()
        val renumbered = remaining.mapIndexed { index, s -> s.copy(position = index) }
        workoutSetDao.updateAll(renumbered)
    }

    suspend fun finishWorkout(workoutId: Long) {
        val workout = workoutDao.getById(workoutId) ?: return
        if (workout.endedAt != null) return
        workoutDao.update(workout.copy(endedAt = System.currentTimeMillis()))
    }

    suspend fun syncTemplateSetsFromWorkout(workoutId: Long) {
        val workoutExercises = workoutExerciseDao.getForWorkout(workoutId)
        for (we in workoutExercises) {
            val exerciseId = we.exerciseId ?: continue
            val completedSets = workoutSetDao.getCompletedSetsForWorkoutExercise(we.id)
            if (completedSets.isEmpty()) continue
            val templateExercises = workoutExerciseDao.getTemplateExercisesForExercise(exerciseId)
            for (templateWe in templateExercises) {
                val templateSets = workoutSetDao.getSetsForWorkoutExercise(templateWe.id)
                if (templateSets.isEmpty()) continue
                for (templateSet in templateSets) {
                    val matching = completedSets.find { it.position == templateSet.position }
                        ?: continue
                    workoutSetDao.update(
                        templateSet.copy(
                            weightKg = matching.weightKg,
                            reps = matching.reps,
                            durationSeconds = matching.durationSeconds
                        )
                    )
                }
            }
        }
    }

    suspend fun discardWorkout(workoutId: Long) {
        val workout = workoutDao.getById(workoutId) ?: return
        workoutDao.delete(workout)
    }

    suspend fun updateWorkoutNotes(workoutId: Long, notes: String?) {
        workoutDao.updateNotes(workoutId, notes)
    }

    suspend fun updateWorkoutPhoto(workoutId: Long, photoPath: String?) {
        workoutDao.updatePhotoPath(workoutId, photoPath)
    }

    suspend fun statsFor(workoutId: Long): WorkoutStats {
        val workout = workoutDao.getById(workoutId)
        val exercises = workoutExerciseDao.observeForWorkout(workoutId).firstValueOrEmpty()

        var totalSets = 0
        var totalVolumeKg = 0f
        var prCount = 0
        val muscleCount = mutableMapOf<MuscleGroup, Int>()

        for (we in exercises) {
            val sets = workoutSetDao.observeForWorkoutExercise(we.id).firstValueOrEmpty()
            for (s in sets) {
                if (!s.isCompleted) continue
                totalSets += 1
                if (s.isPR) prCount += 1
                val w = s.weightKg
                val r = s.reps
                if (w != null && r != null) {
                    totalVolumeKg += w * r
                }
            }
            val muscle = we.exerciseId?.let { exerciseDao.getById(it)?.primaryMuscle }
            if (muscle != null) {
                muscleCount[muscle] = (muscleCount[muscle] ?: 0) + 1
            }
        }

        val duration = if (workout?.startedAt != null && workout.endedAt != null) {
            workout.endedAt - workout.startedAt
        } else null

        val dominantMuscle = muscleCount.maxByOrNull { it.value }?.key

        return WorkoutStats(
            totalSets = totalSets,
            totalVolumeKg = totalVolumeKg,
            durationMillis = duration,
            prCount = prCount,
            dominantMuscle = dominantMuscle
        )
    }

    suspend fun previousSetFor(
        exerciseId: Long,
        position: Int,
        excludingWorkoutId: Long
    ): PreviousSet? {
        val match = workoutSetDao.findPreviousSet(
            exerciseId = exerciseId,
            position = position,
            excludingWorkoutId = excludingWorkoutId
        ) ?: return null
        return PreviousSet(
            weightKg = match.weightKg,
            reps = match.reps,
            durationSeconds = match.durationSeconds
        )
    }

    suspend fun personalBestForExercise(exerciseId: Long, currentWorkoutId: Long): PersonalBest =
        PersonalBest(
            oneRepMaxKg = computeBest1RM(exerciseId, currentWorkoutId),
            durationSeconds = workoutSetDao.getMaxDurationForExercise(exerciseId, currentWorkoutId)
        )

    private suspend fun computeBest1RM(exerciseId: Long, excludingWorkoutId: Long): Float? =
        workoutSetDao.getCompletedWeightRepsSets(exerciseId, excludingWorkoutId)
            .mapNotNull { set ->
                val w = set.weightKg ?: return@mapNotNull null
                val r = set.reps?.takeIf { it > 0 } ?: return@mapNotNull null
                epley1RM(w, r)
            }
            .maxOrNull()

    private fun epley1RM(weightKg: Float, reps: Int): Float =
        if (reps == 1) weightKg else weightKg * (1f + reps / 30f)
}

private suspend fun <T> Flow<List<T>>.firstValueOrEmpty(): List<T> = first()
