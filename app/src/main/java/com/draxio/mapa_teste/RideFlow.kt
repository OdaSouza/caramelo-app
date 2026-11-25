package com.draxio.mapa_teste

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Map as MapIcon
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.draxio.mapa_teste.ui.components.*
import com.draxio.mapa_teste.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------------------------------------------------------------- Dados do fluxo

internal data class ChatLine(val fromMe: Boolean, val text: String)

internal val CANCEL_REASONS = listOf(
    "Motorista demorando",
    "Mudei de ideia",
    "Endereço errado",
    "Motorista pediu para cancelar",
    "Outro"
)

internal val TIP_OPTIONS = listOf(0f, 2f, 5f, 10f)

// Cada "minuto" do ETA dura 10 segundos na simulação
private const val SECONDS_PER_SIM_MINUTE = 10

internal fun paymentLabel(method: String): String =
    if (method == CarameloRepository.PAY_CARD) "Cartão de demonstração" else method

private fun nowText(): String =
    SimpleDateFormat("dd 'de' MMMM, HH:mm", Locale.forLanguageTag("pt-BR")).format(Date())

// Grava a corrida concluída no histórico
internal fun saveCompletedTrip(
    context: android.content.Context,
    offer: DriverOffer?,
    origin: String,
    destination: String,
    category: String,
    fare: Float,
    rating: Int,
    tip: String,
    payment: String
) {
    CarameloRepository.addTrip(
        context,
        TripHistoryItem(
            date = nowText(),
            origin = origin,
            destination = destination,
            driverName = offer?.driverName ?: "Motorista",
            driverRating = offer?.rating ?: "",
            category = category,
            driverType = offer?.driverTypeTag ?: "",
            price = formatBrl(fare),
            rating = rating,
            tip = tip,
            payment = payment,
            status = CarameloRepository.TRIP_DONE
        )
    )
}

// Grava a corrida cancelada (depois do aceite) no histórico
internal fun saveCanceledTrip(
    context: android.content.Context,
    offer: DriverOffer?,
    origin: String,
    destination: String,
    category: String,
    payment: String
) {
    CarameloRepository.addTrip(
        context,
        TripHistoryItem(
            date = nowText(),
            origin = origin,
            destination = destination,
            driverName = offer?.driverName ?: "Motorista",
            driverRating = offer?.rating ?: "",
            category = category,
            driverType = offer?.driverTypeTag ?: "",
            price = formatBrl(0f),
            rating = 0,
            tip = "",
            payment = payment,
            status = CarameloRepository.TRIP_CANCELED
        )
    )
}

// ---------------------------------------------------------------- Motorista a caminho

