package com.jefferson.antenas.ui.screens.services

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import javax.inject.Inject
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val STORE_LAT = -13.5327
private const val STORE_LON = -58.8189

data class TravelCostUiState(
    val isCalculating: Boolean = false,
    val resultKm: Double? = null,
    val errorMsg: String? = null
)

@HiltViewModel
class TravelCostViewModel @Inject constructor(
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(TravelCostUiState())
    val uiState: StateFlow<TravelCostUiState> = _uiState.asStateFlow()

    fun travelCost(km: Double): Double = if (km <= 5.0) 0.0 else km * 2 * 2.5

    fun calcDistance(context: Context, addressInput: String) {
        if (addressInput.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCalculating = true, errorMsg = null, resultKm = null) }
            try {
                val km = withContext(Dispatchers.IO) {
                    // Passo 1: Geocodificar o endereço → lat/lon
                    if (!Geocoder.isPresent()) throw Exception("Geocoder indisponível")
                    @Suppress("DEPRECATION")
                    val locations = Geocoder(context, Locale("pt", "BR"))
                        .getFromLocationName(addressInput, 1)
                    if (locations.isNullOrEmpty()) throw Exception("Endereço não encontrado")
                    val destLat = locations[0].latitude
                    val destLon = locations[0].longitude

                    // Passo 2: OSRM — distância real de rota por estrada
                    try {
                        val url = "https://router.project-osrm.org/route/v1/driving/" +
                            "$STORE_LON,$STORE_LAT;$destLon,$destLat?overview=false"
                        val body = okHttpClient.newCall(Request.Builder().url(url).build())
                            .execute().use { resp ->
                                if (!resp.isSuccessful) throw Exception("Serviço de rota indisponível")
                                resp.body?.string() ?: throw Exception("Resposta vazia")
                            }
                        val json = JSONObject(body)
                        if (json.optString("code") != "Ok") throw Exception("Rota não encontrada pelo OSRM")
                        json.getJSONArray("routes").getJSONObject(0).getDouble("distance") / 1000.0
                    } catch (e: Exception) {
                        // Fallback: Haversine × 1.3 (correção de estrada)
                        Log.w("TravelCostViewModel", "OSRM falhou, usando Haversine como fallback: ${e.message}")
                        haversineKm(STORE_LAT, STORE_LON, destLat, destLon) * 1.3
                    }
                }
                _uiState.update { it.copy(isCalculating = false, resultKm = km) }
            } catch (e: Exception) {
                Log.e("TravelCostViewModel", "Erro ao calcular distância", e)
                _uiState.update {
                    it.copy(
                        isCalculating = false,
                        errorMsg = "Endereço não encontrado. Informe cidade e estado " +
                            "(ex: Campos de Júlio, MT) ou use o botão \"Ver Rota\"."
                    )
                }
            }
        }
    }

    fun reset() = _uiState.update { TravelCostUiState() }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return r * 2 * asin(sqrt(a))
    }
}
