package com.pdv.app.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.pdv.app.R
import com.pdv.app.data.Venta
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun generarYCompartirRecibo(context: Context, venta: Venta) {
    val bitmap = Bitmap.createBitmap(600, 900, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)

    // Cargar fuente Courier Prime
    val typeface = ResourcesCompat.getFont(context, R.font.courier_prime) ?: Typeface.MONOSPACE

    val paint = Paint().apply {
        color = Color.BLACK
        textSize = 24f
        isAntiAlias = true
        this.typeface = typeface
    }

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

    // Formato: America/Caracas y 12 horas
    val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("America/Caracas")
    val fechaStr = dateFormat.format(Date(venta.fecha))

    y += 40f
    paint.textAlign = Paint.Align.LEFT
    canvas.drawText("Ref: VSJA-${String.format("%04d", venta.id)}", 40f, y, paint)
    
    y += 30f
    canvas.drawText("Fecha: $fechaStr", 40f, y, paint)
    
    if (venta.cliente.isNotBlank()) {
        y += 30f
        canvas.drawText("Cliente: ${venta.cliente}", 40f, y, paint)
    }

    y += 30f
    canvas.drawText("----------------------------------------", 40f, y, paint)

    y += 40f
    canvas.drawText("Detalles:", 40f, y, paint)
    
    y += 30f
    paint.textSize = 18f
    val palabras = venta.detalles.split(", ")
    for (palabra in palabras) {
        if (y < 750f) {
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

    val cachePath = File(context.cacheDir, "images")
    cachePath.mkdirs()
    val file = File(cachePath, "recibo_VSJA${venta.id}.png")
    val stream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    stream.close()

    val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir recibo vía..."))
}
