package com.jefferson.antenas.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressRepositoryImpl @Inject constructor(
    private val okHttpClient: OkHttpClient
) : AddressRepository {

    override suspend fun fetchAddress(cep: String): AddressResult {
        val rawCep = cep.filter { it.isDigit() }
        if (rawCep.length != 8) throw IllegalArgumentException("CEP inválido. Digite os 8 números.")

        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://viacep.com.br/ws/$rawCep/json/")
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("CEP não encontrado")
                val body = response.body?.string() ?: throw Exception("Resposta vazia")
                val json = JSONObject(body)
                if (json.optBoolean("erro", false)) throw Exception("CEP não encontrado")
                AddressResult(
                    logradouro = json.optString("logradouro", ""),
                    bairro = json.optString("bairro", ""),
                    cidade = json.optString("localidade", ""),
                    uf = json.optString("uf", "")
                )
            }
        }
    }
}
