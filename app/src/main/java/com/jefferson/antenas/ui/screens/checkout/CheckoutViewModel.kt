package com.jefferson.antenas.ui.screens.checkout

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.jefferson.antenas.data.model.CartItem
import com.jefferson.antenas.data.model.CheckoutItemDto
import com.jefferson.antenas.data.model.CheckoutRequest
import com.jefferson.antenas.data.model.CheckoutResponse
import com.jefferson.antenas.data.model.CustomerInfoDto
import com.jefferson.antenas.data.remote.JeffersonApi
import com.jefferson.antenas.data.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject

data class CheckoutUiState(
    val name: String = "",
    val cep: String = "",
    val address: String = "",
    val neighborhood: String = "",
    val city: String = "",
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val isCepLoading: Boolean = false,
    val paymentInfo: CheckoutResponse? = null,
    val error: String? = null,
    val isPaymentSuccessful: Boolean = false
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val api: JeffersonApi,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    val cartTotal: StateFlow<Double> = cartRepository.items
        .map { items -> items.sumOf { it.total } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    fun onNameChange(newValue: String) = _uiState.update { it.copy(name = newValue) }
    fun onCepChange(newValue: String) = _uiState.update { it.copy(cep = newValue) }
    fun onAddressChange(newValue: String) = _uiState.update { it.copy(address = newValue) }
    fun onNeighborhoodChange(newValue: String) = _uiState.update { it.copy(neighborhood = newValue) }
    fun onCityChange(newValue: String) = _uiState.update { it.copy(city = newValue) }
    fun onPhoneChange(newValue: String) = _uiState.update { it.copy(phoneNumber = newValue) }

    fun preparePayment() {
        if (auth.currentUser == null) {
            _uiState.update { it.copy(error = "Faça login antes de continuar com o pagamento.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val cartItems: List<CartItem> = cartRepository.items.first()
                if (cartItems.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, error = "Seu carrinho está vazio.") }
                    return@launch
                }

                val customerInfo = CustomerInfoDto(
                    name = uiState.value.name,
                    address = uiState.value.address,
                    city = uiState.value.city,
                    phoneNumber = uiState.value.phoneNumber
                )

                val itemsDto: List<CheckoutItemDto> = cartItems.map { item ->
                    CheckoutItemDto(id = item.product.id, quantity = item.quantity)
                }

                val response = api.createPaymentSheet(CheckoutRequest(items = itemsDto, customerInfo = customerInfo))
                _uiState.update { it.copy(isLoading = false, paymentInfo = response) }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false, error = "Erro ao iniciar pagamento: ${e.message}") }
            }
        }
    }

    fun searchCep() {
        val rawCep = uiState.value.cep.filter { it.isDigit() }
        if (rawCep.length != 8) {
            _uiState.update { it.copy(error = "CEP inválido. Digite os 8 números.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isCepLoading = true, error = null) }
            try {
                val (logradouro, bairro, cidadeUf) = withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url("https://viacep.com.br/ws/$rawCep/json/")
                        .build()
                    okHttpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw Exception("CEP não encontrado")
                        val body = response.body?.string() ?: throw Exception("Resposta vazia")
                        val json = JSONObject(body)
                        if (json.optBoolean("erro", false)) throw Exception("CEP não encontrado")
                        Triple(
                            json.optString("logradouro", ""),
                            json.optString("bairro", ""),
                            "${json.optString("localidade", "")} — ${json.optString("uf", "")}"
                        )
                    }
                }
                _uiState.update { state ->
                    state.copy(
                        isCepLoading = false,
                        address = if (logradouro.isNotBlank()) logradouro else state.address,
                        neighborhood = if (bairro.isNotBlank()) bairro else state.neighborhood,
                        city = if (cidadeUf.isNotBlank()) cidadeUf else state.city
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isCepLoading = false, error = "CEP não encontrado. Verifique e tente novamente.") }
            }
        }
    }

    fun onPaymentSuccess() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            val cartItems: List<CartItem> = cartRepository.items.first()
            val totalAmount = cartTotal.value
            val state = uiState.value

            // Salva o pedido na coleção "orders" do Firestore
            val orderData = hashMapOf(
                "userId" to (currentUser?.uid ?: "anonymous"),
                "status" to "paid",
                "total" to totalAmount,
                "createdAt" to FieldValue.serverTimestamp(),
                "items" to cartItems.map { item ->
                    hashMapOf(
                        "productId" to item.product.id,
                        "productName" to item.product.name,
                        "quantity" to item.quantity,
                        "unitPrice" to item.product.getDiscountedPrice(),
                        "total" to item.total
                    )
                },
                "deliveryAddress" to hashMapOf(
                    "name" to state.name,
                    "cep" to state.cep,
                    "address" to state.address,
                    "neighborhood" to state.neighborhood,
                    "city" to state.city,
                    "phone" to state.phoneNumber
                )
            )

            firestore.collection("orders")
                .add(orderData)
                .addOnSuccessListener { docRef ->
                    Log.d("CheckoutViewModel", "Pedido salvo com ID: ${docRef.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("CheckoutViewModel", "Erro ao salvar pedido no Firestore", e)
                    cartRepository.clearCart()
                    _uiState.update { it.copy(isPaymentSuccessful = true, paymentInfo = null) }
                }

            if (currentUser == null) {
                Log.e("CheckoutViewModel", "Usuário não logado, não é possível dar pontos.")
                cartRepository.clearCart()
                _uiState.update { it.copy(isPaymentSuccessful = true, paymentInfo = null) }
                return@launch
            }

            val pointsToAward = (totalAmount / 10).toLong()

            if (pointsToAward <= 0) {
                Log.d("CheckoutViewModel", "Compra de valor $totalAmount não gera pontos.")
                cartRepository.clearCart()
                _uiState.update { it.copy(isPaymentSuccessful = true, paymentInfo = null) }
                return@launch
            }

            val userDocRef = firestore.collection("users").document(currentUser.uid)

            userDocRef.update("points", FieldValue.increment(pointsToAward))
                .addOnSuccessListener {
                    Log.d("CheckoutViewModel", "$pointsToAward pontos adicionados para o usuário ${currentUser.uid}")
                    cartRepository.clearCart()
                    _uiState.update { it.copy(isPaymentSuccessful = true, paymentInfo = null) }
                }
                .addOnFailureListener { e ->
                    Log.e("CheckoutViewModel", "Erro ao adicionar pontos para ${currentUser.uid}", e)
                    cartRepository.clearCart()
                    _uiState.update { s -> s.copy(error = "Pagamento aprovado, mas houve um erro ao creditar seus pontos. Contate o suporte.", isPaymentSuccessful = true, paymentInfo = null) }
                }
        }
    }

    fun onPaymentResultHandled() {
        _uiState.update { it.copy(paymentInfo = null) }
    }
}