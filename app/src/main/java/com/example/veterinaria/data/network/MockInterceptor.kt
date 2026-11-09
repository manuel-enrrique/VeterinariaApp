package com.example.veterinaria.data.network

import com.example.veterinaria.data.dto.AuthResponse
import com.example.veterinaria.data.dto.MascotaDto
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong

/**
 * Interceptor de demostración que simula el backend de VeterinariaApp.
 *
 * Se activa únicamente cuando `BuildConfig.USE_MOCK_API` es true, por lo que
 * toda la arquitectura (Retrofit + OkHttp + repositorios) se mantiene real.
 * Los datos viven en memoria y se reinician al cerrar el proceso.
 */
class MockInterceptor(
    private val gson: Gson = Gson()
) : Interceptor {

    private val mascotas = CopyOnWriteArrayList<MascotaDto>()
    private val nextId = AtomicLong(1L)

    init {
        seedMascotas()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath.trimEnd('/')
        val method = request.method

        return when {
            path == "/auth/login" && method == "POST" -> authResponse(request, "Sesión iniciada")
            path == "/auth/register" && method == "POST" -> authResponse(request, "Usuario registrado")
            path == "/mascotas" && method == "GET" -> jsonResponse(
                request = request,
                code = 200,
                body = gson.toJson(mascotas.toList())
            )

            path == "/mascotas" && method == "POST" -> createMascota(request)
            MASCOTA_BY_ID.matches(path) -> handleMascotaById(request, path.substringAfterLast('/').toLong())
            else -> jsonResponse(request, 404, errorJson("Recurso no encontrado"))
        }
    }

    private fun authResponse(request: Request, message: String): Response {
        val body = AuthResponse(
            token = "mock-jwt-${System.currentTimeMillis()}",
            mensaje = message
        )
        return jsonResponse(request, 200, gson.toJson(body))
    }

    private fun createMascota(request: Request): Response {
        val dto = request.parseBody() ?: return invalidBody(request)
        val nueva = dto.copy(id = nextId.getAndIncrement())
        mascotas.add(nueva)
        return jsonResponse(request, 201, gson.toJson(nueva))
    }

    private fun handleMascotaById(request: Request, id: Long): Response =
        when (request.method) {
            "GET" -> getMascota(request, id)
            "PUT" -> updateMascota(request, id)
            "DELETE" -> deleteMascota(request, id)
            else -> jsonResponse(request, 405, errorJson("Método no permitido"))
        }

    private fun getMascota(request: Request, id: Long): Response {
        val mascota = mascotas.firstOrNull { it.id == id }
            ?: return notFound(request)
        return jsonResponse(request, 200, gson.toJson(mascota))
    }

    private fun updateMascota(request: Request, id: Long): Response {
        val index = mascotas.indexOfFirst { it.id == id }
        if (index == -1) return notFound(request)

        val dto = request.parseBody() ?: return invalidBody(request)
        val actualizada = dto.copy(id = id)
        mascotas[index] = actualizada
        return jsonResponse(request, 200, gson.toJson(actualizada))
    }

    private fun deleteMascota(request: Request, id: Long): Response {
        val eliminada = mascotas.removeAll { it.id == id }
        return if (eliminada) {
            jsonResponse(request, 200, """{"mensaje":"Mascota eliminada"}""")
        } else {
            notFound(request)
        }
    }

    private fun notFound(request: Request): Response =
        jsonResponse(request, 404, errorJson("Mascota no encontrada"))

    private fun invalidBody(request: Request): Response =
        jsonResponse(request, 400, errorJson("Cuerpo de la petición inválido"))

    private fun jsonResponse(request: Request, code: Int, body: String): Response =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code in 200..299) "OK" else "Error")
            .body(body.toResponseBody(JSON_MEDIA_TYPE))
            .build()

    private fun errorJson(message: String): String = """{"error":"$message"}"""

    private fun Request.parseBody(): MascotaDto? = body?.let { requestBody ->
        val buffer = Buffer()
        requestBody.writeTo(buffer)
        gson.fromJson(buffer.readUtf8(), MascotaDto::class.java)
    }

    private fun seedMascotas() {
        mascotas.addAll(
            listOf(
                MascotaDto(
                    id = 1L,
                    nombre = "Firulais",
                    especie = "Perro",
                    pesoKg = 12.5,
                    fechaNacimiento = "2020-05-14",
                    vacunado = true
                ),
                MascotaDto(
                    id = 2L,
                    nombre = "Michi",
                    especie = "Gato",
                    pesoKg = 4.2,
                    fechaNacimiento = "2022-08-03",
                    vacunado = false
                ),
                MascotaDto(
                    id = 3L,
                    nombre = "Luna",
                    especie = "Conejo",
                    pesoKg = 1.8,
                    fechaNacimiento = "2023-01-27",
                    vacunado = true
                )
            )
        )
        nextId.set(4L)
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        val MASCOTA_BY_ID = Regex("^/mascotas/\\d+$")
    }
}
