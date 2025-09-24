package com.urbansetu.app.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.urbansetu.app.hotspots.HotspotsScreen
import com.urbansetu.app.map.MapScreenWith3DSim
import com.urbansetu.app.petition.PetitionScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place

// Routes
sealed class Route(val r: String) {
  object Map : Route("map")
  object Hotspots : Route("hotspots")
  object Petition : Route("petition")
}

@Composable
fun NavGraph(nav: NavHostController) {
  var current by remember { mutableStateOf(Route.Map.r) }

  Scaffold(
    bottomBar = {
      NavigationBar {
        @Composable
        fun item(label: String, route: String) = NavigationBarItem(
          selected = current == route,
          onClick = {
            current = route
            nav.navigate(route) {
              popUpTo(nav.graph.startDestinationId) { saveState = true }
              launchSingleTop = true
              restoreState = true
            }
          },
          label = { Text(label) },
          icon = { Icon(Icons.Filled.Place, contentDescription = label) }
        )

        item("Map", Route.Map.r)
        item("Hotspots", Route.Hotspots.r)
        item("Petition", Route.Petition.r)
      }
    }
  ) { paddingValues ->
    NavHost(
      navController = nav,
      startDestination = Route.Map.r,
      modifier = Modifier.padding(paddingValues)
    ) {
      composable(Route.Map.r) { MapScreenWith3DSim() }
      composable(Route.Hotspots.r) { HotspotsScreen() }
      composable(Route.Petition.r) { PetitionScreen() }
    }
  }
}
