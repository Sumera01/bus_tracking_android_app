package com.example.bustrackingapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
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

        if (triggeringGeofences != null && triggeringGeofences.isNotEmpty()) {
            for (geofence in triggeringGeofences) {
                val zoneId = geofence.requestId
                when (transitionType) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> {
                        Toast.makeText(context, "🟢 Entered zone: $zoneId", Toast.LENGTH_LONG).show()
                        // TODO: Trigger notification / send to backend for Parent/Admin
                    }

                    Geofence.GEOFENCE_TRANSITION_EXIT -> {
                        Toast.makeText(context, "🔴 Exited zone: $zoneId", Toast.LENGTH_LONG).show()
                        // TODO: Trigger notification / send to backend for Parent/Admin
                    }

                    else -> {
                        Toast.makeText(context, "⚠️ Unknown geofence transition", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(context, "⚠️ No triggering geofences", Toast.LENGTH_SHORT).show()
        }
    }
}
