package com.example.veterinaria.data.repository

import com.example.veterinaria.data.datasource.MascotaApiService
import com.example.veterinaria.data.mapper.toDomain
import com.example.veterinaria.data.mapper.toDomainList
import com.example.veterinaria.data.mapper.toDto
import com.example.veterinaria.model.AppException
import com.example.veterinaria.model.Mascota
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * Implementación del CRUD de mascotas. Aplica las validaciones de negocio
 * obligatorias antes de cualquier llamada al backend.
 */
class MascotaRepositoryImpl(
    private val mascotaApiService: MascotaApiService
) : MascotaRepository {

    override suspend fun getMascotas(): Result<List<Mascota>> =
        withContext(Dispatchers.IO) {
            safeApiCall {
                mascotaApiService.getMascotas().toDomainList()
            }
        }

    override suspend fun getMascota(id: Long): Result<Mascota> =
        withContext(Dispatchers.IO) {
            safeApiCall {
                requireValidId(id)
                mascotaApiService.getMascota(id).toDomain()
            }
        }

    override suspend fun createMascota(mascota: Mascota): Result<Mascota> =
        withContext(Dispatchers.IO) {
            safeApiCall {
                MascotaValidator.validate(mascota)
                mascotaApiService.createMascota(mascota.copy(id = 0L).toDto()).toDomain()
            }
        }

    override suspend fun updateMascota(mascota: Mascota): Result<Mascota> =
        withContext(Dispatchers.IO) {
            safeApiCall {
                requireValidId(mascota.id)
                MascotaValidator.validate(mascota)
                mascotaApiService.updateMascota(mascota.id, mascota.toDto()).toDomain()
            }
        }

    override suspend fun deleteMascota(id: Long): Result<Unit> =
        withContext(Dispatchers.IO) {
            safeApiCall {
                requireValidId(id)
                val response = mascotaApiService.deleteMascota(id)
                if (!response.isSuccessful) {
                    throw HttpException(response)
                }
            }
        }

    private fun requireValidId(id: Long) {
        if (id <= 0L) {
            throw AppException.Validation("La mascota no tiene un identificador válido.")
        }
    }
}
