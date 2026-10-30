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
import com.pdv.app.utils.formatCOP
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

    val carrito = mutableStateMapOf<Product, Int>()
    var cliente by mutableStateOf("")

    fun procesarVenta() {
        if (carrito.isEmpty()) return
        val carritoCopia = carrito.toMap()
        val clienteCopia = cliente
        
        viewModelScope.launch {
            // Nuevo formato: "2x Pan @ $2.000 = $4.000 | 1x Leche @ $3.500 = $3.500"
            val detallesStr = carritoCopia.entries.joinToString(" | ") { 
                "${it.value}x ${it.key.nombre} @ ${formatCOP(it.key.precio)} = ${formatCOP(it.key.precio * it.value)}" 
            }
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
            val items = venta.detalles.split(" | ")
            items.forEach { item ->
                val parts = item.split("x ", limit = 2)
                if (parts.size == 2) {
                    val cantidad = parts[0].toIntOrNull() ?: 0
                    // Extraemos solo el nombre, ignorando el precio unitario y el total
                    val nombreProducto = parts[1].substringBefore(" @").trim()
                    val producto = productDao.getProductByName(nombreProducto)
                    if (producto != null) {
                        productDao.updateProduct(producto.copy(stock = producto.stock + cantidad))
                    }
                }
            }
            ventaDao.deleteVenta(venta)
        }
    }
}
