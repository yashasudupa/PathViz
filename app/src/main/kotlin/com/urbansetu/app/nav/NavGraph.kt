package com.urbansetu.app.nav

import androidx.lifecycle.viewmodel.compose.viewModel
import com.urbansetu.app.wallet.WalletViewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.urbansetu.app.map.MapScreenWith3DSimEco
import com.urbansetu.app.hotspots.HotspotsScreen
import com.urbansetu.app.petition.PetitionScreen
import com.urbansetu.app.brands.BrandsScreen

// Routes
sealed class Route(val r: String) {
  object Brands   : Route("brands")   // <-- ADD THIS
  object Map : Route("map")
  object Hotspots : Route("hotspots")
  object Petition : Route("petition")
}

@Composable
fun NavGraph(
  navController: NavHostController,
  modifier: Modifier = Modifier,
  start: String = Route.Brands.r   // default to Brands for demo
) {
  // ✅ Create the ViewModel once and share it with all destinations
  val walletViewModel: WalletViewModel = viewModel()

  NavHost(
    navController = navController,
    startDestination = Route.Brands.r,
    modifier = modifier
  ) {
    composable(Route.Brands.r) {
      BrandsScreen(
        onBrandClick = { /* handle brand click */ },
        walletViewModel = walletViewModel
      )
    }
    composable(Route.Map.r)      { MapScreenWith3DSimEco() }
    composable(Route.Hotspots.r) { HotspotsScreen() }
    composable(Route.Petition.r) { PetitionScreen() }
  }
}

