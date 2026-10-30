package com.pdv.app

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pdv.app.data.AppDatabase
import com.pdv.app.data.Product
import com.pdv.app.data.Venta
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VentaViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val productDao = db.productDao()
    private val ventaDao = db.ventaDao()

    val productos = productDao.getAllProducts().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // ESTADO PERSISTENTE DEL CARRITO Y CLIENTE
    val carrito = mutableStateMapOf<Product, Int>()
    var cliente by mutableStateOf("")

    fun procesarVenta() {
        if (carrito.isEmpty()) return
        val carritoCopia = carrito.toMap()
        val clienteCopia = cliente
        
        viewModelScope.launch {
            val detallesStr = carritoCopia.entries.joinToString(", ") { "${it.value}x ${it.key.nombre}" }
            val total = carritoCopia.entries.sumOf { it.key.precio * it.value }
            
            ventaDao.insertVenta(Venta(total = total, detalles = detallesStr, cliente = clienteCopia))
            
            carritoCopia.forEach { (producto, cantidad) ->
                if (producto.id > 0) {
                    val nuevoStock = producto.stock - cantidad
                    if (nuevoStock >= 0) {
                        productDao.updateProduct(producto.copy(stock = nuevoStock))
                    }
                }
            }
            carrito.clear()
            cliente = ""
        }
    }

    fun anularVenta(venta: Venta) {
        viewModelScope.launch {
            // 1. Devolver el stock
            val items = venta.detalles.split(", ")
            items.forEach { item ->
                val parts = item.split("x ", limit = 2)
                if (parts.size == 2) {
                    val cantidad = parts[0].toIntOrNull() ?: 0
                    val nombreProducto = parts[1]
                    val producto = productDao.getProductByName(nombreProducto)
                    if (producto != null) {
                        productDao.updateProduct(producto.copy(stock = producto.stock + cantidad))
                    }
                }
            }
            // 2. Eliminar la venta
            ventaDao.deleteVenta(venta)
        }
    }
}
