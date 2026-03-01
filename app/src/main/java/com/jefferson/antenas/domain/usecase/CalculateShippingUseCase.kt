package com.jefferson.antenas.domain.usecase

import javax.inject.Inject

/**
 * Calcula o custo de frete com base na distância em km.
 * Regra: até 5 km = grátis; acima = km × 2 viagens × R$ 2,50/km.
 */
class CalculateShippingUseCase @Inject constructor() {
    operator fun invoke(km: Double): Double =
        if (km <= 5.0) 0.0 else km * 2 * 2.5
}
