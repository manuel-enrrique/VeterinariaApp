package com.example.veterinaria.data.repository

import com.example.veterinaria.model.AppException
import com.example.veterinaria.model.Mascota
import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Reglas de negocio obligatorias para una mascota. */
internal object MascotaValidator {

    fun validate(mascota: Mascota) {
        if (mascota.nombre.isBlank()) {
            throw AppException.Validation("El nombre de la mascota es obligatorio.")
        }
        if (mascota.especie.isBlank()) {
            throw AppException.Validation("La especie de la mascota es obligatoria.")
        }
        if (!mascota.pesoKg.isFinite() || mascota.pesoKg <= 0.0) {
            throw AppException.Validation("El peso debe ser mayor a 0 kg.")
        }
        validateFechaNacimiento(mascota.fechaNacimiento)
    }

    private fun validateFechaNacimiento(fecha: String) {
        if (fecha.isBlank()) {
            throw AppException.Validation("La fecha de nacimiento es obligatoria.")
        }
        try {
            val fechaNacimiento = LocalDate.parse(fecha)
            if (fechaNacimiento.isAfter(LocalDate.now())) {
                throw AppException.Validation(
                    "La fecha de nacimiento no puede ser futura."
                )
            }
        } catch (_: DateTimeParseException) {
            throw AppException.Validation(
                "La fecha de nacimiento debe tener el formato yyyy-MM-dd."
            )
        }
    }
}
