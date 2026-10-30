package com.pdv.app.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.core.content.FileProvider
import com.pdv.app.data.Venta
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun generarYCompartirRecibo(context: Context, venta: Venta) {
    // 1. Crear el Bitmap (lienzo en blanco de 600x800)
    val bitmap = Bitmap.createBitmap(600, 800, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)

    val paint = Paint().apply {
        color = Color.BLACK
        textSize = 24f
        isAntiAlias = true
    }

    // 2. Dibujar el recibo
    var y = 60f
    val centerX = 300f

    paint.textAlign = Paint.Align.CENTER
    paint.textSize = 36f
    paint.isFakeBoldText = true
    canvas.drawText("PUNTO DE VENTA", centerX, y, paint)
    
    y += 40f
    paint.textSize = 20f
    paint.isFakeBoldText = false
    canvas.drawText("Comprobante de Venta", centerX, y, paint)

    y += 40f
    paint.textAlign = Paint.Align.LEFT
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    canvas.drawText("Fecha: ${dateFormat.format(Date(venta.fecha))}", 40f, y, paint)
    
    y += 30f
    canvas.drawText("Venta #: ${venta.id}", 40f, y, paint)

    y += 30f
    canvas.drawText("----------------------------------------", 40f, y, paint)

    y += 40f
    canvas.drawText("Detalles:", 40f, y, paint)
    
    y += 30f
    paint.textSize = 18f
    // Si el texto es muy largo, lo partimos en varias líneas
    val palabras = venta.detalles.split(", ")
    for (palabra in palabras) {
        if (y < 650f) {
            canvas.drawText("• $palabra", 60f, y, paint)
            y += 25f
        }
    }

    y += 20f
    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText("----------------------------------------", 40f, y, paint)
    
    y += 40f
    paint.textSize = 30f
    canvas.drawText("TOTAL: ${formatCOP(venta.total)}", 40f, y, paint)

    y += 60f
    paint.textAlign = Paint.Align.CENTER
    paint.textSize = 18f
    paint.isFakeBoldText = false
    canvas.drawText("¡Gracias por su compra!", centerX, y, paint)

    // 3. Guardar la imagen en la caché
    val cachePath = File(context.cacheDir, "images")
    cachePath.mkdirs()
    val file = File(cachePath, "recibo_${venta.id}.png")
    val stream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    stream.close()

    // 4. Compartir usando FileProvider
    val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir recibo vía..."))
}
