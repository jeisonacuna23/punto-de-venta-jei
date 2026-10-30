package com.pdv.app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdv.app.HistorialViewModel
import com.pdv.app.data.Venta
import com.pdv.app.utils.formatCOP
import com.pdv.app.utils.generarReciboBitmap
import com.pdv.app.utils.compartirRecibo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun RecibosScreen(viewModel: HistorialViewModel = viewModel()) {
    val ventas by viewModel.ventas.collectAsState()
    var ventaParaPrevisualizar by remember { mutableStateOf<Venta?>(null) }
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
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("VSJA-${String.format("%04d", venta.id)} - ${formatCOP(venta.total)}", style = MaterialTheme.typography.titleMedium, color = Color(0xFF5D4037))
                            if (venta.cliente.isNotBlank()) Text("Cliente: ${venta.cliente}", style = MaterialTheme.typography.bodySmall)
                            if (!venta.pagado) Text("ESTADO: FIADO", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
                            dateFormat.timeZone = TimeZone.getTimeZone("America/Caracas")
                            Text(dateFormat.format(Date(venta.fecha)), style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { ventaParaPrevisualizar = venta }) {
                            Icon(Icons.Filled.Share, contentDescription = "Previsualizar y Compartir", tint = Color(0xFF5D4037))
                        }
                    }
                }
            }
        }
    }

    if (ventaParaPrevisualizar != null) {
        val bitmap = remember(ventaParaPrevisualizar) { generarReciboBitmap(context, ventaParaPrevisualizar!!) }
        AlertDialog(
            onDismissRequest = { ventaParaPrevisualizar = null },
            title = { Text("Vista Previa del Recibo") },
            text = {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Recibo",
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                )
            },
            confirmButton = {
                Button(onClick = {
                    compartirRecibo(context, bitmap, ventaParaPrevisualizar!!.id)
                    ventaParaPrevisualizar = null
                }) { Text("Compartir") }
            },
            dismissButton = {
                TextButton(onClick = { ventaParaPrevisualizar = null }) { Text("Cerrar") }
            }
        )
    }
}
