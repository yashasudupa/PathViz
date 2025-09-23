package com.urbansetu.app.hotspots

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HotspotsScreen() {
  Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text("Litter Hotspots", style = MaterialTheme.typography.headlineSmall)
    Text("• Heatmap available on Map tab.\n• This screen can show a ranked list, filters (severity, reports, last-cleaned), and per-ward stats.")
    OutlinedCard { Text("Coming soon: offline tiles, on-device clustering, report verification queue", modifier = Modifier.padding(12.dp)) }
  }
}
