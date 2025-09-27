package com.urbansetu.app.map
import androidx.compose.ui.geometry.Offset
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.net.toFile
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip            // <<--- the missing import
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.MoreVert
// animation imports
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.FilterChip
// Compose core / foundation / layout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

// Compose animation
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically

// Compose UI (draw, graphics, alignment, units)
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow      // <-- correct import for shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

// Material3
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

// Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Upload



fun generateTimeslots(): List<String> {
    val fmt = DateTimeFormatter.ofPattern("h:mm a")
    return (0..23).map { hour -> LocalTime.of(hour, 0).format(fmt) }
}

@Composable
fun MapTopActionIcon(
    timeslots: List<String> = generateTimeslots(),
    modifier: Modifier = Modifier,
    iconSize: Dp = 48.dp,
    onUploadClicked: () -> Unit,
    onTimeslotSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(12.dp)
            .wrapContentSize()
            .zIndex(20f),
        horizontalAlignment = Alignment.End
    ) {
        IconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .size(iconSize)
                .clip(CircleShape)
                .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = if (expanded) "Close menu" else "Open menu",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // animated popup card
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .wrapContentHeight()
                    .shadow(8.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Upload row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onUploadClicked()
                                expanded = false
                            }
                            .padding(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = "Upload awareness image",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Upload awareness image", style = MaterialTheme.typography.bodyMedium)
                    }

                    Divider(modifier = Modifier.padding(vertical = 6.dp))

                    // Timeslot chips (horizontally scrollable)
                    Text("Select timeslot", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 6.dp))
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        timeslots.forEach { ts ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    onTimeslotSelected(ts)
                                    expanded = false
                                },
                                label = { Text(ts) },
                                modifier = Modifier.height(36.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Simple heuristic mapping from an hour (0..23) to a traffic level.
 * - Morning peak: 8-10 -> "high"
 * - Evening peak: 17-19 -> "high"
 * - Late night: 0-5 -> "low"
 * - Midday: 11-15 -> "medium"
 * - otherwise -> "normal"
 */
fun approxTrafficForTime(timeslot: String): String {
    return try {
        val parsed = java.time.format.DateTimeFormatter.ofPattern("h:mm a").let { fmt ->
            java.time.LocalTime.parse(timeslot, fmt)
        }
        val h = parsed.hour
        when (h) {
            in 8..10 -> "high"
            in 17..19 -> "high"
            in 0..5 -> "low"
            in 11..15 -> "medium"
            else -> "normal"
        }
    } catch (e: Exception) {
        "normal"
    }
}

// ---------- Utility: convert meters to degrees approx ----------
private fun metersToDegreesLat(meters: Double) = meters / 111_111.0
private fun metersToDegreesLng(meters: Double, lat: Double) = meters / (111_111.0 * cos(Math.toRadians(lat)))

// haversine distance (meters)
private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}

// linear interpolation between two LatLng points
private fun lerpLatLng(a: LatLng, b: LatLng, t: Double): LatLng {
    val lat = a.latitude + (b.latitude - a.latitude) * t
    val lng = a.longitude + (b.longitude - a.longitude) * t
    return LatLng(lat, lng)
}

