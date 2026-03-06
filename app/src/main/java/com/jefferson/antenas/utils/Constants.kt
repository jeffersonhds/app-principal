package com.jefferson.antenas.utils

// ── WhatsApp ───────────────────────────────────────────────────────────────────
/** Número WhatsApp do Jefferson Antenas (com código do país). */
const val WHATSAPP_PHONE = "5565992895296"

// ── Frete & Carrinho ───────────────────────────────────────────────────────────
/** Valor mínimo do pedido para frete grátis (R$ 100,00). */
const val FREE_SHIPPING_THRESHOLD = 100.0

/** Custo do frete padrão quando abaixo do mínimo (R$ 15,00). */
const val STANDARD_SHIPPING_COST = 15.0

/** Custo do frete expresso (R$ 25,00). */
const val EXPRESS_SHIPPING_COST = 25.0

// ── Pagamento ──────────────────────────────────────────────────────────────────
/** Desconto concedido para pagamento via PIX (5% = 0.05). */
const val PIX_DISCOUNT = 0.05

// ── Cupons de desconto ─────────────────────────────────────────────────────────
/** Mapa de cupons válidos: código → fração de desconto (0.10 = 10%). */
val VALID_COUPONS: Map<String, Double> = mapOf(
    "JEFF10" to 0.10,
    "ANTENAS15" to 0.15
)

// ── Localização da loja ────────────────────────────────────────────────────────
/** Latitude da loja Jefferson Antenas (Sapezal — MT). */
const val STORE_LAT = -13.5327

/** Longitude da loja Jefferson Antenas (Sapezal — MT). */
const val STORE_LON = -58.8189
