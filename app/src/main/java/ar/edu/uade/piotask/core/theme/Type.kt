package ar.edu.uade.piotask.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ar.edu.uade.piotask.R

val PlexSans = FontFamily(
    Font(R.font.ibm_plex_sans_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_medium, FontWeight.Medium),
    Font(R.font.ibm_plex_sans_semibold, FontWeight.SemiBold),
)

val PlexMono = FontFamily(Font(R.font.ibm_plex_mono_medium, FontWeight.Medium))

/** Etiqueta de sección ("RAMA", "NIDO") en mayúsculas y monoespaciada, como en el diseño. */
val EstiloSeccion = TextStyle(
    fontFamily = PlexMono,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    letterSpacing = 0.72.sp,
)

private val Base = Typography()

val PioTipografia = Typography(
    displayLarge = Base.displayLarge.copy(fontFamily = PlexSans),
    displayMedium = Base.displayMedium.copy(fontFamily = PlexSans),
    displaySmall = Base.displaySmall.copy(fontFamily = PlexSans),
    headlineLarge = Base.headlineLarge.copy(fontFamily = PlexSans, fontWeight = FontWeight.SemiBold),
    headlineMedium = Base.headlineMedium.copy(fontFamily = PlexSans, fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    headlineSmall = Base.headlineSmall.copy(fontFamily = PlexSans, fontWeight = FontWeight.SemiBold),
    titleLarge = Base.titleLarge.copy(fontFamily = PlexSans, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleMedium = Base.titleMedium.copy(fontFamily = PlexSans, fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    titleSmall = Base.titleSmall.copy(fontFamily = PlexSans, fontWeight = FontWeight.SemiBold),
    bodyLarge = Base.bodyLarge.copy(fontFamily = PlexSans),
    bodyMedium = Base.bodyMedium.copy(fontFamily = PlexSans, fontSize = 14.sp),
    bodySmall = Base.bodySmall.copy(fontFamily = PlexSans, fontSize = 13.sp),
    labelLarge = Base.labelLarge.copy(fontFamily = PlexSans, fontWeight = FontWeight.Medium),
    labelMedium = Base.labelMedium.copy(fontFamily = PlexSans, fontSize = 13.sp),
    labelSmall = Base.labelSmall.copy(fontFamily = PlexSans, fontWeight = FontWeight.Medium),
)