@Composable
internal fun RideArrivingContent(
    driver: DriverOffer,
    pin: String,
    onBoarded: () -> Unit,
    onCancel: () -> Unit,
    onCall: () -> Unit,
    onChat: () -> Unit,
    onRoute: () -> Unit,
    onSafety: () -> Unit
) {
    val etaMinutes = remember(driver.id) {
        driver.eta.filter { it.isDigit() }.toIntOrNull()?.coerceAtLeast(1) ?: 2
    }
    val totalSeconds = etaMinutes * SECONDS_PER_SIM_MINUTE
    var remaining by rememberSaveable { mutableIntStateOf(totalSeconds) }
    LaunchedEffect(Unit) {
        while (remaining > 0) {
            delay(1000)
            remaining -= 1
        }
    }
    val arrived = remaining <= 0
    val minutesLeft = (remaining + SECONDS_PER_SIM_MINUTE - 1) / SECONDS_PER_SIM_MINUTE
    val firstName = driver.driverName.substringBefore(' ')

    CarameloStatusPill(
        text = if (arrived) "Seu motorista chegou" else "Motorista a caminho",
        dotColor = CarameloGreen
    )

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        CarameloBigNumber(
            label = if (arrived) "$firstName está" else "$firstName chega em",
            value = if (arrived) "No local" else "$minutesLeft min",
            modifier = Modifier.weight(1f)
        )
        CarameloBigNumber(label = "Valor", value = driver.offeredPrice, accent = true, alignEnd = true)
    }

    CarameloSegmentedProgress(progress = 1f - remaining.toFloat() / totalSeconds)

    // Código de embarque
    CarameloCard(color = MaterialTheme.colorScheme.primaryContainer) {
        CarameloSectionLabel(text = "Código de embarque")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            pin.forEach { digit ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = digit.toString(),
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        Text(
            text = "Diga este código ao motorista antes de entrar",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    DriverCard(driver = driver, onSafety = onSafety)
    RideActionsRow(onCall = onCall, onChat = onChat, onRoute = onRoute, onSafety = onSafety)

    CarameloPillButton(
        text = "Já embarquei",
        onClick = onBoarded,
        modifier = Modifier.fillMaxWidth()
    )
    CarameloPillButton(
        text = "Cancelar corrida",
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth(),
        style = PillStyle.Outline
    )
}

// ---------------------------------------------------------------- Em viagem

@Composable
internal fun RideInTripContent(
    driver: DriverOffer,
    destination: String,
    onArrived: () -> Unit,
    onCancel: () -> Unit,
    onCall: () -> Unit,
    onChat: () -> Unit,
    onRoute: () -> Unit,
    onSafety: () -> Unit,
    paused: Boolean = false
) {
    var progress by rememberSaveable { mutableFloatStateOf(0f) }
    val latestArrived by rememberUpdatedState(onArrived)
    // Pausa enquanto o dialogo de cancelamento esta aberto; retoma de onde parou
    LaunchedEffect(paused) {
        if (!paused) {
            // 0 a 1 em cerca de 20 segundos
            while (progress < 1f) {
                delay(200)
                progress = (progress + 0.01f).coerceAtMost(1f)
            }
            latestArrived()
        }
    }

    CarameloStatusPill(text = "Em viagem", dotColor = CarameloGreen)

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Destino", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(destination, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        CarameloBigNumber(label = "Valor", value = driver.offeredPrice, accent = true, alignEnd = true)
    }

    CarameloSegmentedProgress(progress = progress)

    DriverCard(driver = driver, onSafety = onSafety)
    RideActionsRow(onCall = onCall, onChat = onChat, onRoute = onRoute, onSafety = onSafety)

    CarameloPillButton(
        text = "Cheguei ao destino",
        onClick = onArrived,
        modifier = Modifier.fillMaxWidth()
    )
    CarameloPillButton(
        text = "Cancelar corrida",
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth(),
        style = PillStyle.Outline
    )
}

@Composable
private fun DriverCard(driver: DriverOffer, onSafety: () -> Unit) {
    CarameloCard(color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OdaInitialsAvatar(name = driver.driverName, size = 52.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(driver.driverName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "${driver.vehicle} · ${driver.rating}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OdaVerifiedBadge(onClick = onSafety, modifier = Modifier.padding(top = 6.dp))
            }
            OdaPlateBadge(plate = driver.plate)
        }
    }
}

@Composable
private fun RideActionsRow(onCall: () -> Unit, onChat: () -> Unit, onRoute: () -> Unit, onSafety: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        CarameloCircleAction(icon = Icons.Rounded.Phone, label = "Ligar", onClick = onCall)
        CarameloCircleAction(icon = Icons.Rounded.ChatBubble, label = "Mensagem", onClick = onChat)
        CarameloCircleAction(icon = Icons.Rounded.MapIcon, label = "Rota", onClick = onRoute)
        CarameloCircleAction(icon = Icons.Rounded.Shield, label = "Segurança", onClick = onSafety)
    }
}

// ---------------------------------------------------------------- Recibo, avaliação e gorjeta

