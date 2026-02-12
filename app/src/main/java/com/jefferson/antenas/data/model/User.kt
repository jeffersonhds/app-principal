package com.jefferson.antenas.data.model

/**
 * Representa a identidade permanente de um cliente no aplicativo.
 *
 * @property uid O ID único fornecido pelo Firebase Authentication. É a chave primária.
 * @property name O nome de exibição do cliente.
 * @property email O email usado para login e comunicação.
 * @property points O saldo de pontos de fidelidade do cliente.
 */
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val points: Int = 0
)
