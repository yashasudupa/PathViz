package com.urbansetu.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import com.google.android.gms.maps.model.LatLng

object AssetUtils {
  fun readAsset(ctx: Context, name: String): String =
    ctx.assets.open(name).bufferedReader().use { it.readText() }

  // Expects json like: { "points": [ {"lat":..., "lng":...}, ... ] }
  fun latLngsFromPath(json: String): List<LatLng> {
    val arr = JSONObject(json).getJSONArray("points")
    return (0 until arr.length()).map { i ->
      val o = arr.getJSONObject(i)
      LatLng(o.getDouble("lat"), o.getDouble("lng"))
    }
  }

  // Produce a GeoJSON FeatureCollection string containing one Feature with a LineString geometry.
  // Coordinates are [longitude, latitude] per GeoJSON spec.
  fun lineString(coords: List<LatLng>): String {
    val coordsArray = JSONArray()
    for (c in coords) {
      val pos = JSONArray()
      pos.put(c.longitude)
      pos.put(c.latitude)
      coordsArray.put(pos)
    }

    val geometry = JSONObject()
      .put("type", "LineString")
      .put("coordinates", coordsArray)

    val feature = JSONObject()
      .put("type", "Feature")
      .put("geometry", geometry)
      .put("properties", JSONObject())

    val fc = JSONObject()
      .put("type", "FeatureCollection")
      .put("features", JSONArray().put(feature))

    return fc.toString()
  }
}
