package com.pdv.app.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdv.app.VentaViewModel
import com.pdv.app.data.Product
import com.pdv.app.utils.formatCOP

@Composable
fun VentaScreen(viewModel: VentaViewModel = viewModel()) {
    val productos by viewModel.productos.collectAsState()
    val carrito = remember { mutableStateMapOf<Product, Int>() }

    Column(modifier = Modifier.fillMaxSize()) {
        // Lista de productos disponibles
        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp)) {
            items(productos) { producto ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (producto.stock > (carrito[producto] ?: 0)) {
                                carrito[producto] = (carrito[producto] ?: 0) + 1
                            }
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(producto.nombre, fontWeight = FontWeight.Bold)
                        Text(formatCOP(producto.precio), style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("Stock: ${producto.stock}", style = MaterialTheme.typography.bodySmall)
                }
                Divider()
            }
        }

        // Resumen del carrito y botón de cobro
        if (carrito.isNotEmpty()) {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    carrito.forEach { (producto, cantidad) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${cantidad}x ${producto.nombre}")
                            Text(formatCOP(producto.precio * cantidad))
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    val total = carrito.entries.sumOf { it.key.precio * it.value }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(formatCOP(total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            viewModel.procesarVenta(carrito)
                            carrito.clear()
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Cobrar")
                    }
                }
            }
        }
    }
}
