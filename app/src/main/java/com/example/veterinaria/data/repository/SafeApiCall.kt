package com.example.veterinaria.data.repository

import com.example.veterinaria.model.AppException
import kotlinx.coroutines.CancellationException

/**
 * Ejecuta una llamada suspendible capturando los errores esperados, pero
 * dejando propagar [CancellationException] para no romper la cancelación
 * estructurada de corrutinas.
 */
internal suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (error: Exception) {
    Result.failure(error.toAppException())
}
