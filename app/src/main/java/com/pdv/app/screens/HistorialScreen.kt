package com.pdv.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdv.app.HistorialViewModel
import com.pdv.app.VentaViewModel
import com.pdv.app.data.Venta
import com.pdv.app.utils.formatCOP
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(viewModel: HistorialViewModel = viewModel(), ventaViewModel: VentaViewModel = viewModel()) {
    val ventas by viewModel.ventas.collectAsState()
    var ventaAAnular by remember { mutableStateOf<Venta?>(null) }

    // Calcular estadísticas del día (Zona horaria Venezuela)
    val dateFormatToday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    dateFormatToday.timeZone = TimeZone.getTimeZone("America/Caracas")
    val todayStr = dateFormatToday.format(Date())

    val ventasHoy = ventas.filter {
        dateFormatToday.format(Date(it.fecha)) == todayStr
    }

    val totalHoy = ventasHoy.sumOf { it.total }
    var itemsHoy = 0
    ventasHoy.forEach { venta ->
        venta.detalles.split(" | ").forEach { item ->
            val qty = item.substringBefore("x").trim().toIntOrNull() ?: 0
            itemsHoy += qty
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // PANEL DE RESUMEN
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Resumen de Hoy", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ventas realizadas: ${ventasHoy.size}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Total: ${formatCOP(totalHoy)}", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                }
                Text("Productos vendidos: $itemsHoy", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.bodySmall)
            }
        }

        if (ventas.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay ventas registradas.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ventas) { venta ->
                    VentaItem(venta, onAnular = { ventaAAnular = venta })
                }
            }
        }
    }

    if (ventaAAnular != null) {
        AlertDialog(
            onDismissRequest = { ventaAAnular = null },
            title = { Text("Anular Venta") },
            text = { Text("¿Estás seguro de anular la venta VSJA-${String.format("%04d", ventaAAnular!!.id)}?\nEl stock será devuelto al inventario.") },
            confirmButton = {
                TextButton(onClick = {
                    ventaViewModel.anularVenta(ventaAAnular!!)
                    ventaAAnular = null
                }) { Text("Anular", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { ventaAAnular = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun VentaItem(venta: Venta, onAnular: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("America/Caracas")
    val fechaStr = dateFormat.format(Date(venta.fecha))

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("VSJA-${String.format("%04d", venta.id)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(fechaStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (venta.cliente.isNotBlank()) {
                Text("Cliente: ${venta.cliente}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(venta.detalles.replace(" | ", "\n"), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total: ${formatCOP(venta.total)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onAnular) {
                    Icon(Icons.Filled.Delete, contentDescription = "Anular Venta", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
