package com.draxio.mapa_teste

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.draxio.mapa_teste.ui.components.*
import com.draxio.mapa_teste.ui.theme.*

// Apresentação Comercial (Slide 01/07) - Mobilidade com calor humano
@Composable
fun CarameloPresentationScreen(onClose: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header do Slide: voltar ao app, contador de slides e logo
            CarameloTopBar(
                title = "01 / 07",
                onBack = onClose,
                trailing = { Spacer(modifier = Modifier.size(48.dp)) }
            )

            CarameloLogo()

            Spacer(modifier = Modifier.height(8.dp))

            // Conteúdo principal do slide
            CarameloSectionLabel(text = "Apresentação comercial · 2025")

            Text(
                text = "Mobilidade\ncom calor\nhumano.",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Uma plataforma brasileira de mobilidade urbana criada para transformar trajetos cotidianos em experiências mais simples, seguras e próximas.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Diagrama com círculos concêntricos e ícone de carro
            CarameloCard(contentPadding = PaddingValues(20.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .background(CarameloYellow.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .background(CarameloYellow.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Rounded.DirectionsCar,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "caramelo",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            CarameloPillButton(
                text = "Voltar ao app",
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
                style = PillStyle.Outline
            )
        }
    }
}
