package com.draxio.mapa_teste.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.draxio.mapa_teste.ui.theme.CarameloCocoa
import com.draxio.mapa_teste.ui.theme.CarameloDarkBackground
import com.draxio.mapa_teste.ui.theme.CarameloOrange

/*
 * Componentes do redesign Caramelo (canvas "Caramelo — UI/UX").
 * Linguagem: fundo cinza quente, cartoes brancos de 24 dp, uma cor de acao (caramelo),
 * botoes em pilula (caramelo + cacau), acoes circulares, barra de progresso em blocos
 * e menu inferior flutuante. Use estes componentes em vez de estilizar cada tela a mao.
 */

/** true quando o tema escuro do Caramelo esta ativo. */
@Composable
fun isCarameloDark(): Boolean = MaterialTheme.colorScheme.background == CarameloDarkBackground

/** Cor dos botoes e cartoes "escuros" (cacau). No modo noturno vira a superficie variante, para nao sumir no fundo. */
@Composable
fun carameloDarkFill(): Color = if (isCarameloDark()) MaterialTheme.colorScheme.surfaceVariant else CarameloCocoa

// ---------------------------------------------------------------- Cartao

/** Cartao branco arredondado (24 dp) com sombra leve. */
@Composable
fun CarameloCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = color,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

// ---------------------------------------------------------------- Botoes em pilula

enum class PillStyle { Primary, Dark, Outline }

/** Botao em pilula, 54 dp de altura. Primary = caramelo, Dark = cacau, Outline = contorno. */
@Composable
fun CarameloPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PillStyle = PillStyle.Primary,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val (bg, fg, border) = when (style) {
        PillStyle.Primary -> Triple(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary, null)
        PillStyle.Dark -> Triple(carameloDarkFill(), Color.White, null)
        PillStyle.Outline -> Triple(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.onSurface,
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
        )
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 54.dp),
        shape = CircleShape,
        color = if (enabled) bg else bg.copy(alpha = 0.4f),
        contentColor = fg,
        border = border
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(text = text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        }
    }
}

// ---------------------------------------------------------------- Botoes circulares

enum class CircleStyle { Primary, Dark, Outline, Surface }

/** Botao redondo so com icone (48 dp por padrao). Sempre passe contentDescription. */
@Composable
fun CarameloCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: CircleStyle = CircleStyle.Surface,
    size: Dp = 48.dp
) {
    val (bg, fg, border) = when (style) {
        CircleStyle.Primary -> Triple(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary, null)
        CircleStyle.Dark -> Triple(carameloDarkFill(), Color.White, null)
        CircleStyle.Outline -> Triple(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.onSurface,
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
        )
        CircleStyle.Surface -> Triple(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, null)
    }
    Surface(
        onClick = onClick,
        modifier = modifier.size(size),
        shape = CircleShape,
        color = bg,
        contentColor = fg,
        border = border,
        shadowElevation = if (style == CircleStyle.Surface) 2.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(22.dp))
        }
    }
}

/** Acao circular de 56 dp com legenda embaixo (Ligar, Mensagem, Rota, Seguranca). */
@Composable
fun CarameloCircleAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: CircleStyle = CircleStyle.Outline
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CarameloCircleButton(icon = icon, contentDescription = label, onClick = onClick, style = style, size = 56.dp)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ---------------------------------------------------------------- Barra de progresso em blocos

/** Barra de progresso feita de blocos (estilo do redesign). progress de 0f a 1f. */
@Composable
fun CarameloSegmentedProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    segments: Int = 10,
    height: Dp = 28.dp,
    activeColor: Color = MaterialTheme.colorScheme.primary
) {
    // + 0.001f evita que 1/3 * 3 = 0,9999 vire 0 segmentos
    val filled = (progress.coerceIn(0f, 1f) * segments + 0.001f).toInt()
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(segments) { i ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(height)
                    .background(
                        if (i < filled) activeColor else MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(6.dp)
                    )
            )
        }
    }
}

// ---------------------------------------------------------------- Chips

/** Chip em pilula. Selecionado = cacau com texto branco; normal = contorno. */
@Composable
fun CarameloChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 40.dp),
        shape = CircleShape,
        color = if (selected) carameloDarkFill() else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
        border = if (selected) null else BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

