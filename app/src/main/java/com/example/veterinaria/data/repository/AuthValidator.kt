package com.example.veterinaria.data.repository

import com.example.veterinaria.model.AppException

/** Validaciones compartidas por login y registro. */
internal object AuthValidator {

    private val EMAIL_REGEX =
        Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateEmail(email: String) {
        if (email.isBlank()) {
            throw AppException.Validation("El correo electrónico es obligatorio.")
        }
        if (!EMAIL_REGEX.matches(email.trim())) {
            throw AppException.Validation("Ingresa un correo electrónico válido.")
        }
    }

    fun validatePassword(password: String) {
        if (password.isBlank()) {
            throw AppException.Validation("La contraseña es obligatoria.")
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            throw AppException.Validation(
                "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres."
            )
        }
    }

    fun validateName(nombre: String) {
        if (nombre.isBlank()) {
            throw AppException.Validation("El nombre es obligatorio.")
        }
    }

    private const val MIN_PASSWORD_LENGTH = 6
}
