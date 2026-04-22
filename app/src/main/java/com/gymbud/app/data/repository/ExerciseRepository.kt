package com.gymbud.app.data.repository

import com.gymbud.app.data.local.dao.ExerciseDao
import com.gymbud.app.data.local.entity.Exercise
import com.gymbud.app.domain.model.Equipment
import com.gymbud.app.domain.model.MuscleGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf


class ExerciseRepository(
    private val dao: ExerciseDao
) {

    fun observeFiltered(
        query: String?,
        muscle: MuscleGroup?,
        equipment: Equipment?
    ): Flow<List<Exercise>> {
        val source: Flow<List<Exercise>> = when {
            !query.isNullOrBlank() -> dao.searchByName("%${query.trim()}%")
            muscle != null         -> dao.observeByMuscle(muscle)
            equipment != null      -> dao.observeByEquipment(equipment)
            else                   -> dao.observeAll()
        }

        return source.combine(flowOf(Unit)) { list, _ ->
            list.filter { exercise ->
                (muscle == null || exercise.primaryMuscle == muscle) &&
                        (equipment == null || exercise.equipment == equipment) &&
                        (query.isNullOrBlank() ||
                                exercise.name.contains(query.trim(), ignoreCase = true))
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