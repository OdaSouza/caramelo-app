package com.draxio.mapa_teste

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.draxio.mapa_teste.ui.components.CarameloCard
import com.draxio.mapa_teste.ui.components.CarameloListRow
import com.draxio.mapa_teste.ui.components.CarameloPillButton
import com.draxio.mapa_teste.ui.components.CarameloSectionLabel
import com.draxio.mapa_teste.ui.components.CarameloStatusPill
import com.draxio.mapa_teste.ui.components.CarameloTextField
import com.draxio.mapa_teste.ui.components.CarameloTopBar
import com.draxio.mapa_teste.ui.components.PillStyle
import com.draxio.mapa_teste.ui.theme.Mapa_testeTheme

private data class PayOption(val method: String, val icon: ImageVector, val subtitle: String)

private val payOptions = listOf(
    PayOption(CarameloRepository.PAY_PIX, Icons.Rounded.QrCode, "Pague pelo app do seu banco ao final"),
    PayOption(CarameloRepository.PAY_CASH, Icons.Rounded.Payments, "Pague direto ao motorista"),
    PayOption(CarameloRepository.PAY_CARD, Icons.Rounded.CreditCard, "Cartão de demonstração, sem cobrança")
)

/** Tela sobreposta da Carteira: forma de pagamento e cupom (modo demonstração). */
@Composable
fun WalletScreen(onClose: () -> Unit, onToast: (String) -> Unit) {
    val context = LocalContext.current
    val preview = LocalInspectionMode.current

    var method by rememberSaveable {
        mutableStateOf(if (preview) CarameloRepository.PAY_PIX else CarameloRepository.getPaymentMethod(context))
    }
    var code by rememberSaveable { mutableStateOf("") }
    var invalid by rememberSaveable { mutableStateOf(false) }
    var activeCoupon by rememberSaveable {
        mutableStateOf(if (preview) "" else CarameloRepository.getActiveCoupon(context).orEmpty())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CarameloTopBar("Carteira", onBack = onClose)

        CarameloCard(color = MaterialTheme.colorScheme.surfaceVariant) {
            Text(
                "Modo demonstração: nenhum pagamento é cobrado e nenhum dado de cartão é pedido.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        CarameloSectionLabel("Forma de pagamento")
        payOptions.forEach { option ->
            val selected = method == option.method
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    if (selected) 2.dp else 1.dp,
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                CarameloListRow(
                    icon = option.icon,
                    title = option.method,
                    subtitle = option.subtitle,
                    highlightIcon = selected,
                    modifier = Modifier.padding(horizontal = 14.dp),
                    onClick = {
                        method = option.method
                        if (!preview) CarameloRepository.setPaymentMethod(context, option.method)
                    },
                    trailing = {
                        if (selected) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = "Selecionado",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        }

        CarameloSectionLabel("Cupom")
        CarameloCard {
            if (activeCoupon.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CarameloStatusPill(text = activeCoupon, dark = true)
                    CarameloPillButton(
                        text = "Remover",
                        style = PillStyle.Outline,
                        onClick = {
                            CarameloRepository.clearCoupon(context)
                            activeCoupon = ""
                        }
                    )
                }
            } else {
                CarameloTextField(
                    value = code,
                    onValueChange = {
                        code = it
                        invalid = false
                    },
                    label = "Código do cupom",
                    placeholder = "Digite o código",
                    leadingIcon = Icons.Rounded.LocalOffer,
                    isError = invalid,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                )
                if (invalid) {
                    Text(
                        "Cupom inválido",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                CarameloPillButton(
                    text = "Aplicar",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (CarameloRepository.applyCoupon(context, code)) {
                            invalid = false
                            code = ""
                            activeCoupon = CarameloRepository.getActiveCoupon(context).orEmpty()
                            onToast("Cupom aplicado: 10% de desconto, até R$ 10")
                        } else {
                            invalid = true
                        }
                    }
                )
            }
            Text(
                "Teste com o cupom ${CarameloRepository.DEMO_COUPON}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun WalletScreenPreview() {
    Mapa_testeTheme {
        WalletScreen(onClose = {}, onToast = {})
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun WalletScreenDarkPreview() {
    Mapa_testeTheme(darkTheme = true) {
        WalletScreen(onClose = {}, onToast = {})
    }
}
