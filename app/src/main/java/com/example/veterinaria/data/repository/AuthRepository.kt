package com.example.veterinaria.data.repository

/** Contrato de autenticación usado por la capa de presentación. */
interface AuthRepository {

    fun isLoggedIn(): Boolean

    suspend fun login(email: String, password: String): Result<Unit>

    suspend fun register(nombre: String, email: String, password: String): Result<Unit>

    fun logout()
}
