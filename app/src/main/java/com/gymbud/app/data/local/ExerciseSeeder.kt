package com.gymbud.app.data.local

import com.gymbud.app.data.local.dao.ExerciseDao
import com.gymbud.app.data.local.entity.Exercise
import com.gymbud.app.domain.model.Equipment
import com.gymbud.app.domain.model.Equipment.BARBELL
import com.gymbud.app.domain.model.Equipment.BODYWEIGHT
import com.gymbud.app.domain.model.Equipment.CABLE
import com.gymbud.app.domain.model.Equipment.DUMBBELL
import com.gymbud.app.domain.model.Equipment.MACHINE
import com.gymbud.app.domain.model.ExerciseType
import com.gymbud.app.domain.model.MuscleGroup
import com.gymbud.app.domain.model.MuscleGroup.BACK
import com.gymbud.app.domain.model.MuscleGroup.BICEPS
import com.gymbud.app.domain.model.MuscleGroup.CALVES
import com.gymbud.app.domain.model.MuscleGroup.CHEST
import com.gymbud.app.domain.model.MuscleGroup.CORE
import com.gymbud.app.domain.model.MuscleGroup.GLUTES
import com.gymbud.app.domain.model.MuscleGroup.HAMSTRINGS
import com.gymbud.app.domain.model.MuscleGroup.QUADS
import com.gymbud.app.domain.model.MuscleGroup.SHOULDERS
import com.gymbud.app.domain.model.MuscleGroup.TRICEPS
import com.gymbud.app.domain.model.MuscleGroup.FULL_BODY

object ExerciseSeeder {

    suspend fun seed(dao: ExerciseDao) {
        if (dao.count() > 0) return
        dao.insertAll(presets())
    }

    private fun presets(): List<Exercise> = listOf(

        preset("Barbell Bench Press",             BARBELL,   CHEST,     listOf(TRICEPS, SHOULDERS)),
        preset("Incline Barbell Bench Press",    BARBELL,   CHEST,     listOf(SHOULDERS, TRICEPS)),
        preset("Dumbbell Bench Press",           DUMBBELL,  CHEST,     listOf(TRICEPS, SHOULDERS)),
        preset("Incline Dumbbell Press",         DUMBBELL,  CHEST,     listOf(SHOULDERS, TRICEPS)),
        preset("Dumbbell Fly",                   DUMBBELL,  CHEST),
        preset("Cable Crossover",                CABLE,     CHEST),
        preset("Chest Press Machine",            MACHINE,   CHEST,     listOf(TRICEPS)),
        preset("Dips",            BODYWEIGHT,   CHEST,     listOf(TRICEPS)),
        preset("Push-Up",                        BODYWEIGHT, CHEST,    listOf(TRICEPS, CORE)),

        preset("Deadlift",                       BARBELL,   BACK,      listOf(GLUTES, HAMSTRINGS, CORE)),
        preset("Barbell Row",                    BARBELL,   BACK,      listOf(BICEPS)),
        preset("Dumbbell Row",                   DUMBBELL,  BACK,      listOf(BICEPS)),
        preset("Lat Pulldown",                   MACHINE,     BACK,      listOf(BICEPS)),
        preset("Seated Cable Row",               CABLE,     BACK,      listOf(BICEPS)),
        preset("Pull-Up",                        BODYWEIGHT, BACK,     listOf(BICEPS)),

        preset("Overhead Press",                 BARBELL,   SHOULDERS, listOf(TRICEPS)),
        preset("Dumbbell Shoulder Press",        DUMBBELL,  SHOULDERS, listOf(TRICEPS)),
        preset("Lateral Raise",                  DUMBBELL,  SHOULDERS),
        preset("Face Pull",                      CABLE,     SHOULDERS, listOf(BACK)),

        preset("Barbell Curl",                   BARBELL,   BICEPS),
        preset("Dumbbell Curl",                  DUMBBELL,  BICEPS),
        preset("Hammer Curl",                    DUMBBELL,  BICEPS),
        preset("Triceps Pushdown",                CABLE,     TRICEPS),
        preset("Skull Crusher",                  BARBELL,   TRICEPS),
        preset("Triceps Dips",                     BODYWEIGHT, TRICEPS, listOf(CHEST)),

        preset("Back Squat",                     BARBELL,   QUADS,     listOf(GLUTES, HAMSTRINGS, CORE)),
        preset("Front Squat",                    BARBELL,   QUADS,     listOf(GLUTES, CORE)),
        preset("Romanian Deadlift",              BARBELL,   HAMSTRINGS, listOf(GLUTES, BACK)),
        preset("Leg Press",                      MACHINE,   QUADS,     listOf(GLUTES, HAMSTRINGS)),
        preset("Leg Extension",                  MACHINE,   QUADS),
        preset("Leg Curl",                       MACHINE,   HAMSTRINGS),
        preset("Hip Thrust",                     BARBELL,   GLUTES,    listOf(HAMSTRINGS)),
        preset("Standing Calf Raise",            MACHINE,   CALVES),

        preset(
            name = "Plank",
            equipment = BODYWEIGHT,
            primaryMuscle = CORE,
            type = ExerciseType.TIME
        ),
        preset(
            name = "Treadmill",
            equipment = MACHINE,
            primaryMuscle = FULL_BODY,
            type = ExerciseType.TIME
        ),
        preset(
            name = "Exercise Bike",
            equipment = MACHINE,
            primaryMuscle = FULL_BODY,
            type = ExerciseType.TIME
        ),
        preset("Hanging Leg Raise",              BODYWEIGHT, CORE),
        preset("Cable Crunch",                   CABLE,     CORE)
    )

    private fun preset(
        name: String,
        equipment: Equipment,
        primaryMuscle: MuscleGroup,
        secondaryMuscles: List<MuscleGroup> = emptyList(),
        type: ExerciseType = ExerciseType.WEIGHT_REPS
    ): Exercise = Exercise(
        name = name,
        nameKey = nameKeyFor(name),
        equipment = equipment,
        primaryMuscle = primaryMuscle,
        secondaryMuscles = secondaryMuscles,
        type = type,
        isCustom = false
    )

    private fun nameKeyFor(name: String): String =
        "exercise_" + name.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
}