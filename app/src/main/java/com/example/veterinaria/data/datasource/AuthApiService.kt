package com.example.veterinaria.data.datasource

import com.example.veterinaria.data.dto.AuthResponse
import com.example.veterinaria.data.dto.LoginRequest
import com.example.veterinaria.data.dto.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

/** Endpoints de autenticación expuestos por el backend. */
interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse
}
