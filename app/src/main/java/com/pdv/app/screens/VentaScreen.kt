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
    
    var showSearchDialog by remember { mutableStateOf(false) }
    var showRapidaDialog by remember { mutableStateOf(false) }
    var showPagoDialog by remember { mutableStateOf(false) }
    var showExitoDialog by remember { mutableStateOf(false) }
    var ventaParaRecibo by remember { mutableStateOf<Venta?>(null) }
    var itemAEliminar by remember { mutableStateOf<Product?>(null) }
    var showAlertaCliente by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. CLIENTE OBLIGATORIO
        OutlinedTextField(
            value = cliente,
            onValueChange = { viewModel.cliente = it },
            label = { Text("Nombre del Cliente (Obligatorio) *") },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF5D4037),
                unfocusedBorderColor = if (cliente.isEmpty()) Color.Red else Color(0xFFE5D8CA),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        // 2. BOTONES DE ACCIÓN (Buscar y Venta Rápida)
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { 
                    if (cliente.isBlank()) showAlertaCliente = true 
                    else showSearchDialog = true 
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))
            ) {
                Icon(Icons.Filled.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar Producto")
            }
            Button(
                onClick = { 
                    if (cliente.isBlank()) showAlertaCliente = true 
                    else showRapidaDialog = true 
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF795548))
            ) {
                Text("Rápida")
            }
        }

        // 3. CARRITO (Ocupa el resto de la pantalla)
        if (carrito.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("El carrito está vacío.\nBusca un producto para empezar.", textAlign = TextAlign.Center, color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f).padding(16.dp)) {
                items(carrito.entries.toList()) { (producto, cantidad) ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(producto.nombre, fontWeight = FontWeight.Bold, color = Color(0xFF30231F))
                                Text(formatCOP(producto.precio), style = MaterialTheme.typography.bodySmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
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
                            }
                            Text(formatCOP(producto.precio * cantidad), modifier = Modifier.width(80.dp), textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                        }
                    }
                }
            }

            // 4. TOTAL Y COBRAR
            Surface(shadowElevation = 8.dp, color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val total = carrito.entries.sumOf { it.key.precio * it.value }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Total:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(formatCOP(total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
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

    // --- DIÁLOGO DE BÚSQUEDA (Para no mostrar todo el inventario de fondo) ---
    if (showSearchDialog) {
        var searchQuery by remember { mutableStateOf("") }
        val filtrados = productos.filter { it.nombre.contains(searchQuery, ignoreCase = true) }

        AlertDialog(
            onDismissRequest = { showSearchDialog = false },
            containerColor = Color.White,
            title = { Text("Buscar Producto") },
            text = {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Escribir nombre...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(filtrados) { prod ->
                            val enCarrito = carrito[prod] ?: 0
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable {
                                        if (prod.stock > enCarrito) carrito[prod] = enCarrito + 1
                                    }
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(prod.nombre, fontWeight = FontWeight.Bold)
                                    Text("Stock: ${prod.stock}", style = MaterialTheme.typography.bodySmall)
                                }
                                Text(formatCOP(prod.precio), fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                            }
                            Divider(color = Color(0xFFE5D8CA))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSearchDialog = false }) { Text("Listo", color = Color(0xFF5D4037), fontWeight = FontWeight.Bold) }
            }
        )
    }

    // --- RESTO DE DIÁLOGOS (Forzados a color blanco) ---
    if (showAlertaCliente) {
        AlertDialog(
            onDismissRequest = { showAlertaCliente = false },
            containerColor = Color.White,
            title = { Text("Cliente Requerido") },
            text = { Text("Por favor, ingrese el nombre del cliente antes de agregar productos.") },
            confirmButton = { TextButton(onClick = { showAlertaCliente = false }) { Text("Entendido", color = Color(0xFF5D4037)) } }
        )
    }

    if (showPagoDialog) {
        var metodo by remember { mutableStateOf("Efectivo") }
        val total = carrito.entries.sumOf { it.key.precio * it.value }

        AlertDialog(
            onDismissRequest = { showPagoDialog = false },
            containerColor = Color.White,
            title = { Text("Método de Pago") },
            text = {
                Column {
                    Text("Total: ${formatCOP(total)}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = metodo == "Efectivo", onClick = { metodo = "Efectivo" }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF5D4037)))
                        Text("Efectivo")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = metodo == "Nequi", onClick = { metodo = "Nequi" }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF5D4037)))
                        Text("Nequi")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = metodo == "Fiado", onClick = { metodo = "Fiado" }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF5D4037)))
                        Text("Fiado (Pendiente)")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showPagoDialog = false
                    viewModel.procesarVenta(metodo, metodo == "Fiado") { ventaGuardada ->
                        ventaParaRecibo = ventaGuardada
                        showExitoDialog = true
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { showPagoDialog = false }) { Text("Cancelar", color = Color.Gray) } }
        )
    }

    if (itemAEliminar != null) {
        AlertDialog(
            onDismissRequest = { itemAEliminar = null },
            containerColor = Color.White,
            title = { Text("Quitar del carrito") },
            text = { Text("¿Quitar ${itemAEliminar!!.nombre}?") },
            confirmButton = { TextButton(onClick = { carrito.remove(itemAEliminar); itemAEliminar = null }) { Text("Quitar", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { itemAEliminar = null }) { Text("Cancelar", color = Color.Gray) } }
        )
    }

    if (showRapidaDialog) {
        var concepto by remember { mutableStateOf("") }
        var monto by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showRapidaDialog = false },
            containerColor = Color.White,
            title = { Text("Venta Rápida") },
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
                }) { Text("Agregar", color = Color(0xFF5D4037)) }
            },
            dismissButton = { TextButton(onClick = { showRapidaDialog = false }) { Text("Cancelar", color = Color.Gray) } }
        )
    }

    if (showExitoDialog && ventaParaRecibo != null) {
        AlertDialog(
            onDismissRequest = { showExitoDialog = false },
            containerColor = Color.White,
            title = { Text("¡Venta Exitosa!") },
            text = { Text("Total: ${formatCOP(ventaParaRecibo!!.total)}") },
            confirmButton = {
                Button(onClick = { 
                    showExitoDialog = false
                    val bitmap = generarReciboBitmap(context, ventaParaRecibo!!)
                    compartirRecibo(context, bitmap, ventaParaRecibo!!.id)
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))) { Text("Compartir Recibo") }
            },
            dismissButton = { TextButton(onClick = { showExitoDialog = false }) { Text("Cerrar", color = Color.Gray) } }
        )
    }
}
