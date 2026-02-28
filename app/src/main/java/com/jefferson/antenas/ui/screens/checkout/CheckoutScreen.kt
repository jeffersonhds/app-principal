package com.jefferson.antenas.ui.screens.checkout

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jefferson.antenas.BuildConfig
import com.jefferson.antenas.ui.theme.*
import com.jefferson.antenas.utils.toCurrency
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import java.net.URLEncoder

// ══════════════════════════════════════════════════════════════════════════════
// MODELOS LOCAIS
// ══════════════════════════════════════════════════════════════════════════════

private enum class PaymentMethod(val label: String, val icon: ImageVector, val detail: String) {
    CARD("Cartão de Crédito", Icons.Default.CreditCard, "Até 12x sem juros"),
    PIX("PIX", Icons.Default.QrCode, "5% de desconto à vista"),
    WHATSAPP("WhatsApp", Icons.Default.Message, "Combinar pagamento direto")
}

private enum class DeliveryOption(val label: String, val subtitle: String, val days: String) {
    STANDARD("Entrega Padrão", "Econômico e seguro", "5 a 10 dias úteis"),
    EXPRESS("Entrega Expressa", "Receba mais rápido", "2 a 4 dias úteis")
}

// ══════════════════════════════════════════════════════════════════════════════
// TELA PRINCIPAL
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBackClick: () -> Unit,
    onOrderSuccess: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cartTotal by viewModel.cartTotal.collectAsState(initial = 0.0)
    val context = LocalContext.current

    var selectedPayment by remember { mutableStateOf(PaymentMethod.CARD) }
    var selectedDelivery by remember { mutableStateOf(DeliveryOption.STANDARD) }

    val deliveryCost = when {
        selectedDelivery == DeliveryOption.EXPRESS -> 25.0
        cartTotal >= 100.0 -> 0.0
        else -> 15.0
    }
    val pixDiscount = if (selectedPayment == PaymentMethod.PIX) cartTotal * 0.05 else 0.0
    val finalTotal = cartTotal - pixDiscount + deliveryCost
    val installment = finalTotal / 12.0

    // ── Stripe setup ──────────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        PaymentConfiguration.init(context, BuildConfig.STRIPE_PUBLIC_KEY)
    }

    val paymentSheet = rememberPaymentSheet { result ->
        when (result) {
            is PaymentSheetResult.Completed -> {
                viewModel.onPaymentSuccess()
                onOrderSuccess()
            }
            is PaymentSheetResult.Canceled -> viewModel.onPaymentResultHandled()
            is PaymentSheetResult.Failed -> viewModel.onPaymentResultHandled()
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

    val isFormValid = uiState.name.isNotBlank() &&
            uiState.phoneNumber.isNotBlank() &&
            uiState.cep.isNotBlank() &&
            uiState.address.isNotBlank() &&
            uiState.city.isNotBlank()

    Scaffold(
        containerColor = MidnightBlueStart,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Finalizar Pedido",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MidnightBlueStart
                )
            )
        },
        bottomBar = {
            CheckoutBottomBar(
                total = finalTotal,
                pixDiscount = pixDiscount,
                installment = installment,
                selectedPayment = selectedPayment,
                isLoading = uiState.isLoading,
                isEnabled = isFormValid && !uiState.isLoading,
                onConfirm = {
                    when (selectedPayment) {
                        PaymentMethod.CARD -> viewModel.preparePayment()
                        PaymentMethod.PIX, PaymentMethod.WHATSAPP -> {
                            val phone = "5565992895296"
                            val method = if (selectedPayment == PaymentMethod.PIX) "PIX" else "WhatsApp"
                            val msg = "Olá Jefferson! Quero finalizar um pedido.\n" +
                                    "Nome: ${uiState.name}\n" +
                                    "Endereço: ${uiState.address}, ${uiState.neighborhood}\n" +
                                    "Cidade: ${uiState.city}\n" +
                                    "Telefone: ${uiState.phoneNumber}\n" +
                                    "Total: ${finalTotal.toCurrency()}\n" +
                                    "Forma de pagamento: $method"
                            try {
                                val url = "https://api.whatsapp.com/send?phone=$phone&text=${
                                    URLEncoder.encode(msg, "UTF-8")
                                }"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            } catch (_: Exception) {
                                Toast.makeText(context, "WhatsApp não encontrado. Instale o app e tente novamente.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(brush = BackgroundGradient)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Indicador de etapas ──────────────────────────────────────────
            CheckoutStepIndicator(currentStep = 1)

            Spacer(Modifier.height(8.dp))

            // ════════════════════════════════════════════════════════════════
            // SEÇÃO 1 — DADOS PESSOAIS
            // ════════════════════════════════════════════════════════════════
            CheckoutSection(
                icon = Icons.Default.Person,
                title = "Dados Pessoais",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                CheckoutField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = "Nome Completo *",
                    icon = Icons.Default.Person,
                    placeholder = "Ex: João da Silva"
                )
                Spacer(Modifier.height(10.dp))
                CheckoutField(
                    value = uiState.phoneNumber,
                    onValueChange = viewModel::onPhoneChange,
                    label = "WhatsApp / Telefone",
                    icon = Icons.Default.Phone,
                    placeholder = "(65) 9 9999-9999",
                    keyboardType = KeyboardType.Phone
                )
            }

            // ════════════════════════════════════════════════════════════════
            // SEÇÃO 2 — ENDEREÇO DE ENTREGA
            // ════════════════════════════════════════════════════════════════
            CheckoutSection(
                icon = Icons.Default.LocationOn,
                title = "Endereço de Entrega",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                CheckoutField(
                    value = uiState.cep,
                    onValueChange = viewModel::onCepChange,
                    label = "CEP",
                    icon = Icons.Default.Map,
                    placeholder = "00000-000",
                    keyboardType = KeyboardType.Number,
                    trailingContent = {
                        Surface(
                            color = SatelliteBlue.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable(enabled = !uiState.isCepLoading) { viewModel.searchCep() }
                        ) {
                            if (uiState.isCepLoading) {
                                CircularProgressIndicator(
                                    color = SatelliteBlue,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .size(14.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Buscar",
                                    fontSize = 11.sp,
                                    color = SatelliteBlue,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                )
                Spacer(Modifier.height(10.dp))
                CheckoutField(
                    value = uiState.address,
                    onValueChange = viewModel::onAddressChange,
                    label = "Rua e Número *",
                    icon = Icons.Default.Home,
                    placeholder = "Rua das Antenas, 123"
                )
                Spacer(Modifier.height(10.dp))
                CheckoutField(
                    value = uiState.neighborhood,
                    onValueChange = viewModel::onNeighborhoodChange,
                    label = "Bairro",
                    icon = Icons.Default.Place,
                    placeholder = "Centro"
                )
                Spacer(Modifier.height(10.dp))
                CheckoutField(
                    value = uiState.city,
                    onValueChange = viewModel::onCityChange,
                    label = "Cidade e Estado *",
                    icon = Icons.Default.LocationCity,
                    placeholder = "Cuiabá — MT"
                )
            }

            // ════════════════════════════════════════════════════════════════
            // SEÇÃO 3 — FORMA DE ENVIO
            // ════════════════════════════════════════════════════════════════
            CheckoutSection(
                icon = Icons.Default.LocalShipping,
                title = "Forma de Envio",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                DeliveryOption.entries.forEach { option ->
                    val isSelected = selectedDelivery == option
                    val price = when {
                        option == DeliveryOption.EXPRESS -> "R$ 25,00"
                        cartTotal >= 100.0 -> "Grátis 🎉"
                        else -> "R$ 15,00"
                    }
                    DeliveryOptionCard(
                        option = option,
                        price = price,
                        isSelected = isSelected,
                        onClick = { selectedDelivery = option }
                    )
                    if (option != DeliveryOption.entries.last()) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // ════════════════════════════════════════════════════════════════
            // SEÇÃO 4 — FORMA DE PAGAMENTO
            // ════════════════════════════════════════════════════════════════
            CheckoutSection(
                icon = Icons.Default.Payment,
                title = "Forma de Pagamento",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                PaymentMethod.entries.forEach { method ->
                    val isSelected = selectedPayment == method
                    PaymentMethodCard(
                        method = method,
                        isSelected = isSelected,
                        cartTotal = cartTotal,
                        installment = cartTotal / 12.0,
                        onClick = { selectedPayment = method }
                    )
                    if (method != PaymentMethod.entries.last()) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // ════════════════════════════════════════════════════════════════
            // SEÇÃO 5 — RESUMO DO PEDIDO
            // ════════════════════════════════════════════════════════════════
            CheckoutSection(
                icon = Icons.Default.Receipt,
                title = "Resumo do Pedido",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OrderSummaryRows(
                    subtotal = cartTotal,
                    deliveryCost = deliveryCost,
                    pixDiscount = pixDiscount,
                    finalTotal = finalTotal,
                    selectedPayment = selectedPayment,
                    selectedDelivery = selectedDelivery
                )
            }

            // ════════════════════════════════════════════════════════════════
            // SEÇÃO 6 — SELOS DE SEGURANÇA
            // ════════════════════════════════════════════════════════════════
            SecurityBadgesSection(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Erro
            if (uiState.error != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    color = ErrorRed.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, ErrorRed.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Error, null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                        Text(uiState.error ?: "", color = ErrorRed, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(140.dp))
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// INDICADOR DE ETAPAS
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun CheckoutStepIndicator(currentStep: Int) {
    val steps = listOf("Entrega", "Pagamento", "Confirmação")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val stepNum = index + 1
            val isDone = stepNum < currentStep
            val isCurrent = stepNum == currentStep

            // Círculo do step
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isDone -> SuccessGreen
                            isCurrent -> SignalOrange
                            else -> CardGradientStart
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                } else {
                    Text(
                        stepNum.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCurrent) MidnightBlueStart else TextTertiary
                    )
                }
            }

            Spacer(Modifier.width(4.dp))

            // Label
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isDone -> SuccessGreen
                    isCurrent -> SignalOrange
                    else -> TextTertiary
                }
            )

            // Linha conectora
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.5.dp)
                        .padding(horizontal = 4.dp)
                        .background(
                            if (isDone) SuccessGreen else CardBorder
                        )
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// CONTAINER DE SEÇÃO
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun CheckoutSection(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MidnightBlueCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header da seção
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                Surface(
                    color = SignalOrange.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        icon, null,
                        tint = SignalOrange,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(7.dp)
                    )
                }
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            HorizontalDivider(color = CardBorder, thickness = 0.5.dp, modifier = Modifier.padding(bottom = 14.dp))

            content()
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// CAMPO DE FORMULÁRIO ESTILIZADO
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun CheckoutField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingContent: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        placeholder = { Text(placeholder, color = TextTertiary, fontSize = 13.sp) },
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = SignalOrange, modifier = Modifier.size(20.dp))
        },
        trailingIcon = trailingContent?.let { { it() } },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MidnightBlueStart,
            unfocusedContainerColor = MidnightBlueStart,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = SignalOrange,
            unfocusedBorderColor = CardBorder,
            focusedLabelColor = SignalOrange,
            unfocusedLabelColor = TextTertiary,
            cursorColor = SignalOrange
        ),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}

// Mantido para compatibilidade com outros arquivos que importam esse composable
@Composable
fun CheckoutTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    CheckoutField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        icon = icon,
        keyboardType = keyboardType
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// CARD DE OPÇÃO DE ENTREGA
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun DeliveryOptionCard(
    option: DeliveryOption,
    price: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) SignalOrange else CardBorder,
        animationSpec = tween(200),
        label = "border"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) SignalOrange.copy(alpha = 0.06f) else MidnightBlueStart,
        animationSpec = tween(200),
        label = "bg"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 0.5.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Radio button visual
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (isSelected) 0.dp else 1.5.dp,
                        color = if (isSelected) Color.Transparent else CardBorder,
                        shape = CircleShape
                    )
                    .background(if (isSelected) SignalOrange else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MidnightBlueStart)
                    )
                }
            }

            // Ícone
            Icon(
                Icons.Default.LocalShipping, null,
                tint = if (isSelected) SignalOrange else TextTertiary,
                modifier = Modifier.size(22.dp)
            )

            // Texto
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    option.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) TextPrimary else TextSecondary
                )
                Text(
                    option.days,
                    fontSize = 11.sp,
                    color = if (isSelected) SignalOrange else TextTertiary
                )
            }

            // Preço
            Text(
                price,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (price.contains("Grátis")) SuccessGreen else TextPrimary
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// CARD DE FORMA DE PAGAMENTO
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PaymentMethodCard(
    method: PaymentMethod,
    isSelected: Boolean,
    cartTotal: Double,
    installment: Double,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) SignalOrange else CardBorder,
        animationSpec = tween(200),
        label = "border"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) SignalOrange.copy(alpha = 0.06f) else MidnightBlueStart,
        animationSpec = tween(200),
        label = "bg"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 0.5.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Radio button visual
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (isSelected) 0.dp else 1.5.dp,
                        color = if (isSelected) Color.Transparent else CardBorder,
                        shape = CircleShape
                    )
                    .background(if (isSelected) SignalOrange else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MidnightBlueStart)
                    )
                }
            }

            // Ícone do método
            Surface(
                color = when (method) {
                    PaymentMethod.CARD -> SatelliteBlue.copy(alpha = 0.15f)
                    PaymentMethod.PIX -> SuccessGreen.copy(alpha = 0.15f)
                    PaymentMethod.WHATSAPP -> SuccessGreen.copy(alpha = 0.15f)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    method.icon, null,
                    tint = when (method) {
                        PaymentMethod.CARD -> SatelliteBlue
                        PaymentMethod.PIX -> SuccessGreen
                        PaymentMethod.WHATSAPP -> SuccessGreen
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .padding(8.dp)
                )
            }

            // Info do método
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    method.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) TextPrimary else TextSecondary
                )
                Text(
                    method.detail,
                    fontSize = 11.sp,
                    color = when {
                        !isSelected -> TextTertiary
                        method == PaymentMethod.PIX || method == PaymentMethod.CARD -> SuccessGreen
                        else -> TextSecondary
                    }
                )
            }

            // Detalhes adicionais (à direita)
            if (isSelected) {
                Column(horizontalAlignment = Alignment.End) {
                    when (method) {
                        PaymentMethod.CARD -> {
                            Text("12x", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SignalOrange)
                            Text(
                                installment.toCurrency(),
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        PaymentMethod.PIX -> {
                            Text("-5%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            Text("desconto", fontSize = 10.sp, color = TextTertiary)
                        }
                        PaymentMethod.WHATSAPP -> {
                            Text("Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            Text("combinado", fontSize = 10.sp, color = TextTertiary)
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// RESUMO DO PEDIDO
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun OrderSummaryRows(
    subtotal: Double,
    deliveryCost: Double,
    pixDiscount: Double,
    finalTotal: Double,
    selectedPayment: PaymentMethod,
    selectedDelivery: DeliveryOption
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SummaryLine("Subtotal dos produtos", subtotal.toCurrency(), TextSecondary, TextSecondary)

        if (pixDiscount > 0) {
            SummaryLine(
                "Desconto PIX (5%)",
                "-${pixDiscount.toCurrency()}",
                SuccessGreen,
                SuccessGreen
            )
        }

        SummaryLine(
            label = if (selectedDelivery == DeliveryOption.EXPRESS) "Entrega Expressa" else "Frete Padrão",
            value = if (deliveryCost == 0.0) "Grátis 🎉" else deliveryCost.toCurrency(),
            labelColor = TextSecondary,
            valueColor = if (deliveryCost == 0.0) SuccessGreen else TextSecondary
        )

        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text("Total", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(
                finalTotal.toCurrency(),
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SignalOrange
            )
        }

        // Parcelamento (só para cartão)
        if (selectedPayment == PaymentMethod.CARD) {
            Surface(
                color = SatelliteBlue.copy(alpha = 0.10f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CreditCard, null, tint = SatelliteBlue, modifier = Modifier.size(16.dp))
                    Text(
                        "12x de ${(finalTotal / 12.0).toCurrency()} sem juros no cartão",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Desconto PIX destacado
        if (selectedPayment == PaymentMethod.PIX) {
            Surface(
                color = SuccessGreen.copy(alpha = 0.10f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.QrCode, null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                    Text(
                        "Você economiza ${pixDiscount.toCurrency()} pagando com PIX",
                        fontSize = 12.sp,
                        color = SuccessGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String, labelColor: Color, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = labelColor)
        Text(value, fontSize = 13.sp, color = valueColor, fontWeight = FontWeight.SemiBold)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SELOS DE SEGURANÇA
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SecurityBadgesSection(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = CardGradientStart,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Lock, null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                Text(
                    "Pagamento 100% Seguro e Criptografado",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SuccessGreen
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SecurityBadge("🔒", "SSL 256-bit")
                SecurityBadge("✅", "Dados Seguros")
                SecurityBadge("↩", "Devolução\n7 dias")
                SecurityBadge("🛡️", "Compra\nProtegida")
            }

            Spacer(Modifier.height(10.dp))

            Text(
                "Ao confirmar o pedido você concorda com os Termos de Uso e Política de Privacidade da Jefferson Antenas.",
                fontSize = 10.sp,
                color = TextTertiary,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun SecurityBadge(icon: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(icon, fontSize = 18.sp)
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 9.sp, color = TextSecondary, textAlign = TextAlign.Center, lineHeight = 12.sp)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// BARRA INFERIOR DE CONFIRMAÇÃO
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun CheckoutBottomBar(
    total: Double,
    pixDiscount: Double,
    installment: Double,
    selectedPayment: PaymentMethod,
    isLoading: Boolean,
    isEnabled: Boolean,
    onConfirm: () -> Unit
) {
    Surface(
        color = MidnightBlueCard,
        shadowElevation = 24.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            // Linha de totais
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total do pedido", fontSize = 11.sp, color = TextTertiary)
                    Text(
                        total.toCurrency(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SignalOrange
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    when (selectedPayment) {
                        PaymentMethod.CARD -> {
                            Text(
                                "12x ${installment.toCurrency()} s/juros",
                                fontSize = 11.sp,
                                color = SatelliteBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        PaymentMethod.PIX -> {
                            Text(
                                "Economia: ${pixDiscount.toCurrency()}",
                                fontSize = 11.sp,
                                color = SuccessGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        PaymentMethod.WHATSAPP -> {
                            Text(
                                "Via WhatsApp",
                                fontSize = 11.sp,
                                color = SuccessGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Text(
                        when (selectedPayment) {
                            PaymentMethod.CARD -> "no cartão"
                            PaymentMethod.PIX -> "no pix"
                            PaymentMethod.WHATSAPP -> "combinado"
                        },
                        fontSize = 10.sp,
                        color = TextTertiary
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Botão confirmar
            Button(
                onClick = onConfirm,
                enabled = isEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SignalOrange,
                    disabledContainerColor = CardGradientStart
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MidnightBlueStart,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        when (selectedPayment) {
                            PaymentMethod.CARD -> Icons.Default.CreditCard
                            PaymentMethod.PIX -> Icons.Default.QrCode
                            PaymentMethod.WHATSAPP -> Icons.Default.Message
                        },
                        null,
                        tint = if (isEnabled) MidnightBlueStart else TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        when (selectedPayment) {
                            PaymentMethod.CARD -> "Pagar com Cartão"
                            PaymentMethod.PIX -> "Pagar com PIX"
                            PaymentMethod.WHATSAPP -> "Enviar Pedido via WhatsApp"
                        },
                        color = if (isEnabled) MidnightBlueStart else TextTertiary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }

            if (!isEnabled && !isLoading) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Preencha todos os campos obrigatórios para continuar",
                    fontSize = 10.sp,
                    color = TextTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
