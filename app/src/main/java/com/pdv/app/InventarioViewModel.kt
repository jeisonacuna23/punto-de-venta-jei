package com.pdv.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pdv.app.data.AppDatabase
import com.pdv.app.data.Product
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventarioViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).productDao()

    val productos = dao.getAllProducts().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun agregarProducto(nombre: String, precio: Double, stock: Int) {
        viewModelScope.launch {
            dao.insertProduct(Product(nombre = nombre, precio = precio, stock = stock))
        }
    }

    fun actualizarProducto(producto: Product) {
        viewModelScope.launch {
            dao.updateProduct(producto)
        }
    }

    fun eliminarProducto(producto: Product) {
        viewModelScope.launch {
            dao.deleteProduct(producto)
        }
    }

    fun importarProductos(productos: List<Product>) {
        viewModelScope.launch {
            dao.insertAllProducts(productos)
        }
    }
}
