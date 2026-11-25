package com.draxio.mapa_teste

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.core.graphics.scale
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Diversity3
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Man
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Woman
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.draxio.mapa_teste.ui.components.*
import com.draxio.mapa_teste.ui.theme.*

/**
 * Passo 5 do cadastro de motorista: checagem de segurança no modelo dos apps de corrida.
 * Selfie x CNH, certidões de antecedentes criminais, CNH com EAR, grupo autodeclarado
 * para o filtro de preferência e consentimento. Sem servidor ainda: o envio só muda o
 * status local para "em análise". A análise real precisa ser feita pelo backend.
 */
class SecurityCheckActivity : ComponentActivity() {

    private var selfie by mutableStateOf<Bitmap?>(null)
    private var federalCertName by mutableStateOf<String?>(null)
    private var stateCertName by mutableStateOf<String?>(null)
    private var status by mutableStateOf(CarameloRepository.VERIFICATION_NOT_SENT)

    private val selfieLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) selfie = bitmap
        else Toast.makeText(this, "Selfie cancelada.", Toast.LENGTH_SHORT).show()
    }

    private val federalCertLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) federalCertName = displayName(uri) }

    private val stateCertLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) stateCertName = displayName(uri) }

    private fun displayName(uri: Uri): String {
        return try {
            contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            } ?: "Arquivo enviado"
        } catch (_: Exception) {
            "Arquivo enviado"
        }
    }

    // Aparelho sem app de câmera ou de arquivos: avisa em vez de fechar o app
    private fun safeLaunch(launch: () -> Unit) {
        try {
            launch()
        } catch (_: android.content.ActivityNotFoundException) {
            Toast.makeText(this, "Nenhum app disponível para esta ação.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToMap() {
        startActivity(Intent(this, Mapao_do_Oda::class.java))
        finishAffinity()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // rotação: preserva a foto já tirada e os nomes das certidões
        outState.putString("federal_cert_name", federalCertName)
        outState.putString("state_cert_name", stateCertName)
        selfie?.let { bmp ->
            bitmapToSmallJpeg(bmp)?.let { outState.putByteArray("selfie_jpeg", it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState != null) {
            federalCertName = savedInstanceState.getString("federal_cert_name")
            stateCertName = savedInstanceState.getString("state_cert_name")
            if (selfie == null) selfie = jpegToBitmap(savedInstanceState.getByteArray("selfie_jpeg"))
        }
        status =CarameloRepository.getDriverVerificationStatus(this)
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val certMimeTypes = arrayOf("application/pdf", "image/*")

        setContent {
            Mapa_testeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (status == CarameloRepository.VERIFICATION_NOT_SENT) {
                        SecurityCheckScreen(
                            selfie = selfie,
                            federalCertName = federalCertName,
                            stateCertName = stateCertName,
                            onBack = { finish() },
                            onTakeSelfie = { safeLaunch { selfieLauncher.launch(null) } },
                            onPickFederalCert = { safeLaunch { federalCertLauncher.launch(certMimeTypes) } },
                            onPickStateCert = { safeLaunch { stateCertLauncher.launch(certMimeTypes) } },
                            onSubmit = { group ->
                                CarameloRepository.setDriverDeclaredGroup(this, group)
                                CarameloRepository.setDriverVerificationStatus(this, CarameloRepository.VERIFICATION_IN_REVIEW)
                                CarameloRepository.setDriverRegistered(this, true)
                                status = CarameloRepository.VERIFICATION_IN_REVIEW
                                Toast.makeText(this, "Documentos enviados para análise!", Toast.LENGTH_LONG).show()
                            },
                            onShowError = { message ->
                                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        SecurityStatusScreen(
                            status = status,
                            declaredGroup = CarameloRepository.getDriverDeclaredGroup(this),
                            onBackToMap = { goToMap() },
                            // Só aparece em build de teste: o app ainda não tem servidor para aprovar.
                            onSimulateApproval = if (isDebuggable) {
                                {
                                    CarameloRepository.setDriverVerificationStatus(this, CarameloRepository.VERIFICATION_APPROVED)
                                    status = CarameloRepository.VERIFICATION_APPROVED
                                }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

private val declaredGroups = listOf("Geral", "Mulher", "Homem", "LGBTQIA+")

private fun declaredGroupLabel(group: String): String = if (group == "Geral") "Não informar" else group

private fun declaredGroupIcon(group: String): ImageVector = when (group) {
    "LGBTQIA+" -> Icons.Rounded.Diversity3
    "Mulher" -> Icons.Rounded.Woman
    "Homem" -> Icons.Rounded.Man
    else -> Icons.Rounded.Person
}

@Composable
fun SecurityCheckScreen(
    selfie: Bitmap?,
    federalCertName: String?,
    stateCertName: String?,
    onBack: () -> Unit,
    onTakeSelfie: () -> Unit,
    onPickFederalCert: () -> Unit,
    onPickStateCert: () -> Unit,
    onSubmit: (declaredGroup: String) -> Unit,
    onShowError: (String) -> Unit
) {
    var hasEar by rememberSaveable { mutableStateOf(false) }
    var consent by rememberSaveable { mutableStateOf(false) }
    var groupIndex by rememberSaveable { mutableIntStateOf(0) }

    val missing = buildList {
        if (selfie == null) add("selfie")
        if (federalCertName == null) add("certidão federal")
        if (stateCertName == null) add("certidão estadual")
        if (!hasEar) add("confirmação da EAR")
        if (!consent) add("autorização")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CarameloTopBar(title = "Verificação de segurança", onBack = onBack)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CarameloSegmentedProgress(progress = 5f / 5f, segments = 5, height = 8.dp)
                Text(
                    text = "Passo 5 de 5",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            CarameloScreenTitle(title = "Checagem de segurança", overline = "Igual para todos os motoristas")

            Text(
                text = "Passageiros só recebem propostas de motoristas verificados. Envie os itens abaixo: a equipe Caramelo confere tudo antes de liberar suas viagens.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Documentos
            CarameloCard {
                VerificationItem(
                    icon = Icons.Rounded.Face,
                    title = "Selfie de confirmação",
                    subtitle = if (selfie == null) "Comparamos seu rosto com a foto da CNH" else "Selfie capturada",
                    done = selfie != null,
                    actionLabel = if (selfie == null) "Tirar" else "Refazer",
                    onAction = onTakeSelfie,
                    thumbnail = selfie
                )
                CarameloDivider()
                VerificationItem(
                    icon = Icons.Rounded.Gavel,
                    title = "Antecedentes criminais · Federal",
                    subtitle = federalCertName ?: "Certidão da Polícia Federal, emitida grátis no gov.br (PDF ou foto)",
                    done = federalCertName != null,
                    actionLabel = if (federalCertName == null) "Enviar" else "Trocar",
                    onAction = onPickFederalCert
                )
                CarameloDivider()
                VerificationItem(
                    icon = Icons.Rounded.Gavel,
                    title = "Antecedentes criminais · Estadual",
                    subtitle = stateCertName ?: "Certidão do seu estado: Secretaria de Segurança Pública ou Tribunal de Justiça",
                    done = stateCertName != null,
                    actionLabel = if (stateCertName == null) "Enviar" else "Trocar",
                    onAction = onPickStateCert
                )
            }

            CarameloCard {
                CheckRow(
                    checked = hasEar,
                    onCheckedChange = { hasEar = it },
                    icon = Icons.Rounded.Badge,
                    title = "Minha CNH tem a observação EAR",
                    subtitle = "Exerce Atividade Remunerada: exigida para transporte pago de passageiros"
                )
            }

            // Grupo autodeclarado para o filtro de preferência do passageiro
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CarameloSectionLabel(text = "Grupo no filtro de preferência (opcional)")
                Text(
                    text = "Passageiros podem escolher motoristas Mulher, Homem ou LGBTQIA+. Para aparecer nesses filtros, escolha seu grupo. É autodeclaração e pode ser mudada depois.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    declaredGroups.forEachIndexed { idx, group ->
                        CarameloChip(
                            label = declaredGroupLabel(group),
                            selected = idx == groupIndex,
                            onClick = { groupIndex = idx },
                            icon = declaredGroupIcon(group)
                        )
                    }
                }
            }

            // Consentimento (LGPD): finalidade, dado biométrico e grupo informado
            CarameloCard(color = MaterialTheme.colorScheme.surfaceVariant) {
                CheckRow(
                    checked = consent,
                    onCheckedChange = { consent = it },
                    icon = Icons.Rounded.VerifiedUser,
                    title = "Autorizo a checagem",
                    subtitle = "Autorizo o Caramelo a consultar meus antecedentes criminais, comparar minha selfie com a CNH (dado biométrico) e, se eu informar, usar meu grupo no filtro de preferência. Uso exclusivo para a segurança das viagens. A checagem é refeita todo ano."
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        CarameloPillButton(
            text = "Enviar para análise",
            icon = Icons.Rounded.VerifiedUser,
            onClick = {
                if (missing.isEmpty()) {
                    onSubmit(declaredGroups[groupIndex])
                } else {
                    onShowError("Falta: ${missing.joinToString(", ")}.")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            style = if (missing.isEmpty()) PillStyle.Primary else PillStyle.Outline
        )
    }
}

@Composable
fun SecurityStatusScreen(
    status: String,
    declaredGroup: String,
    onBackToMap: () -> Unit,
    onSimulateApproval: (() -> Unit)?
) {
    val approved = status == CarameloRepository.VERIFICATION_APPROVED

    // Voltar do sistema leva ao mapa e tira os passos 1 a 4 da pilha
    BackHandler { onBackToMap() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            CarameloTopBar(title = "Verificação de segurança", onBack = onBackToMap)

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(
                            if (approved) CarameloGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (approved) Icons.Rounded.VerifiedUser else Icons.Rounded.HourglassTop,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = if (approved) CarameloGreen else MaterialTheme.colorScheme.primary
                    )
                }
                CarameloStatusPill(
                    text = if (approved) "Verificado" else "Em análise",
                    dotColor = if (approved) CarameloGreen else CarameloOrange
                )
                Text(
                    text = if (approved) "Você está verificado" else "Recebemos seus documentos",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (approved)
                        "Seu selo de verificado já aparece para os passageiros nas suas propostas."
                    else
                        "A equipe Caramelo está conferindo sua identidade e seus antecedentes. Você recebe um aviso quando terminar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            CarameloCard {
                TimelineRow(title = "Documentos enviados", state = StepState.Done)
                TimelineRow(
                    title = "Checagem de identidade e antecedentes",
                    state = if (approved) StepState.Done else StepState.Current
                )
                TimelineRow(
                    title = "Liberado para dirigir",
                    state = if (approved) StepState.Done else StepState.Pending
                )
            }

            CarameloCard(color = MaterialTheme.colorScheme.surfaceVariant) {
                CarameloListRow(
                    icon = declaredGroupIcon(declaredGroup),
                    title = "Grupo no filtro: ${declaredGroupLabel(declaredGroup)}",
                    subtitle = "Autodeclarado. A checagem é refeita todo ano.",
                    trailing = null
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (!approved && onSimulateApproval != null) {
            CarameloPillButton(
                text = "Simular aprovação (só em teste)",
                onClick = onSimulateApproval,
                modifier = Modifier.fillMaxWidth(),
                style = PillStyle.Outline
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        CarameloPillButton(
            text = "Voltar ao mapa",
            onClick = onBackToMap,
            modifier = Modifier.fillMaxWidth(),
            style = PillStyle.Primary
        )
    }
}

// Linha de item da checagem: ícone (ou miniatura), texto e botão de ação.
@Composable
private fun VerificationItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    done: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
    thumbnail: Bitmap? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (done) CarameloGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            if (thumbnail != null) {
                Image(
                    bitmap = thumbnail.asImageBitmap(),
                    contentDescription = "Selfie capturada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = if (done) Icons.Rounded.CheckCircle else icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = if (done) CarameloGreen else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TextButton(onClick = onAction, modifier = Modifier.heightIn(min = 44.dp)) {
            Text(actionLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

// Caixa de seleção com texto, a linha inteira é clicável.
@Composable
private fun CheckRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, onValueChange = onCheckedChange, role = Role.Checkbox)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(22.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    }
}

private enum class StepState { Done, Current, Pending }

@Composable
private fun TimelineRow(title: String, state: StepState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = when (state) {
                StepState.Done -> Icons.Rounded.CheckCircle
                StepState.Current -> Icons.Rounded.HourglassTop
                StepState.Pending -> Icons.Rounded.RadioButtonUnchecked
            },
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = when (state) {
                StepState.Done -> CarameloGreen
                StepState.Current -> CarameloOrange
                StepState.Pending -> MaterialTheme.colorScheme.outline
            }
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (state == StepState.Pending) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SecurityCheckScreenPreview() {
    Mapa_testeTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            SecurityCheckScreen(
                selfie = null,
                federalCertName = "certidao-pf.pdf",
                stateCertName = null,
                onBack = {},
                onTakeSelfie = {},
                onPickFederalCert = {},
                onPickStateCert = {},
                onSubmit = {},
                onShowError = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SecurityStatusScreenPreview() {
    Mapa_testeTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            SecurityStatusScreen(
                status = CarameloRepository.VERIFICATION_IN_REVIEW,
                declaredGroup = "Mulher",
                onBackToMap = {},
                onSimulateApproval = {}
            )
        }
    }
}

// rotação: preserva a foto já tirada (versão reduzida em JPEG, até ~200 KB, para caber no Bundle)
private fun bitmapToSmallJpeg(source: Bitmap, maxBytes: Int = 200 * 1024): ByteArray? {
    return try {
        var current = source
        val longSide = maxOf(current.width, current.height)
        if (longSide > 1024) {
            val scale = 1024f / longSide
            current = current.scale((current.width * scale).toInt().coerceAtLeast(1), (current.height * scale).toInt().coerceAtLeast(1))
        }
        var bytes = ByteArray(0)
        repeat(5) {
            val stream = java.io.ByteArrayOutputStream()
            current.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            bytes = stream.toByteArray()
            if (bytes.size <= maxBytes) return bytes
            current = current.scale((current.width * 0.75f).toInt().coerceAtLeast(1), (current.height * 0.75f).toInt().coerceAtLeast(1))
        }
        bytes
    } catch (_: Exception) {
        null
    }
}

private fun jpegToBitmap(bytes: ByteArray?): Bitmap? =
    if (bytes == null) null else try {
        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (_: Exception) {
        null
    }
