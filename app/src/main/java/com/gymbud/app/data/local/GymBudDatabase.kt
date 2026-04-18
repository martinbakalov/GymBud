package com.gymbud.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gymbud.app.data.local.converters.Converters
import com.gymbud.app.data.local.dao.ExerciseDao
import com.gymbud.app.data.local.entity.Exercise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Database(
    entities = [Exercise::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GymBudDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao

    companion object {
        @Volatile
        private var INSTANCE: GymBudDatabase? = null

        fun getDatabase(
            context: Context,
            applicationScope: CoroutineScope
        ): GymBudDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymBudDatabase::class.java,
                    "gymbud.db"
                )
                    .addCallback(SeedCallback(applicationScope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class SeedCallback(
            private val scope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        ExerciseSeeder.seed(database.exerciseDao())
                    }
                }
            }
        }
    }
}