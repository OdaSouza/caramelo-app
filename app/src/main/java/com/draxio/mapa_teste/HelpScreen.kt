package com.draxio.mapa_teste

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.draxio.mapa_teste.ui.components.CarameloCard
import com.draxio.mapa_teste.ui.components.CarameloDivider
import com.draxio.mapa_teste.ui.components.CarameloListRow
import com.draxio.mapa_teste.ui.components.CarameloPillButton
import com.draxio.mapa_teste.ui.components.CarameloSectionLabel
import com.draxio.mapa_teste.ui.components.CarameloTopBar
import com.draxio.mapa_teste.ui.components.PillStyle
import com.draxio.mapa_teste.ui.theme.Mapa_testeTheme

// Perguntas frequentes (pergunta, resposta)
private val faq = listOf(
    "Como pedir uma corrida?" to "Toque em \"Para onde vamos?\", escolha o destino, a categoria e o valor que quer oferecer. Depois escolha uma das propostas dos motoristas.",
    "Como funciona a oferta de preço?" to "Você sugere o valor com os botões − e + ou com a barra. Os motoristas respondem com propostas e você aceita a que preferir.",
    "O que é o código de embarque (PIN)?" to "São 4 números que aparecem quando o motorista aceita. Diga o código ao motorista antes de entrar no carro: assim você confirma que é a corrida certa.",
    "Como funciona a verificação dos motoristas?" to "Todo motorista envia selfie, CNH com EAR, certidões de antecedentes criminais e dados do veículo. O selo \"Verificado\" mostra o que foi checado.",
    "Como apagar meus dados?" to "Em Configurações → Apagar meus dados. Tudo o que está guardado no aparelho é removido."
)

// Termos de Uso: (título, texto) — resumo fiel da minuta em docs/MINUTA-TERMOS-PRIVACIDADE.md
private val termsSections = listOf(
    "1. O que é o Caramelo" to "Plataforma de intermediação que conecta passageiros a motoristas parceiros independentes. O passageiro propõe um valor e escolhe entre as propostas. O Caramelo não presta o serviço de transporte.",
    "2. Cadastro" to "É preciso ter 18 anos ou mais e informar dados verdadeiros. Você é responsável pela sua senha. Contas com dados falsos podem ser suspensas.",
    "3. Motoristas parceiros" to "Para receber corridas, o motorista passa por verificação anual: CNH válida com EAR, selfie comparada com a CNH, certidões de antecedentes criminais federal e estadual e conferência do veículo.",
    "4. Preferência de motorista" to "Você pode preferir motoristas que se autodeclararam Mulher, Homem ou LGBTQIA+. A informação é autodeclarada e não é verificada por documento, e a preferência não garante motorista disponível no grupo.",
    "5. Preço, pagamento e cancelamento" to "O valor é combinado antes do embarque. Cancelamentos após o aceite podem ter regras informadas no app. A gorjeta é opcional e vai inteira para o motorista.",
    "6. Conduta e segurança" to "É proibido usar o app para fins ilícitos, assediar ou discriminar. O app oferece ligar para o 190, compartilhar a viagem e o código de embarque (PIN).",
    "7. Avaliações" to "Passageiros e motoristas podem avaliar a viagem. Avaliações abusivas podem ser removidas.",
    "8. Responsabilidade" to "O Caramelo responde nos limites da lei pela intermediação, sem afastar os direitos do Código de Defesa do Consumidor (Lei 8.078/1990). [Conferir a redação com o modelo de negócio definitivo.]",
    "9. Alterações e foro" to "Mudanças serão avisadas no app. Foro do domicílio do consumidor."
)

private val privacySections = listOf(
    "1. Quem trata os dados" to "Controlador: [RAZÃO SOCIAL], CNPJ [CNPJ], [ENDEREÇO]. Encarregado (DPO): [ENCARREGADO/DPO], [E-MAIL DE CONTATO]. (LGPD, art. 41 — conferir.)",
    "2. Dados coletados" to "• Nome, e-mail e endereço: criar a conta e o embarque (execução de contrato, art. 7º, V — conferir).\n" +
        "• Localização: mapa e ponto de embarque (execução de contrato).\n" +
        "• Histórico e avaliações: recibo, suporte e segurança (contrato e legítimo interesse, art. 7º, V e IX — conferir).\n" +
        "• Motorista — CNH e veículo: habilitação para a atividade (obrigação legal e contrato).\n" +
        "• Motorista — certidões de antecedentes: segurança dos passageiros (base legal a definir).\n" +
        "• Motorista — selfie, dado biométrico sensível: confirmar identidade (art. 11, I ou II, \"g\" — definir).\n" +
        "• Motorista — grupo autodeclarado, dado sensível: filtro de preferência (consentimento destacado, art. 11, I — conferir).",
    "3. Compartilhamento" to "O passageiro vê nome, veículo, placa e selo do motorista; o motorista vê o nome do passageiro e o embarque. Os dados não são vendidos. Podem ser fornecidos a autoridades mediante ordem judicial (Marco Civil da Internet — conferir).",
    "4. Retenção" to "Enquanto a conta existir e pelos prazos legais (registros de acesso: 6 meses, Marco Civil, art. 15 — conferir). Certidões e selfie são descartadas após a verificação; fica o resultado e a data. [Definir.]",
    "5. Seus direitos (LGPD, art. 18 — conferir)" to "Confirmação e acesso, correção, anonimização ou eliminação, portabilidade, informação sobre compartilhamento e revogação do consentimento. No app: Configurações → Apagar meus dados, ou escreva para [E-MAIL DE CONTATO].",
    "6. Segurança" to "Dados de CNH e veículo ficam criptografados no aparelho, e a cópia de segurança automática do Android está desativada para o app.",
    "7. Crianças e adolescentes" to "O app não se destina a menores de 18 anos.",
    "8. Contato" to "[E-MAIL DE CONTATO]. Reclamações também podem ser feitas à ANPD."
)

