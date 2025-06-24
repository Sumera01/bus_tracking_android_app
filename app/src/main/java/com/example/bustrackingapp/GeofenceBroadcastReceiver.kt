package com.example.bustrackingapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent == null || geofencingEvent.hasError()) {
            Toast.makeText(context, "⚠️ Geofencing error occurred", Toast.LENGTH_SHORT).show()
            return
        }

        val transitionType = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences

        if (!triggeringGeofences.isNullOrEmpty()) {
            for (geofence in triggeringGeofences) {
                val zoneId = geofence.requestId
                val transitionMsg = when (transitionType) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> "🟢 Entered zone: $zoneId"
                    Geofence.GEOFENCE_TRANSITION_EXIT -> "🔴 Exited zone: $zoneId"
                    else -> "⚠️ Unknown geofence transition"
                }

                Toast.makeText(context, transitionMsg, Toast.LENGTH_LONG).show()

                // 🔔 Show notification
                sendNotification(context, "Geofence Alert", transitionMsg)

                // 🌐 TODO: Call backend API to notify Parent/Admin
                // sendToBackend(zoneId, transitionType)
            }
        } else {
            Toast.makeText(context, "⚠️ No triggering geofences", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendNotification(context: Context, title: String, message: String) {
        val channelId = "geo_alert_channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 🔧 Create notification channel (for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Geofence Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    // Optional: send to your server (you can implement using Retrofit/Volley/HttpUrlConnection)
    /*
    private fun sendToBackend(zoneId: String, transition: Int) {
        // Example API call with Retrofit
    }
    */
}
