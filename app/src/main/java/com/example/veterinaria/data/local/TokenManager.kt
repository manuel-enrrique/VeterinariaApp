package com.example.veterinaria.data.local

import android.content.Context

/**
 * Abstracción sobre el almacenamiento de la sesión.
 *
 * Permite sustituir SharedPreferences por DataStore sin afectar a las capas
 * superiores y facilita el uso de implementaciones falsas en pruebas.
 */
interface TokenManager {

    fun saveToken(token: String)

    fun getToken(): String?

    fun clearToken()

    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()
}

/** Implementación basada en SharedPreferences para persistir el token JWT. */
class SharedPreferencesTokenManager(context: Context) : TokenManager {

    private val preferences = context.applicationContext
        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun saveToken(token: String) {
        preferences.edit().putString(KEY_TOKEN, token).apply()
    }

    override fun getToken(): String? =
        preferences.getString(KEY_TOKEN, null)?.takeIf(String::isNotBlank)

    override fun clearToken() {
        preferences.edit().remove(KEY_TOKEN).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "veterinaria_prefs"
        const val KEY_TOKEN = "jwt_token"
    }
}
