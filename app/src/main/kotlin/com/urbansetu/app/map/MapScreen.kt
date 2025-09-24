package com.urbansetu.app.map
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlin.math.*
import kotlin.random.Random
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex

// ---------- Utility: convert meters to degrees approx ----------
private fun metersToDegreesLat(meters: Double) = meters / 111_111.0
private fun metersToDegreesLng(meters: Double, lat: Double) = meters / (111_111.0 * cos(Math.toRadians(lat)))

// haversine distance (meters)
private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat/2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon/2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1-a))
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
private fun makeBuildingBitmap(widthPx: Int, heightPx: Int, color: Int): Bitmap {
    val bmp = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.style = Paint.Style.FILL
    paint.color = color
    c.drawRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat() * 0.7f, paint)
    paint.color = (color and 0x00FFFFFF) or (0x66000000.toInt() and -0x1000000)
    c.drawRect(widthPx*0.75f, heightPx*0.1f, widthPx.toFloat(), heightPx.toFloat(), paint)
    paint.style = Paint.Style.STROKE
    paint.color = 0xFF000000.toInt()
    paint.strokeWidth = 2f
    c.drawRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat() * 0.7f, paint)
    return bmp
}

// create litter bitmap
private fun makeLitterBitmap(sizePx: Int, color: Int): Bitmap {
    val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.style = Paint.Style.FILL
    paint.color = color
    c.drawOval(RectF(sizePx*0.15f, sizePx*0.2f, sizePx*0.85f, sizePx*0.7f), paint)
    paint.color = 0xFF444444.toInt()
    c.drawRect(sizePx*0.35f, sizePx*0.7f, sizePx*0.65f, sizePx*0.9f, paint)
    return bmp
}

@Composable
fun MapScreenWith3DSim(modifier: Modifier = Modifier.fillMaxSize()) {
    val context = LocalContext.current
    val density = LocalDensity.current

    // camera state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(12.9716, 77.5946), 15f)
    }

    // user selection
    var selectionCenter by remember { mutableStateOf<LatLng?>(null) }
    var selectionRadius by remember { mutableStateOf(200.0) }

    // generated content
    val streets = remember { mutableStateListOf<List<LatLng>>() }
    val buildings = remember { mutableStateListOf<Pair<LatLng, Int>>() }
    val litter = remember { mutableStateListOf<LatLng>() }

    // UI controls
    var runningNavigation by remember { mutableStateOf(false) }
    val navPath = remember { mutableStateListOf<LatLng>() }

    val buildingBmp = remember { makeBuildingBitmap(80, 120, 0xFF8B4513.toInt()) }
    val litterBmp = remember { makeLitterBitmap(40, 0xFFFFA500.toInt()) }

    var infoText by remember { mutableStateOf("Long-press map to select area center") }

    val movingPos = remember { mutableStateOf(LatLng(0.0, 0.0)) }

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
                    movingPos.value = lerpLatLng(a, b, t)
                    cameraPositionState.position = CameraPosition.builder()
                        .target(movingPos.value)
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
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        onMapLongClick = { latlng ->
            selectionCenter = latlng

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
                for (i in 0 until line.size-1) {
                    if (rng.nextFloat() < 0.06f) {
                        val t = rng.nextDouble()
                        litter.add(lerpLatLng(line[i], line[i+1], t))
                    }
                }
            }

            navPath.clear()
            if (streets.isNotEmpty()) navPath.addAll(streets.first())

            infoText = "Area selected: ${"%.0f".format(radius)}m radius. Streets: ${streets.size}, buildings: ${buildings.size}, litter: ${litter.size}"
        }
    ) {
        streets.forEach { Polyline(points = it, width = 6f) }
        buildings.forEach { (pos, hIdx) ->
            val scale = 0.6f + hIdx*0.5f
            val bmp = Bitmap.createScaledBitmap(buildingBmp, (buildingBmp.width*scale).toInt(), (buildingBmp.height*scale).toInt(), true)
            Marker(
                state = MarkerState(position = pos),
                title = "Building",
                snippet = "height $hIdx",
                icon = BitmapDescriptorFactory.fromBitmap(bmp),
                zIndex = 1f
            )
        }
        litter.forEach { lt ->
            Marker(
                state = MarkerState(position = lt),
                title = "Litter",
                snippet = "please cleanup",
                icon = BitmapDescriptorFactory.fromBitmap(litterBmp),
                zIndex = 2f
            )
        }
        if (navPath.isNotEmpty()) {
            Polyline(points = navPath, width = 10f)
            Marker(state = MarkerState(position = movingPos.value), title = "Navigator")
        }
        selectionCenter?.let { c ->
            Circle(center = c, radius = selectionRadius, strokeWidth = 2f)
        }
    }

    Column(
        modifier = Modifier
            .padding(12.dp)
            .zIndex(10f)
    ) {
        androidx.compose.material3.Card {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(infoText)
                Row {
                    Button(onClick = { runningNavigation = !runningNavigation }) {
                        Text(if (runningNavigation) "Stop nav" else "Start nav")
                    }
                    Button(onClick = {
                        selectionCenter = null
                        streets.clear(); buildings.clear(); litter.clear(); navPath.clear()
                        infoText = "Cleared"
                    }) { Text("Clear") }
                }
            }
        }
    }
}
