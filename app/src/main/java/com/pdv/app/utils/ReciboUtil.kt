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
import kotlin.math.max

fun generarReciboBitmap(context: Context, venta: Venta): Bitmap {
    val prefs = context.getSharedPreferences("pdv_prefs", Context.MODE_PRIVATE)
    val bizName = prefs.getString("biz_name", "VARIEDADES Y SERVICIOS PDB") ?: "VARIEDADES Y SERVICIOS PDB"
    val bizPhone = prefs.getString("biz_phone", "") ?: ""
    val bizAddress = prefs.getString("biz_address", "") ?: ""
    val bizFooter = prefs.getString("biz_footer", "¡Gracias por su compra!") ?: "¡Gracias por su compra!"

    val items = venta.detalles.split(" | ")
    val calculatedHeight = 600 + (items.size * 35)
    val finalHeight = max(calculatedHeight, 700)
    
    val bitmap = Bitmap.createBitmap(600, finalHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)

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
    
    // CABECERA DEL NEGOCIO
    paint.textSize = 32f
    paint.isFakeBoldText = true
    canvas.drawText(bizName.uppercase(), centerX, y, paint)
    
    paint.textSize = 16f
    paint.isFakeBoldText = false
    if (bizAddress.isNotBlank()) {
        y += 25f
        canvas.drawText(bizAddress, centerX, y, paint)
    }
    if (bizPhone.isNotBlank()) {
        y += 25f
        canvas.drawText("Tel: $bizPhone", centerX, y, paint)
    }

    y += 35f
    paint.textSize = 20f
    paint.isFakeBoldText = true
    canvas.drawText("Comprobante de Venta", centerX, y, paint)

    // DATOS DE LA VENTA
    val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("America/Caracas")
    val fechaStr = dateFormat.format(Date(venta.fecha))

    y += 40f
    paint.textAlign = Paint.Align.LEFT
    paint.isFakeBoldText = false
    canvas.drawText("Ref: VSJA-${String.format("%04d", venta.id)}", 40f, y, paint)
    
    y += 30f
    canvas.drawText("Fecha: $fechaStr", 40f, y, paint)
    
    if (venta.cliente.isNotBlank()) {
        y += 30f
        canvas.drawText("Cliente: ${venta.cliente}", 40f, y, paint)
    }

    y += 30f
    canvas.drawText("Pago: ${venta.metodoPago}", 40f, y, paint)

    y += 30f
    canvas.drawText("----------------------------------------", 40f, y, paint)

    // PRODUCTOS
    y += 40f
    paint.textSize = 18f
    for (item in items) {
        canvas.drawText("• $item", 40f, y, paint)
        y += 30f
    }

    y += 10f
    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText("----------------------------------------", 40f, y, paint)
    
    y += 40f
    paint.textSize = 30f
    canvas.drawText("TOTAL: ${formatCOP(venta.total)}", 40f, y, paint)

    y += 50f
    paint.textAlign = Paint.Align.CENTER
    paint.textSize = 22f
    
    if (!venta.pagado) {
        paint.color = Color.RED
        paint.isFakeBoldText = true
        canvas.drawText("*** FIADO - PENDIENTE ***", centerX, y, paint)
        paint.color = Color.BLACK
        y += 35f
    }

    paint.textSize = 18f
    paint.isFakeBoldText = false
    canvas.drawText(bizFooter, centerX, y, paint)

    return bitmap
}

fun compartirRecibo(context: Context, bitmap: Bitmap, ventaId: Int) {
    val cachePath = File(context.cacheDir, "images")
    cachePath.mkdirs()
    val file = File(cachePath, "recibo_VSJA$ventaId.png")
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
