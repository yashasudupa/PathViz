package com.urbansetu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.urbansetu.app.home.FadeInContent
import com.urbansetu.app.home.SplashScreen
import com.urbansetu.app.home.MainScaffold
import com.urbansetu.app.ui.theme.UrbanSetuTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      UrbanSetuTheme {
        var showSplash by remember { mutableStateOf(true) }

        Surface {
          if (showSplash) {
            SplashScreen { showSplash = false }
          } else {
            // ✅ Show your bottom nav + screens here
            MainScaffold()
          }
        }
      }
    }
  }
}
