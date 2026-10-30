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

    fun procesarVenta(metodoPago: String, esFiado: Boolean, onComplete: (Venta) -> Unit) {
        if (carrito.isEmpty()) return
        val carritoCopia = carrito.toMap()
        val clienteCopia = cliente
        
        viewModelScope.launch {
            val detallesStr = carritoCopia.entries.joinToString(" | ") { 
                "${it.value}x ${it.key.nombre} @ ${formatCOP(it.key.precio)} = ${formatCOP(it.key.precio * it.value)}" 
            }
            val total = carritoCopia.entries.sumOf { it.key.precio * it.value }
            
            val nuevaVenta = Venta(
                total = total, 
                detalles = detallesStr, 
                cliente = clienteCopia,
                metodoPago = metodoPago,
                pagado = !esFiado
            )
            
            val id = ventaDao.insertVenta(nuevaVenta)
            val ventaGuardada = nuevaVenta.copy(id = id.toInt())
            
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
            onComplete(ventaGuardada)
        }
    }

    fun anularVenta(venta: Venta) {
        viewModelScope.launch {
            val items = venta.detalles.split(" | ")
            items.forEach { item ->
                val parts = item.split("x ", limit = 2)
                if (parts.size == 2) {
                    val cantidad = parts[0].toIntOrNull() ?: 0
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

    fun saldarDeuda(venta: Venta) {
        viewModelScope.launch {
            ventaDao.updateVenta(venta.copy(pagado = true))
        }
    }
}
