package com.pdv.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.pdv.app.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf(
        "Venta" to Icons.Filled.ShoppingCart,
        "Inventario" to Icons.Filled.Inventory,
        "Historial" to Icons.Filled.History,
        "Recibos" to Icons.Filled.Receipt
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(items[selectedItem].first) })
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
