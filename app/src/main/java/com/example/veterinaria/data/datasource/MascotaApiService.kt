package com.example.veterinaria.data.datasource

import com.example.veterinaria.data.dto.MascotaDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Endpoints REST para el CRUD de mascotas. */
interface MascotaApiService {

    @GET("mascotas")
    suspend fun getMascotas(): List<MascotaDto>

    @GET("mascotas/{id}")
    suspend fun getMascota(@Path("id") id: Long): MascotaDto

    @POST("mascotas")
    suspend fun createMascota(@Body mascota: MascotaDto): MascotaDto

    @PUT("mascotas/{id}")
    suspend fun updateMascota(
        @Path("id") id: Long,
        @Body mascota: MascotaDto
    ): MascotaDto

    @DELETE("mascotas/{id}")
    suspend fun deleteMascota(@Path("id") id: Long): Response<Unit>
}
