package com.urbansetu.app.util

import android.Manifest
import android.os.Build

object Permissions {
  val location: Array<String> = if (Build.VERSION.SDK_INT >= 29) arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
  ) else arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
}
