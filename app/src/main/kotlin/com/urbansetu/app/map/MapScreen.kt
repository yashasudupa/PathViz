package com.urbansetu.app.map

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.urbansetu.app.data.AssetUtils
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    jsonPathAsset: String? = null // optional: path to a GeoJSON file in assets
) {
    // Camera position state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(12.9716, 77.5946), 12f) // default: Bengaluru
    }

    // Parse coordinates from JSON if provided
    val polylinePoints = remember(jsonPathAsset) {
        mutableStateListOf<LatLng>().apply {
            jsonPathAsset?.let { assetName ->
                try {
                    val jsonStr = AssetUtils.readAsset(context, assetName)
                    val pointsArray = JSONObject(jsonStr).getJSONArray("points")
                    for (i in 0 until pointsArray.length()) {
                        val obj = pointsArray.getJSONObject(i)
                        add(LatLng(obj.getDouble("lat"), obj.getDouble("lng")))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Google Map Composable
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = false),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = false,
            compassEnabled = true
        )
    ) {
        // Draw polyline if any points exist
        if (polylinePoints.isNotEmpty()) {
            Polyline(
                points = polylinePoints,
                color = androidx.compose.ui.graphics.Color.Blue,
                width = 5f
            )
        }

        // Optional: Add markers at each point
        for (point in polylinePoints) {
            Marker(
                state = MarkerState(position = point),
                title = "Point",
                snippet = "${point.latitude}, ${point.longitude}"
            )
        }
    }
}
