package com.draxio.mapa_teste

import java.util.Locale
import kotlin.math.roundToInt

enum class RideStatus {
    IDLE,
    SEARCHING_DRIVER,
    DRIVER_ARRIVING,
    IN_TRIP,
    COMPLETED
}

data class RideCategory(
    val name: String,
    val price: String,
    val timeAndType: String,
    val capacity: Int,
    val icon: String
)

data class DriverPreferenceOption(
    val id: String,
    val title: String,
    val icon: String
)

data class DriverOffer(
    val id: String,
    val driverName: String,
    val rating: String,
    val vehicle: String,
    val plate: String,
    val offeredPrice: String,
    val eta: String,
    val driverTypeTag: String,
    val icon: String,
    // Verificação de segurança do motorista (selfie x CNH, antecedentes, EAR, veículo)
    val verified: Boolean = true,
    val checkedOn: String = "jan/2026"
)

// Formata valor em reais (ex.: 18.4f -> "R$ 18,40")
internal fun formatBrl(value: Float): String =
    String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", value)

// Propostas dos motoristas a partir do valor oferecido pelo passageiro.
// Variacoes fixas por motorista (+0%, +5%, -3%, +8%, +2%), arredondadas a R$ 0,50.
internal fun buildDriverOffers(basePrice: Float): List<DriverOffer> {
    fun priced(percent: Int): String {
        val raw = basePrice * (100 + percent) / 100f
        val rounded = (raw * 2f).roundToInt() / 2f
        return formatBrl(rounded.coerceAtLeast(0.5f))
    }
    return listOf(
        DriverOffer("1", "Carlos Silva", "4.9 ★", "Honda Civic", "ABC-1234", priced(0), "2 min", "🏳️‍🌈 LGBTQIA+", "🚖"),
        DriverOffer("2", "Fernanda Lima", "5.0 ★", "Toyota Corolla", "XYZ-5678", priced(5), "3 min", "👩 Mulher", "🚘"),
        DriverOffer("3", "Lucas Almeida", "4.8 ★", "Hyundai HB20", "KML-9012", priced(-3), "1 min", "👤 Geral", "🚕"),
        DriverOffer("4", "Rafael Costa", "4.9 ★", "Chevrolet Onix", "QWE-3456", priced(8), "4 min", "👨 Homem", "🚗", checkedOn = "fev/2026"),
        DriverOffer("5", "Juliana Rocha", "4.9 ★", "VW Polo", "RTY-7890", priced(2), "5 min", "👩 Mulher", "🚗", checkedOn = "mar/2026")
    )
}

// Iniciais: primeira letra do primeiro e do ultimo nome, maiusculas
internal fun userInitialsOf(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    if (parts.isEmpty()) return "P"
    val first = parts.first().first()
    val last = if (parts.size > 1) parts.last().first().toString() else ""
    return "$first$last".uppercase()
}

// Grupo do motorista sem o emoji ("🏳️‍🌈 LGBTQIA+" -> "LGBTQIA+")
internal fun DriverOffer.groupName(): String = driverTypeTag.substringAfter(' ')

// TELA DE HISTÓRICO DE VIAGENS
data class TripHistoryItem(
    val date: String,
    val origin: String,
    val destination: String,
    val driverName: String,
    val driverRating: String,
    val category: String,
    val driverType: String,
    val price: String,
    // Fim da corrida: nota dada (0 = sem nota), gorjeta, forma de pagamento e situação
    val rating: Int = 0,
    val tip: String = "",
    val payment: String = "",
    val status: String = CarameloRepository.TRIP_DONE
)
