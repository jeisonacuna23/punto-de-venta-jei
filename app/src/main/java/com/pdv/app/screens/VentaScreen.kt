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
    val carrito = viewModel.carrito
    var cliente by remember { mutableStateOf(viewModel.cliente) } // Sincrónico con ViewModel
    cliente = viewModel.cliente 
    
    var searchQuery by remember { mutableStateOf("") }
    var showRapidaDialog by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var ventaParaRecibo by remember { mutableStateOf<Venta?>(null) }
    
    // Para confirmar eliminación del carrito
    var itemAEliminar by remember { mutableStateOf<Product?>(null) }
    
    val context = LocalContext.current
    val productosFiltrados = productos.filter { it.nombre.contains(searchQuery, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar producto...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { showRapidaDialog = true }, contentPadding = PaddingValues(horizontal = 12.dp)) {
                Text("Rápida")
            }
        }
        
        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 16.dp)) {
            items(productosFiltrados) { producto ->
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
                                if (cantidad > 1) carrito[producto] = cantidad - 1 else itemAEliminar = producto
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
                            
                            IconButton(onClick = { itemAEliminar = producto }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    OutlinedTextField(
                        value = cliente,
                        onValueChange = { viewModel.cliente = it },
                        label = { Text("Nombre del Cliente (Opcional)") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true
                    )
                    
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
                            // Se predice el próximo ID para el recibo visual, aunque Room asigne el real
                            // (Para la vista previa antes de guardar, usamos 0 o el tamaño de ventas + 1, pero lo ideal es guardar y luego compartir)
                            // Simplificación: Mostramos el recibo temporal con el ID que retornará Room (no podemos saberlo exacto antes de guardar, así que el recibo visual usará un ID falso hasta que se guarde)
                            ventaParaRecibo = Venta(id = 0, total = total, detalles = detallesStr, cliente = cliente) 
                            viewModel.procesarVenta()
                            showDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Cobrar y Generar Venta")
                    }
                }
            }
        }
    }

    if (itemAEliminar != null) {
        AlertDialog(
            onDismissRequest = { itemAEliminar = null },
            title = { Text("Quitar del carrito") },
            text = { Text("¿Estás seguro de quitar ${itemAEliminar!!.nombre} de la venta?") },
            confirmButton = {
                TextButton(onClick = {
                    carrito.remove(itemAEliminar)
                    itemAEliminar = null
                }) { Text("Quitar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { itemAEliminar = null }) { Text("Cancelar") } }
        )
    }

    if (showRapidaDialog) {
        VentaRapidaDialog(
            onDismiss = { showRapidaDialog = false },
            onSave = { concepto, monto ->
                val tempProduct = Product(id = -System.currentTimeMillis().toInt(), nombre = concepto, precio = monto, stock = 1)
                carrito[tempProduct] = 1
                showRapidaDialog = false
            }
        )
    }

    if (showDialog && ventaParaRecibo != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("¡Venta Exitosa!") },
            text = { Text("Se ha registrado un cobro por ${formatCOP(ventaParaRecibo!!.total)}.\nEl stock ha sido actualizado.") },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    // Como el ID real lo asigna la BD, generamos el recibo con el objeto que ya se envió (el ID se sincronizará en el historial, pero para este recibo inmediato usaremos el temporal)
                    // NOTA: Para que el recibo salga con el VSJA correcto, deberíamos consultar la última venta, pero para simplificar, le pasamos el objeto.
                    generarYCompartirRecibo(context, ventaParaRecibo!!)
                }) { Text("Compartir Recibo") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cerrar") }
            }
        )
    }
}

@Composable
fun VentaRapidaDialog(onDismiss: () -> Unit, onSave: (String, Double) -> Unit) {
    var concepto by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Venta Rápida (Monto Manual)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = concepto, onValueChange = { concepto = it }, label = { Text("Concepto / Descripción") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = monto, onValueChange = { monto = it }, label = { Text("Monto $") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val m = monto.toDoubleOrNull() ?: 0.0
                if (concepto.isNotBlank() && m > 0) onSave(concepto, m)
            }) { Text("Agregar al Carrito") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
