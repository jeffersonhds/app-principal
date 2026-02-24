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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutUiState(
    val name: String = "",
    val cep: String = "",
    val address: String = "",
    val neighborhood: String = "",
    val city: String = "",
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val paymentInfo: CheckoutResponse? = null,
    val error: String? = null,
    val isPaymentSuccessful: Boolean = false
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val api: JeffersonApi,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val customerInfo = CustomerInfoDto(
                    name = uiState.value.name,
                    address = uiState.value.address,
                    city = uiState.value.city,
                    phoneNumber = uiState.value.phoneNumber
                )

                val cartItems: List<CartItem> = cartRepository.items.first()
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

    fun onPaymentSuccess() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e("CheckoutViewModel", "Usuário não logado, não é possível dar pontos.")
                cartRepository.clearCart()
                _uiState.update { it.copy(isPaymentSuccessful = true, paymentInfo = null) }
                return@launch
            }

            val totalAmount = cartTotal.value
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