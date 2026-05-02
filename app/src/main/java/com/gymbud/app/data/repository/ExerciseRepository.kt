package com.gymbud.app.data.repository

import android.content.Context
import com.gymbud.app.data.local.dao.ExerciseDao
import com.gymbud.app.data.local.entity.Exercise
import com.gymbud.app.model.Equipment
import com.gymbud.app.model.MuscleGroup
import com.gymbud.app.ui.util.exerciseNameResId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map


class ExerciseRepository(
    private val dao: ExerciseDao,
    private val context: Context
) {

    private fun Exercise.resolvedDisplayName(): String {
        val key = nameKey ?: return name
        val resId = exerciseNameResId(key) ?: return name
        return context.getString(resId)
    }

    fun observeFiltered(
        query: String?,
        muscle: MuscleGroup?,
        equipment: Equipment?
    ): Flow<List<Exercise>> {
        val trimmed = query?.trim()

        val source: Flow<List<Exercise>> = when {
            !trimmed.isNullOrBlank() -> dao.observeAll().map { list ->
                list.filter { exercise ->
                    exercise.name.contains(trimmed, ignoreCase = true) ||
                    exercise.resolvedDisplayName().contains(trimmed, ignoreCase = true)
                }
            }
            muscle != null           -> dao.observeByMuscle(muscle)
            equipment != null        -> dao.observeByEquipment(equipment)
            else                     -> dao.observeAll()
        }

        return source.combine(flowOf(Unit)) { list, _ ->
            list.filter { exercise ->
                (muscle == null || exercise.primaryMuscle == muscle) &&
                        (equipment == null || exercise.equipment == equipment)
            }
        }
    }

    suspend fun getById(id: Long): Exercise? = dao.getById(id)

    fun observeById(id: Long): Flow<Exercise?> = dao.observeById(id)
    suspend fun createCustom(exercise: Exercise): Long =
        dao.insert(exercise.copy(isCustom = true))

    suspend fun update(exercise: Exercise) = dao.update(exercise)

    suspend fun deleteCustom(exercise: Exercise): Int =
        if (exercise.isCustom) dao.delete(exercise) else 0
}