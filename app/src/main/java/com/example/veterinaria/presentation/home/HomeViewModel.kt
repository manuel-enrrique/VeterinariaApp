package com.example.veterinaria.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.veterinaria.data.repository.AuthRepository
import com.example.veterinaria.data.repository.MascotaRepository
import com.example.veterinaria.model.Mascota
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** Estados explícitos del listado de mascotas. */
sealed interface HomeUiState {

    data object Loading : HomeUiState

    data class Success(
        val mascotas: List<Mascota>,
        val mascotaAEliminar: Mascota? = null,
        val isDeleting: Boolean = false
    ) : HomeUiState

    data object Empty : HomeUiState

    data class Error(val message: String) : HomeUiState
}

/** Eventos de una sola ejecución (snackbar y navegación). */
sealed interface HomeEvent {

    data object LoggedOut : HomeEvent

    data class Message(val text: String) : HomeEvent
}

class HomeViewModel(
    private val mascotaRepository: MascotaRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun cargarMascotas() {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            mascotaRepository.getMascotas()
                .onSuccess { mascotas ->
                    _uiState.value = if (mascotas.isEmpty()) {
                        HomeUiState.Empty
                    } else {
                        HomeUiState.Success(mascotas = mascotas)
                    }
                }
                .onFailure { error ->
                    _uiState.value = HomeUiState.Error(
                        error.message ?: "No se pudieron cargar las mascotas."
                    )
                }
        }
    }

    fun refrescar() {
        cargarMascotas()
    }

    fun solicitarEliminar(mascota: Mascota) {
        val state = _uiState.value as? HomeUiState.Success ?: return
        _uiState.value = state.copy(mascotaAEliminar = mascota)
    }

    fun cancelarEliminar() {
        val state = _uiState.value as? HomeUiState.Success ?: return
        if (state.isDeleting) return
        _uiState.value = state.copy(mascotaAEliminar = null)
    }

    fun confirmarEliminar() {
        val state = _uiState.value as? HomeUiState.Success ?: return
        val mascota = state.mascotaAEliminar ?: return
        if (state.isDeleting) return

        _uiState.value = state.copy(isDeleting = true)
        viewModelScope.launch {
            mascotaRepository.deleteMascota(mascota.id)
                .onSuccess {
                    val current = _uiState.value as? HomeUiState.Success ?: return@onSuccess
                    val restantes = current.mascotas.filterNot { it.id == mascota.id }
                    _uiState.value = if (restantes.isEmpty()) {
                        HomeUiState.Empty
                    } else {
                        current.copy(
                            mascotas = restantes,
                            mascotaAEliminar = null,
                            isDeleting = false
                        )
                    }
                    _events.trySend(
                        HomeEvent.Message("${mascota.nombre} fue eliminada correctamente.")
                    )
                }
                .onFailure { error ->
                    val current = _uiState.value as? HomeUiState.Success
                    if (current != null) {
                        _uiState.value = current.copy(
                            mascotaAEliminar = null,
                            isDeleting = false
                        )
                    }
                    _events.trySend(
                        HomeEvent.Message(
                            error.message ?: "No se pudo eliminar la mascota."
                        )
                    )
                }
        }
    }

    fun cerrarSesion() {
        authRepository.logout()
        _events.trySend(HomeEvent.LoggedOut)
    }
}