// ---------------------------------------------------------------- Barra superior

/** Barra do topo: botao voltar redondo, titulo centralizado e espaco para uma acao a direita. */
@Composable
fun CarameloTopBar(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = { Spacer(Modifier.size(48.dp)) }
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            CarameloCircleButton(icon = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Voltar", onClick = onBack)
        } else {
            Spacer(Modifier.size(48.dp))
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        trailing()
    }
}

/** Titulo grande de tela, com subtitulo opcional acima (ex.: "Boa tarde, Marina"). */
@Composable
fun CarameloScreenTitle(title: String, modifier: Modifier = Modifier, overline: String? = null) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        if (overline != null) {
            Text(overline, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
    }
}

/** Rotulo de secao em cinza ("Seguranca", "Motorista preferido"). */
@Composable
fun CarameloSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(top = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// ---------------------------------------------------------------- Numero em destaque

/** Numero grande com rotulo pequeno acima ("Carlos chega em" / "3 min"). */
@Composable
fun CarameloBigNumber(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
    alignEnd: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = if (accent) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
            else MaterialTheme.typography.displaySmall,
            color = if (accent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

// ---------------------------------------------------------------- Linhas de lista e configuracao

/** Linha de lista com icone num quadrado arredondado, titulo, subtitulo e algo a direita. */
@Composable
fun CarameloListRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    highlightIcon: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = {
        if (onClick != null) Icon(
            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
) {
    val rowContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (highlightIcon) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (highlightIcon) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (trailing != null) trailing()
        }
    }
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier, color = Color.Transparent) { rowContent() }
    } else {
        Box(modifier = modifier) { rowContent() }
    }
}

/** Linha com interruptor (Modo noturno, Compartilhar rota...). */
@Composable
fun CarameloSwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    CarameloListRow(
        icon = icon,
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = MaterialTheme.colorScheme.outline,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    )
}

/** Divisoria fina usada entre linhas dentro de um cartao. */
@Composable
fun CarameloDivider(modifier: Modifier = Modifier, startIndent: Dp = 52.dp) {
    Box(
        modifier = modifier
            .padding(start = startIndent)
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    )
}

// ---------------------------------------------------------------- Campo de texto

/** Campo de texto preenchido (sem contorno), cantos de 16 dp, rotulo acima. */
@Composable
fun CarameloTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: ImageVector? = null
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = if (placeholder != null) {
                { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else null,
            leadingIcon = if (leadingIcon != null) {
                { Icon(leadingIcon, contentDescription = null) }
            } else null,
            isError = isError,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

// ---------------------------------------------------------------- Menu inferior flutuante

enum class CarameloNavItem { Inicio, Viagens, Carteira, Perfil }

/** Menu inferior flutuante em pilula. O item ativo vira um circulo caramelo. */
@Composable
fun CarameloFloatingNavBar(
    selected: CarameloNavItem,
    onSelect: (CarameloNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple(CarameloNavItem.Inicio, Icons.Rounded.Home, "Início"),
        Triple(CarameloNavItem.Viagens, Icons.Rounded.History, "Viagens"),
        Triple(CarameloNavItem.Carteira, Icons.Rounded.AccountBalanceWallet, "Carteira"),
        Triple(CarameloNavItem.Perfil, Icons.Rounded.Person, "Perfil")
    )
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { (item, icon, label) ->
                val isSelected = item == selected
                Surface(
                    onClick = { onSelect(item) },
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Selo de status

/** Selo pequeno em pilula com bolinha colorida ("Confirmado", "12 motoristas por perto"). */
@Composable
fun CarameloStatusPill(
    text: String,
    modifier: Modifier = Modifier,
    dotColor: Color = CarameloOrange,
    dark: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = if (dark) carameloDarkFill() else MaterialTheme.colorScheme.surface,
        contentColor = if (dark) Color.White else MaterialTheme.colorScheme.onSurface,
        shadowElevation = if (dark) 0.dp else 3.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.size(8.dp).background(dotColor, CircleShape))
            Text(text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** Espaco a deixar no fim do conteudo rolavel quando o menu flutuante esta visivel. */
val CarameloNavBarClearance: Dp = 96.dp
