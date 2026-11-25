package com.draxio.mapa_teste

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.draxio.mapa_teste.ui.components.*

// Folha de escolha de destino: digitar livremente, atalhos Casa/Trabalho e recentes.
@Composable
internal fun DestinationPickerSheet(
    current: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var text by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf(false) }
    var workAddress by remember { mutableStateOf(CarameloRepository.getWorkAddress(context)) }
    var homeAddress by remember { mutableStateOf(CarameloRepository.getHomeAddress(context)) }
    val recents = remember { CarameloRepository.getRecentDestinations(context) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    fun confirm(value: String) {
        val clean = value.trim()
        if (clean.isEmpty()) {
            error = true
        } else {
            onPick(clean)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CarameloTopBar(title = "Para onde vamos?", onBack = onDismiss)

                CarameloTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        error = false
                    },
                    label = "Destino",
                    placeholder = if (current.isNotBlank()) current else "Rua, bairro ou lugar",
                    isError = error,
                    leadingIcon = Icons.Rounded.Search,
                    modifier = Modifier.focusRequester(focusRequester)
                )
                if (error) {
                    Text(
                        text = "Digite um destino para continuar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                CarameloSectionLabel(text = "Atalhos")
                CarameloCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                    CarameloListRow(
                        icon = Icons.Rounded.Home,
                        title = if (homeAddress.isBlank()) "Definir casa" else "Casa",
                        subtitle = homeAddress.ifBlank { "Toque para salvar o endereço digitado" },
                        onClick = {
                            if (homeAddress.isNotBlank()) {
                                confirm(homeAddress)
                            } else if (text.isNotBlank()) {
                                homeAddress = text.trim()
                                CarameloRepository.saveHomeAddress(context, homeAddress)
                                Toast.makeText(context, "Casa salva", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Digite o endereço e toque de novo", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    CarameloDivider()
                    CarameloListRow(
                        icon = Icons.Rounded.Work,
                        title = if (workAddress.isBlank()) "Definir trabalho" else "Trabalho",
                        subtitle = workAddress.ifBlank { "Toque para salvar o endereço digitado" },
                        onClick = {
                            if (workAddress.isNotBlank()) {
                                confirm(workAddress)
                            } else if (text.isNotBlank()) {
                                workAddress = text.trim()
                                CarameloRepository.saveWorkAddress(context, workAddress)
                                Toast.makeText(context, "Trabalho salvo", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Digite o endereço e toque de novo", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                if (recents.isNotEmpty()) {
                    CarameloSectionLabel(text = "Recentes")
                    CarameloCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                        recents.forEachIndexed { index, place ->
                            if (index > 0) CarameloDivider()
                            CarameloListRow(
                                icon = Icons.Rounded.History,
                                title = place,
                                onClick = { confirm(place) }
                            )
                        }
                    }
                }

                CarameloPillButton(
                    text = "Confirmar destino",
                    onClick = { confirm(text) },
                    modifier = Modifier.fillMaxWidth(),
                    style = PillStyle.Primary
                )
            }
        }
    }
}
