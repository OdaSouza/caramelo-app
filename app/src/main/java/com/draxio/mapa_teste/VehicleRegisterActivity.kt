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
import androidx.compose.material.icons.rounded.DirectionsCar
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

class VehicleRegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Mapa_testeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VehicleRegisterScreen(
                        onVehicleSaved = { brand, model, plate, year ->
                            try {
                                val sharedPreferences = securePrefs(this, "secure_vehicle_prefs")

                                sharedPreferences.edit().apply {
                                    putString("vehicle_brand", brand)
                                    putString("vehicle_model", model)
                                    putString("vehicle_plate", plate)
                                    putString("vehicle_year", year)
                                    apply()
                                }

                                Toast.makeText(this, "Veículo cadastrado com segurança!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, VehiclePhotoActivity::class.java)
                                startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(this, "Erro ao criptografar dados do veículo: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        },
                        onBack = { finish() },
                        onShowError = { message ->
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
fun VehicleRegisterScreen(
    onVehicleSaved: (String, String, String, String) -> Unit,
    onBack: () -> Unit,
    onShowError: (String) -> Unit
) {
    var brand by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf("") }
    var plate by rememberSaveable { mutableStateOf("") }
    var year by rememberSaveable { mutableStateOf("") }

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
            CarameloTopBar(title = "Dados do veículo", onBack = onBack)

            // Indicador de etapa
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CarameloSegmentedProgress(progress = 3f / 5f, segments = 5, height = 8.dp)
                Text(
                    text = "Passo 3 de 5",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            CarameloScreenTitle(title = "Seu automóvel")

            CarameloSectionLabel(text = "Dados do veículo")
            CarameloCard {
                CarameloTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = "Marca",
                    placeholder = "ex: Toyota, Ford",
                    leadingIcon = Icons.Rounded.DirectionsCar,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                CarameloTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = "Modelo",
                    placeholder = "ex: Corolla, Ka",
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CarameloTextField(
                        value = plate,
                        onValueChange = { plate = it },
                        label = "Placa do veículo",
                        placeholder = "ABC1D23",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                    )
                    CarameloTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = "Ano de fabricação",
                        placeholder = "AAAA",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        CarameloPillButton(
            text = "Avançar para foto",
            icon = Icons.Rounded.PhotoCamera,
            onClick = {
                if (brand.isNotBlank() && model.isNotBlank() && plate.isNotBlank() && year.isNotBlank()) {
                    onVehicleSaved(brand, model, plate, year)
                } else {
                    onShowError("Por favor, preencha todos os dados do veículo.")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            style = PillStyle.Primary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VehicleRegisterScreenPreview() {
    Mapa_testeTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            VehicleRegisterScreen(
                onVehicleSaved = { _, _, _, _ -> },
                onBack = {},
                onShowError = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VehicleRegisterScreenDarkPreview() {
    Mapa_testeTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            VehicleRegisterScreen(
                onVehicleSaved = { _, _, _, _ -> },
                onBack = {},
                onShowError = {}
            )
        }
    }
}
