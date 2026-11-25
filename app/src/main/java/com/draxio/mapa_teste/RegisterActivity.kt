package com.draxio.mapa_teste

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.draxio.mapa_teste.ui.components.CarameloCard
import com.draxio.mapa_teste.ui.components.CarameloPillButton
import com.draxio.mapa_teste.ui.components.CarameloScreenTitle
import com.draxio.mapa_teste.ui.components.CarameloSectionLabel
import com.draxio.mapa_teste.ui.components.CarameloTextField
import com.draxio.mapa_teste.ui.components.CarameloTopBar
import com.draxio.mapa_teste.ui.components.PillStyle
import com.draxio.mapa_teste.ui.theme.*

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Mapa_testeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RegisterScreen(
                        onRegisterSuccess = { name, email, password, logradouro, number, bairro, complement, cep, city, state ->
                            try {
                                CarameloRepository.saveUserProfile(
                                    context = this,
                                    name = name,
                                    email = email,
                                    logradouro = logradouro,
                                    number = number,
                                    bairro = bairro,
                                    complement = complement,
                                    cep = cep,
                                    city = city,
                                    state = state
                                )

                                CarameloRepository.setLoggedIn(this, true)
                                Toast.makeText(this, "Cadastro salvo localmente com sucesso!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, Mapao_do_Oda::class.java))
                                finishAffinity()
                            } catch (e: Exception) {
                                Toast.makeText(this, "Erro ao salvar dados localmente: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        },
                        onGoogleRegister = {
                            CarameloRepository.setLoggedIn(this, true)
                            Toast.makeText(this, "Modo demonstração: entrando sem conta Google", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, Mapao_do_Oda::class.java))
                            finishAffinity()
                        },
                        onBackToLogin = {
                            finish()
                        },
                        onShowError = { message ->
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: (String, String, String, String, String, String, String, String, String, String) -> Unit,
    onGoogleRegister: () -> Unit,
    onBackToLogin: () -> Unit,
    onShowError: (String) -> Unit
) {
    // Dados Pessoais
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var confirmEmail by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") } // senha fora do estado salvo
    var confirmPassword by remember { mutableStateOf("") }

    // Endereço de Cadastro Detalhado
    var logradouro by rememberSaveable { mutableStateOf("") }
    var number by rememberSaveable { mutableStateOf("") }
    var bairro by rememberSaveable { mutableStateOf("") }
    var complement by rememberSaveable { mutableStateOf("") }
    var cep by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var state by rememberSaveable { mutableStateOf("") }

    // Ultima mensagem de validacao, mostrada tambem na tela (alem do Toast)
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val showError: (String) -> Unit = { message ->
        errorMessage = message
        onShowError(message)
    }

    val emailMismatch = confirmEmail.isNotEmpty() && email != confirmEmail
    val passwordMismatch = confirmPassword.isNotEmpty() && password != confirmPassword

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
            CarameloTopBar(title = "Criar conta", onBack = onBackToLogin)

            CarameloScreenTitle(
                title = "Vamos começar",
                overline = "Leva menos de um minuto"
            )

            // 1. SEUS DADOS
            CarameloSectionLabel(text = "Seus dados")
            CarameloCard {
                CarameloTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nome completo",
                    leadingIcon = Icons.Rounded.Person,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text
                    )
                )

                CarameloTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "E-mail",
                    placeholder = "voce@email.com",
                    leadingIcon = Icons.Rounded.Email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                CarameloTextField(
                    value = confirmEmail,
                    onValueChange = { confirmEmail = it },
                    label = "Confirmar e-mail",
                    leadingIcon = Icons.Rounded.Email,
                    isError = emailMismatch,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                CarameloTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Senha",
                    leadingIcon = Icons.Rounded.Lock,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                CarameloTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirmar senha",
                    leadingIcon = Icons.Rounded.Lock,
                    isError = passwordMismatch,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            }

            // 2. ENDEREÇO (CEP primeiro)
            CarameloSectionLabel(text = "Endereço")
            CarameloCard {
                CarameloTextField(
                    value = cep,
                    onValueChange = { cep = it },
                    label = "CEP",
                    placeholder = "01310-100",
                    leadingIcon = Icons.Rounded.Place,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Logradouro e Número
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CarameloTextField(
                        value = logradouro,
                        onValueChange = { logradouro = it },
                        label = "Logradouro",
                        placeholder = "Rua / Av.",
                        modifier = Modifier.weight(1.3f)
                    )
                    CarameloTextField(
                        value = number,
                        onValueChange = { number = it },
                        label = "Nº",
                        modifier = Modifier.weight(0.7f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                // Bairro e Complemento
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CarameloTextField(
                        value = bairro,
                        onValueChange = { bairro = it },
                        label = "Bairro",
                        modifier = Modifier.weight(1f)
                    )
                    CarameloTextField(
                        value = complement,
                        onValueChange = { complement = it },
                        label = "Complemento",
                        placeholder = "Opcional",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Cidade e Estado (UF)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CarameloTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = "Cidade",
                        modifier = Modifier.weight(1.3f)
                    )
                    CarameloTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = "UF",
                        modifier = Modifier.weight(0.6f),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                    )
                }
            }

            // Mensagem de erro da validacao
            errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Botão de Cadastrar
            CarameloPillButton(
                text = "Criar conta",
                onClick = {
                    when {
                        name.isBlank() || email.isBlank() || password.isBlank() -> {
                            showError("Por favor, preencha nome, e-mail e senha.")
                        }
                        email != confirmEmail -> {
                            showError("Os e-mails informados não coincidem.")
                        }
                        password != confirmPassword -> {
                            showError("As senhas informadas não coincidem.")
                        }
                        logradouro.isBlank() || number.isBlank() || bairro.isBlank() || cep.isBlank() || city.isBlank() || state.isBlank() -> {
                            showError("Por favor, preencha todos os campos do endereço.")
                        }
                        else -> {
                            errorMessage = null
                            onRegisterSuccess(name, email, password, logradouro, number, bairro, complement, cep, city, state)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                style = PillStyle.Primary
            )

            // Cadastro com Google (contorno)
            Surface(
                onClick = onGoogleRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "G",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Cadastrar com o Google",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Rodapé: voltar ao login
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Já tem uma conta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onBackToLogin,
                    modifier = Modifier.heightIn(min = 44.dp)
                ) {
                    Text(
                        text = "Faça login",
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
fun RegisterScreenPreview() {
    Mapa_testeTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            RegisterScreen(
                onRegisterSuccess = { _, _, _, _, _, _, _, _, _, _ -> },
                onGoogleRegister = {},
                onBackToLogin = {},
                onShowError = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenDarkPreview() {
    Mapa_testeTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            RegisterScreen(
                onRegisterSuccess = { _, _, _, _, _, _, _, _, _, _ -> },
                onGoogleRegister = {},
                onBackToLogin = {},
                onShowError = {}
            )
        }
    }
}