// compute bearing in degrees from one LatLng to another
private fun computeBearing(from: LatLng, to: LatLng): Float {
    val lat1 = Math.toRadians(from.latitude)
    val lon1 = Math.toRadians(from.longitude)
    val lat2 = Math.toRadians(to.latitude)
    val lon2 = Math.toRadians(to.longitude)
    val dLon = lon2 - lon1
    val y = sin(dLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
    val bearingRad = atan2(y, x)
    return (Math.toDegrees(bearingRad).toFloat() + 360) % 360
}

// generate grid-like "streets" inside a circle area
private fun generateGridPaths(center: LatLng, radiusMeters: Double, spacingMeters: Double): List<List<LatLng>> {
    val paths = mutableListOf<List<LatLng>>()
    val latSpan = metersToDegreesLat(radiusMeters)
    val lngSpan = metersToDegreesLng(radiusMeters, center.latitude)

    val spacingLat = metersToDegreesLat(spacingMeters)
    val spacingLng = metersToDegreesLng(spacingMeters, center.latitude)

    val latMin = center.latitude - latSpan
    val latMax = center.latitude + latSpan
    val lngMin = center.longitude - lngSpan
    val lngMax = center.longitude + lngSpan

    // vertical streets
    var lng = lngMin
    while (lng <= lngMax) {
        val line = mutableListOf<LatLng>()
        var lat = latMin
        while (lat <= latMax) {
            if (haversineMeters(center.latitude, center.longitude, lat, lng) <= radiusMeters) {
                line.add(LatLng(lat, lng))
            }
            lat += spacingLat
        }
        if (line.size >= 2) paths.add(line)
        lng += spacingLng
    }

    // horizontal streets
    var lat = latMin
    while (lat <= latMax) {
        val line = mutableListOf<LatLng>()
        var lng2 = lngMin
        while (lng2 <= lngMax) {
            if (haversineMeters(center.latitude, center.longitude, lat, lng2) <= radiusMeters) {
                line.add(LatLng(lat, lng2))
            }
            lng2 += spacingLng
        }
        if (line.size >= 2) paths.add(line)
        lat += spacingLat
    }

    return paths
}

// create a simple building bitmap
private fun makeBuildingBitmap3D(widthPx: Int, heightPx: Int, color: Int): Bitmap {
    val bmp = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // base color
    paint.style = Paint.Style.FILL
    paint.color = color
    c.drawRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat() * 0.7f, paint)

    // lighter top for 3D effect
    paint.color = lightenColor(color, 0.3f)
    val topHeight = heightPx * 0.2f
    c.drawRect(0f, 0f, widthPx.toFloat(), topHeight, paint)

    // darker side
    paint.color = darkenColor(color, 0.4f)
    c.drawRect(widthPx * 0.75f, topHeight, widthPx.toFloat(), heightPx.toFloat(), paint)

    // shadow
    paint.color = 0x55000000
    c.drawRect(widthPx * 0.05f, heightPx * 0.7f, widthPx.toFloat(), heightPx.toFloat(), paint)

    // outline
    paint.style = Paint.Style.STROKE
    paint.color = 0xFF000000.toInt()
    paint.strokeWidth = 2f
    c.drawRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat() * 0.7f, paint)

    return bmp
}

private fun lightenColor(color: Int, factor: Float): Int {
    val r = ((color shr 16 and 0xFF) + (255 - (color shr 16 and 0xFF)) * factor).toInt()
    val g = ((color shr 8 and 0xFF) + (255 - (color shr 8 and 0xFF)) * factor).toInt()
    val b = ((color and 0xFF) + (255 - (color and 0xFF)) * factor).toInt()
    return (color and -0x1000000) or (r shl 16) or (g shl 8) or b
}

private fun darkenColor(color: Int, factor: Float): Int {
    val r = ((color shr 16 and 0xFF) * (1 - factor)).toInt()
    val g = ((color shr 8 and 0xFF) * (1 - factor)).toInt()
    val b = ((color and 0xFF) * (1 - factor)).toInt()
    return (color and -0x1000000) or (r shl 16) or (g shl 8) or b
}

// create litter bitmap
private fun makeLitterBitmap(sizePx: Int, color: Int): Bitmap {
    val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.style = Paint.Style.FILL
    paint.color = color
    c.drawOval(RectF(sizePx * 0.15f, sizePx * 0.2f, sizePx * 0.85f, sizePx * 0.7f), paint)
    paint.color = 0xFF444444.toInt()
    c.drawRect(sizePx * 0.35f, sizePx * 0.7f, sizePx * 0.65f, sizePx * 0.9f, paint)
    return bmp
}

// --- Vehicles and recommendations ---

