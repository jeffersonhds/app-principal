package com.jefferson.antenas.utils

import com.jefferson.antenas.data.model.Product
import java.text.NumberFormat
import java.util.Locale

// --- Calculadora de Desconto (CORRIGIDA) ---
fun Product.getDiscountedPrice(): Double {
    // 1. Limpa o texto e transforma em número (Blinda contra erros)
    val priceString = this.price.toString()
        .replace("R$", "")
        .trim()
        .replace(",", ".") // Troca vírgula por ponto para o sistema entender

    val priceDouble = priceString.toDoubleOrNull() ?: 0.0

    // 2. Agora sim fazemos a conta matemática
    return if (this.discount != null && this.discount > 0) {
        priceDouble * (1 - this.discount / 100.0)
    } else {
        priceDouble
    }
}

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