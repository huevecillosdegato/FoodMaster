package com.foodmaster.app.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.foodmaster.app.R
import com.foodmaster.app.domain.model.InventoryItem

/** Builds and posts the "expiring soon" notification. */
object ExpiryNotifier {

    private const val CHANNEL_ID = "expiry_alerts"
    private const val NOTIFICATION_ID = 1001

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Caducidades",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Avisos de productos próximos a caducar" }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission") // guarded by the POST_NOTIFICATIONS check below
    fun notifyExpiring(context: Context, items: List<InventoryItem>) {
        if (items.isEmpty()) return
        ensureChannel(context)

        // POST_NOTIFICATIONS is required from API 33; skip silently if not granted.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val names = items.take(5).joinToString(", ") { it.product.name }
        val title = if (items.size == 1) {
            "1 producto caduca pronto"
        } else {
            "${items.size} productos caducan pronto"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(names)
            .setStyle(NotificationCompat.BigTextStyle().bigText(names))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
