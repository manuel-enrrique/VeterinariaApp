package com.example.veterinaria.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.veterinaria.di.ViewModelFactory
import com.example.veterinaria.model.Mascota
import com.example.veterinaria.presentation.common.calcularEdad
import com.example.veterinaria.presentation.common.formatFecha
import com.example.veterinaria.presentation.common.formatPeso
import com.example.veterinaria.ui.theme.VeterinariaAppTheme

@Composable
fun MascotaDetailRoute(
    mascotaId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val factory = remember(mascotaId) { ViewModelFactory.detail(mascotaId) }
    val viewModel: MascotaDetailViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.cargarMascota()
    }

    MascotaDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onEdit = onEdit,
        onRetry = viewModel::cargarMascota
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MascotaDetailScreen(
    uiState: MascotaDetailUiState,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de la mascota") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    val mascota = (uiState as? MascotaDetailUiState.Success)?.mascota
                    IconButton(
                        onClick = { mascota?.let { onEdit(it.id) } },
                        enabled = mascota != null
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Editar mascota"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                MascotaDetailUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                is MascotaDetailUiState.Error -> ErrorContent(
                    message = uiState.message,
                    onRetry = onRetry
                )

                is MascotaDetailUiState.Success -> DetailContent(
                    mascota = uiState.mascota,
                    onEdit = onEdit
                )
            }
        }
    }
}

@Composable
private fun DetailContent(
    mascota: Mascota,
    onEdit: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mascota.nombre,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = mascota.especie,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                VacunadoStatus(vacunado = mascota.vacunado)
            }
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow(
                    icon = Icons.Filled.Pets,
                    label = "Especie",
                    value = mascota.especie
                )
                HorizontalDivider()
                DetailRow(
                    icon = Icons.Filled.MonitorWeight,
                    label = "Peso",
                    value = formatPeso(mascota.pesoKg)
                )
                HorizontalDivider()
                DetailRow(
                    icon = Icons.Filled.CalendarMonth,
                    label = "Fecha de nacimiento",
                    value = formatFecha(mascota.fechaNacimiento)
                )
                calcularEdad(mascota.fechaNacimiento)?.let { edad ->
                    HorizontalDivider()
                    DetailRow(
                        icon = Icons.Filled.Cake,
                        label = "Edad aproximada",
                        value = edad
                    )
                }
                HorizontalDivider()
                DetailRow(
                    icon = Icons.Filled.Vaccines,
                    label = "Estado de vacunación",
                    value = if (mascota.vacunado) {
                        "Vacunado"
                    } else {
                        "Sin vacunas registradas"
                    }
                )
            }
        }

        Button(
            onClick = { onEdit(mascota.id) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Filled.Edit, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Editar mascota")
        }
    }
}

@Composable
private fun VacunadoStatus(vacunado: Boolean) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (vacunado) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        }
    ) {
        Text(
            text = if (vacunado) "Vacunado" else "Sin vacunas",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (vacunado) {
                MaterialTheme.colorScheme.onTertiaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
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
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Reintentar")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MascotaDetailScreenPreview() {
    VeterinariaAppTheme(dynamicColor = false) {
        MascotaDetailScreen(
            uiState = MascotaDetailUiState.Success(
                Mascota(
                    id = 1L,
                    nombre = "Firulais",
                    especie = "Perro",
                    pesoKg = 12.5,
                    fechaNacimiento = "2020-05-14",
                    vacunado = true
                )
            ),
            onBack = {},
            onEdit = {},
            onRetry = {}
        )
    }
}
