package com.example.bustrackingapp

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TrackBus : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofencePendingIntent: PendingIntent
    private lateinit var busDetailsTextView: TextView

    private val geofenceList = listOf(
        LatLng(16.6875, 74.2187) to "DYP_College", // DY Patil College
        LatLng(16.7054, 74.2400) to "Shahupuri_Stop" // Shahupuri Stop
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_bus2)

        busDetailsTextView = findViewById(R.id.busDetailsTextView)

        findViewById<FloatingActionButton>(R.id.homeButton).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        geofencingClient = LocationServices.getGeofencingClient(this)
        geofencePendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(this, GeofenceBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        val boundsBuilder = LatLngBounds.builder()

        for ((location, id) in geofenceList) {
            // 🟢 Draw circle for each geofence
            mMap.addCircle(
                CircleOptions()
                    .center(location)
                    .radius(150.0)
                    .strokeColor(0xFF6200EE.toInt()) // Purple border
                    .fillColor(0x306200EE) // Semi-transparent fill
                    .strokeWidth(3f)
            )

            // 📍 Marker
            mMap.addMarker(MarkerOptions().position(location).title("Zone: $id"))

            // 📦 Include in bounds
            boundsBuilder.include(location)
        }

        // 📸 Move camera to fit all geofences with padding
        val bounds = boundsBuilder.build()
        val padding = 100 // px
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))

        busDetailsTextView.text = "Geofence zones initialized."

        // 🔐 Check permissions & add geofences
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "❗ Grant location permission", Toast.LENGTH_SHORT).show()
            return
        }

        addGeofences()
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun addGeofences() {
        val geofences = geofenceList.map { (latLng, id) ->
            Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(latLng.latitude, latLng.longitude, 150f)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(
                    Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
                )
                .build()
        }

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofences)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        geofencingClient.addGeofences(request, geofencePendingIntent)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Geofences added", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "❌ Failed to add geofences", Toast.LENGTH_SHORT).show()
            }
    }
}
