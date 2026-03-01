package com.jefferson.antenas.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.jefferson.antenas.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    override val currentUserId: String?
        get() = auth.currentUser?.uid

    override val currentUserEmail: String?
        get() = auth.currentUser?.email

    override suspend fun getUser(userId: String): User? {
        val doc = firestore.collection("users").document(userId).get().await()
        return if (doc.exists()) doc.toObject(User::class.java) else null
    }

    override suspend fun createUser(user: User) {
        firestore.collection("users").document(user.uid).set(user).await()
        Log.d("UserRepository", "Usuário criado: ${user.uid}")
    }

    override suspend fun updateUserName(userId: String, newName: String) {
        // Atualiza no Firestore
        firestore.collection("users").document(userId)
            .update("name", newName).await()
        // Atualiza o displayName no Firebase Auth
        auth.currentUser?.updateProfile(
            userProfileChangeRequest { displayName = newName }
        )?.await()
        Log.d("UserRepository", "Nome atualizado para: $newName")
    }

    override suspend fun incrementPoints(userId: String, points: Long) {
        if (points <= 0) return
        firestore.collection("users").document(userId)
            .update("points", FieldValue.increment(points)).await()
        Log.d("UserRepository", "$points pontos creditados para $userId")
    }

    override suspend fun getFavorites(userId: String): Set<String> {
        val doc = firestore.collection("users").document(userId).get().await()
        @Suppress("UNCHECKED_CAST")
        return (doc.get("favorites") as? List<String>)?.toSet() ?: emptySet()
    }

    override suspend fun updateFavorites(userId: String, favorites: List<String>) {
        firestore.collection("users").document(userId)
            .update("favorites", favorites).await()
    }
}
