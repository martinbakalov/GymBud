package com.gymbud.app.model

enum class WeightUnit {
    KG,
    LBS;

    companion object {
        private const val KG_PER_LB = 0.45359237

        fun kgToLbs(kg: Float): Float = (kg / KG_PER_LB).toFloat()
        fun lbsToKg(lbs: Float): Float = (lbs * KG_PER_LB).toFloat()

        fun fromKg(kg: Float, unit: WeightUnit): Float =
            if (unit == KG) kg else kgToLbs(kg)

        fun toKg(value: Float, unit: WeightUnit): Float =
            if (unit == KG) value else lbsToKg(value)
    }
}