package com.draxio.mapa_teste

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
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
                                val masterKey = MasterKey.Builder(this)
                                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                                    .build()

                                val sharedPreferences = EncryptedSharedPreferences.create(
                                    this,
                                    "secure_driver_prefs",
                                    masterKey,
                                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                                )

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

@Composable
fun DriverRegisterScreen(
    onProceedToCnhPhoto: (String, String, String, String, String, String) -> Unit,
    onBack: () -> Unit,
    onShowError: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var confirmEmail by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Dados da CNH
    var cnhNumber by remember { mutableStateOf("") }
    var cnhCategory by remember { mutableStateOf("B") }
    var cnhValidity by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CarameloLogo()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Cadastro de Motorista",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 1. DADOS PESSOAIS DO MOTORISTA
            Text(
                text = "— DADOS DO MOTORISTA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome Completo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = confirmEmail,
                onValueChange = { confirmEmail = it },
                label = { Text("Confirmar E-mail") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Telefone / Celular") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. DADOS DA CNH
            Text(
                text = "— CARTEIRA DE HABILITAÇÃO (CNH)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = cnhNumber,
                onValueChange = { cnhNumber = it },
                label = { Text("Número do Registro da CNH") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = cnhCategory,
                    onValueChange = { cnhCategory = it },
                    label = { Text("Categoria (ex: B, AB)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                OutlinedTextField(
                    value = cnhValidity,
                    onValueChange = { cnhValidity = it },
                    label = { Text("Validade (MM/AAAA)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão Avançar para Foto da CNH
            Button(
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "📸", fontSize = 16.sp)
                    Text(
                        text = "Tirar Foto da CNH",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onBack) {
                Text(
                    text = "Voltar",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
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
