package com.example.veterinaria.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.veterinaria.di.ViewModelFactory
import com.example.veterinaria.ui.theme.VeterinariaAppTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun MascotaFormRoute(
    mascotaId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val factory = remember(mascotaId) { ViewModelFactory.form(mascotaId) }
    val viewModel: MascotaFormViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnSaved by rememberUpdatedState(onSaved)

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) currentOnSaved()
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    MascotaFormScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNombreChange = viewModel::onNombreChange,
        onEspecieChange = viewModel::onEspecieChange,
        onPesoChange = viewModel::onPesoChange,
        onFechaChange = viewModel::onFechaChange,
        onVacunadoChange = viewModel::onVacunadoChange,
        onGuardar = viewModel::guardar,
        onBack = onBack,
        onRetry = viewModel::cargarMascota
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MascotaFormScreen(
    uiState: MascotaFormUiState,
    snackbarHostState: SnackbarHostState,
    onNombreChange: (String) -> Unit,
    onEspecieChange: (String) -> Unit,
    onPesoChange: (String) -> Unit,
    onFechaChange: (String) -> Unit,
    onVacunadoChange: (Boolean) -> Unit,
    onGuardar: () -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isEditMode) "Editar mascota" else "Nueva mascota"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            uiState.loadError != null -> LoadErrorState(
                message = uiState.loadError,
                onRetry = onRetry,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )

            else -> FormContent(
                uiState = uiState,
                onNombreChange = onNombreChange,
                onEspecieChange = onEspecieChange,
                onPesoChange = onPesoChange,
                onFechaClick = { showDatePicker = true },
                onVacunadoChange = onVacunadoChange,
                onGuardar = onGuardar,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    }

    if (showDatePicker) {
        DatePickerModal(
            initialDate = uiState.fechaNacimiento,
            onDateSelected = {
                onFechaChange(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun FormContent(
    uiState: MascotaFormUiState,
    onNombreChange: (String) -> Unit,
    onEspecieChange: (String) -> Unit,
    onPesoChange: (String) -> Unit,
    onFechaClick: () -> Unit,
    onVacunadoChange: (Boolean) -> Unit,
    onGuardar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Datos de la mascota",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
            value = uiState.nombre,
            onValueChange = onNombreChange,
            label = { Text("Nombre") },
            leadingIcon = { Icon(Icons.Filled.Pets, contentDescription = null) },
            isError = uiState.nombreError != null,
            supportingText = { uiState.nombreError?.let { Text(it) } },
            singleLine = true,
            enabled = !uiState.isSaving,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.especie,
            onValueChange = onEspecieChange,
            label = { Text("Especie") },
            placeholder = { Text("Perro, gato, conejo...") },
            isError = uiState.especieError != null,
            supportingText = { uiState.especieError?.let { Text(it) } },
            singleLine = true,
            enabled = !uiState.isSaving,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.pesoKg,
            onValueChange = onPesoChange,
            label = { Text("Peso (kg)") },
            placeholder = { Text("Ej. 12.5") },
            isError = uiState.pesoError != null,
            supportingText = {
                Text(uiState.pesoError ?: "Ingresa el peso en kilogramos.")
            },
            singleLine = true,
            enabled = !uiState.isSaving,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.fechaNacimiento,
            onValueChange = {},
            label = { Text("Fecha de nacimiento") },
            placeholder = { Text("yyyy-MM-dd") },
            readOnly = true,
            isError = uiState.fechaError != null,
            supportingText = {
                Text(
                    uiState.fechaError
                        ?: "Toca el calendario para seleccionar la fecha."
                )
            },
            trailingIcon = {
                IconButton(onClick = onFechaClick, enabled = !uiState.isSaving) {
                    Icon(
                        Icons.Filled.CalendarMonth,
                        contentDescription = "Seleccionar fecha"
                    )
                }
            },
            singleLine = true,
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(4.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Vaccines,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Vacunado",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Marca si tiene sus vacunas al día.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.vacunado,
                    onCheckedChange = onVacunadoChange,
                    enabled = !uiState.isSaving
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onGuardar,
            enabled = !uiState.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (uiState.isEditMode) "Guardar cambios" else "Registrar mascota"
                )
            }
        }
    }
}

@Composable
private fun LoadErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    initialDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialSelectedDateMillis = remember(initialDate) {
        runCatching {
            LocalDate.parse(initialDate)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis <= System.currentTimeMillis()
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val fecha = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                            .toString()
                        onDateSelected(fecha)
                    }
                }
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Preview(showBackground = true)
@Composable
private fun MascotaFormScreenPreview() {
    VeterinariaAppTheme(dynamicColor = false) {
        MascotaFormScreen(
            uiState = MascotaFormUiState(
                nombre = "Firulais",
                especie = "Perro",
                pesoKg = "12.5",
                fechaNacimiento = "2020-05-14",
                vacunado = true
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onNombreChange = {},
            onEspecieChange = {},
            onPesoChange = {},
            onFechaChange = {},
            onVacunadoChange = {},
            onGuardar = {},
            onBack = {},
            onRetry = {}
        )
    }
}
