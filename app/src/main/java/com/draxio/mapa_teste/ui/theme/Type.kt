package com.draxio.mapa_teste.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.draxio.mapa_teste.R

// Escala do redesign: cinco tamanhos (40 / 24 / 17 / 15 / 13).
// Fontes em res/font (Google Fonts, licença SIL OFL 1.1, texto em assets/licencas):
// Plus Jakarta Sans para todo o texto, Fredoka para o nome da marca.
// Arquivos estáticos, um por peso, para funcionar também no Android 7 (minSdk 24).
val CarameloFont: FontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold)
)

// Fredoka vai até o peso 700: pedidos de ExtraBold usam o Bold.
val CarameloBrandFont: FontFamily = FontFamily(
    Font(R.font.fredoka_medium, FontWeight.Medium),
    Font(R.font.fredoka_semibold, FontWeight.SemiBold),
    Font(R.font.fredoka_bold, FontWeight.Bold)
)

val Typography = Typography(
    // Numeros em destaque: preco, tempo de chegada
    displaySmall = TextStyle(fontFamily = CarameloFont, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-1).sp),
    // Titulo da tela: "Para onde vamos?"
    headlineSmall = TextStyle(fontFamily = CarameloFont, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.3).sp),
    // Titulo de secao: "Escolha sua viagem"
    titleMedium = TextStyle(fontFamily = CarameloFont, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = CarameloFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp),
    // Corpo
    bodyLarge = TextStyle(fontFamily = CarameloFont, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = CarameloFont, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    // Legendas
    bodySmall = TextStyle(fontFamily = CarameloFont, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    // Botoes
    labelLarge = TextStyle(fontFamily = CarameloFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = CarameloFont, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp)
)
