package com.draxio.mapa_teste

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.draxio.mapa_teste.ui.components.*
import com.draxio.mapa_teste.ui.theme.*

@Composable
fun CarameloTripHistoryScreen(onClose: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val allTrips = remember { CarameloRepository.getTripHistory(context) }
    // Filtro e total só contam viagens concluídas; as canceladas aparecem na lista com selo cinza
    val historyItems = allTrips

    var selectedFilter by rememberSaveable { mutableStateOf("Todas") }
    val filters = listOf("Todas", "Carro", "Moto", "Entregas")
    val visibleItems = if (selectedFilter == "Todas") historyItems
    else historyItems.filter { it.category.contains(selectedFilter, ignoreCase = true) }
    val doneTrips = allTrips.filter { it.status == CarameloRepository.TRIP_DONE }
    val totalSpent = doneTrips.sumOf { parseBrlPrice(it.price) + parseBrlPrice(it.tip) }
    val totalText = formatBrl(totalSpent.toFloat())

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CarameloTopBar(title = "Suas viagens", onBack = onClose)

            // Resumo escuro: quantidade de viagens e total gasto
            CarameloCard(color = carameloDarkFill(), contentPadding = PaddingValues(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Seu histórico",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Text(
                        text = if (doneTrips.size == 1) "1 viagem" else "${doneTrips.size} viagens",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
                Text(
                    text = totalText,
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White
                )
                Text(
                    text = "Total gasto nas viagens concluídas",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }

            // Filtros por categoria
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filters.forEach { filter ->
                    CarameloChip(
                        label = filter,
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter }
                    )
                }
            }

            if (visibleItems.isEmpty()) {
                CarameloCard {
                    Text(
                        text = "Nenhuma viagem por aqui ainda",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Suas corridas concluídas aparecem nesta lista.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                CarameloCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                    visibleItems.forEachIndexed { index, trip ->
                        if (index > 0) CarameloDivider()
                        CarameloListRow(
                            icon = rideCategoryIcon(trip.category),
                            title = trip.destination,
                            subtitle = buildString {
                                append("${trip.date} · ${trip.driverName} · ${trip.driverRating}\nde ${trip.origin} · ${trip.driverType}")
                                val extras = listOfNotNull(
                                    trip.tip.takeIf { it.isNotBlank() }?.let { "Gorjeta $it" },
                                    trip.payment.takeIf { it.isNotBlank() }
                                )
                                if (extras.isNotEmpty()) append("\n" + extras.joinToString(" · "))
                            },
                            highlightIcon = index == 0,
                            trailing = {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = trip.price,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = trip.category,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val done = trip.status == CarameloRepository.TRIP_DONE
                                    val badgeColor = if (done) CarameloGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    Surface(
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                        color = badgeColor.copy(alpha = 0.14f),
                                        contentColor = badgeColor
                                    ) {
                                        Text(
                                            text = if (done) "Concluída" else "Cancelada",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                    if (trip.rating > 0) {
                                        Row {
                                            repeat(5) { i ->
                                                Icon(
                                                    imageVector = if (i < trip.rating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
