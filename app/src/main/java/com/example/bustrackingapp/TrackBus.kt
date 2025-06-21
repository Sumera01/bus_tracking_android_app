package com.example.bustrackingapp

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
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

    private val testLat = 18.5204    // 📍 Pune coordinates
    private val testLng = 73.8567
    private val busId = "BUS01"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_bus2)

        // 🧭 Request location permissions
        requestPermissionsIfNeeded()

        // 📄 Bus Details UI
        busDetailsTextView = findViewById(R.id.busDetailsTextView)

        // 🏠 Home button
        findViewById<FloatingActionButton>(R.id.homeButton).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        // 📍 Initialize geofencing
        geofencingClient = LocationServices.getGeofencingClient(this)
        geofencePendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(this, GeofenceBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 🗺️ Load map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        val location = LatLng(testLat, testLng)

        mMap.addMarker(
            MarkerOptions().position(location).title("Bus: $busId")
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16f))

        busDetailsTextView.text = """
            Bus ID: $busId
            Latitude: $testLat
            Longitude: $testLng
        """.trimIndent()

        addGeofence(location, "SchoolZone")
    }

    private fun addGeofence(center: LatLng, id: String) {
        val geofence = Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(center.latitude, center.longitude, 100f)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
            )
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        geofencingClient.addGeofences(request, geofencePendingIntent)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Geofence Added for $id", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "❌ Failed to add Geofence", Toast.LENGTH_SHORT).show()
            }
    }

    private fun requestPermissionsIfNeeded() {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        if (permissions.any {
                ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 1001)
        }
    }
}
