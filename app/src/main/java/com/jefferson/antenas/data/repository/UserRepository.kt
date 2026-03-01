package com.jefferson.antenas.data.repository

import com.jefferson.antenas.data.model.User

interface UserRepository {
    /** UID do usuário autenticado no momento, ou null se não logado */
    val currentUserId: String?

    /** Email do usuário autenticado, ou null */
    val currentUserEmail: String?

    suspend fun getUser(userId: String): User?
    suspend fun createUser(user: User)
    suspend fun updateUserName(userId: String, newName: String)
    suspend fun incrementPoints(userId: String, points: Long)

    // Favoritos — armazenados como lista no documento do usuário
    suspend fun getFavorites(userId: String): Set<String>
    suspend fun updateFavorites(userId: String, favorites: List<String>)
}
