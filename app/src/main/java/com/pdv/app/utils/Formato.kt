package com.pdv.app.utils

import java.text.NumberFormat
import java.util.Locale

fun formatCOP(precio: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    format.maximumFractionDigits = 0
    return format.format(precio)
}
