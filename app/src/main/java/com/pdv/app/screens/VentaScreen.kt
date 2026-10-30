package com.pdv.app.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdv.app.VentaViewModel
import com.pdv.app.data.Product
import com.pdv.app.data.Venta
import com.pdv.app.utils.formatCOP
import com.pdv.app.utils.generarYCompartirRecibo

@Composable
fun VentaScreen(viewModel: VentaViewModel = viewModel()) {
    val productos by viewModel.productos.collectAsState()
    val carrito = remember { mutableStateMapOf<Product, Int>() }
    
    var showDialog by remember { mutableStateOf(false) }
    var ventaParaRecibo by remember { mutableStateOf<Venta?>(null) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Toca para agregar al carrito", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
        
        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 16.dp)) {
            items(productos) { producto ->
                val enCarrito = carrito[producto] ?: 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (producto.stock > enCarrito) {
                                carrito[producto] = enCarrito + 1
                            }
                        }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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

        if (carrito.isNotEmpty()) {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Carrito de Venta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    carrito.entries.toList().forEach { (producto, cantidad) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(producto.nombre, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            
                            IconButton(onClick = {
                                if (cantidad > 1) carrito[producto] = cantidad - 1 else carrito.remove(producto)
                            }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.Remove, contentDescription = "Menos", modifier = Modifier.size(20.dp))
                            }
                            
                            Text("$cantidad", modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                            
                            IconButton(onClick = {
                                if (producto.stock > cantidad) carrito[producto] = cantidad + 1
                            }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.Add, contentDescription = "Más", modifier = Modifier.size(20.dp))
                            }
                            
                            Text(formatCOP(producto.precio * cantidad), modifier = Modifier.width(80.dp), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                            
                            IconButton(onClick = { carrito.remove(producto) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            }
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
                        Text(formatCOP(total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    
                    Button(
                        onClick = {
                            val detallesStr = carrito.entries.joinToString(", ") { "${it.value}x ${it.key.nombre}" }
                            ventaParaRecibo = Venta(total = total, detalles = detallesStr)
                            viewModel.procesarVenta(carrito.toMap())
                            carrito.clear()
                            showDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                    ) {
                        Text("Cobrar y Generar Venta")
                    }
                }
            }
        }
    }

    if (showDialog && ventaParaRecibo != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("¡Venta Exitosa!") },
            text = { Text("Se ha registrado un cobro por ${formatCOP(ventaParaRecibo!!.total)}.\nEl stock ha sido actualizado.") },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    generarYCompartirRecibo(context, ventaParaRecibo!!)
                }) {
                    Text("Compartir Recibo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
