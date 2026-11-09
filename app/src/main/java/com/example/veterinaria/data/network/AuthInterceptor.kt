package com.example.veterinaria.data.network

import com.example.veterinaria.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Añade la cabecera `Authorization: Bearer <token>` cuando existe una sesión
 * activa. Si no hay token, la petición continúa sin dicha cabecera.
 */
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenManager.getToken()

        val request = if (token.isNullOrBlank()) {
            originalRequest
        } else {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }

        return chain.proceed(request)
    }
}
