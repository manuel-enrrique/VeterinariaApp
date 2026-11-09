package com.example.veterinaria.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.veterinaria.data.repository.MascotaRepository
import com.example.veterinaria.model.Mascota
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Estado del formulario de alta/edición de mascotas. */
data class MascotaFormUiState(
    val mascotaId: Long? = null,
    val nombre: String = "",
    val especie: String = "",
    val pesoKg: String = "",
    val fechaNacimiento: String = "",
    val vacunado: Boolean = false,
    val nombreError: String? = null,
    val especieError: String? = null,
    val pesoError: String? = null,
    val fechaError: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val loadError: String? = null
) {
    val isEditMode: Boolean get() = mascotaId != null
}

class MascotaFormViewModel(
    private val mascotaId: Long?,
    private val mascotaRepository: MascotaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MascotaFormUiState(
            mascotaId = mascotaId,
            isLoading = mascotaId != null
        )
    )
    val uiState: StateFlow<MascotaFormUiState> = _uiState.asStateFlow()

    private val _events = Channel<String>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        if (mascotaId != null) {
            cargarMascota()
        }
    }

    fun cargarMascota() {
        val id = mascotaId ?: return
        _uiState.update { it.copy(isLoading = true, loadError = null) }

        viewModelScope.launch {
            mascotaRepository.getMascota(id)
                .onSuccess { mascota ->
                    _uiState.value = MascotaFormUiState(
                        mascotaId = mascota.id,
                        nombre = mascota.nombre,
                        especie = mascota.especie,
                        pesoKg = mascota.pesoKg.toString(),
                        fechaNacimiento = mascota.fechaNacimiento,
                        vacunado = mascota.vacunado
                    )
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadError = error.message
                                ?: "No se pudo cargar la mascota."
                        )
                    }
                }
        }
    }

    fun onNombreChange(value: String) {
        _uiState.update { it.copy(nombre = value, nombreError = null) }
    }

    fun onEspecieChange(value: String) {
        _uiState.update { it.copy(especie = value, especieError = null) }
    }

    fun onPesoChange(value: String) {
        val normalized = value.replace(',', '.')
        if (normalized.isEmpty() || PESO_REGEX.matches(normalized)) {
            _uiState.update { it.copy(pesoKg = normalized, pesoError = null) }
        }
    }

    fun onFechaChange(value: String) {
        _uiState.update { it.copy(fechaNacimiento = value, fechaError = null) }
    }

    fun onVacunadoChange(value: Boolean) {
        _uiState.update { it.copy(vacunado = value) }
    }

    fun guardar() {
        val state = _uiState.value
        if (state.isSaving) return

        val nombreError = if (state.nombre.isBlank()) {
            "El nombre de la mascota es obligatorio."
        } else {
            null
        }
        val especieError = if (state.especie.isBlank()) {
            "La especie es obligatoria."
        } else {
            null
        }
        val pesoError = validatePeso(state.pesoKg)
        val fechaError = validateFecha(state.fechaNacimiento)

        if (nombreError != null || especieError != null ||
            pesoError != null || fechaError != null
        ) {
            _uiState.update {
                it.copy(
                    nombreError = nombreError,
                    especieError = especieError,
                    pesoError = pesoError,
                    fechaError = fechaError
                )
            }
            return
        }

        val peso = checkNotNull(state.pesoKg.toDoubleOrNull())
        val mascota = Mascota(
            id = state.mascotaId ?: 0L,
            nombre = state.nombre.trim(),
            especie = state.especie.trim(),
            pesoKg = peso,
            fechaNacimiento = state.fechaNacimiento,
            vacunado = state.vacunado
        )

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = if (state.mascotaId == null) {
                mascotaRepository.createMascota(mascota)
            } else {
                mascotaRepository.updateMascota(mascota)
            }

            result
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, isSaved = true) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false) }
                    _events.trySend(
                        error.message ?: "No se pudo guardar la mascota."
                    )
                }
        }
    }

    private fun validatePeso(value: String): String? {
        if (value.isBlank()) return "El peso es obligatorio."
        val peso = value.toDoubleOrNull() ?: return "Ingresa un peso válido."
        if (!peso.isFinite() || peso <= 0.0) return "El peso debe ser mayor a 0 kg."
        return null
    }

    private fun validateFecha(value: String): String? {
        if (value.isBlank()) return "La fecha de nacimiento es obligatoria."
        val fecha = runCatching { LocalDate.parse(value) }.getOrNull()
            ?: return "Selecciona una fecha válida."
        if (fecha.isAfter(LocalDate.now())) {
            return "La fecha de nacimiento no puede ser futura."
        }
        return null
    }

    private companion object {
        val PESO_REGEX = Regex("^\\d*\\.?\\d*$")
    }
}
