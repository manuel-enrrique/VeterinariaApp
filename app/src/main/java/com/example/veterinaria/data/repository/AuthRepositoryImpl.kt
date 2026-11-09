package com.example.veterinaria.data.repository

import com.example.veterinaria.data.datasource.AuthApiService
import com.example.veterinaria.data.dto.AuthResponse
import com.example.veterinaria.data.dto.LoginRequest
import com.example.veterinaria.data.dto.RegisterRequest
import com.example.veterinaria.data.local.TokenManager
import com.example.veterinaria.model.AppException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementación de autenticación. Valida los datos antes de viajar a la red
 * y persiste el token JWT en [TokenManager] cuando el backend responde.
 */
class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    override suspend fun login(email: String, password: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            safeApiCall {
                AuthValidator.validateEmail(email)
                AuthValidator.validatePassword(password)

                val response = authApiService.login(
                    LoginRequest(email = email.trim(), password = password)
                )
                persistToken(response)
            }
        }

    override suspend fun register(
        nombre: String,
        email: String,
        password: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        safeApiCall {
            AuthValidator.validateName(nombre)
            AuthValidator.validateEmail(email)
            AuthValidator.validatePassword(password)

            val response = authApiService.register(
                RegisterRequest(
                    nombre = nombre.trim(),
                    email = email.trim(),
                    password = password
                )
            )
            persistToken(response)
        }
    }

    override fun logout() {
        tokenManager.clearToken()
    }

    private fun persistToken(response: AuthResponse) {
        val token = response.token.trim()
        if (token.isEmpty()) {
            throw AppException.Server("El servidor no devolvió un token válido.")
        }
        tokenManager.saveToken(token)
    }
}
