package com.jefferson.antenas.ui.screens.checkout

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.jefferson.antenas.data.model.CartItem
import com.jefferson.antenas.data.model.CheckoutItemDto
import com.jefferson.antenas.data.model.CheckoutRequest
import com.jefferson.antenas.data.model.CheckoutResponse
import com.jefferson.antenas.data.model.CustomerInfoDto
import com.jefferson.antenas.data.remote.JeffersonApi
import com.jefferson.antenas.data.repository.AddressRepository
import com.jefferson.antenas.data.repository.CartRepository
import com.jefferson.antenas.data.repository.OrderRepository
import com.jefferson.antenas.data.repository.UserRepository
import com.jefferson.antenas.domain.usecase.AwardPointsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val isCepLoading: Boolean = false,
    val paymentInfo: CheckoutResponse? = null,
    val error: String? = null,
    val isPaymentSuccessful: Boolean = false
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val api: JeffersonApi,
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
    private val addressRepository: AddressRepository,
    private val awardPointsUseCase: AwardPointsUseCase
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

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v) }
    fun onCepChange(v: String) = _uiState.update { it.copy(cep = v) }
    fun onAddressChange(v: String) = _uiState.update { it.copy(address = v) }
    fun onNeighborhoodChange(v: String) = _uiState.update { it.copy(neighborhood = v) }
    fun onCityChange(v: String) = _uiState.update { it.copy(city = v) }
    fun onPhoneChange(v: String) = _uiState.update { it.copy(phoneNumber = v) }

    fun searchCep() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCepLoading = true, error = null) }
            try {
                val result = addressRepository.fetchAddress(uiState.value.cep)
                _uiState.update { state ->
                    state.copy(
                        isCepLoading = false,
                        address = result.logradouro.ifBlank { state.address },
                        neighborhood = result.bairro.ifBlank { state.neighborhood },
                        city = if (result.cidade.isNotBlank()) "${result.cidade} — ${result.uf}" else state.city
                    )
                }
            } catch (e: Exception) {
                Log.e("CheckoutViewModel", "Erro ao buscar CEP", e)
                _uiState.update { it.copy(isCepLoading = false, error = e.message ?: "CEP não encontrado.") }
            }
        }
    }

    fun preparePayment() {
        if (userRepository.currentUserId == null) {
            _uiState.update { it.copy(error = "Faça login antes de continuar com o pagamento.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val cartItems: List<CartItem> = cartRepository.items.value
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
                val itemsDto = cartItems.map { CheckoutItemDto(id = it.product.id, quantity = it.quantity) }
                val response = api.createPaymentSheet(CheckoutRequest(items = itemsDto, customerInfo = customerInfo))
                _uiState.update { it.copy(isLoading = false, paymentInfo = response) }
            } catch (e: Exception) {
                Log.e("CheckoutViewModel", "Erro ao iniciar pagamento", e)
                _uiState.update { it.copy(isLoading = false, error = "Erro ao iniciar pagamento: ${e.message}") }
            }
        }
    }

    fun onPaymentSuccess() {
        viewModelScope.launch {
            val userId = userRepository.currentUserId
            val cartItems = cartRepository.items.value
            val totalAmount = cartTotal.value
            val state = uiState.value

            val orderData = hashMapOf(
                "userId" to (userId ?: "anonymous"),
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

            try {
                orderRepository.saveOrder(orderData)
            } catch (e: Exception) {
                Log.e("CheckoutViewModel", "Erro ao salvar pedido", e)
                cartRepository.clearCart()
                _uiState.update {
                    it.copy(
                        isPaymentSuccessful = true,
                        paymentInfo = null,
                        error = "Pagamento aprovado, mas houve um erro ao registrar seu pedido. Anote o comprovante e entre em contato com o suporte."
                    )
                }
                return@launch
            }

            if (userId != null) {
                try {
                    awardPointsUseCase(totalAmount, userId)
                } catch (e: Exception) {
                    Log.e("CheckoutViewModel", "Erro ao creditar pontos", e)
                }
            }

            cartRepository.clearCart()
            _uiState.update { it.copy(isPaymentSuccessful = true, paymentInfo = null) }
        }
    }

    fun onPaymentResultHandled() {
        _uiState.update { it.copy(paymentInfo = null) }
    }
}
