package com.gorthaur.financetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gorthaur.financetracker.data.local.entity.ServiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {

    @Insert
    suspend fun insert(service: ServiceEntity): Long

    @Update
    suspend fun update(service: ServiceEntity)

    @Delete
    suspend fun delete(service: ServiceEntity)

    @Query("SELECT * FROM services ORDER BY billingDayOfMonth ASC, name ASC")
    fun observeAll(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services")
    suspend fun getAll(): List<ServiceEntity>

    @Query("DELETE FROM services WHERE id = :id")
    suspend fun deleteById(id: Long)
}
