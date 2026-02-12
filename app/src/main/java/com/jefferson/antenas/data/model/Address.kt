package com.jefferson.antenas.data.model

/**
 * Representa um endereço físico, usado para informações de entrega e cobrança.
 */
data class Address(
    val city: String?,
    val country: String? = "BR", // Padrão para Brasil
    val line1: String?,
    val line2: String? = null,
    val postalCode: String?,
    val state: String?
)
