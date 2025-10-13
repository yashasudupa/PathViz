package com.urbansetu.app.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.urbansetu.app.nav.NavGraph
import com.urbansetu.app.nav.Route
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Store

@Composable
fun MainScaffold() {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()

    // ✅ make currentRoute mutable so we can change it
    var currentRoute by remember { mutableStateOf(Route.Brands.r) }

    // ✅ Explicit list type (Pair<Route, String>)
    val items: List<Pair<Route, String>> = listOf(
        Pair(Route.Brands, "Brands"),
        Pair(Route.Map, "Map"),
        Pair(Route.Hotspots, "Hotspots"),
        Pair(Route.Petition, "Petition")
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == Route.Brands.r,
                    onClick = {
                        currentRoute = Route.Brands.r
                        nav.navigate(Route.Brands.r) {
                            popUpTo(nav.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Filled.LocalOffer, contentDescription = "Brands") },
                    label = { Text("Brands") }
                )

                NavigationBarItem(
                    selected = currentRoute == Route.Map.r,
                    onClick = {
                        currentRoute = Route.Map.r
                        nav.navigate(Route.Map.r) {
                            popUpTo(nav.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Filled.Place, contentDescription = "Map") },
                    label = { Text("Map") }
                )

                NavigationBarItem(
                    selected = currentRoute == Route.Hotspots.r,
                    onClick = {
                        currentRoute = Route.Hotspots.r
                        nav.navigate(Route.Hotspots.r) {
                            popUpTo(nav.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Filled.Group, contentDescription = "Hotspots") },
                    label = { Text("Hotspots") }
                )

                NavigationBarItem(
                    selected = currentRoute == Route.Petition.r,
                    onClick = {
                        currentRoute = Route.Petition.r
                        nav.navigate(Route.Petition.r) {
                            popUpTo(nav.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Filled.Flag, contentDescription = "Petition") },
                    label = { Text("Petition") }
                )
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = nav,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
