package com.example.veterinaria.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.veterinaria.data.repository.MascotaRepository
import com.example.veterinaria.model.Mascota
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Estados del detalle de una mascota. */
sealed interface MascotaDetailUiState {

    data object Loading : MascotaDetailUiState

    data class Success(val mascota: Mascota) : MascotaDetailUiState

    data class Error(val message: String) : MascotaDetailUiState
}

class MascotaDetailViewModel(
    private val mascotaId: Long,
    private val mascotaRepository: MascotaRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<MascotaDetailUiState>(MascotaDetailUiState.Loading)
    val uiState: StateFlow<MascotaDetailUiState> = _uiState.asStateFlow()

    fun cargarMascota() {
        _uiState.value = MascotaDetailUiState.Loading
        viewModelScope.launch {
            mascotaRepository.getMascota(mascotaId)
                .onSuccess { mascota ->
                    _uiState.value = MascotaDetailUiState.Success(mascota)
                }
                .onFailure { error ->
                    _uiState.value = MascotaDetailUiState.Error(
                        error.message ?: "No se pudo cargar la mascota."
                    )
                }
        }
    }
}
