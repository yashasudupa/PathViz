package com.urbansetu.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Colors = lightColorScheme(
  primary = Color(0xFF2F4156),       // slate blue
  secondary = Color(0xFF3C6E71),     // dusty teal
  tertiary = Color(0xFFF4A261),      // warm accent for offer pills
  background = Color(0xFFF3F5F7),
  surface = Color(0xFFFFFFFF),
  onPrimary = Color.White,
  onSecondary = Color.White,
  onBackground = Color(0xFF22303A),
  onSurface = Color(0xFF22303A)
)

@Composable fun UrbanSetuTheme(content: @Composable ()->Unit){
  MaterialTheme(colorScheme = Colors, typography = Typography(), content = content)
}
