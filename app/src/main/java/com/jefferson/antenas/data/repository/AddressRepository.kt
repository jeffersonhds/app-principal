package com.jefferson.antenas.data.repository

data class AddressResult(
    val logradouro: String,
    val bairro: String,
    val cidade: String,
    val uf: String
)

interface AddressRepository {
    suspend fun fetchAddress(cep: String): AddressResult
}
