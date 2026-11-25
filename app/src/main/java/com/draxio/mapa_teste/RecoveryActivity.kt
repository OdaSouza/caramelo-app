package com.draxio.mapa_teste

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.draxio.mapa_teste.ui.components.CarameloCard
import com.draxio.mapa_teste.ui.components.CarameloPillButton
import com.draxio.mapa_teste.ui.components.CarameloScreenTitle
import com.draxio.mapa_teste.ui.components.CarameloTextField
import com.draxio.mapa_teste.ui.components.CarameloTopBar
import com.draxio.mapa_teste.ui.components.PillStyle
import com.draxio.mapa_teste.ui.theme.*

class RecoveryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Mapa_testeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RecoveryScreen(
                        onRecoverSent = { _ ->
                            // Sem servidor: nada é enviado de verdade
                            Toast.makeText(this, "Modo demonstração: sem servidor, nenhum e-mail foi enviado.", Toast.LENGTH_LONG).show()
                            finish()
                        },
                        onBack = { finish() },
                        onShowError = { msg ->
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RecoveryScreen(
    onRecoverSent: (String) -> Unit,
    onBack: () -> Unit,
    onShowError: (String) -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CarameloTopBar(title = "", onBack = onBack)

            CarameloScreenTitle(title = "Recuperar conta")

            Text(
                text = "Digite seu e-mail cadastrado para receber as instruções de redefinição de senha.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CarameloCard {
                CarameloTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "E-mail de recuperação",
                    placeholder = "voce@email.com",
                    leadingIcon = Icons.Rounded.Email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }

            CarameloPillButton(
                text = "Enviar instruções",
                onClick = {
                    if (email.isNotBlank()) {
                        onRecoverSent(email)
                    } else {
                        onShowError("Por favor, digite seu e-mail.")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                style = PillStyle.Primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lembrou a senha?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.heightIn(min = 44.dp)
                ) {
                    Text(
                        text = "Voltar ao login",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecoveryScreenPreview() {
    Mapa_testeTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            RecoveryScreen(
                onRecoverSent = {},
                onBack = {},
                onShowError = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecoveryScreenDarkPreview() {
    Mapa_testeTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            RecoveryScreen(
                onRecoverSent = {},
                onBack = {},
                onShowError = {}
            )
        }
    }
}
