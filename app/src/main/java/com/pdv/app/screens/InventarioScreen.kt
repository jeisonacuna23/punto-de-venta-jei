package com.pdv.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdv.app.InventarioViewModel
import com.pdv.app.data.Product
import com.pdv.app.utils.formatCOP

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen(viewModel: InventarioViewModel = viewModel()) {
    val productos by viewModel.productos.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var productoEditando by remember { mutableStateOf<Product?>(null) }
    var productoAEliminar by remember { mutableStateOf<Product?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val productosFiltrados = productos.filter { it.nombre.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                productoEditando = null
                showDialog = true 
            }, containerColor = Color(0xFF5D4037), contentColor = Color.White) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar Producto")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar producto...") },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF5D4037),
                    unfocusedBorderColor = Color(0xFFE5D8CA)
                )
            )

            if (productosFiltrados.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (productos.isEmpty()) "No hay productos. Toca el + para agregar." else "No se encontraron resultados.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(productosFiltrados) { producto ->
                        ProductoItem(
                            producto = producto,
                            onEdit = { 
                                productoEditando = producto
                                showDialog = true 
                            },
                            onDelete = { productoAEliminar = producto }
                        )
                    }
                }
            }
        }
    }

    if (productoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("Eliminar Producto") },
            text = { Text("¿Estás seguro de eliminar '${productoAEliminar!!.nombre}' del inventario?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarProducto(productoAEliminar!!)
                    productoAEliminar = null
                }) { Text("Eliminar", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { productoAEliminar = null }) { Text("Cancelar") } }
        )
    }

    if (showDialog) {
        ProductDialog(
            producto = productoEditando,
            onDismiss = { showDialog = false },
            onSave = { nombre, precio, stock ->
                if (productoEditando == null) {
                    viewModel.agregarProducto(nombre, precio, stock)
                } else {
                    viewModel.actualizarProducto(productoEditando!!.copy(nombre = nombre, precio = precio, stock = stock))
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun ProductoItem(producto: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    // AQUÍ ESTABA EL ERROR: Forzamos el color de la tarjeta a Blanco puro (0xFFFFFFFF)
    Card(
        modifier = Modifier.fillMaxWidth(), 
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color(0xFF30231F))
                Text("Precio: ${formatCOP(producto.precio)}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF30231F))
                Text("Stock: ${producto.stock} unidades", style = MaterialTheme.typography.bodySmall, color = Color(0xFF795548))
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = Color(0xFF5D4037))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = Color.Red)
            }
        }
    }
}

@Composable
fun ProductDialog(producto: Product?, onDismiss: () -> Unit, onSave: (String, Double, Int) -> Unit) {
    var nombre by remember { mutableStateOf(producto?.nombre ?: "") }
    var precio by remember { mutableStateOf(producto?.precio?.toString() ?: "") }
    var stock by remember { mutableStateOf(producto?.stock?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (producto == null) "Nuevo Producto" else "Editar Producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val p = precio.toDoubleOrNull() ?: 0.0
                val s = stock.toIntOrNull() ?: 0
                if (nombre.isNotBlank()) onSave(nombre, p, s)
            }) { Text("Guardar", color = Color(0xFF5D4037)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) } }
    )
}
