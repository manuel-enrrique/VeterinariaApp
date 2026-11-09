package com.example.veterinaria.data.mapper

import com.example.veterinaria.data.dto.MascotaDto
import com.example.veterinaria.model.Mascota

/** Convierte un [MascotaDto] de la capa de red en una entidad de dominio. */
fun MascotaDto.toDomain(): Mascota = Mascota(
    id = id,
    nombre = nombre,
    especie = especie,
    pesoKg = pesoKg,
    fechaNacimiento = fechaNacimiento,
    vacunado = vacunado
)

/** Convierte una entidad de dominio en su representación de red. */
fun Mascota.toDto(): MascotaDto = MascotaDto(
    id = id,
    nombre = nombre,
    especie = especie,
    pesoKg = pesoKg,
    fechaNacimiento = fechaNacimiento,
    vacunado = vacunado
)

/** Convierte una colección de DTOs en entidades de dominio. */
fun List<MascotaDto>.toDomainList(): List<Mascota> = map(MascotaDto::toDomain)
