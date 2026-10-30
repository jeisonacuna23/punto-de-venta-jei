package com.pdv.app

import android.content.Context
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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdv.app.screens.*
import com.pdv.app.ui.theme.PdvTheme
import com.pdv.app.utils.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PdvTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var isLoggedIn by remember { mutableStateOf(false) }

    if (!isLoggedIn) {
        LoginScreen(onLoginSuccess = { isLoggedIn = true })
    } else {
        MainScreen(onLogout = { isLoggedIn = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onLogout: () -> Unit) {
    var selectedItem by remember { mutableStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val inventarioViewModel: InventarioViewModel = viewModel()
    val historialViewModel: HistorialViewModel = viewModel()

    val items = listOf(
        "Venta" to Icons.Filled.ShoppingCart,
        "Inventario" to Icons.Filled.Inventory,
        "Historial" to Icons.Filled.History,
        "Recibos" to Icons.Filled.Receipt
    )

    val exportProductsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { exportarProductosCSV(context, it, inventarioViewModel.productos.value); Toast.makeText(context, "Productos exportados", Toast.LENGTH_SHORT).show() }
    }
    val exportSalesLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { exportarVentasCSV(context, it, historialViewModel.ventas.value); Toast.makeText(context, "Ventas exportadas", Toast.LENGTH_SHORT).show() }
    }
    val importProductsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { importarProductosCSV(context, it) { prod -> inventarioViewModel.importarProductos(prod); Toast.makeText(context, "${prod.size} productos importados", Toast.LENGTH_SHORT).show() } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(items[selectedItem].first, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF30231F)),
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Opciones", tint = Color.White)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Configurar Recibo / Negocio") }, onClick = { showSettingsDialog = true; showMenu = false })
                        HorizontalDivider(color = Color(0xFFE5D8CA))
                        DropdownMenuItem(text = { Text("Exportar Inventario (CSV)") }, onClick = { exportProductsLauncher.launch("inventario.csv"); showMenu = false })
                        DropdownMenuItem(text = { Text("Exportar Ventas (CSV)") }, onClick = { exportSalesLauncher.launch("ventas.csv"); showMenu = false })
                        DropdownMenuItem(text = { Text("Importar Inventario (CSV)") }, onClick = { importProductsLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/csv")); showMenu = false })
                        HorizontalDivider(color = Color(0xFFE5D8CA))
                        DropdownMenuItem(text = { Text("Cerrar Sesión", color = Color.Red) }, onClick = { showMenu = false; onLogout() })
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFF7F3ED)) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.second, contentDescription = item.first) },
                        label = { Text(item.first) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color(0xFF30231F),
                            indicatorColor = Color(0xFF5D4037),
                            unselectedIconColor = Color(0xFF795548),
                            unselectedTextColor = Color(0xFF30231F)
                        )
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

    if (showSettingsDialog) {
        SettingsDialog(onDismiss = { showSettingsDialog = false })
    }
}

@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pdv_prefs", Context.MODE_PRIVATE) }
    
    var bizName by remember { mutableStateOf(prefs.getString("biz_name", "VARIEDADES Y SERVICIOS PDB") ?: "") }
    var bizPhone by remember { mutableStateOf(prefs.getString("biz_phone", "") ?: "") }
    var bizAddress by remember { mutableStateOf(prefs.getString("biz_address", "") ?: "") }
    var bizFooter by remember { mutableStateOf(prefs.getString("biz_footer", "¡Gracias por su compra!") ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Configuración del Recibo", color = Color(0xFF5D4037), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = bizName, onValueChange = { bizName = it }, label = { Text("Nombre del Negocio") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = bizPhone, onValueChange = { bizPhone = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = bizAddress, onValueChange = { bizAddress = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = bizFooter, onValueChange = { bizFooter = it }, label = { Text("Mensaje Final (Pie de página)") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)
            }
        },
        confirmButton = {
            Button(onClick = {
                prefs.edit()
                    .putString("biz_name", bizName)
                    .putString("biz_phone", bizPhone)
                    .putString("biz_address", bizAddress)
                    .putString("biz_footer", bizFooter)
                    .apply()
                Toast.makeText(context, "Datos guardados", Toast.LENGTH_SHORT).show()
                onDismiss()
            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
        }
    )
}
