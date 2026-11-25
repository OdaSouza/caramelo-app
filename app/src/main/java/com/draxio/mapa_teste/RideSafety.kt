package com.draxio.mapa_teste

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Diversity3
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Man
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Woman
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.draxio.mapa_teste.ui.components.*
import com.draxio.mapa_teste.ui.theme.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Selo verde "Verificado": abre a ficha de segurança do motorista.
@Composable
internal fun OdaVerifiedBadge(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        color = CarameloGreen.copy(alpha = 0.14f),
        contentColor = CarameloGreen
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Rounded.VerifiedUser, contentDescription = null, modifier = Modifier.size(14.dp))
            Text("Verificado", style = MaterialTheme.typography.labelMedium)
        }
    }
}

// Ficha de segurança: o que foi checado no motorista. Durante a corrida, também
// mostra Compartilhar viagem e Ligar 190.
@Composable
internal fun DriverSafetyDialog(
    offer: DriverOffer,
    duringTrip: Boolean,
    onDismiss: () -> Unit,
    onCallEmergency: () -> Unit,
    onShareTrip: () -> Unit
) {
    val group = offer.groupName()
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OdaInitialsAvatar(name = offer.driverName, size = 48.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(offer.driverName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "${offer.vehicle} · ${offer.plate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OdaVerifiedBadge(onClick = {})
                }

                CarameloSectionLabel(text = "O que foi checado")

                SafetyCheckLine("Identidade confirmada", "Selfie conferida com a foto da CNH")
                SafetyCheckLine("CNH válida com EAR", "Habilitação para transporte pago de passageiros")
                SafetyCheckLine("Antecedentes criminais", "Certidões federal e estadual sem registros · ${offer.checkedOn}")
                SafetyCheckLine("Veículo conferido", "Placa e documento do carro batem com o cadastro")
                if (group != "Geral") {
                    SafetyCheckLine("Grupo: $group", "Autodeclarado pelo motorista")
                }

                Text(
                    text = "A checagem é refeita todo ano. Se algo der errado na viagem, use os botões abaixo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (duringTrip) {
                    CarameloPillButton(
                        text = "Compartilhar viagem",
                        icon = Icons.Rounded.Share,
                        onClick = onShareTrip,
                        modifier = Modifier.fillMaxWidth(),
                        style = PillStyle.Outline
                    )
                }
                CarameloPillButton(
                    text = "Ligar 190",
                    icon = Icons.Rounded.Phone,
                    onClick = onCallEmergency,
                    modifier = Modifier.fillMaxWidth(),
                    style = PillStyle.Dark
                )
                CarameloPillButton(
                    text = "Fechar",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    style = PillStyle.Primary
                )
            }
        }
    }
}

@Composable
internal fun SafetyCheckLine(title: String, subtitle: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            modifier = Modifier
                .padding(top = 1.dp)
                .size(20.dp),
            tint = CarameloGreen
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// Avatar redondo com as iniciais do motorista (substitui os emojis de carro).
@Composable
internal fun OdaInitialsAvatar(name: String, size: Dp) {
    val initials = name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
    Box(
        modifier = Modifier
            .size(size)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

// Placa do carro em selo escuro (cacau).
@Composable
internal fun OdaPlateBadge(plate: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = carameloDarkFill(),
        contentColor = Color.White
    ) {
        Text(
            text = plate,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

// Icone Material de cada tipo de motorista preferido.
internal fun driverPreferenceIcon(id: String): ImageVector = when (id) {
    "LGBTQIA+" -> Icons.Rounded.Diversity3
    "Mulher" -> Icons.Rounded.Woman
    "Homem" -> Icons.Rounded.Man
    else -> Icons.Rounded.Person
}
// Converte "R$ 18,40" em 18.40 (valores invalidos contam como zero).
internal fun parseBrlPrice(price: String): Double =
    price.filter { it.isDigit() || it == ',' || it == '.' }
        .replace(".", "")
        .replace(',', '.')
        .toDoubleOrNull() ?: 0.0

// Icone Material de cada categoria de corrida.
internal fun rideCategoryIcon(category: String): ImageVector = when {
    category.contains("Ar", ignoreCase = false) -> Icons.Rounded.AcUnit
    category.contains("Moto", ignoreCase = true) -> Icons.Rounded.TwoWheeler
    category.contains("Entrega", ignoreCase = true) -> Icons.Rounded.Inventory2
    category.contains("Frete", ignoreCase = true) -> Icons.Rounded.LocalShipping
    else -> Icons.Rounded.DirectionsCar
}
