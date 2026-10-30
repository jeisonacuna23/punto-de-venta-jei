package com.pdv.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ventas")
data class Venta(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fecha: Long = System.currentTimeMillis(),
    val total: Double,
    val detalles: String,
    val cliente: String = ""
)
