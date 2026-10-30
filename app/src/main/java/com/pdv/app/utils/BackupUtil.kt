package com.pdv.app.utils

import android.content.Context
import android.net.Uri
import com.pdv.app.data.Product
import com.pdv.app.data.Venta
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

fun exportarProductosCSV(context: Context, uri: Uri, productos: List<Product>) {
    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
        outputStream.bufferedWriter().use { writer ->
            writer.write("ID,Nombre,Precio,Stock\n")
            productos.forEach {
                writer.write("${it.id},\"${it.nombre}\",${it.precio},${it.stock}\n")
            }
        }
    }
}

fun exportarVentasCSV(context: Context, uri: Uri, ventas: List<Venta>) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
        outputStream.bufferedWriter().use { writer ->
            writer.write("ID,Fecha,Cliente,Detalles,Total\n")
            ventas.forEach {
                val fecha = dateFormat.format(Date(it.fecha))
                writer.write("${it.id},$fecha,\"${it.cliente}\",\"${it.detalles}\",${it.total}\n")
            }
        }
    }
}

fun importarProductosCSV(context: Context, uri: Uri, onComplete: (List<Product>) -> Unit) {
    val productos = mutableListOf<Product>()
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            reader.readLine() // Saltar cabecera
            var line = reader.readLine()
            while (line != null) {
                val parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                if (parts.size >= 4) {
                    val nombre = parts[1].replace("\"", "")
                    val precio = parts[2].toDoubleOrNull() ?: 0.0
                    val stock = parts[3].toIntOrNull() ?: 0
                    productos.add(Product(nombre = nombre, precio = precio, stock = stock))
                }
                line = reader.readLine()
            }
        }
    }
    onComplete(productos)
}
