package com.example.veterinaria.presentation.common

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/** Formatea el peso con un decimal y la unidad de medida. */
fun formatPeso(pesoKg: Double): String =
    String.format(Locale.getDefault(), "%.1f kg", pesoKg)

/** Convierte una fecha ISO (`yyyy-MM-dd`) al formato visible `dd/MM/yyyy`. */
fun formatFecha(fecha: String): String = runCatching {
    LocalDate.parse(fecha).format(DATE_FORMATTER)
}.getOrDefault(fecha)

/** Calcula la edad aproximada de una mascota a partir de su fecha de nacimiento. */
fun calcularEdad(fechaNacimiento: String): String? = runCatching {
    val nacimiento = LocalDate.parse(fechaNacimiento)
    val periodo = Period.between(nacimiento, LocalDate.now())
    when {
        periodo.years > 0 -> {
            val unidad = if (periodo.years == 1) "año" else "años"
            "${periodo.years} $unidad"
        }

        periodo.months > 0 -> {
            val unidad = if (periodo.months == 1) "mes" else "meses"
            "${periodo.months} $unidad"
        }

        else -> {
            val dias = periodo.days.coerceAtLeast(0)
            val unidad = if (dias == 1) "día" else "días"
            "$dias $unidad"
        }
    }
}.getOrNull()
