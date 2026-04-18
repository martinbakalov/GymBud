package com.gymbud.app

import android.app.Application
import com.gymbud.app.data.local.GymBudDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class GymBudApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    val database: GymBudDatabase by lazy {
        GymBudDatabase.getDatabase(this, applicationScope)
    }
}