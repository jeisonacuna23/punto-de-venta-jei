package com.pdv.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val todasLasVentas by viewModel.ventas.collectAsState()
    var tabSeleccionada by remember { mutableStateOf(0) }
    var ventaAAnular by remember { mutableStateOf<Venta?>(null) }

    val ventas = if (tabSeleccionada == 0) todasLasVentas else todasLasVentas.filter { !it.pagado }

    val dateFormatToday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    dateFormatToday.timeZone = TimeZone.getTimeZone("America/Caracas")
    val todayStr = dateFormatToday.format(Date())
    val ventasHoy = todasLasVentas.filter { dateFormatToday.format(Date(it.fecha)) == todayStr }
    val totalHoy = ventasHoy.sumOf { it.total }

    Column(modifier = Modifier.fillMaxSize()) {
        // PANEL DE RESUMEN
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF5D4037))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Resumen de Hoy", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ventas: ${ventasHoy.size}", color = Color.White)
                    Text("Total: ${formatCOP(totalHoy)}", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        TabRow(selectedTabIndex = tabSeleccionada, containerColor = Color(0xFFF7F3ED)) {
            Tab(selected = tabSeleccionada == 0, onClick = { tabSeleccionada = 0 }, text = { Text("Todas") })
            Tab(selected = tabSeleccionada == 1, onClick = { tabSeleccionada = 1 }, text = { Text("Fiados Pendientes") })
        }

        if (ventas.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (tabSeleccionada == 0) "No hay ventas." else "No hay fiados pendientes.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ventas) { venta ->
                    VentaItem(venta, onAnular = { ventaAAnular = venta }, onSaldar = { ventaViewModel.saldarDeuda(venta) })
                }
            }
        }
    }

    if (ventaAAnular != null) {
        AlertDialog(
            onDismissRequest = { ventaAAnular = null },
            title = { Text("Anular Venta") },
            text = { Text("¿Estás seguro de anular la venta VSJA-${String.format("%04d", ventaAAnular!!.id)}?") },
            confirmButton = { TextButton(onClick = { ventaViewModel.anularVenta(ventaAAnular!!); ventaAAnular = null }) { Text("Anular", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { ventaAAnular = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun VentaItem(venta: Venta, onAnular: () -> Unit, onSaldar: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("America/Caracas")
    val fechaStr = dateFormat.format(Date(venta.fecha))

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("VSJA-${String.format("%04d", venta.id)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(fechaStr, style = MaterialTheme.typography.bodySmall)
            }
            if (venta.cliente.isNotBlank()) Text("Cliente: ${venta.cliente}", fontWeight = FontWeight.Bold)
            Text("Pago: ${venta.metodoPago}", style = MaterialTheme.typography.bodySmall, color = if (!venta.pagado) Color.Red else Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(venta.detalles.replace(" | ", "\n"), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Total: ${formatCOP(venta.total)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row {
                    if (!venta.pagado) {
                        IconButton(onClick = onSaldar) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Saldar", tint = Color(0xFF2E7D32))
                        }
                    }
                    IconButton(onClick = onAnular) {
                        Icon(Icons.Filled.Delete, contentDescription = "Anular", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
