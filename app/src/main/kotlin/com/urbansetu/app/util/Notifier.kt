package com.urbansetu.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.urbansetu.app.R

object Notifier {
    private const val CHANNEL_ID = "offers"
    private var channelReady = false

    fun ensureChannel(ctx: Context) {
        if (channelReady) return
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val ch = NotificationChannel(CHANNEL_ID, "Offers near you",
            NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Brand proximity offers"
            enableLights(true); lightColor = Color.CYAN
            enableVibration(true)
        }
        nm.createNotificationChannel(ch)
        channelReady = true
    }

    fun notifyBrand(ctx: Context, id: String, title: String, text: String) {
        ensureChannel(ctx)
        val n = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_offer)       // or another valid icon
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(ctx).notify(id.hashCode(), n)
    }
}
