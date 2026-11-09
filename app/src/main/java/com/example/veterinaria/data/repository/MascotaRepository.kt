package com.example.veterinaria.data.repository

import com.example.veterinaria.model.Mascota

/** Contrato del CRUD de mascotas. */
interface MascotaRepository {

    suspend fun getMascotas(): Result<List<Mascota>>

    suspend fun getMascota(id: Long): Result<Mascota>

    suspend fun createMascota(mascota: Mascota): Result<Mascota>

    suspend fun updateMascota(mascota: Mascota): Result<Mascota>

    suspend fun deleteMascota(id: Long): Result<Unit>
}