private fun openSafely(context: Context, intent: Intent, failMessage: String) {
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, failMessage, Toast.LENGTH_SHORT).show()
    }
}

/** Central de Ajuda: perguntas frequentes, relatar problema, segurança, termos e privacidade. */
@Composable
fun HelpScreen(onClose: () -> Unit) {
    var page by rememberSaveable { mutableStateOf("menu") }

    // Voltar do sistema: de uma subtela volta ao menu; do menu fecha a Ajuda
    BackHandler { if (page == "menu") onClose() else page = "menu" }

    when (page) {
        "termos" -> LegalTextPage("Termos de Uso", termsSections) { page = "menu" }
        "privacidade" -> LegalTextPage("Política de Privacidade", privacySections) { page = "menu" }
        else -> HelpMenu(
            onClose = onClose,
            onOpenTerms = { page = "termos" },
            onOpenPrivacy = { page = "privacidade" }
        )
    }
}

@Composable
private fun HelpMenu(onClose: () -> Unit, onOpenTerms: () -> Unit, onOpenPrivacy: () -> Unit) {
    val context = LocalContext.current
    var faqOpen by rememberSaveable { mutableStateOf(false) }
    var safetyOpen by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CarameloTopBar("Ajuda", onBack = onClose)

        CarameloCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
            CarameloListRow(
                icon = Icons.AutoMirrored.Rounded.HelpOutline,
                title = "Perguntas frequentes",
                subtitle = "Corrida, preço, PIN, verificação e dados",
                onClick = { faqOpen = !faqOpen },
                trailing = {
                    Icon(
                        if (faqOpen) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = if (faqOpen) "Recolher" else "Expandir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            if (faqOpen) {
                Column(
                    modifier = Modifier.padding(start = 52.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    faq.forEach { (q, a) ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(q, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                            Text(a, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            CarameloDivider()
            CarameloListRow(
                icon = Icons.Rounded.Email,
                title = "Relatar um problema",
                subtitle = "Abre seu app de e-mail",
                onClick = {
                    val mail = Intent(Intent.ACTION_SENDTO, "mailto:".toUri())
                        .putExtra(Intent.EXTRA_SUBJECT, "Caramelo: problema no app")
                    openSafely(context, mail, "Nenhum app de e-mail encontrado")
                }
            )
            CarameloDivider()
            CarameloListRow(
                icon = Icons.Rounded.Shield,
                title = "Segurança",
                subtitle = "190, compartilhar viagem, PIN e verificação",
                onClick = { safetyOpen = !safetyOpen },
                trailing = {
                    Icon(
                        if (safetyOpen) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = if (safetyOpen) "Recolher" else "Expandir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            if (safetyOpen) {
                Column(
                    modifier = Modifier.padding(start = 52.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Em emergência, ligue 190. Durante a corrida, use Segurança para compartilhar a viagem com quem você confia. " +
                            "Confira o código de embarque (PIN) antes de entrar e veja no selo \"Verificado\" o que foi checado no motorista.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CarameloPillButton(
                        text = "Ligar 190",
                        icon = Icons.Rounded.Phone,
                        onClick = { openSafely(context, Intent(Intent.ACTION_DIAL, "tel:190".toUri()), "Nenhum app de telefone encontrado. Ligue 190.") },
                        modifier = Modifier.fillMaxWidth(),
                        style = PillStyle.Dark
                    )
                }
            }
            CarameloDivider()
            CarameloListRow(
                icon = Icons.Rounded.Description,
                title = "Termos de Uso",
                onClick = onOpenTerms
            )
            CarameloDivider()
            CarameloListRow(
                icon = Icons.Rounded.Lock,
                title = "Política de Privacidade",
                onClick = onOpenPrivacy
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

// Texto jurídico em seções, com aviso de minuta no topo
@Composable
private fun LegalTextPage(title: String, sections: List<Pair<String, String>>, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CarameloTopBar(title, onBack = onBack)

        CarameloCard(color = MaterialTheme.colorScheme.primaryContainer) {
            Text(
                "MINUTA — texto em revisão jurídica. Versão de demonstração: corridas, pagamentos e verificações são simulados e nada sai deste aparelho.",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        CarameloCard {
            sections.forEachIndexed { i, (heading, body) ->
                if (i > 0) Spacer(Modifier.height(6.dp))
                CarameloSectionLabel(heading)
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun HelpScreenPreview() {
    Mapa_testeTheme {
        HelpScreen(onClose = {})
    }
}
