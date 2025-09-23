package com.urbansetu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.urbansetu.app.nav.NavGraph
import com.urbansetu.app.ui.theme.UrbanSetuTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      UrbanSetuTheme {
        val nav = rememberNavController()
        Surface { NavGraph(nav) }
      }
    }
  }
}
