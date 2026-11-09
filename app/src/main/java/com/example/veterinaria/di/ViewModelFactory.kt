package com.example.veterinaria.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.veterinaria.presentation.auth.AuthViewModel
import com.example.veterinaria.presentation.home.HomeViewModel
import com.example.veterinaria.presentation.home.MascotaDetailViewModel
import com.example.veterinaria.presentation.home.MascotaFormViewModel

/**
 * Fábricas de ViewModels construidas a mano. Evitan frameworks de DI y
 * mantienen explícitas las dependencias de cada pantalla.
 */
object ViewModelFactory {

    val auth: ViewModelProvider.Factory = viewModelFactory {
        initializer { AuthViewModel(AppModule.authRepository) }
    }

    val home: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                mascotaRepository = AppModule.mascotaRepository,
                authRepository = AppModule.authRepository
            )
        }
    }

    fun form(mascotaId: Long?): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            MascotaFormViewModel(
                mascotaId = mascotaId,
                mascotaRepository = AppModule.mascotaRepository
            )
        }
    }

    fun detail(mascotaId: Long): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            MascotaDetailViewModel(
                mascotaId = mascotaId,
                mascotaRepository = AppModule.mascotaRepository
            )
        }
    }
}
