package com.gymbud.app.domain.model

enum class WeightUnit(val abbreviation: String) {
    KG("kg"),
    LBS("lbs");

    companion object {
        private const val KG_PER_LB = 0.45359237f

        fun kgToLbs(kg: Float): Float = kg / KG_PER_LB
        fun lbsToKg(lbs: Float): Float = lbs * KG_PER_LB

        fun fromKg(kg: Float, unit: WeightUnit): Float =
            if (unit == KG) kg else kgToLbs(kg)

        fun toKg(value: Float, unit: WeightUnit): Float =
            if (unit == KG) value else lbsToKg(value)
    }
}