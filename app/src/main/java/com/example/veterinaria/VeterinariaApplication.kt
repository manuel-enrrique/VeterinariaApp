package com.example.veterinaria

import android.app.Application
import com.example.veterinaria.di.AppModule

/** Punto de arranque del proceso: inicializa el contenedor de dependencias. */
class VeterinariaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppModule.init(this)
    }
}
