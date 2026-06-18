package com.gorthaur.financetracker.data.local.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null
    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this){
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "finance_tracker_db"
            )
                // La app aún está en desarrollo: si cambia el esquema preferimos
                // recrear la base de datos local antes que escribir migraciones.
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()

            INSTANCE = instance
            instance
        }
    }
}