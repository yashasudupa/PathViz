package com.urbansetu.app.brands

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import android.widget.Toast

data class UserLocation(val lat: Double, val lng: Double, val distanceMeters: Double)

private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = kotlin.math.sin(dLat/2)*kotlin.math.sin(dLat/2) +
            kotlin.math.cos(Math.toRadians(lat1))*kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon/2)*kotlin.math.sin(dLon/2)
    val c = 2 * Math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    return R * c
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandAdminScreen(
    brand: Brand,
    onBack: () -> Unit,
    onSave: (Brand) -> Unit
) {
    // Make an editable copy
    var enabled by remember { mutableStateOf(brand.notifyEnabled) }
    var radius by remember { mutableStateOf(brand.notifyRadiusMeters.toFloat()) }
    var message by remember { mutableStateOf(brand.notifyMessage) }
    var items by remember { mutableStateOf(brand.items) }
    val ctx = androidx.compose.ui.platform.LocalContext.current

    // Demo users near brand (if lat/lng known)
    val mockUsers = remember(brand.lat, brand.lng) {
        if (brand.lat != null && brand.lng != null) listOf(
            brand.lat + 0.001 to brand.lng + 0.001,
            brand.lat + 0.002 to brand.lng - 0.001,
            brand.lat - 0.001 to brand.lng - 0.002,
            brand.lat + 0.003 to brand.lng + 0.002
        ) else emptyList()
    }
    val users = remember(mockUsers, brand.lat, brand.lng, radius) {
        if (brand.lat == null || brand.lng == null) emptyList()
        else mockUsers.map { (uLat, uLng) ->
            val d = haversineMeters(brand.lat, brand.lng, uLat, uLng)
            UserLocation(uLat, uLng, d)
        }
    }
    val nearbyUsers = users.filter { it.distanceMeters <= radius }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage ${brand.name}") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    TextButton(onClick = {
                        onSave(
                            brand.copy(
                                notifyEnabled = enabled,
                                notifyRadiusMeters = radius.toInt(),
                                notifyMessage = message,
                                items = items
                            )
                        )
                        onBack()
                    }) { Text("Save") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {

            // 1) Geofence controls
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Enable proximity alerts")
                Spacer(Modifier.width(8.dp))
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }
            Spacer(Modifier.height(8.dp))
            Text("Radius: ${radius.toInt()} m")
            Slider(value = radius, onValueChange = { radius = it }, valueRange = 200f..3000f)

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Push message") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            // 2) Map (if brand has lat/lng)
            if (brand.lat != null && brand.lng != null) {
                val cameraState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(LatLng(brand.lat, brand.lng), 15f)
                }
                GoogleMap(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    cameraPositionState = cameraState
                ) {
                    Marker(state = MarkerState(LatLng(brand.lat, brand.lng)), title = brand.name)
                    // draw users
                    nearbyUsers.forEach {
                        Marker(state = MarkerState(LatLng(it.lat, it.lng)), title = "User")
                    }
                    // show geofence circle (visual only)
                    Circle(
                        center = LatLng(brand.lat, brand.lng),
                        radius = radius.toDouble(),
                        fillColor = androidx.compose.ui.graphics.Color(0x2200FF00),
                        strokeColor = androidx.compose.ui.graphics.Color(0x8800AA00),
                        strokeWidth = 2f
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Users inside radius: ${nearbyUsers.size}", fontWeight = FontWeight.Bold)
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 160.dp)
            ) {
                items(nearbyUsers) { u ->
                    Text("📍 ${"%.4f".format(u.lat)}, ${"%.4f".format(u.lng)} · ${"%.0f".format(u.distanceMeters)}m")
                }
            }

            Spacer(Modifier.height(12.dp))

// 3) Items (offers)
            // 3) Items / Offers (repo-backed)
            Spacer(Modifier.height(12.dp))
            Text("Items / Offers", style = MaterialTheme.typography.titleMedium)

// form state
            var itemTitle by remember { mutableStateOf("") }
            var itemSubtitle by remember { mutableStateOf("") }
            var itemPrice by remember { mutableStateOf("") }
            var itemDiscount by remember { mutableStateOf("") }

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = itemTitle,
                onValueChange = { itemTitle = it },
                label = { Text("Item Title*") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = itemSubtitle,
                onValueChange = { itemSubtitle = it },
                label = { Text("Subtitle") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = itemPrice,
                onValueChange = { itemPrice = it },
                label = { Text("Price (e.g., ₹199)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = itemDiscount,
                onValueChange = { itemDiscount = it },
                label = { Text("Discount (e.g., 20% OFF)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    BrandRepo.addItem(
                        BrandItem(
                            brandId = brand.id,           // ✅ link to current brand
                            title = itemTitle.trim(),
                            subtitle = itemSubtitle.takeIf { it.isNotBlank() },
                            price = itemPrice.takeIf { it.isNotBlank() },
                            discountText = itemDiscount.takeIf { it.isNotBlank() }
                        )
                    )
                    // reset fields
                    itemTitle = ""; itemSubtitle = ""; itemPrice = ""; itemDiscount = ""
                    android.widget.Toast.makeText(ctx, "Item added!", android.widget.Toast.LENGTH_SHORT).show()
                },
                enabled = itemTitle.isNotBlank()
            ) {
                Text("Add Item")
            }

            Spacer(Modifier.height(16.dp))
            Text("Current Offers", style = MaterialTheme.typography.titleMedium)

// read from repo (not from local 'items' var)
            val repoItems = remember(brand.id) { BrandRepo.itemsFor(brand.id) }
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)
            ) {
                items(repoItems) { it ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(it.title, fontWeight = FontWeight.Bold)
                            it.subtitle?.let { s -> Text(s) }
                            it.price?.let { p -> Text("Price: $p") }
                            it.discountText?.let { d -> Text(d, color = MaterialTheme.colorScheme.tertiary) }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 4) Push notification (demo)
            FilledTonalButton(
                enabled = enabled && nearbyUsers.isNotEmpty(),
                onClick = {
                    nearbyUsers.forEach {
                        com.urbansetu.app.util.Notifier.notifyBrand(
                            ctx = ctx,    // ✅ Pass context here
                            id = brand.id,
                            title = "${brand.name}: ${brand.headline}",
                            text = message
                        )
                    }
                    android.widget.Toast.makeText(ctx, "Sent to ${nearbyUsers.size} users", android.widget.Toast.LENGTH_SHORT).show()
                }
            ) {
                Text("Send notification to nearby users")
            }
        }
    }
}
