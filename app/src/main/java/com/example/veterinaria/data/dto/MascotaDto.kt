package com.example.veterinaria.data.dto

import com.google.gson.annotations.SerializedName

/** Representación de red de una mascota. */
data class MascotaDto(
    @SerializedName("id") val id: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("especie") val especie: String,
    @SerializedName("peso_kg") val pesoKg: Double,
    @SerializedName("fecha_nacimiento") val fechaNacimiento: String,
    @SerializedName("vacunado") val vacunado: Boolean
)
