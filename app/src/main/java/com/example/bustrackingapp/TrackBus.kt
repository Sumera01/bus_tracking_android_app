package com.example.bustrackingapp

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TrackBus : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofencePendingIntent: PendingIntent
    private lateinit var busDetailsTextView: TextView

    // Request code for location permission
    private val LOCATION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_bus2)

        busDetailsTextView = findViewById(R.id.busDetailsTextView)

        findViewById<FloatingActionButton>(R.id.homeButton).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        // Request permissions at runtime
        requestLocationPermissions()

        // Initialize geofencing
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

        // 📍 D.Y. Patil School Location
        val collegeLocation = LatLng(16.6875, 74.2187)

        // 🚌 Add marker
        mMap.addMarker(MarkerOptions().position(collegeLocation).title("Bus: BUS01"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(collegeLocation, 14f))

        busDetailsTextView.text = """
            Bus ID: BUS01
            Latitude: ${collegeLocation.latitude}
            Longitude: ${collegeLocation.longitude}
        """.trimIndent()

        // Start geofencing
        addGeofences()
    }

    private fun addGeofences() {
        val geofences = listOf(
            Geofence.Builder()
                .setRequestId("dy_patils_college")
                .setCircularRegion(16.6875, 74.2187, 150f)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build(),

            Geofence.Builder()
                .setRequestId("shahupuri_stop")
                .setCircularRegion(16.7054, 74.2400, 150f)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
        )

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofences)
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "❗ Location permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        geofencingClient.addGeofences(request, geofencePendingIntent)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Geofences added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "❌ Failed to add geofences: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
    }

    private fun requestLocationPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )

        if (permissions.any {
                ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
        ) {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_REQUEST_CODE)
        }
    }
}
