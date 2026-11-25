package com.draxio.mapa_teste

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.draxio.mapa_teste.ui.components.*
import com.draxio.mapa_teste.ui.theme.*


// Drawer / Menu Lateral da Marina Costa
@Composable
fun CarameloDrawerContent(
    onClose: () -> Unit,
    onOpenTripHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPresentation: () -> Unit,
    onToast: (String) -> Unit,
    onOpenWallet: () -> Unit = {},
    onOpenHelp: () -> Unit = {}
) {
    val drawerContext = androidx.compose.ui.platform.LocalContext.current
    val drawerUserName = remember { CarameloRepository.getUserName(drawerContext) }
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
            // Topo: fechar redondo e logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CarameloCircleButton(
                    icon = Icons.Rounded.Close,
                    contentDescription = "Fechar menu",
                    onClick = onClose
                )
                CarameloLogo()
                Spacer(modifier = Modifier.size(48.dp))
            }

            // Cartao do perfil
            CarameloCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(carameloDarkFill(), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitialsOf(drawerUserName),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = drawerUserName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "4,92 de avaliação",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Itens de Menu
            val menuItems: List<Triple<ImageVector, String, () -> Unit>> = listOf(
                Triple(Icons.Rounded.History, "Suas viagens", onOpenTripHistory),
                Triple(Icons.Rounded.Settings, "Configurações", onOpenSettings),
                Triple(Icons.Rounded.CreditCard, "Pagamentos", onOpenWallet),
                Triple(Icons.Rounded.Shield, "Segurança", onOpenHelp),
                Triple(Icons.AutoMirrored.Rounded.HelpOutline, "Ajuda", onOpenHelp),
                Triple(Icons.Rounded.Groups, "Convide amigos", { shareInvite(drawerContext) })
            )

            CarameloCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                menuItems.forEachIndexed { index, (icon, title, action) ->
                    if (index > 0) CarameloDivider()
                    CarameloListRow(
                        icon = icon,
                        title = title,
                        onClick = action
                    )
                }
            }

            // Card Promocional "Convide e ganhe"
            CarameloCard(color = MaterialTheme.colorScheme.primary, contentPadding = PaddingValues(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CardGiftcard,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Convide e ganhe R$ 10",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = "Indique o Caramelo para alguém especial.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                CarameloPillButton(
                    text = "Convidar agora",
                    onClick = { shareInvite(drawerContext) },
                    style = PillStyle.Dark,
                    icon = Icons.Rounded.Share
                )
            }

            // Link para a Apresentação Comercial
            CarameloPillButton(
                text = "Ver apresentação comercial",
                onClick = onOpenPresentation,
                modifier = Modifier.fillMaxWidth(),
                style = PillStyle.Outline,
                icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
            )
        }
    }
}

// Convite: abre o seletor de apps para compartilhar o texto
private fun shareInvite(context: android.content.Context) {
    val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            android.content.Intent.EXTRA_TEXT,
            "Conheça o Caramelo, app de corridas com motoristas verificados."
        )
    }
    val chooser = android.content.Intent.createChooser(send, "Convidar amigos")
    try {
        context.startActivity(chooser)
    } catch (_: android.content.ActivityNotFoundException) {
        android.widget.Toast.makeText(context, "Nenhum app para compartilhar", android.widget.Toast.LENGTH_SHORT).show()
    }
}
