package com.draxio.mapa_teste

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.draxio.mapa_teste.ui.components.CarameloCard
import com.draxio.mapa_teste.ui.components.CarameloPillButton
import com.draxio.mapa_teste.ui.components.CarameloScreenTitle
import com.draxio.mapa_teste.ui.components.CarameloSectionLabel
import com.draxio.mapa_teste.ui.components.CarameloSegmentedProgress
import com.draxio.mapa_teste.ui.components.CarameloTextField
import com.draxio.mapa_teste.ui.components.CarameloTopBar
import com.draxio.mapa_teste.ui.components.PillStyle
import com.draxio.mapa_teste.ui.theme.*

class DriverRegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Mapa_testeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DriverRegisterScreen(
                        onProceedToCnhPhoto = { name, email, phone, cnhNumber, cnhCategory, cnhValidity ->
                            try {
                                val sharedPreferences = securePrefs(this, "secure_driver_prefs")

                                sharedPreferences.edit().apply {
                                    putString("driver_name", name)
                                    putString("driver_email", email)
                                    putString("driver_phone", phone)
                                    putString("driver_cnh_number", cnhNumber)
                                    putString("driver_cnh_category", cnhCategory)
                                    putString("driver_cnh_validity", cnhValidity)
                                    apply()
                                }

                                Toast.makeText(this, "Dados do motorista salvos! Tire a foto da CNH.", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, CnhPhotoActivity::class.java)
                                startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(this, "Erro ao salvar dados: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
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

// security-crypto foi descontinuado pelo AndroidX; migrar para DataStore + Tink quando houver backend
@Suppress("DEPRECATION")
private fun securePrefs(context: Context, name: String): SharedPreferences {
    val masterKey = androidx.security.crypto.MasterKey.Builder(context)
        .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
        .build()
    return androidx.security.crypto.EncryptedSharedPreferences.create(
        context,
        name,
        masterKey,
        androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

@Composable
fun DriverRegisterScreen(
    onProceedToCnhPhoto: (String, String, String, String, String, String) -> Unit,
    onBack: () -> Unit,
    onShowError: (String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var confirmEmail by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }

    // Dados da CNH
    var cnhNumber by rememberSaveable { mutableStateOf("") }
    var cnhCategory by rememberSaveable { mutableStateOf("B") }
    var cnhValidity by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        CarameloTopBar(title = "Cadastro de motorista", onBack = onBack)

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Indicador de etapa
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CarameloSegmentedProgress(progress = 1f / 5f, segments = 5, height = 8.dp)
                Text(
                    text = "Passo 1 de 5",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            CarameloScreenTitle(title = "Seus dados e sua CNH")

            // 1. DADOS PESSOAIS DO MOTORISTA
            CarameloSectionLabel(text = "Dados do motorista")
            CarameloCard {
                CarameloTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nome completo",
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                CarameloTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "E-mail",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                CarameloTextField(
                    value = confirmEmail,
                    onValueChange = { confirmEmail = it },
                    label = "Confirmar e-mail",
                    isError = confirmEmail.isNotEmpty() && confirmEmail != email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                CarameloTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Telefone / celular",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }

            // 2. DADOS DA CNH
            CarameloSectionLabel(text = "Carteira de habilitação (CNH)")
            CarameloCard {
                CarameloTextField(
                    value = cnhNumber,
                    onValueChange = { cnhNumber = it },
                    label = "Número do registro da CNH",
                    leadingIcon = Icons.Rounded.Badge,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CarameloTextField(
                        value = cnhCategory,
                        onValueChange = { cnhCategory = it },
                        label = "Categoria",
                        placeholder = "ex: B, AB",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                    )
                    CarameloTextField(
                        value = cnhValidity,
                        onValueChange = { cnhValidity = it },
                        label = "Validade",
                        placeholder = "MM/AAAA",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botao Avancar para Foto da CNH
        CarameloPillButton(
            text = "Tirar foto da CNH",
            icon = Icons.Rounded.PhotoCamera,
            onClick = {
                if (name.isBlank() || email.isBlank() || confirmEmail.isBlank() || phone.isBlank()) {
                    onShowError("Por favor, preencha nome, e-mail e telefone.")
                } else if (email != confirmEmail) {
                    onShowError("Os e-mails informados não coincidem.")
                } else if (cnhNumber.isBlank() || cnhValidity.isBlank()) {
                    onShowError("Por favor, informe o número e a validade da CNH.")
                } else {
                    onProceedToCnhPhoto(name, email, phone, cnhNumber, cnhCategory, cnhValidity)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            style = PillStyle.Primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        CarameloPillButton(
            text = "Voltar",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            style = PillStyle.Outline
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DriverRegisterScreenPreview() {
    Mapa_testeTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            DriverRegisterScreen(
                onProceedToCnhPhoto = { _, _, _, _, _, _ -> },
                onBack = {},
                onShowError = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DriverRegisterScreenDarkPreview() {
    Mapa_testeTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            DriverRegisterScreen(
                onProceedToCnhPhoto = { _, _, _, _, _, _ -> },
                onBack = {},
                onShowError = {}
            )
        }
    }
}
