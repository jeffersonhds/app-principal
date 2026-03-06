package com.jefferson.antenas.utils

import java.text.NumberFormat
import java.util.Locale

// --- Formatadores de Moeda ---
fun Double.toCurrency(): String {
    val ptBr = Locale("pt", "BR")
    return NumberFormat.getCurrencyInstance(ptBr).format(this)
}

fun String.toCurrency(): String {
    val value = this
        .trim()
        .replace("R$", "")
        .replace(" ", "")
        .replace(".", "") // Remove pontos de milhar (ex: 1.000 -> 1000)
        .replace(",", ".") // Troca vírgula decimal por ponto
        .toDoubleOrNull()
        ?: 0.0

    return value.toCurrency()
}