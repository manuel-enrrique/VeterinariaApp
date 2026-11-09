package com.example.veterinaria.di

import android.content.Context
import com.example.veterinaria.BuildConfig
import com.example.veterinaria.data.datasource.AuthApiService
import com.example.veterinaria.data.datasource.MascotaApiService
import com.example.veterinaria.data.local.SharedPreferencesTokenManager
import com.example.veterinaria.data.local.TokenManager
import com.example.veterinaria.data.network.AuthInterceptor
import com.example.veterinaria.data.network.MockInterceptor
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Contenedor de dependencias manual.
 *
 * Todas las instancias se crean de forma perezosa (`by lazy`) y se comparten
 * durante toda la vida del proceso. Se inicializa desde [com.example.veterinaria.VeterinariaApplication].
 */
object AppModule {

    private lateinit var appContext: Context

    private val gson: Gson by lazy { Gson() }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    val tokenManager: TokenManager by lazy {
        SharedPreferencesTokenManager(appContext)
    }

    private val authInterceptor: AuthInterceptor by lazy {
        AuthInterceptor(tokenManager)
    }

    private val mockInterceptor: MockInterceptor by lazy {
        MockInterceptor(gson)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .apply {
                if (BuildConfig.USE_MOCK_API) {
                    addInterceptor(mockInterceptor)
                }
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val mascotaApiService: MascotaApiService by lazy {
        retrofit.create(MascotaApiService::class.java)
    }

    fun init(context: Context) {
        if (!::appContext.isInitialized) {
            appContext = context.applicationContext
        }
    }
}
