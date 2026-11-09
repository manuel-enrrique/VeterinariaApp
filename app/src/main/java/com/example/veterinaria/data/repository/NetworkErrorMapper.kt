package com.example.veterinaria.data.repository

import com.example.veterinaria.model.AppException
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import retrofit2.HttpException
import java.io.IOException

private val gson = Gson()

private data class ApiErrorDto(
    @SerializedName("error") val error: String? = null,
    @SerializedName("mensaje") val mensaje: String? = null
)

/**
 * Normaliza cualquier excepción de red/HTTP en un [AppException] con un
 * mensaje apto para la interfaz.
 */
internal fun Throwable.toAppException(): AppException = when (this) {
    is AppException -> this
    is HttpException -> {
        val message = serverMessageOrNull() ?: fallbackHttpMessage(code())
        if (code() == 401) {
            AppException.Unauthorized(message)
        } else {
            AppException.Server(message, this)
        }
    }

    is IOException -> AppException.Network()
    else -> AppException.Unknown()
}

private fun HttpException.serverMessageOrNull(): String? = runCatching {
    val body = response()?.errorBody()?.string().orEmpty()
    if (body.isBlank()) return@runCatching null

    val error = gson.fromJson(body, ApiErrorDto::class.java)
    error.error?.takeIf(String::isNotBlank)
        ?: error.mensaje?.takeIf(String::isNotBlank)
}.getOrNull()

private fun fallbackHttpMessage(code: Int): String = when (code) {
    400 -> "La solicitud no es válida."
    401 -> "Correo o contraseña incorrectos."
    403 -> "No tienes permisos para realizar esta acción."
    404 -> "No se encontró el recurso solicitado."
    409 -> "Ya existe un registro con esos datos."
    in 500..599 -> "Error del servidor. Intenta de nuevo más tarde."
    else -> "Error de red ($code)."
}
