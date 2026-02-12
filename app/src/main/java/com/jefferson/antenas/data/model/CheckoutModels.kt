package com.jefferson.antenas.data.model

import com.google.gson.annotations.SerializedName

// O que enviamos para o servidor (Agora com customerInfo)
data class CheckoutRequest(
    @SerializedName("items") val items: List<CheckoutItemDto>,
    @SerializedName("customerInfo") val customerInfo: CustomerInfoDto // Campo Novo
)

// Dados do Cliente
data class CustomerInfoDto(
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("city") val city: String,
    @SerializedName("phoneNumber") val phoneNumber: String
)

data class CheckoutItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("quantity") val quantity: Int
)

// O que recebemos (Igual)
data class CheckoutResponse(
    @SerializedName("paymentIntent") val paymentIntent: String,
    @SerializedName("ephemeralKey") val ephemeralKey: String,
    @SerializedName("customer") val customer: String
)