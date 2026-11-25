package com.draxio.mapa_teste

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.draxio.mapa_teste.ui.theme.*

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = androidx.compose.ui.platform.LocalContext.current
            var isDarkMode by remember { mutableStateOf(ThemePreferences.isDarkMode(context)) }

            Mapa_testeTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsScreen(
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = { enabled ->
                            isDarkMode = enabled
                            ThemePreferences.setDarkMode(context, enabled)
                        },
                        onClose = { finish() },
                        onToast = { message ->
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

data class BlockedDriver(
    val id: String,
    val name: String,
    val carInfo: String,
    val reason: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkMode: Boolean = false,
    onToggleDarkMode: (Boolean) -> Unit = {},
    onClose: () -> Unit = {},
    onToast: (String) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    var showTripHistorySubscreen by remember { mutableStateOf(false) }
    var showDriverBlockSubscreen by remember { mutableStateOf(false) }

    // Estado do Endereço Cadastrado (carregado do Repositório Local)
    var streetAddress by remember { mutableStateOf("${CarameloRepository.getUserLogradouro(context)}, ${CarameloRepository.getUserNumber(context)} - ${CarameloRepository.getUserBairro(context)}") }
    var cityAddress by remember { mutableStateOf("${CarameloRepository.getUserCity(context)} - ${CarameloRepository.getUserState(context)}") }
    var cepAddress by remember { mutableStateOf(CarameloRepository.getUserCep(context)) }

    // Estado de Bloqueio de Motoristas (carregado do Repositório Local)
    var autoBlockLowRating by remember { mutableStateOf(true) }
    var enforceNoBlockedMatch by remember { mutableStateOf(true) }
    val initialBlockedList = remember { CarameloRepository.getBlockedDrivers(context) }
    val blockedDrivers = remember { mutableStateListOf<BlockedDriver>().apply { addAll(initialBlockedList) } }

    // Outras Configurações
    var defaultDriverPref by remember { mutableStateOf(CarameloRepository.getDefaultDriverPreference(context)) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var shareRouteSafety by remember { mutableStateOf(true) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            showTripHistorySubscreen -> {
                CarameloTripHistoryView(
                    onBackToSettings = { showTripHistorySubscreen = false }
                )
            }
            showDriverBlockSubscreen -> {
                CarameloDriverBlockView(
                    blockedDrivers = blockedDrivers,
                    autoBlockLowRating = autoBlockLowRating,
                    onToggleAutoBlock = { autoBlockLowRating = it },
                    enforceNoBlockedMatch = enforceNoBlockedMatch,
                    onToggleEnforceNoMatch = { enforceNoBlockedMatch = it },
                    onAddBlockedDriver = { name, reason ->
                        if (name.isNotBlank()) {
                            blockedDrivers.add(
                                BlockedDriver(
                                    id = System.currentTimeMillis().toString(),
                                    name = name,
                                    carInfo = "Veículo não especificado",
                                    reason = if (reason.isBlank()) "Sem motivo informado" else reason
                                )
                            )
                            CarameloRepository.saveBlockedDrivers(context, blockedDrivers)
                            onToast("Motorista $name adicionado à lista de bloqueio!")
                        }
                    },
                    onRemoveBlockedDriver = { driver ->
                        blockedDrivers.remove(driver)
                        CarameloRepository.saveBlockedDrivers(context, blockedDrivers)
                        onToast("Motorista ${driver.name} desbloqueado!")
                    },
                    onBackToSettings = { showDriverBlockSubscreen = false }
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CarameloLogo()

                        OutlinedButton(
                            onClick = onClose,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(
                                text = "✕ Voltar",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "— PREFERÊNCIAS E CONTA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Configurações",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = FontFamily.Serif
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. MODO NOTURNO (TEMA ESCURO)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "🎨 Aparência e Tema",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🌙 Modo Noturno (Tema Escuro)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Ativar tons escuros no aplicativo para proteger a visão",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isDarkMode,
                                    onCheckedChange = {
                                        onToggleDarkMode(it)
                                        onToast(if (it) "Modo Noturno ativado!" else "Modo Claro ativado!")
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // CARD DE CADASTRO DE MOTORISTA PARCEIRO (CNH)
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val isDriverRegistered = remember {
                        CarameloRepository.isDriverRegistered(context)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = android.content.Intent(context, DriverRegisterActivity::class.java)
                                context.startActivity(intent)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isDriverRegistered) 1.5.dp else 1.dp,
                            color = if (isDriverRegistered) CarameloGreen else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(text = "🪪", fontSize = 24.sp)
                                Column {
                                    Text(
                                        text = if (isDriverRegistered) "Motorista Parceiro Cadastrado" else "Seja um Motorista Parceiro",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isDriverRegistered) "Sua CNH e dados de motorista foram validados" else "Cadastre sua CNH para dirigir pelo Caramelo",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isDriverRegistered) CarameloGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = if (isDriverRegistered) "🟢 Cadastrado" else "Cadastrar",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDriverRegistered) CarameloGreen else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. OPÇÃO DE HISTÓRICO DE VIAGENS
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTripHistorySubscreen = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(text = "🕒", fontSize = 24.sp)
                                Column {
                                    Text(
                                        text = "Histórico de Viagens",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Consulte e revise suas corridas anteriores",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(text = "›", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. OPÇÃO DE BLOQUEIO DE MOTORISTAS
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDriverBlockSubscreen = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(text = "🚫", fontSize = 24.sp)
                                Column {
                                    Text(
                                        text = "Bloqueio de Motoristas",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${blockedDrivers.size} motoristas na lista de bloqueio",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Text(text = "›", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. ENDEREÇO DE CADASTRO
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "📍 Endereço de Cadastro",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // CEP em primeiro lugar
                            OutlinedTextField(
                                value = cepAddress,
                                onValueChange = { cepAddress = it },
                                label = { Text("CEP (ex: 01310-100)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = streetAddress,
                                onValueChange = { streetAddress = it },
                                label = { Text("Logradouro, Número e Bairro") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = cityAddress,
                                onValueChange = { cityAddress = it },
                                label = { Text("Cidade / UF") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    CarameloRepository.saveUserProfile(
                                        context = context,
                                        name = CarameloRepository.getUserName(context),
                                        email = CarameloRepository.getUserEmail(context),
                                        logradouro = streetAddress,
                                        number = "",
                                        bairro = "",
                                        complement = "",
                                        cep = cepAddress,
                                        city = cityAddress,
                                        state = ""
                                    )
                                    onToast("Endereço salvo localmente com sucesso!")
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Salvar Endereço", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5. PREFERÊNCIA DE MOTORISTA PADRÃO / FIXA
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "🚘 Preferência de Motorista Padrão / Fixa",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Perfil de motorista preferido (Geral, LGBTQIA+, Mulher, Homem)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val options = listOf("Geral", "LGBTQIA+", "Mulher", "Homem")
                            options.forEach { option ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            defaultDriverPref = option
                                            CarameloRepository.saveDefaultDriverPreference(context, option)
                                            onToast("Preferência padrão salva: $option")
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (defaultDriverPref == option),
                                        onClick = {
                                            defaultDriverPref = option
                                            CarameloRepository.saveDefaultDriverPreference(context, option)
                                            onToast("Preferência padrão salva: $option")
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = option,
                                        fontSize = 14.sp,
                                        fontWeight = if (defaultDriverPref == option) FontWeight.Bold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(8.dp))

                            var isFixedInSettings by remember { mutableStateOf(CarameloRepository.isFixedPreferenceEnabled(context)) }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📌 Manter preferência fixa para todas as viagens",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(
                                    checked = isFixedInSettings,
                                    onCheckedChange = {
                                        isFixedInSettings = it
                                        CarameloRepository.saveFixedPreferenceEnabled(context, it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 6. PRECIFICAÇÃO E INCREMENTO DOS BOTÕES (+ / -)
                    var priceStep by remember { mutableStateOf(CarameloRepository.getPriceIncrementStep(context)) }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "💰 Valor Adicionado pelos Botões (+ / -)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Defina quanto cada clique nos botões '+' e '-' adiciona ou subtrai no valor da oferta",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            val steps = listOf("R$ 0,50", "R$ 1,00", "R$ 2,00", "R$ 5,00")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                steps.forEach { step ->
                                    val isSelected = (priceStep == step)
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                priceStep = step
                                                CarameloRepository.savePriceIncrementStep(context, step)
                                                onToast("Valor dos botões definido para $step")
                                            },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = step,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 7. NOTIFICAÇÕES E SEGURANÇA
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "🔔 Notificações e Segurança",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Notificações de Corrida", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Switch(
                                    checked = notificationsEnabled,
                                    onCheckedChange = { notificationsEnabled = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Compartilhar Rota em Tempo Real", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Switch(
                                    checked = shareRouteSafety,
                                    onCheckedChange = { shareRouteSafety = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onToast("Sua conta foi desconectada") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B))
                    ) {
                        Text(text = "Sair da Conta", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// VISUALIZAÇÃO DE BLOQUEIO DE MOTORISTAS
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarameloDriverBlockView(
    blockedDrivers: List<BlockedDriver>,
    autoBlockLowRating: Boolean,
    onToggleAutoBlock: (Boolean) -> Unit,
    enforceNoBlockedMatch: Boolean,
    onToggleEnforceNoMatch: (Boolean) -> Unit,
    onAddBlockedDriver: (String, String) -> Unit,
    onRemoveBlockedDriver: (BlockedDriver) -> Unit,
    onBackToSettings: () -> Unit
) {
    var newName by remember { mutableStateOf("") }
    var newReason by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CarameloLogo()

            OutlinedButton(
                onClick = onBackToSettings,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text(
                    text = "✕ Voltar às Configurações",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "— SEGURANÇA E PROTEÇÃO",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )

        Text(
            text = "Bloqueio de Motoristas",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = FontFamily.Serif
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Regras Automáticas
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🛡️ Regras de Bloqueio Automático",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nunca emparelhar com motoristas bloqueados",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = enforceNoBlockedMatch,
                        onCheckedChange = onToggleEnforceNoMatch,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Evitar motoristas com nota abaixo de 4.5 ★",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = autoBlockLowRating,
                        onCheckedChange = onToggleAutoBlock,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Form para adicionar motorista a bloquear
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "➕ Bloquear Novo Motorista",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nome do Motorista") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newReason,
                    onValueChange = { newReason = it },
                    label = { Text("Motivo do bloqueio (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onAddBlockedDriver(newName, newReason)
                            newName = ""
                            newReason = ""
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Bloquear Motorista", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "MOTORISTAS ATUALMENTE BLOQUEADOS (${blockedDrivers.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (blockedDrivers.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text(
                    text = "Nenhum motorista bloqueado.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        } else {
            blockedDrivers.forEach { driver ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🚫 ${driver.name}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = driver.carInfo,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Motivo: ${driver.reason}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        TextButton(
                            onClick = { onRemoveBlockedDriver(driver) }
                        ) {
                            Text(
                                text = "Desbloquear",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// TELA DE HISTÓRICO DE VIAGENS (INCORPORADA)
@Composable
fun CarameloTripHistoryView(onBackToSettings: () -> Unit) {
    val historyItems = remember {
        listOf(
            TripHistoryItem("12 de Março, 14:30", "Localização atual", "Shopping Cidade Jardim", "Carlos Silva", "4.9 ★", "Caramelo Leve", "LGBTQIA+", "R$ 18,40"),
            TripHistoryItem("10 de Março, 09:15", "Alameda Santos, 1420", "Av. Faria Lima, 3400", "Fernanda Lima", "5.0 ★", "Caramelo Plus", "Mulher", "R$ 24,90"),
            TripHistoryItem("05 de Março, 20:00", "Parque Ibirapuera", "Rua Augusta, 500", "Lucas Almeida", "4.8 ★", "Caramelo Grupo", "Geral", "R$ 32,60"),
            TripHistoryItem("28 de Fevereiro, 11:45", "Aeroporto de Congonhas", "Alameda Santos, 1420", "Rafael Costa", "4.9 ★", "Caramelo Leve", "Homem", "R$ 38,00")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CarameloLogo()

            OutlinedButton(
                onClick = onBackToSettings,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text(
                    text = "✕ Voltar às Configurações",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "— SEU HISTÓRICO",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )

        Text(
            text = "Histórico de Viagens",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = FontFamily.Serif
        )

        Spacer(modifier = Modifier.height(16.dp))

        historyItems.forEach { trip ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = trip.date,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CarameloGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "✓ Concluída",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CarameloGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "de: ${trip.origin}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "para: ${trip.destination}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Motorista: ${trip.driverName} (${trip.driverRating})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${trip.category} • Motorista: ${trip.driverType}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = trip.price,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SeparateSettingsPreview() {
    Mapa_testeTheme {
        SettingsScreen(onClose = {}, onToast = {})
    }
}

@Preview(showBackground = true)
@Composable
fun SeparateSettingsDarkPreview() {
    Mapa_testeTheme(darkTheme = true) {
        SettingsScreen(isDarkMode = true, onClose = {}, onToast = {})
    }
}