@Composable
internal fun RideReceiptContent(
    driver: DriverOffer,
    destination: String,
    onFinish: (rating: Int, tipText: String, fare: Float, payment: String) -> Unit
) {
    val context = LocalContext.current
    val payment = remember { paymentLabel(CarameloRepository.getPaymentMethod(context)) }
    val coupon = remember { CarameloRepository.getActiveCoupon(context) }
    val price = remember(driver.id) { parseBrlPrice(driver.offeredPrice).toFloat() }
    val discount = remember(driver.id) { CarameloRepository.couponDiscount(context, price) }
    var rating by rememberSaveable { mutableIntStateOf(0) }
    var tipIndex by rememberSaveable { mutableIntStateOf(0) }
    val tip = TIP_OPTIONS[tipIndex]
    val fare = (price - discount).coerceAtLeast(0f)
    val total = fare + tip

    CarameloStatusPill(text = "Corrida concluída", dotColor = CarameloGreen)
    Text(
        text = "Você chegou em $destination",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface
    )

    CarameloCard(color = MaterialTheme.colorScheme.surfaceVariant) {
        CarameloSectionLabel(text = "Recibo")
        ReceiptLine("Corrida", formatBrl(price))
        if (discount > 0f) ReceiptLine("Cupom ${coupon ?: ""}".trim(), "- ${formatBrl(discount)}")
        ReceiptLine("Gorjeta", formatBrl(tip))
        CarameloDivider(startIndent = 0.dp)
        ReceiptLine("Total", formatBrl(total), bold = true)
        ReceiptLine("Pagamento", payment)
    }

    CarameloSectionLabel(text = "Como foi a corrida com ${driver.driverName.substringBefore(' ')}?")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        (1..5).forEach { star ->
            Icon(
                imageVector = if (star <= rating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                contentDescription = "$star ${if (star == 1) "estrela" else "estrelas"}",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(44.dp)
                    .clickable { rating = star }
                    .padding(4.dp)
            )
        }
    }

    CarameloSectionLabel(text = "Gorjeta")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TIP_OPTIONS.forEachIndexed { index, value ->
            CarameloChip(
                label = if (value == 0f) "Sem gorjeta" else "R$ ${value.toInt()}",
                selected = tipIndex == index,
                onClick = { tipIndex = index }
            )
        }
    }

    CarameloPillButton(
        text = "Concluir",
        onClick = { onFinish(rating, if (tip > 0f) formatBrl(tip) else "", fare, payment) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ReceiptLine(label: String, value: String, bold: Boolean = false) {
    val style = if (bold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = style, color = MaterialTheme.colorScheme.onSurface)
        Text(value, style = style, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ---------------------------------------------------------------- Diálogos

@Composable
internal fun CancelRideDialog(
    onConfirm: (reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by rememberSaveable { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Cancelar corrida?", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Conte o motivo para melhorarmos o Caramelo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CANCEL_REASONS.forEach { option ->
                    CarameloChip(
                        label = option,
                        selected = reason == option,
                        onClick = { reason = option }
                    )
                }
                CarameloPillButton(
                    text = "Cancelar corrida",
                    onClick = { onConfirm(reason) },
                    modifier = Modifier.fillMaxWidth(),
                    style = PillStyle.Dark,
                    enabled = reason.isNotEmpty()
                )
                CarameloPillButton(
                    text = "Voltar",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    style = PillStyle.Outline
                )
            }
        }
    }
}

@Composable
internal fun RideChatDialog(
    driverName: String,
    messages: List<ChatLine>,
    onSend: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val quick = listOf("Estou a caminho", "Já estou no local", "Pode me esperar 2 min?")
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Conversa com ${driverName.substringBefore(' ')}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp, max = 240.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (messages.isEmpty()) {
                        Text(
                            "Escolha uma resposta rápida abaixo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    messages.forEach { line ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (line.fromMe) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (line.fromMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (line.fromMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ) {
                                Text(
                                    line.text,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
                quick.forEach { text ->
                    CarameloChip(label = text, selected = false, onClick = { onSend(text) })
                }
                CarameloPillButton(
                    text = "Fechar",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    style = PillStyle.Outline
                )
            }
        }
    }
}

@Composable
internal fun CallDemoDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Ligar para o motorista", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Modo demonstração: a ligação mascarada com o motorista depende do servidor.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CarameloPillButton(
                    text = "Entendi",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
