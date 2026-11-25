package com.draxio.mapa_teste

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.draxio.mapa_teste.ui.components.CarameloPillButton
import com.draxio.mapa_teste.ui.components.CarameloTextField
import com.draxio.mapa_teste.ui.components.PillStyle
import com.draxio.mapa_teste.ui.theme.*

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Sessão ativa: vai direto para o mapa
        if (CarameloRepository.isLoggedIn(this)) {
            startActivity(Intent(this, Mapao_do_Oda::class.java))
            finish()
            return
        }
        setContent {
            Mapa_testeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(
                        onLoginSuccess = {
                            CarameloRepository.setLoggedIn(this, true)
                            val intent = Intent(this, Mapao_do_Oda::class.java)
                            startActivity(intent)
                            finish()
                        },
                        onNavigateToRegister = {
                            val intent = Intent(this, RegisterActivity::class.java)
                            startActivity(intent)
                        },
                        onNavigateToRecovery = {
                            val intent = Intent(this, RecoveryActivity::class.java)
                            startActivity(intent)
                        },
                        onGoogleLogin = {
                            CarameloRepository.setLoggedIn(this, true)
                            Toast.makeText(this, "Modo demonstração: entrando sem conta Google", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, Mapao_do_Oda::class.java)
                            startActivity(intent)
                            finish()
                        },
                        onAppleLogin = {
                            CarameloRepository.setLoggedIn(this, true)
                            Toast.makeText(this, "Modo demonstração: entrando sem conta Apple", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, Mapao_do_Oda::class.java)
                            startActivity(intent)
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
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToRecovery: () -> Unit,
    onGoogleLogin: () -> Unit,
    onAppleLogin: () -> Unit = {},
    onShowError: (String) -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") } // senha fora do estado salvo

    // Tela inteira na cor da folha, para a folha "continuar" ate o fim sem emenda
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo oficial completo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.caramelo_logo),
                    contentDescription = "Caramelo",
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(32.dp))
                )
            }

            // Folha branca com cantos superiores arredondados
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 22.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Mobilidade com calor humano",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Entre para pedir sua próxima viagem.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    CarameloTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "E-mail",
                        placeholder = "voce@email.com",
                        leadingIcon = Icons.Rounded.Email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    CarameloTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Senha",
                        placeholder = "Sua senha",
                        leadingIcon = Icons.Rounded.Lock,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation()
                    )

                    // "Esqueci minha senha" alinhado a direita
                    TextButton(
                        onClick = onNavigateToRecovery,
                        modifier = Modifier
                            .align(Alignment.End)
                            .heightIn(min = 44.dp)
                    ) {
                        Text(
                            text = "Esqueci minha senha",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Botão Entrar Principal
                    CarameloPillButton(
                        text = "Entrar",
                        onClick = {
                            if (email.isNotBlank() && password.isNotBlank()) {
                                onLoginSuccess()
                            } else {
                                onShowError("Por favor, preencha o e-mail e a senha.")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        style = PillStyle.Primary
                    )

                    // Google (contorno) + Apple (cacau), lado a lado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            onClick = onGoogleLogin,
                            modifier = Modifier
                                .weight(1f)
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
                                    text = "Google",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        CarameloPillButton(
                            text = "Apple",
                            onClick = onAppleLogin,
                            modifier = Modifier.weight(1f),
                            style = PillStyle.Dark
                        )
                    }

                    // Rodapé: "Novo por aqui? Criar conta"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Novo por aqui?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        TextButton(
                            onClick = onNavigateToRegister,
                            modifier = Modifier.heightIn(min = 44.dp)
                        ) {
                            Text(
                                text = "Criar conta",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    Mapa_testeTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {},
                onNavigateToRecovery = {},
                onGoogleLogin = {},
                onAppleLogin = {},
                onShowError = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenDarkPreview() {
    Mapa_testeTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {},
                onNavigateToRecovery = {},
                onGoogleLogin = {},
                onAppleLogin = {},
                onShowError = {}
            )
        }
    }
}
