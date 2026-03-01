package com.jefferson.antenas.domain.usecase

import com.jefferson.antenas.data.repository.UserRepository
import javax.inject.Inject

/**
 * Calcula e credita pontos de fidelidade ao usuário.
 * Regra: 1 ponto para cada R$ 10,00 gastos.
 */
class AwardPointsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(totalAmount: Double, userId: String) {
        val points = (totalAmount / 10).toLong()
        if (points > 0) {
            userRepository.incrementPoints(userId, points)
        }
    }
}
