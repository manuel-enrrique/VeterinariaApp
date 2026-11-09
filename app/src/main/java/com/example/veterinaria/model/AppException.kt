package com.example.veterinaria.model

/**
 * Errores de negocio que pueden viajar desde la capa de datos hasta la UI.
 * El [message] siempre está listo para mostrarse al usuario en español.
 */
sealed class AppException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    /** Los datos enviados no cumplen las reglas de negocio. */
    class Validation(message: String) : AppException(message)

    /** El token no es válido o la sesión expiró. */
    class Unauthorized(
        message: String = "Tu sesión expiró. Inicia sesión nuevamente."
    ) : AppException(message)

    /** No fue posible comunicarse con el servidor. */
    class Network(
        message: String = "No se pudo conectar con el servidor. Revisa tu conexión."
    ) : AppException(message)

    /** El servidor respondió con un error. */
    class Server(
        message: String,
        cause: Throwable? = null
    ) : AppException(message, cause)

    /** Cualquier fallo inesperado. */
    class Unknown(
        message: String = "Ocurrió un error inesperado. Intenta de nuevo."
    ) : AppException(message)
}