data class Vehicle(
    val id: String,
    var position: LatLng,
    var heading: Float,
    var speedMps: Float,
    val type: String // "Taxi","Auto","Bus","Car"
)

sealed class Recommendation {
    data class EcoTask(
        val id: String,
        val title: String,
        val description: String,
        val rewardPoints: Int
    ) : Recommendation()

    data class AwarenessUpload(
        val id: String,
        val title: String,
        val description: String
    ) : Recommendation()

    data class DriverCheck(
        val id: String,
        val vehicleType: String,
        val description: String,
        val rewardPoints: Int
    ) : Recommendation()
}

// Helpers from before
fun approxTrafficForSlot(slot: String): String {
    return when (slot) {
        "Early Morning" -> "low"
        "Morning Peak" -> "high"
        "Noon" -> "medium"
        "Evening Peak" -> "high"
        "Night" -> "low"
        else -> "normal"
    }
}

@Composable
fun MapScreenWith3DSimEco(modifier: Modifier = Modifier.fillMaxSize()) {
    val context = LocalContext.current
    val density = LocalDensity.current

    // camera state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(12.9716, 77.5946), 15f)
    }

    // selection + double-tap detection
    var lastTapTime by remember { mutableStateOf(0L) }
    var lastTapPos by remember { mutableStateOf<LatLng?>(null) }
    val doubleTapThreshold = 350L // ms
    val doubleTapDistanceMeters = 30.0

    var selectionCenter by remember { mutableStateOf<LatLng?>(null) }
    var selectionRadius by remember { mutableStateOf(200.0) }

    // generated content
    val streets = remember { mutableStateListOf<List<LatLng>>() }
    val buildings = remember { mutableStateListOf<Pair<LatLng, Int>>() }
    val litter = remember { mutableStateListOf<LatLng>() }

    // vehicles
    val vehicles = remember { mutableStateListOf<Vehicle>() }

    // UI controls
    var runningNavigation by remember { mutableStateOf(false) }
    val navPath = remember { mutableStateListOf<LatLng>() }

    val buildingBmp: Bitmap = remember { makeBuildingBitmap3D(80, 120, 0xFF8B4513.toInt()) }
    val litterBmp: Bitmap = remember { makeLitterBitmap(40, 0xFFFFA500.toInt()) }

    var infoText by remember { mutableStateOf("Double-tap map to create recommendations for that street section") }
    val movingPos = remember { mutableStateOf(LatLng(0.0, 0.0)) }

    // Rewards
    var userPoints by remember { mutableStateOf(0) }
    val completedTasks = remember { mutableStateListOf<String>() }

    // Recommendations local to selected street section
    val localRecommendations = remember { mutableStateListOf<Recommendation>() }

    // Time slot + traffic prediction
    var selectedTimeSlot by remember { mutableStateOf("Now") }
    var trafficPredictionText by remember { mutableStateOf("Traffic: normal") }

    // Dialog state for explanations
    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }

    // Image upload (awareness only)
    var uploadedImageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uploadedImageUri = uri
    }

    // --- Heuristic helpers for street-level "usual" traffic and cleanliness ---
    fun streetVehicleCount(street: List<LatLng>): Int {
        // count vehicles within ~40m of any point on the street
        var cnt = 0
        for (v in vehicles) {
            for (i in 0 until street.size - 1) {
                val mid = lerpLatLng(street[i], street[i + 1], 0.5)
                val d = haversineMeters(v.position.latitude, v.position.longitude, mid.latitude, mid.longitude)
                if (d < 40.0) { cnt++; break }
            }
        }
        return cnt
    }

    fun streetLitterDensity(street: List<LatLng>): Double {
        // number of litter items per 100 meters of street length
        var hits = 0
        var lengthMeters = 0.0
        for (i in 0 until street.size - 1) {
            val a = street[i]; val b = street[i+1]
            lengthMeters += haversineMeters(a.latitude, a.longitude, b.latitude, b.longitude)
            val mid = lerpLatLng(a, b, 0.5)
            for (lt in litter) {
                val d = haversineMeters(lt.latitude, lt.longitude, mid.latitude, mid.longitude)
                if (d < 30.0) { hits++ }
            }
        }
        if (lengthMeters <= 0.0) return 0.0
        return hits / (lengthMeters / 100.0)
    }

    fun heuristicTrafficLevelForStreet(street: List<LatLng>): String {
        // simple heuristic combining vehicle count and time-slot expectation
        val cnt = streetVehicleCount(street)
        val base = approxTrafficForSlot(selectedTimeSlot) // low/medium/high/normal
        // amplify based on local vehicle count
        return when {
            cnt >= 8 -> "high"
            cnt >= 4 -> if (base == "low") "medium" else base
            cnt >= 1 -> if (base == "high") "high" else base
            else -> base
        }
    }

    fun explanationForSlotAndStreet(slot: String, street: List<LatLng>?): String {
        val base = approxTrafficForSlot(slot)
        val sb = StringBuilder()
        sb.append("Time slot: $slot -> typical traffic: $base. ")
        if (street != null) {
            val vCnt = streetVehicleCount(street)
            val litterD = streetLitterDensity(street)
            sb.append("This street currently has ~$vCnt vehicles nearby and litter density ${"%.2f".format(litterD)} items/100m. ")
            val finalLevel = heuristicTrafficLevelForStreet(street)
            sb.append("Heuristic traffic level for this street: $finalLevel. ")
            // actionable advice
            when (finalLevel) {
                "low" -> sb.append("Good time to travel — expect faster trips and lower emissions.")
                "medium" -> sb.append("Moderate traffic — consider off-peak or public transit for longer trips.")
                "high" -> sb.append("Heavy traffic — shifting your commute or taking public transit will likely save time and emissions.")
                else -> sb.append("Choose an off-peak slot to reduce congestion and emissions.")
            }
        } else {
            sb.append("Choose off-peak to reduce congestion.")
        }
        return sb.toString()
    }

    fun explanationForRouteChangeCompared(current: List<LatLng>?, candidate: List<LatLng>): String {
        val curVehicles = current?.let { streetVehicleCount(it) } ?: 0
        val candVehicles = streetVehicleCount(candidate)
        val curLitter = current?.let { streetLitterDensity(it) } ?: 0.0
        val candLitter = streetLitterDensity(candidate)
        val sb = StringBuilder()
        sb.append("Alternate route recommendation:\n")
        sb.append("- Current: vehicles=$curVehicles, litter=${"%.2f".format(curLitter)} per100m.\n")
        sb.append("- Alternate: vehicles=$candVehicles, litter=${"%.2f".format(candLitter)} per100m.\n")

        if (candVehicles < curVehicles) {
            sb.append("Alternate route has fewer vehicles — likely faster and less idling.")
        } else {
            sb.append("Alternate route has similar vehicle count.")
        }
        sb.append(" ")

        if (candLitter < curLitter) {
            sb.append("Also cleaner — better for walkability and health.")
        }
        return sb.toString()
    }

    fun applyTrafficToVehicles(level: String) {
        when (level) {
            "low" -> vehicles.forEach { it.speedMps = (8..12).random().toFloat() }
            "medium" -> vehicles.forEach { it.speedMps = (5..9).random().toFloat() }
            "high" -> {
                if (vehicles.size < 8) {
                    val toAdd = 8 - vehicles.size
                    val rng = Random(System.currentTimeMillis())
                    repeat(toAdd) {
                        val v = Vehicle(
                            id = "v${System.nanoTime()}",
                            position = LatLng(12.9716 + rng.nextDouble(-0.003, 0.003), 77.5946 + rng.nextDouble(-0.003, 0.003)),
                            heading = rng.nextFloat() * 360f,
                            speedMps = (2..5).random().toFloat(),
                            type = listOf("Taxi", "Auto", "Bus", "Car").random()
                        )
                        vehicles.add(v)
                    }
                }
                vehicles.forEach { it.speedMps = (2..5).random().toFloat() }
            }
            else -> vehicles.forEach { it.speedMps = (5..10).random().toFloat() }
        }
    }

    // spawn vehicles along a given street polyline
    fun spawnVehiclesOnStreet(street: List<LatLng>, count: Int) {
        val rng = Random(System.currentTimeMillis())
        if (street.isEmpty()) return
        repeat(count) {
            val idx = rng.nextInt(0, max(1, street.size - 1))
            val t = rng.nextDouble()
            val pos = lerpLatLng(street[idx], street[min(idx + 1, street.size - 1)], t)
            val heading = computeBearing(street[idx], street[min(idx + 1, street.size - 1)])
            val v = Vehicle(
                id = "v${System.nanoTime()}",
                position = pos,
                heading = heading,
                speedMps = (4..10).random().toFloat(),
                type = listOf("Taxi", "Auto", "Bus", "Car").random()
            )
            vehicles.add(v)
        }
    }

    // move vehicles loop
    LaunchedEffect(Unit) {
        while (true) {
            val dtSeconds = 0.8f
            val rng = Random(System.currentTimeMillis())
            for (i in vehicles.indices) {
                val v = vehicles[i]
                val distMeters = v.speedMps * dtSeconds
                val dLat = metersToDegreesLat(distMeters * cos(Math.toRadians(v.heading.toDouble())))
                val dLng = metersToDegreesLng(distMeters * sin(Math.toRadians(v.heading.toDouble())), v.position.latitude)
                v.position = LatLng(v.position.latitude + dLat, v.position.longitude + dLng)
                if (rng.nextFloat() < 0.08f) v.heading = (v.heading + rng.nextFloat() * 60f - 30f) % 360f
            }
            delay(700L)
        }
    }

    // navigation movement coroutine
    LaunchedEffect(runningNavigation, navPath) {
        if (!runningNavigation || navPath.size < 2) return@LaunchedEffect
        while (runningNavigation) {
            for (i in 0 until navPath.size - 1) {
                val a = navPath[i]
                val b = navPath[i + 1]
                val bearing = computeBearing(a, b)
                val steps = 40
                val stepDelay = 50L
                for (s in 1..steps) {
                    val t = s / steps.toDouble()
                    // update camera
                    val moving = lerpLatLng(a, b, t)
                    cameraPositionState.position = CameraPosition.builder()
                        .target(moving)
                        .zoom(18f)
                        .tilt(55f)
                        .bearing(bearing)
                        .build()
                    delay(stepDelay)
                }
            }
            navPath.reverse()
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapLongClick = { latlng ->
            infoText = "Generating area at ${"%.5f".format(latlng.latitude)}, ${"%.5f".format(latlng.longitude)}..."
            val radius = selectionRadius
            val spacing = 40.0
            val newStreets = generateGridPaths(latlng, radius, spacing)
            streets.clear(); streets.addAll(newStreets)

            buildings.clear()
            val rng = Random(latlng.latitude.hashCode() xor latlng.longitude.hashCode())
            for (line in newStreets) {
                for (i in line.indices step 3) {
                    val p = line[i]
                    if (rng.nextFloat() < 0.12f) {
                        buildings.add(p to (1 + rng.nextInt(3)))
                    }
                }
            }

            litter.clear()
            for (line in newStreets) {
                for (i in 0 until line.size - 1) {
                    if (rng.nextFloat() < 0.06f) {
                        val t = rng.nextDouble()
                        litter.add(lerpLatLng(line[i], line[i + 1], t))
                    }
                }
            }

            navPath.clear()
            if (streets.isNotEmpty()) navPath.addAll(streets.first())

            selectionCenter = latlng
            infoText = "Area selected: ${"%.0f".format(radius)}m radius. Streets: ${streets.size}, buildings: ${buildings.size}, litter: ${litter.size}. Double-tap a street to get recommendations."

            // initial vehicle spawn
            vehicles.clear()
            if (streets.isNotEmpty()) spawnVehiclesOnStreet(streets.first(), 5)
            applyTrafficToVehicles(approxTrafficForSlot(selectedTimeSlot))
        },
        onMapClick = { latlng ->
            val now = System.currentTimeMillis()
            val prev = lastTapTime
            val prevPos = lastTapPos
            if (now - prev <= doubleTapThreshold && prevPos != null) {
                val d = haversineMeters(prevPos.latitude, prevPos.longitude, latlng.latitude, latlng.longitude)
                if (d <= doubleTapDistanceMeters) {
                    // double-tap
                    var bestStreet: List<LatLng>? = null
                    var bestDist = Double.MAX_VALUE
                    var bestIndex = -1
                    for (si in streets.indices) {
                        val line = streets[si]
                        for (i in 0 until line.size - 1) {
                            val mid = lerpLatLng(line[i], line[i + 1], 0.5)
                            val dd = haversineMeters(latlng.latitude, latlng.longitude, mid.latitude, mid.longitude)
                            if (dd < bestDist) {
                                bestDist = dd
                                bestStreet = line
                                bestIndex = si
                            }
                        }
                    }
                    if (bestStreet != null && bestDist < 80.0) {
                        localRecommendations.clear()
                        localRecommendations.add(
                            Recommendation.EcoTask(
                                id = "commute_offpeak_${bestIndex}",
                                title = "Shift commute off-peak (this street)",
                                description = "Reduce congestion on this stretch by choosing an off-peak slot.",
                                rewardPoints = 6
                            )
                        )
                        localRecommendations.add(
                            Recommendation.DriverCheck(
                                id = "driver_taxi_check_${bestIndex}",
                                vehicleType = "Taxi",
                                description = "Encourage taxi drivers on this street to keep vehicles clean.",
                                rewardPoints = 10
                            )
                        )
                        localRecommendations.add(
                            Recommendation.AwarenessUpload(
                                id = "upload_awareness_${bestIndex}",
                                title = "Upload cleanliness photo (awareness)",
                                description = "Help raise awareness for this street (no reward)."
                            )
                        )

                        // spawn vehicles on that street
                        vehicles.clear()
                        spawnVehiclesOnStreet(bestStreet, 6)

                        // create navPath sample
                        navPath.clear()
                        val sample = bestStreet
                        for (i in 0 until sample.size step max(1, sample.size / 6)) navPath.add(sample[i])

                        infoText = "Recommendations generated for the selected street (dist ${bestDist.toInt()}m)."

                    } else {
                        infoText = "Double-tap detected but no nearby street segment found."
                    }
                    lastTapTime = 0L
                    lastTapPos = null
                } else {
                    lastTapTime = now
                    lastTapPos = latlng
                }
            } else {
                lastTapTime = now
                lastTapPos = latlng
            }
        }
    ) {
        // draw streets
        streets.forEach { Polyline(points = it, width = 6f) }

        // buildings
        buildings.forEach { (pos, hIdx) ->
            val distance = haversineMeters(
                cameraPositionState.position.target.latitude,
                cameraPositionState.position.target.longitude,
                pos.latitude, pos.longitude
            )
            val baseScale = 0.5f + hIdx * 0.5f
            val distFactor = 200f / max(50f, distance.toFloat())
            val scale = (baseScale * distFactor).coerceIn(0.3f, 1.8f)
            val w = (buildingBmp.width * scale).toInt().coerceAtLeast(8)
            val h = (buildingBmp.height * scale).toInt().coerceAtLeast(8)
            val bmp = Bitmap.createScaledBitmap(buildingBmp, w, h, true)
            Marker(
                state = MarkerState(position = pos),
                title = "Building",
                snippet = "height $hIdx",
                icon = BitmapDescriptorFactory.fromBitmap(bmp),
                zIndex = 1f
            )
        }

        // litter
        litter.forEach { lt ->
            val distance = haversineMeters(
                cameraPositionState.position.target.latitude,
                cameraPositionState.position.target.longitude,
                lt.latitude, lt.longitude
            )
            val scale = (0.4f + 0.6f * max(0.2f, 500f / max(1f, distance.toFloat()))).coerceIn(0.25f, 1f)
            val s = (litterBmp.width * scale).toInt().coerceAtLeast(6)
            val bmp = Bitmap.createScaledBitmap(litterBmp, s, s, true)
            Marker(
                state = MarkerState(position = lt),
                title = "Litter",
                snippet = "please cleanup",
                icon = BitmapDescriptorFactory.fromBitmap(bmp),
                zIndex = 2f
            )
        }

        // vehicles
        vehicles.forEach { v ->
            val ico = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            Marker(
                state = MarkerState(position = v.position),
                title = v.type,
                snippet = "${"%.1f".format(v.speedMps)} m/s",
                icon = ico,
                rotation = v.heading,
                anchor = Offset(0.5f, 0.5f),
                zIndex = 3f
            )
        }

        if (navPath.isNotEmpty()) {
            Polyline(points = navPath, width = 10f)
            Marker(state = MarkerState(position = cameraPositionState.position.target), title = "Navigator")
        }

        selectionCenter?.let { c ->
            Circle(center = c, radius = selectionRadius, strokeWidth = 2f)
        }
    }

    // --- put this right AFTER the GoogleMap(...) { ... } block and BEFORE your floating Column ---
    MapTopActionIcon(
        modifier = Modifier.padding(12.dp), // positions it away from the edge
        timeslots = generateTimeslots(),
        onUploadClicked = {
            // use the existing imagePicker you already defined in this composable
            imagePicker.launch("image/*")
        },
        onTimeslotSelected = { timeslot ->
            // update your existing states (selectedTimeSlot, trafficPredictionText, applyTrafficToVehicles, and show dialog)
            selectedTimeSlot = timeslot
            val level = approxTrafficForTime(timeslot)
            trafficPredictionText = "Traffic: $level"
            applyTrafficToVehicles(level)

            // pick a heuristic street (re-using logic you already have)
            var heuristicStreet: List<LatLng>? = null
            if (streets.isNotEmpty()) {
                val pathMid = if (navPath.isNotEmpty()) navPath[navPath.size / 2] else cameraPositionState.position.target
                heuristicStreet = streets.minByOrNull { st ->
                    val mids = st[st.size / 2]
                    haversineMeters(pathMid.latitude, pathMid.longitude, mids.latitude, mids.longitude)
                }
            }

            dialogTitle = "Time: $timeslot"
            dialogMessage = explanationForSlotAndStreet(timeslot, heuristicStreet)
            showDialog = true
        }
    )

    // Floating UI panel with recommendations and controls
    Column(
        modifier = Modifier.padding(12.dp).zIndex(10f)
    ) {
        Card(shape = RoundedCornerShape(8.dp)) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = infoText)
                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    Button(onClick = { runningNavigation = !runningNavigation }) {
                        Text(if (runningNavigation) "Stop nav" else "Start nav")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        selectionCenter = null
                        streets.clear(); buildings.clear(); litter.clear(); navPath.clear()
                        localRecommendations.clear(); uploadedImageUri = null; vehicles.clear()
                        infoText = "Cleared"
                    }) { Text("Clear") }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(trafficPredictionText, modifier = Modifier.padding(4.dp))

                Spacer(modifier = Modifier.height(8.dp))

                // show current points
                Text("Your points: $userPoints", modifier = Modifier.padding(4.dp))

                Spacer(modifier = Modifier.height(8.dp))

                // Recommendations local to a double-tapped street
                if (localRecommendations.isEmpty()) {
                    Text("Double-tap a street to view recommendations for that street segment.")
                } else {
                    localRecommendations.forEach { rec ->
                        when (rec) {
                            is Recommendation.EcoTask -> {
                                Card(modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(rec.title, style = MaterialTheme.typography.titleMedium)
                                            Text(rec.description, style = MaterialTheme.typography.bodySmall)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(onClick = {
                                            if (!completedTasks.contains(rec.id)) {
                                                completedTasks.add(rec.id)
                                                userPoints += rec.rewardPoints
                                                // pick an alternate street and compare heuristics
                                                if (streets.size > 1) {
                                                    val rng = Random(System.currentTimeMillis())
                                                    val pick = streets[rng.nextInt(streets.size)]
                                                    val currentStreet = if (navPath.size >= 2) {
                                                        // try to find a street that matches navPath endpoints
                                                        streets.minByOrNull { st ->
                                                            val d1 = haversineMeters(navPath.first().latitude, navPath.first().longitude, st[0].latitude, st[0].longitude)
                                                            val d2 = haversineMeters(navPath.last().latitude, navPath.last().longitude, st[st.size-1].latitude, st[st.size-1].longitude)
                                                            d1 + d2
                                                        }
                                                    } else null

                                                    navPath.clear();
                                                    for (i in 0 until pick.size step max(1, pick.size / 6)) navPath.add(pick[i])
                                                    // new litter and vehicles
                                                    litter.clear(); vehicles.clear()
                                                    val r2 = Random(System.currentTimeMillis())
                                                    for (line in streets) {
                                                        for (i in 0 until line.size - 1) if (r2.nextFloat() < 0.05f) {
                                                            litter.add(lerpLatLng(line[i], line[i + 1], r2.nextDouble()))
                                                        }
                                                    }
                                                    spawnVehiclesOnStreet(pick, 6)
                                                    infoText = "Followed '${rec.title}'. Switching to alternate route and updating vehicles/litter."
                                                    // show explanation comparing current vs candidate
                                                    dialogTitle = "Why this route?"
                                                    dialogMessage = explanationForRouteChangeCompared(currentStreet, pick)
                                                    showDialog = true
                                                }
                                            }
                                        }) {
                                            Text("Claim +${rec.rewardPoints}")
                                        }
                                    }
                                }
                            }

                            is Recommendation.AwarenessUpload -> {
                                Card(modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(rec.title, style = MaterialTheme.typography.titleMedium)
                                            Text(rec.description, style = MaterialTheme.typography.bodySmall)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(onClick = { imagePicker.launch("image/*") }) { Text("Upload") }
                                    }
                                }
                            }

                            is Recommendation.DriverCheck -> {
                                Card(modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Driver cleanliness: ${rec.vehicleType}", style = MaterialTheme.typography.titleMedium)
                                            Text(rec.description, style = MaterialTheme.typography.bodySmall)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(onClick = {
                                            if (!completedTasks.contains(rec.id)) {
                                                completedTasks.add(rec.id)
                                                userPoints += rec.rewardPoints
                                                // small cleanup effect
                                                val removed = min(litter.size, 3)
                                                repeat(removed) { if (litter.isNotEmpty()) litter.removeAt(0) }
                                                infoText = "Driver verified. Awarded ${rec.rewardPoints} points and cleaned some litter."
                                                // show explanation why driver cleanliness matters here
                                                dialogTitle = "Why verify driver cleanliness?"
                                                dialogMessage = "Cleaner vehicles reduce local trash and improve passenger comfort; drivers who keep vehicles clean help make the route safer and healthier for everyone."
                                                showDialog = true
                                            }
                                        }) { Text("Verify +${rec.rewardPoints}") }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    // Dialog UI
    if (showDialog) {
        AlertDialog(onDismissRequest = { showDialog = false }, confirmButton = {
            Button(onClick = { showDialog = false }) { Text("OK") }
        }, title = { Text(dialogTitle) }, text = { Text(dialogMessage) })
    }
}
