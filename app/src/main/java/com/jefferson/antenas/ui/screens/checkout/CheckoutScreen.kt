package com.jefferson.antenas.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jefferson.antenas.BuildConfig
import com.jefferson.antenas.ui.theme.*
import com.jefferson.antenas.utils.toCurrency
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBackClick: () -> Unit,
    onOrderSuccess: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    // ERRO CORRIGIDO: Um Flow regular (não StateFlow) precisa de um valor inicial.
    val cartTotal by viewModel.cartTotal.collectAsState(initial = 0.0)
    val context = LocalContext.current

    // --- CONFIGURAÇÃO DO STRIPE (Usando BuildConfig) ---
    LaunchedEffect(Unit) {
        // ✅ SEGURO: Chave vem do BuildConfig, não do código-fonte
        PaymentConfiguration.init(
            context,
            BuildConfig.STRIPE_PUBLIC_KEY
        )
    }

    val paymentSheet = rememberPaymentSheet { result ->
        when (result) {
            is PaymentSheetResult.Completed -> {
                viewModel.onPaymentSuccess()
                onOrderSuccess()
            }
            is PaymentSheetResult.Canceled -> {
                viewModel.onPaymentResultHandled()
            }
            is PaymentSheetResult.Failed -> {
                viewModel.onPaymentResultHandled()
            }
        }
    }

    LaunchedEffect(uiState.paymentInfo) {
        uiState.paymentInfo?.let { info ->
            paymentSheet.presentWithPaymentIntent(
                info.paymentIntent,
                PaymentSheet.Configuration(
                    merchantDisplayName = "Jefferson Antenas",
                    customer = PaymentSheet.CustomerConfiguration(
                        id = info.customer,
                        ephemeralKeySecret = info.ephemeralKey
                    )
                )
            )
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Finalizar Pedido", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MidnightBlueStart)
            )
        },
        containerColor = MidnightBlueStart
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Seus Dados de Entrega", style = MaterialTheme.typography.titleMedium, color = SignalOrange)

                CheckoutTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = "Nome Completo",
                    icon = Icons.Default.Person
                )

                CheckoutTextField(
                    value = uiState.phoneNumber,
                    onValueChange = viewModel::onPhoneChange,
                    label = "Telefone / WhatsApp",
                    icon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone
                )

                CheckoutTextField(
                    value = uiState.cep,
                    onValueChange = viewModel::onCepChange,
                    label = "CEP",
                    icon = Icons.Default.Place,
                    keyboardType = KeyboardType.Number
                )

                CheckoutTextField(
                    value = uiState.address,
                    onValueChange = viewModel::onAddressChange,
                    label = "Endereço (Rua e Número)",
                    icon = Icons.Default.Place
                )

                CheckoutTextField(
                    value = uiState.neighborhood,
                    onValueChange = viewModel::onNeighborhoodChange,
                    label = "Bairro",
                    icon = Icons.Default.Place
                )

                CheckoutTextField(
                    value = uiState.city,
                    onValueChange = viewModel::onCityChange,
                    label = "Cidade e Estado",
                    icon = Icons.Default.Place
                )

                HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total a Pagar:", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    Text(
                        cartTotal.toCurrency(),
                        style = MaterialTheme.typography.titleLarge,
                        color = SignalOrange,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.preparePayment() },
                    enabled = !uiState.isLoading && uiState.name.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = SignalOrange),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = Shapes.medium
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = MidnightBlueStart, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Pagar com Cartão", color = MidnightBlueStart, fontWeight = FontWeight.Bold)
                    }
                }

                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = ErrorRed,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CheckoutTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = SignalOrange) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MidnightBlueCard,
            unfocusedContainerColor = MidnightBlueCard,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = SignalOrange,
            unfocusedBorderColor = TextSecondary,
            focusedLabelColor = SignalOrange,
            unfocusedLabelColor = TextSecondary
        ),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}