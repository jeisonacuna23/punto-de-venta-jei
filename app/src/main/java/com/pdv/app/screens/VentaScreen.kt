package com.pdv.app.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdv.app.VentaViewModel
import com.pdv.app.data.Product
import com.pdv.app.data.Venta
import com.pdv.app.utils.formatCOP
import com.pdv.app.utils.generarReciboBitmap
import com.pdv.app.utils.compartirRecibo

@Composable
fun VentaScreen(viewModel: VentaViewModel = viewModel()) {
    val productos by viewModel.productos.collectAsState()
    val carrito = viewModel.carrito
    var cliente by remember { mutableStateOf(viewModel.cliente) }
    cliente = viewModel.cliente 
    
    var searchQuery by remember { mutableStateOf("") }
    var showRapidaDialog by remember { mutableStateOf(false) }
    var showPagoDialog by remember { mutableStateOf(false) }
    var showExitoDialog by remember { mutableStateOf(false) }
    var ventaParaRecibo by remember { mutableStateOf<Venta?>(null) }
    var itemAEliminar by remember { mutableStateOf<Product?>(null) }
    var showAlertaCliente by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val productosFiltrados = productos.filter { it.nombre.contains(searchQuery, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {
        // CLIENTE OBLIGATORIO ARRIBA
        OutlinedTextField(
            value = cliente,
            onValueChange = { viewModel.cliente = it },
            label = { Text("Nombre del Cliente (Obligatorio) *") },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF5D4037),
                unfocusedBorderColor = if (cliente.isEmpty()) Color.Red else Color(0xFFE5D8CA)
            )
        )

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar producto...") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF5D4037),
                    unfocusedBorderColor = Color(0xFFE5D8CA)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { 
                if (cliente.isBlank()) showAlertaCliente = true 
                else showRapidaDialog = true 
            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF795548))) { Text("Rápida") }
        }
        
        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp)) {
            items(productosFiltrados) { producto ->
                val enCarrito = carrito[producto] ?: 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (cliente.isBlank()) {
                                showAlertaCliente = true
                            } else if (producto.stock > enCarrito) {
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
                Divider(color = Color(0xFFE5D8CA))
            }
        }

        if (carrito.isNotEmpty()) {
            Surface(shadowElevation = 8.dp, color = Color(0xFFF7F3ED), modifier = Modifier.fillMaxWidth()) {
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
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE5D8CA))
                    val total = carrito.entries.sumOf { it.key.precio * it.value }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Total:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(formatCOP(total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Button(
                        onClick = { showPagoDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))
                    ) { Text("Cobrar") }
                }
            }
        }
    }

    if (showAlertaCliente) {
        AlertDialog(
            onDismissRequest = { showAlertaCliente = false },
            title = { Text("Cliente Requerido") },
            text = { Text("Por favor, ingrese el nombre del cliente antes de agregar productos.") },
            confirmButton = { TextButton(onClick = { showAlertaCliente = false }) { Text("Entendido") } }
        )
    }

    if (showPagoDialog) {
        var metodo by remember { mutableStateOf("Efectivo") }
        val total = carrito.entries.sumOf { it.key.precio * it.value }

        AlertDialog(
            onDismissRequest = { showPagoDialog = false },
            title = { Text("Método de Pago") },
            text = {
                Column {
                    Text("Total a cobrar: ${formatCOP(total)}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = metodo == "Efectivo", onClick = { metodo = "Efectivo" })
                        Text("Efectivo")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = metodo == "Nequi", onClick = { metodo = "Nequi" })
                        Text("Nequi")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = metodo == "Fiado", onClick = { metodo = "Fiado" })
                        Text("Fiado (Pendiente)")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showPagoDialog = false
                    val esFiado = metodo == "Fiado"
                    viewModel.procesarVenta(metodo, esFiado) { ventaGuardada ->
                        ventaParaRecibo = ventaGuardada
                        showExitoDialog = true
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { showPagoDialog = false }) { Text("Cancelar") } }
        )
    }

    if (itemAEliminar != null) {
        AlertDialog(
            onDismissRequest = { itemAEliminar = null },
            title = { Text("Quitar del carrito") },
            text = { Text("¿Estás seguro de quitar ${itemAEliminar!!.nombre} de la venta?") },
            confirmButton = { TextButton(onClick = { carrito.remove(itemAEliminar); itemAEliminar = null }) { Text("Quitar", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { itemAEliminar = null }) { Text("Cancelar") } }
        )
    }

    if (showRapidaDialog) {
        var concepto by remember { mutableStateOf("") }
        var monto by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showRapidaDialog = false },
            title = { Text("Venta Rápida (Monto Manual)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = concepto, onValueChange = { concepto = it }, label = { Text("Concepto") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = monto, onValueChange = { monto = it }, label = { Text("Monto $") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val m = monto.toDoubleOrNull() ?: 0.0
                    if (concepto.isNotBlank() && m > 0) {
                        val tempProduct = Product(id = -System.currentTimeMillis().toInt(), nombre = concepto, precio = m, stock = 1)
                        carrito[tempProduct] = 1
                        showRapidaDialog = false
                    }
                }) { Text("Agregar") }
            },
            dismissButton = { TextButton(onClick = { showRapidaDialog = false }) { Text("Cancelar") } }
        )
    }

    if (showExitoDialog && ventaParaRecibo != null) {
        AlertDialog(
            onDismissRequest = { showExitoDialog = false },
            title = { Text("¡Venta Exitosa!") },
            text = { Text("Se ha registrado un cobro por ${formatCOP(ventaParaRecibo!!.total)}.\nEl stock ha sido actualizado.") },
            confirmButton = {
                Button(onClick = { 
                    showExitoDialog = false
                    val bitmap = generarReciboBitmap(context, ventaParaRecibo!!)
                    compartirRecibo(context, bitmap, ventaParaRecibo!!.id)
                }) { Text("Compartir Recibo") }
            },
            dismissButton = { TextButton(onClick = { showExitoDialog = false }) { Text("Cerrar") } }
        )
    }
}
