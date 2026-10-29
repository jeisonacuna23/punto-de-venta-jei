package com.pdv.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pdv.app.data.AppDatabase
import com.pdv.app.data.Product
import com.pdv.app.data.Venta
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartItem(val product: Product, val quantity: Int)

class VentaViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val productDao = db.productDao()
    private val ventaDao = db.ventaDao()

    val productos = productDao.getAllProducts().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun procesarVenta(carrito: Map<Product, Int>) {
        if (carrito.isEmpty()) return
        
        viewModelScope.launch {
            val detallesStr = carrito.entries.joinToString(", ") { "${it.value}x ${it.key.nombre}" }
            val total = carrito.entries.sumOf { it.key.precio * it.value }
            
            // 1. Guardar la venta
            ventaDao.insertVenta(Venta(total = total, detalles = detallesStr))
            
            // 2. Descontar stock
            carrito.forEach { (producto, cantidad) ->
                val nuevoStock = producto.stock - cantidad
                productDao.updateProduct(producto.copy(stock = nuevoStock))
            }
        }
    }
}
