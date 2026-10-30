package com.pdv.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.pdv.app.ui.theme.PdvTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdv.app.screens.*
import com.pdv.app.utils.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PdvTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedItem by remember { mutableStateOf(0) }
    var showBackupMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val inventarioViewModel: InventarioViewModel = viewModel()
    val historialViewModel: HistorialViewModel = viewModel()

    val items = listOf(
        "Venta" to Icons.Filled.ShoppingCart,
        "Inventario" to Icons.Filled.Inventory,
        "Historial" to Icons.Filled.History,
        "Recibos" to Icons.Filled.Receipt
    )

    // Lanzadores para guardar archivos CSV
    val exportProductsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let {
            exportarProductosCSV(context, it, inventarioViewModel.productos.value)
            Toast.makeText(context, "Productos exportados", Toast.LENGTH_SHORT).show()
        }
    }
    
    val exportSalesLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let {
            exportarVentasCSV(context, it, historialViewModel.ventas.value)
            Toast.makeText(context, "Ventas exportadas", Toast.LENGTH_SHORT).show()
        }
    }

    val importProductsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            importarProductosCSV(context, it) { productos ->
                inventarioViewModel.importarProductos(productos)
                Toast.makeText(context, "${productos.size} productos importados", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(items[selectedItem].first) },
                actions = {
                    IconButton(onClick = { showBackupMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Opciones de Respaldo")
                    }
                    DropdownMenu(expanded = showBackupMenu, onDismissRequest = { showBackupMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Exportar Inventario (CSV)") },
                            onClick = { exportProductsLauncher.launch("inventario.csv"); showBackupMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Exportar Ventas (CSV)") },
                            onClick = { exportSalesLauncher.launch("ventas.csv"); showBackupMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Importar Inventario (CSV)") },
                            onClick = { importProductsLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/csv")); showBackupMenu = false }
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.second, contentDescription = item.first) },
                        label = { Text(item.first) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedItem) {
                0 -> VentaScreen()
                1 -> InventarioScreen()
                2 -> HistorialScreen()
                3 -> RecibosScreen()
            }
        }
    }
}
