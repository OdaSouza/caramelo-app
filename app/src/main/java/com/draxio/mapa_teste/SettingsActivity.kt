package com.draxio.mapa_teste

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Diversity3
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Man
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Woman
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.draxio.mapa_teste.ui.components.CarameloCard
import com.draxio.mapa_teste.ui.components.CarameloChip
import com.draxio.mapa_teste.ui.components.CarameloDivider
import com.draxio.mapa_teste.ui.components.CarameloListRow
import com.draxio.mapa_teste.ui.components.CarameloPillButton
import com.draxio.mapa_teste.ui.components.CarameloSectionLabel
import com.draxio.mapa_teste.ui.components.CarameloStatusPill
import com.draxio.mapa_teste.ui.components.CarameloSwitchRow
import com.draxio.mapa_teste.ui.components.CarameloTextField
import com.draxio.mapa_teste.ui.components.CarameloTopBar
import com.draxio.mapa_teste.ui.components.PillStyle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.draxio.mapa_teste.ui.theme.*

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Mapa_testeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsScreen(
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
    // Por padrão usa o estado global do tema (ThemeState); o interruptor grava nele
    isDarkMode: Boolean = ThemeState.darkMode ?: isSystemInDarkTheme(),
    onToggleDarkMode: ((Boolean) -> Unit)? = null,
    onClose: () -> Unit = {},
    onToast: (String) -> Unit = {}
) {
    val context = LocalContext.current

    // Volta para o Login limpando a pilha de telas
    val goToLogin = {
        val intent = Intent(context, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }
    var showDeleteDataDialog by rememberSaveable { mutableStateOf(false) }

    var showTripHistorySubscreen by rememberSaveable { mutableStateOf(false) }
    var showDriverBlockSubscreen by rememberSaveable { mutableStateOf(false) }
    // Voltar do sistema numa subtela volta às Configurações (não fecha tudo)
    androidx.activity.compose.BackHandler(enabled = showTripHistorySubscreen || showDriverBlockSubscreen) {
        showTripHistorySubscreen = false
        showDriverBlockSubscreen = false
    }

    // Endereço de cadastro: um campo para cada chave do repositório (sem recompor texto)
    var addrStreet by rememberSaveable { mutableStateOf(CarameloRepository.getUserLogradouro(context)) }
    var addrNumber by rememberSaveable { mutableStateOf(CarameloRepository.getUserNumber(context)) }
    var addrBairro by rememberSaveable { mutableStateOf(CarameloRepository.getUserBairro(context)) }
    var addrCep by rememberSaveable { mutableStateOf(CarameloRepository.getUserCep(context)) }
    var addrCity by rememberSaveable { mutableStateOf(CarameloRepository.getUserCity(context)) }
    var addrState by rememberSaveable { mutableStateOf(CarameloRepository.getUserState(context)) }

    // Cadastro de motorista e verificação: relidos quando a tela volta a ficar visível
    var isDriverRegistered by remember { mutableStateOf(CarameloRepository.isDriverRegistered(context)) }
    var driverVerificationStatus by remember { mutableStateOf(CarameloRepository.getDriverVerificationStatus(context)) }
    DisposableEffect(context) {
        val owner = context as? LifecycleOwner
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isDriverRegistered = CarameloRepository.isDriverRegistered(context)
                driverVerificationStatus = CarameloRepository.getDriverVerificationStatus(context)
            }
        }
        owner?.lifecycle?.addObserver(observer)
        onDispose { owner?.lifecycle?.removeObserver(observer) }
    }

    // Estado de Bloqueio de Motoristas (carregado do Repositório Local)
    var autoBlockLowRating by rememberSaveable { mutableStateOf(CarameloRepository.isAutoBlockLowRating(context)) }
    var enforceNoBlockedMatch by rememberSaveable { mutableStateOf(CarameloRepository.isEnforceNoBlockedMatch(context)) }
    val initialBlockedList = remember { CarameloRepository.getBlockedDrivers(context) }
    val blockedDrivers = remember { mutableStateListOf<BlockedDriver>().apply { addAll(initialBlockedList) } }

    // Outras Configurações
    var defaultDriverPref by rememberSaveable { mutableStateOf(CarameloRepository.getDefaultDriverPreference(context)) }
    var notificationsEnabled by rememberSaveable { mutableStateOf(CarameloRepository.isNotificationsEnabled(context)) }
    var shareRouteSafety by rememberSaveable { mutableStateOf(CarameloRepository.isShareRouteEnabled(context)) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            showTripHistorySubscreen -> {
                CarameloTripHistoryScreen(
                    onClose = { showTripHistorySubscreen = false }
                )
            }
            showDriverBlockSubscreen -> {
                CarameloDriverBlockView(
                    blockedDrivers = blockedDrivers,
                    autoBlockLowRating = autoBlockLowRating,
                    onToggleAutoBlock = {
                        autoBlockLowRating = it
                        CarameloRepository.setAutoBlockLowRating(context, it)
                    },
                    enforceNoBlockedMatch = enforceNoBlockedMatch,
                    onToggleEnforceNoMatch = {
                        enforceNoBlockedMatch = it
                        CarameloRepository.setEnforceNoBlockedMatch(context, it)
                    },
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
                val userName = remember { CarameloRepository.getUserName(context) }
                val userInitials = remember(userName) {
                    userName.trim().split(" ").filter { it.isNotBlank() }
                        .let { parts ->
                            when {
                                parts.isEmpty() -> "?"
                                parts.size == 1 -> parts[0].take(2)
                                else -> "${parts.first().first()}${parts.last().first()}"
                            }
                        }
                        .uppercase()
                }
                val isDriverVerified = driverVerificationStatus == CarameloRepository.VERIFICATION_APPROVED

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CarameloTopBar(title = "Configurações", onBack = onClose)

                    // PERFIL
                    CarameloCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userInitials,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = userName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "4,92 de avaliação",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // 1. APARÊNCIA (MODO NOTURNO)
                    CarameloSectionLabel("Aparência")
                    CarameloCard(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp)) {
                        CarameloSwitchRow(
                            icon = Icons.Rounded.DarkMode,
                            title = "Modo noturno",
                            subtitle = "Tons escuros para proteger a visão",
                            checked = isDarkMode,
                            onCheckedChange = {
                                if (onToggleDarkMode != null) onToggleDarkMode(it) else ThemeState.setDarkMode(context, it)
                                onToast(if (it) "Modo Noturno ativado!" else "Modo Claro ativado!")
                            }
                        )
                    }

                    // 2. ENDEREÇO DE CADASTRO
                    CarameloSectionLabel("Endereço")
                    CarameloCard {
                        val savedAddressText = listOf(
                            listOf(addrStreet.trim(), addrNumber.trim()).filter { it.isNotEmpty() }.joinToString(", "),
                            addrBairro.trim(),
                            listOf(addrCity.trim(), addrState.trim()).filter { it.isNotEmpty() }.joinToString(" - ")
                        ).filter { it.isNotEmpty() }.joinToString(" • ")
                        CarameloListRow(
                            icon = Icons.Rounded.Home,
                            title = "Endereço de cadastro",
                            subtitle = savedAddressText.ifEmpty { "Nenhum endereço cadastrado" },
                            trailing = null
                        )

                        CarameloTextField(
                            value = addrStreet,
                            onValueChange = { addrStreet = it },
                            label = "Rua",
                            leadingIcon = Icons.Rounded.Place
                        )

                        CarameloTextField(
                            value = addrNumber,
                            onValueChange = { addrNumber = it },
                            label = "Número"
                        )

                        CarameloTextField(
                            value = addrBairro,
                            onValueChange = { addrBairro = it },
                            label = "Bairro"
                        )

                        CarameloTextField(
                            value = addrCep,
                            onValueChange = { addrCep = it },
                            label = "CEP",
                            placeholder = "ex: 01310-100"
                        )

                        CarameloTextField(
                            value = addrCity,
                            onValueChange = { addrCity = it },
                            label = "Cidade"
                        )

                        CarameloTextField(
                            value = addrState,
                            onValueChange = { addrState = it.take(2).uppercase() },
                            label = "UF",
                            placeholder = "ex: SP"
                        )

                        CarameloPillButton(
                            text = "Salvar endereço",
                            onClick = {
                                CarameloRepository.saveUserProfile(
                                    context = context,
                                    name = CarameloRepository.getUserName(context),
                                    email = CarameloRepository.getUserEmail(context),
                                    logradouro = addrStreet.trim(),
                                    number = addrNumber.trim(),
                                    bairro = addrBairro.trim(),
                                    complement = CarameloRepository.getUserComplement(context),
                                    cep = addrCep.trim(),
                                    city = addrCity.trim(),
                                    state = addrState.trim()
                                )
                                onToast("Endereço salvo localmente com sucesso!")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            style = PillStyle.Primary
                        )
                    }

                    // 3. PREFERÊNCIA DE MOTORISTA PADRÃO / FIXA
                    CarameloSectionLabel("Motorista preferido")
                    var isFixedInSettings by rememberSaveable { mutableStateOf(CarameloRepository.isFixedPreferenceEnabled(context)) }
                    CarameloCard {
                        Text(
                            text = "Perfil de motorista preferido",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val options = listOf(
                            "Geral" to Icons.Rounded.Groups,
                            "LGBTQIA+" to Icons.Rounded.Diversity3,
                            "Mulher" to Icons.Rounded.Woman,
                            "Homem" to Icons.Rounded.Man
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            options.forEach { (option, optionIcon) ->
                                CarameloChip(
                                    label = option,
                                    selected = defaultDriverPref == option,
                                    icon = optionIcon,
                                    onClick = {
                                        defaultDriverPref = option
                                        CarameloRepository.saveDefaultDriverPreference(context, option)
                                        onToast("Preferência padrão salva: $option")
                                    }
                                )
                            }
                        }
                        CarameloDivider(startIndent = 0.dp)
                        CarameloSwitchRow(
                            icon = Icons.Rounded.CheckCircle,
                            title = "Preferência fixa",
                            subtitle = "Manter em todas as viagens",
                            checked = isFixedInSettings,
                            onCheckedChange = {
                                isFixedInSettings = it
                                CarameloRepository.saveFixedPreferenceEnabled(context, it)
                            }
                        )
                    }

                    // 4. PRECIFICAÇÃO E INCREMENTO DOS BOTÕES (+ / -)
                    CarameloSectionLabel("Valor dos botões + / −")
                    var priceStep by rememberSaveable { mutableStateOf(CarameloRepository.getPriceIncrementStep(context)) }
                    CarameloCard {
                        Text(
                            text = "Quanto cada toque em + ou − soma ou tira do valor da oferta",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val steps = listOf("R$ 0,50", "R$ 1,00", "R$ 2,00", "R$ 5,00")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            steps.forEach { step ->
                                CarameloChip(
                                    label = step,
                                    selected = priceStep == step,
                                    onClick = {
                                        priceStep = step
                                        CarameloRepository.savePriceIncrementStep(context, step)
                                        onToast("Valor dos botões definido para $step")
                                    }
                                )
                            }
                        }
                    }

                    // 5. NOTIFICAÇÕES E SEGURANÇA
                    CarameloSectionLabel("Notificações e segurança")
                    CarameloCard(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp)) {
                        CarameloSwitchRow(
                            icon = Icons.Rounded.Notifications,
                            title = "Notificações de corrida",
                            subtitle = "Avisos sobre o motorista e a viagem",
                            checked = notificationsEnabled,
                            onCheckedChange = {
                                notificationsEnabled = it
                                CarameloRepository.setNotificationsEnabled(context, it)
                            }
                        )
                        CarameloDivider()
                        CarameloSwitchRow(
                            icon = Icons.Rounded.Share,
                            title = "Compartilhar rota",
                            subtitle = "Em tempo real com contatos",
                            checked = shareRouteSafety,
                            onCheckedChange = {
                                shareRouteSafety = it
                                CarameloRepository.setShareRouteEnabled(context, it)
                            }
                        )
                        CarameloDivider()
                        CarameloListRow(
                            icon = Icons.Rounded.Block,
                            title = "Motoristas bloqueados",
                            subtitle = if (blockedDrivers.isEmpty()) "Nenhum motorista bloqueado"
                            else blockedDrivers.joinToString(", ") { it.name },
                            onClick = { showDriverBlockSubscreen = true },
                            trailing = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .defaultMinSize(minWidth = 26.dp, minHeight = 26.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${blockedDrivers.size}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                    Icon(
                                        Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )
                    }

                    // 6. ATIVIDADE (HISTÓRICO DE VIAGENS)
                    CarameloSectionLabel("Atividade")
                    CarameloCard(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp)) {
                        CarameloListRow(
                            icon = Icons.Rounded.History,
                            title = "Histórico de viagens",
                            subtitle = "Consulte e revise suas corridas anteriores",
                            onClick = { showTripHistorySubscreen = true }
                        )
                    }

                    // 7. CADASTRO DE MOTORISTA PARCEIRO (CNH)
                    Surface(
                        onClick = {
                            // Já cadastrado: abre o status da verificação de segurança
                            val target = if (isDriverRegistered) SecurityCheckActivity::class.java else DriverRegisterActivity::class.java
                            val intent = android.content.Intent(context, target)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = if (isDriverRegistered) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary,
                        contentColor = if (isDriverRegistered) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                if (isDriverRegistered) Icons.Rounded.Badge else Icons.Rounded.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = if (isDriverRegistered) CarameloGreen else LocalContentColor.current
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = if (isDriverRegistered) "Motorista parceiro cadastrado" else "Seja motorista Caramelo",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = when {
                                        !isDriverRegistered -> "Cadastre sua CNH e passe pela verificação de segurança"
                                        isDriverVerified -> "Identidade e antecedentes verificados"
                                        else -> "Verificação de segurança em análise"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LocalContentColor.current.copy(alpha = 0.85f)
                                )
                                if (isDriverRegistered) {
                                    CarameloStatusPill(
                                        text = if (isDriverVerified) "Verificado" else "Em análise",
                                        dotColor = if (isDriverVerified) CarameloGreen else CarameloOrange,
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (isDriverRegistered) MaterialTheme.colorScheme.surfaceVariant
                                        else Color.White.copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = if (isDriverRegistered) "Ver cadastro de motorista" else "Cadastrar como motorista",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // SAIR DA CONTA
                    CarameloPillButton(
                        text = "Sair da conta",
                        onClick = {
                            CarameloRepository.setLoggedIn(context, false)
                            goToLogin()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        style = PillStyle.Outline,
                        icon = Icons.AutoMirrored.Rounded.Logout
                    )

                    // APAGAR MEUS DADOS
                    CarameloPillButton(
                        text = "Apagar meus dados",
                        onClick = { showDeleteDataDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        style = PillStyle.Outline,
                        icon = Icons.Rounded.DeleteForever
                    )

                    if (showDeleteDataDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDataDialog = false },
                            title = { Text("Apagar meus dados?") },
                            text = {
                                Text("Isso apaga do aparelho seu perfil, endereço, histórico, motoristas bloqueados e dados de motorista. Não dá para desfazer.")
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDeleteDataDialog = false
                                    CarameloRepository.clearAllUserData(context)
                                    ThemeState.darkMode = null
                                    goToLogin()
                                }) { Text("Apagar") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDataDialog = false }) { Text("Cancelar") }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
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
    var newName by rememberSaveable { mutableStateOf("") }
    var newReason by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CarameloTopBar(title = "Motoristas bloqueados", onBack = onBackToSettings)

        // Regras Automáticas
        CarameloSectionLabel("Regras de bloqueio automático")
        CarameloCard(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp)) {
            CarameloSwitchRow(
                icon = Icons.Rounded.Shield,
                title = "Nunca emparelhar com bloqueados",
                subtitle = "Motoristas da sua lista não aceitam suas corridas",
                checked = enforceNoBlockedMatch,
                onCheckedChange = onToggleEnforceNoMatch
            )
            CarameloDivider()
            CarameloSwitchRow(
                icon = Icons.Rounded.Star,
                title = "Evitar nota abaixo de 4,5 ★",
                subtitle = "Regra automática",
                checked = autoBlockLowRating,
                onCheckedChange = onToggleAutoBlock
            )
        }

        // Form para adicionar motorista a bloquear
        CarameloSectionLabel("Bloquear novo motorista")
        CarameloCard {
            CarameloTextField(
                value = newName,
                onValueChange = { newName = it },
                label = "Nome do motorista",
                leadingIcon = Icons.Rounded.Person
            )
            CarameloTextField(
                value = newReason,
                onValueChange = { newReason = it },
                label = "Motivo do bloqueio (opcional)"
            )
            CarameloPillButton(
                text = "Bloquear motorista",
                onClick = {
                    if (newName.isNotBlank()) {
                        onAddBlockedDriver(newName, newReason)
                        newName = ""
                        newReason = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                style = PillStyle.Dark,
                icon = Icons.Rounded.Block
            )
        }

        CarameloSectionLabel("Bloqueados agora (${blockedDrivers.size})")

        if (blockedDrivers.isEmpty()) {
            CarameloCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = CarameloGreen
                    )
                    Text(
                        text = "Nenhum motorista bloqueado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            CarameloCard(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp)) {
                blockedDrivers.forEachIndexed { index, driver ->
                    if (index > 0) CarameloDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Block,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = driver.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = driver.carInfo,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Motivo: ${driver.reason}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        TextButton(
                            onClick = { onRemoveBlockedDriver(driver) },
                            modifier = Modifier.heightIn(min = 44.dp)
                        ) {
                            Text(
                                text = "Desbloquear",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
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
