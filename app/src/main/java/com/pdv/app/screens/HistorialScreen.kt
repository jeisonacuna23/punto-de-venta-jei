package com.pdv.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
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
import java.util.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(viewModel: HistorialViewModel = viewModel(), ventaViewModel: VentaViewModel = viewModel()) {
    val todasLasVentas by viewModel.ventas.collectAsState()
    var tabSeleccionada by remember { mutableStateOf(0) }
    var ventaAAnular by remember { mutableStateOf<Venta?>(null) }
    var ventaASaldar by remember { mutableStateOf<Venta?>(null) }
    var showReportes by remember { mutableStateOf(false) }

    val ventas = if (tabSeleccionada == 0) todasLasVentas else todasLasVentas.filter { !it.pagado }

    val dateFormatToday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    dateFormatToday.timeZone = TimeZone.getTimeZone("America/Caracas")
    val todayStr = dateFormatToday.format(Date())
    
    val ventasHoy = todasLasVentas.filter { dateFormatToday.format(Date(it.fecha)) == todayStr }
    val totalEfectivo = ventasHoy.filter { it.metodoPago == "Efectivo" && it.pagado }.sumOf { it.total }
    val totalNequi = ventasHoy.filter { it.metodoPago == "Nequi" && it.pagado }.sumOf { it.total }
    val totalFiado = ventasHoy.filter { !it.pagado }.sumOf { it.total }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF30231F))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Resumen de Hoy", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { showReportes = true }) {
                        Icon(Icons.Filled.Info, contentDescription = "Ver Reportes", tint = Color(0xFFE5D8CA))
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Efectivo:", color = Color.White)
                    Text(formatCOP(totalEfectivo), color = Color(0xFFE5D8CA), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Nequi:", color = Color.White)
                    Text(formatCOP(totalNequi), color = Color(0xFFE5D8CA), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Fiados:", color = Color.White)
                    Text(formatCOP(totalFiado), color = Color(0xFFE5D8CA), fontWeight = FontWeight.Bold)
                }
            }
        }

        TabRow(selectedTabIndex = tabSeleccionada, containerColor = Color(0xFFF7F3ED), contentColor = Color(0xFF5D4037)) {
            Tab(selected = tabSeleccionada == 0, onClick = { tabSeleccionada = 0 }, text = { Text("Todas") })
            Tab(selected = tabSeleccionada == 1, onClick = { tabSeleccionada = 1 }, text = { Text("Fiados Pendientes") })
        }

        if (ventas.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (tabSeleccionada == 0) "No hay ventas." else "No hay fiados pendientes.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ventas) { venta ->
                    VentaItem(venta, onAnular = { ventaAAnular = venta }, onSaldar = { ventaASaldar = venta })
                }
            }
        }
    }

    // DIÁLOGO DE REPORTES
    if (showReportes) {
        // Cálculos del mes y semana
        val cal = Calendar.getInstance(TimeZone.getTimeZone("America/Caracas"))
        val mesActual = cal.get(Calendar.MONTH)
        val anioActual = cal.get(Calendar.YEAR)
        
        val ventasMes = todasLasVentas.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.fecha }
            c.get(Calendar.MONTH) == mesActual && c.get(Calendar.YEAR) == anioActual
        }
        val totalMes = ventasMes.sumOf { it.total }
        
        val deudaTotal = todasLasVentas.filter { !it.pagado }.sumOf { it.total }

        // Top 3 productos
        val productosContados = todasLasVentas.flatMap { 
            it.detalles.split(" | ") 
        }.groupBy { 
            it.substringAfter(" ").substringBefore(" -").trim() 
        }.mapValues { it.value.size }.toList().sortedByDescending { it.second }.take(3)

        AlertDialog(
            onDismissRequest = { showReportes = false },
            containerColor = Color.White,
            title = { Text("Reportes del Negocio", color = Color(0xFF5D4037), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Ventas del Mes:", fontWeight = FontWeight.Bold, color = Color(0xFF30231F))
                    Text(formatCOP(totalMes), style = MaterialTheme.typography.headlineSmall, color = Color(0xFF5D4037))
                    
                    Divider(color = Color(0xFFE5D8CA))
                    
                    Text("Deudas Totales (Fiados):", fontWeight = FontWeight.Bold, color = Color(0xFF30231F))
                    Text(formatCOP(deudaTotal), style = MaterialTheme.typography.headlineSmall, color = Color.Red)
                    
                    Divider(color = Color(0xFFE5D8CA))
                    
                    Text("Top Productos Más Vendidos:", fontWeight = FontWeight.Bold, color = Color(0xFF30231F))
                    if (productosContados.isEmpty()) {
                        Text("Aún no hay ventas registradas.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        productosContados.forEachIndexed { index, (nombre, cantidad) ->
                            Text("${index + 1}. $nombre ($cantidad vendidos)", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showReportes = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))) { Text("Cerrar") }
            }
        )
    }

    if (ventaAAnular != null) {
        AlertDialog(
            onDismissRequest = { ventaAAnular = null },
            containerColor = Color.White,
            title = { Text("Anular Venta") },
            text = { Text("¿Estás seguro de anular la venta VSJA-${String.format("%04d", ventaAAnular!!.id)}?") },
            confirmButton = { TextButton(onClick = { ventaViewModel.anularVenta(ventaAAnular!!); ventaAAnular = null }) { Text("Anular", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { ventaAAnular = null }) { Text("Cancelar", color = Color.Gray) } }
        )
    }

    if (ventaASaldar != null) {
        var metodo by remember { mutableStateOf("Efectivo") }
        AlertDialog(
            onDismissRequest = { ventaASaldar = null },
            containerColor = Color.White,
            title = { Text("Saldar Deuda") },
            text = {
                Column {
                    Text("Total a pagar: ${formatCOP(ventaASaldar!!.total)}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = metodo == "Efectivo", onClick = { metodo = "Efectivo" }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF5D4037)))
                        Text("Efectivo")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = metodo == "Nequi", onClick = { metodo = "Nequi" }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF5D4037)))
                        Text("Nequi")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    ventaViewModel.saldarDeuda(ventaASaldar!!, metodo)
                    ventaASaldar = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))) { Text("Confirmar Pago") }
            },
            dismissButton = { TextButton(onClick = { ventaASaldar = null }) { Text("Cancelar", color = Color.Gray) } }
        )
    }
}

@Composable
fun VentaItem(venta: Venta, onAnular: () -> Unit, onSaldar: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("America/Caracas")
    val fechaStr = dateFormat.format(Date(venta.fecha))

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("VSJA-${String.format("%04d", venta.id)}", fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                Text(fechaStr, style = MaterialTheme.typography.bodySmall)
            }
            if (venta.cliente.isNotBlank()) Text("Cliente: ${venta.cliente}", fontWeight = FontWeight.Bold)
            Text("Pago: ${venta.metodoPago}", style = MaterialTheme.typography.bodySmall, color = if (!venta.pagado) Color.Red else Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(venta.detalles.replace(" | ", "\n"), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Total: ${formatCOP(venta.total)}", fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                Row {
                    if (!venta.pagado) {
                        IconButton(onClick = onSaldar) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Saldar", tint = Color(0xFF2E7D32))
                        }
                    }
                    IconButton(onClick = onAnular) {
                        Icon(Icons.Filled.Delete, contentDescription = "Anular", tint = Color.Red)
                    }
                }
            }
        }
    }
}
