package com.gorthaur.financetracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "item_catalog",
    indices = [Index(value = ["normalizedName"], unique = true)]
)
data class ItemCatalogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val normalizedName: String,
    val defaultPhotoUri: String? = null,
    val defaultPrice: Double? = null
)