package com.jefferson.antenas.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class Product(
    // O servidor manda ID numérico, mas o GSON converte para String automaticamente para facilitar a navegação
    @PrimaryKey
    @SerializedName("id") val id: String,

    @SerializedName("name") val name: String,

    @SerializedName("description") val description: String,

    // O servidor manda decimal/string, aqui garantimos String para não dar erro de arredondamento
    @SerializedName("price") val price: String,

    @SerializedName("imageUrl") val imageUrl: String,

    // Campos novos que adicionamos no servidor
    @SerializedName("category") val category: String? = null,

    @SerializedName("discount") val discount: Int? = 0,

    @SerializedName("isNew") val isNew: Boolean? = false
) {
    fun getDiscountedPrice(): Double {
        val basePrice = price
            .trim()
            .replace("R$", "")
            .replace(" ", "")
            .replace(",", ".")
            .toDoubleOrNull()
            ?: 0.0

        val discountPercent = (discount ?: 0).coerceAtLeast(0)
        val multiplier = 1.0 - (discountPercent / 100.0)

        return (basePrice * multiplier).coerceAtLeast(0.0)
    }
}