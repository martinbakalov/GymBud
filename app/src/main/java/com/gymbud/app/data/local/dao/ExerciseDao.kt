package com.gymbud.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gymbud.app.data.local.entity.Exercise
import com.gymbud.app.domain.model.Equipment
import com.gymbud.app.domain.model.MuscleGroup
import kotlinx.coroutines.flow.Flow


@Dao
interface ExerciseDao {


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(exercise: Exercise): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(exercises: List<Exercise>): List<Long>

    @Update
    suspend fun update(exercise: Exercise): Int

    @Delete
    suspend fun delete(exercise: Exercise): Int


    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: Long): Exercise?


    @Query("SELECT * FROM exercises ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    fun observeById(id: Long): Flow<Exercise?>

    @Query(
        "SELECT * FROM exercises " +
                "WHERE name LIKE :query COLLATE NOCASE " +
                "ORDER BY name COLLATE NOCASE ASC"
    )
    fun searchByName(query: String): Flow<List<Exercise>>


    @Query(
        "SELECT * FROM exercises " +
                "WHERE primaryMuscle = :muscle " +
                "ORDER BY name COLLATE NOCASE ASC"
    )
    fun observeByMuscle(muscle: MuscleGroup): Flow<List<Exercise>>


    @Query(
        "SELECT * FROM exercises " +
                "WHERE equipment = :equipment " +
                "ORDER BY name COLLATE NOCASE ASC"
    )
    fun observeByEquipment(equipment: Equipment): Flow<List<Exercise>>


    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int
}