package com.pdv.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdv.app.HistorialViewModel
import com.pdv.app.data.Venta
import com.pdv.app.utils.formatCOP
import com.pdv.app.utils.generarYCompartirRecibo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecibosScreen(viewModel: HistorialViewModel = viewModel()) {
    val ventas by viewModel.ventas.collectAsState()
    val context = LocalContext.current

    if (ventas.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay recibos para mostrar.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ventas) { venta ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Venta #${venta.id} - ${formatCOP(venta.total)}", style = MaterialTheme.typography.titleMedium)
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            Text(dateFormat.format(Date(venta.fecha)), style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { generarYCompartirRecibo(context, venta) }) {
                            Icon(Icons.Filled.Share, contentDescription = "Compartir", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
