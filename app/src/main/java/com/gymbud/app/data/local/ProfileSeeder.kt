package com.gymbud.app.data.local

import com.gymbud.app.data.local.dao.ProfileDao
import com.gymbud.app.data.local.entity.Profile

object ProfileSeeder {
    suspend fun seed(dao: ProfileDao) {
        if (dao.get() == null) {
            dao.upsert(Profile(id = 1L))
        }
    }
}