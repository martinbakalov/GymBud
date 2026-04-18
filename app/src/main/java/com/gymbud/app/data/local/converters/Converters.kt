package com.gymbud.app.data.local.converters

import androidx.room.TypeConverter
import com.gymbud.app.domain.model.Equipment
import com.gymbud.app.domain.model.ExerciseType
import com.gymbud.app.domain.model.MuscleGroup
import com.gymbud.app.domain.model.WeightUnit

/** This class helps Room to understand custom types like enums or lists to primitives it can store. */
class Converters {

    @TypeConverter
    fun equipmentToString(value: Equipment): String = value.name

    @TypeConverter
    fun stringToEquipment(value: String): Equipment = Equipment.valueOf(value)

    @TypeConverter
    fun muscleGroupToString(value: MuscleGroup): String = value.name

    @TypeConverter
    fun stringToMuscleGroup(value: String): MuscleGroup = MuscleGroup.valueOf(value)

    @TypeConverter
    fun muscleGroupListToString(value: List<MuscleGroup>): String =
        value.joinToString(separator = ",") { it.name }

    @TypeConverter
    fun stringToMuscleGroupList(value: String): List<MuscleGroup> =
        if (value.isBlank()) emptyList()
        else value.split(",").map { MuscleGroup.valueOf(it) }

    @TypeConverter
    fun exerciseTypeToString(value: ExerciseType): String = value.name

    @TypeConverter
    fun stringToExerciseType(value: String): ExerciseType = ExerciseType.valueOf(value)

    @TypeConverter
    fun weightUnitToString(value: WeightUnit?): String? = value?.name

    @TypeConverter
    fun stringToWeightUnit(value: String?): WeightUnit? = value?.let { WeightUnit.valueOf(it) }
}