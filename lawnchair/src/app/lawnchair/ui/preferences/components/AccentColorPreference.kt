package app.lawnchair.ui.preferences.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import app.lawnchair.preferences.getAdapter
import app.lawnchair.preferences.preferenceManager
import app.lawnchair.ui.theme.LAWNCHAIR_CYAN
import com.android.launcher3.R

val presetColors = listOf(
        0xFFFF5252,
        0xFFFF4081,
        0xFFE040FB,
        0xFF7C4DFF,
        0xFF536DFE,
        0xFF448AFF,
        0xFF40C4FF,
        0xFF18FFFF,
        0xFF64FFDA,
        0xFF69F0AE,
        0xFFB2FF59,
        0xFFEEFF41,
        0xFFFFFF00,
        0xFFFFD740,
        0xFFFFAB40,
        LAWNCHAIR_CYAN,
        0xFF607D8B,
        0xFF9E9E9E
)

val presets = presetColors.map {
    ColorPreferencePreset(it.toInt(), { it.toInt() })
}

@Composable
@ExperimentalMaterialApi
@ExperimentalAnimationApi
fun AccentColorPreference(showDivider: Boolean = true) {
    ColorPreference(
        previewColor = MaterialTheme.colors.primary.toArgb(),
        customColorAdapter = preferenceManager().accentColor.getAdapter(),
        label = stringResource(id = R.string.accent_color),
        presets = presets,
        showDivider = showDivider,
        useSystemAccentAdapter = preferenceManager().useSystemAccent.getAdapter()
    )
}
