package com.pdv.app.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pdv_prefs", Context.MODE_PRIVATE) }
    
    // Si no hay PIN guardado, entramos en modo "Crear PIN". Si hay, en modo "Ingresar PIN".
    val savedPin = prefs.getString("pin", null)
    val isSetupMode = savedPin == null

    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("PUNTO DE VENTA", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            if (isSetupMode) "Crea un PIN de 4 dígitos para proteger tu app" else "Ingresa tu PIN de 4 dígitos",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF30231F)
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = { 
                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                    pin = it
                    error = false
                }
            },
            label = { Text("PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            isError = error,
            modifier = Modifier.width(200.dp),
            singleLine = true
        )

        if (error) {
            Text("PIN incorrecto", color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (pin.length == 4) {
                    if (isSetupMode) {
                        // Guardar el nuevo PIN
                        prefs.edit().putString("pin", pin).apply()
                        onLoginSuccess()
                    } else {
                        // Verificar el PIN existente
                        if (pin == savedPin) {
                            onLoginSuccess()
                        } else {
                            error = true
                            pin = ""
                        }
                    }
                }
            },
            modifier = Modifier.width(200.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))
        ) {
            Text(if (isSetupMode) "Crear PIN y Entrar" else "Ingresar")
        }
    }
}
