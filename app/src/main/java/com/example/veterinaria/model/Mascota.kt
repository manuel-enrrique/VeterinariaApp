package com.example.veterinaria.model

/**
 * Entidad de dominio que representa una mascota registrada en la veterinaria.
 *
 * [id] vale 0 mientras la mascota aún no ha sido persistida en el backend.
 */
data class Mascota(
    val id: Long = 0L,
    val nombre: String,
    val especie: String,
    val pesoKg: Double,
    val fechaNacimiento: String,
    val vacunado: Boolean = false
)
