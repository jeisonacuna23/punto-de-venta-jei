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

class VentaViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val productDao = db.productDao()
    private val ventaDao = db.ventaDao()

    val productos = productDao.getAllProducts().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun procesarVenta(carrito: Map<Product, Int>, cliente: String) {
        if (carrito.isEmpty()) return
        
        viewModelScope.launch {
            val detallesStr = carrito.entries.joinToString(", ") { "${it.value}x ${it.key.nombre}" }
            val total = carrito.entries.sumOf { it.key.precio * it.value }
            
            ventaDao.insertVenta(Venta(total = total, detalles = detallesStr, cliente = cliente))
            
            carrito.forEach { (producto, cantidad) ->
                if (producto.id > 0) { // Solo si es un producto real (ignora venta rápida)
                    val nuevoStock = producto.stock - cantidad
                    if (nuevoStock >= 0) {
                        productDao.updateProduct(producto.copy(stock = nuevoStock))
                    }
                }
            }
        }
    }
}
