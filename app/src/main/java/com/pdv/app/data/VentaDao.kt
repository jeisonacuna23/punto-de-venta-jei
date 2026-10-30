package com.pdv.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VentaDao {
    @Query("SELECT * FROM ventas ORDER BY fecha DESC")
    fun getAllVentas(): Flow<List<Venta>>

    @Insert
    suspend fun insertVenta(venta: Venta)

    @Delete
    suspend fun deleteVenta(venta: Venta)
}
