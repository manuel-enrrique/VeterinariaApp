package com.example.veterinaria.data.dto

import com.google.gson.annotations.SerializedName

/** Cuerpo de la petición de inicio de sesión. */
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

/** Cuerpo de la petición de registro de un nuevo usuario. */
data class RegisterRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

/** Respuesta devuelta por los endpoints de autenticación. */
data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("mensaje") val mensaje: String? = null
)
