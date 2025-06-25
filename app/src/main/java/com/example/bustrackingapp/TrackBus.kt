package com.example.bustrackingapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import kotlin.math.*

class TrackBus : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var busDetailsTextView: TextView
    private var busMarker: Marker? = null
    private var job: Job? = null
    private var isAppInForeground: Boolean = true

    private val LOCATION_PERMISSION_CODE = 100

    // Geofence zones
    private val geofenceList = listOf(
        LatLng(16.7054, 74.2400) to "Shahupuri_Stop", // Starting point
        LatLng(16.6875, 74.2187) to "DYP_College"   // Destination
    )

    // Simulated bus route coordinates from Shahupuri to DYP
    private val busRoute = listOf(
        LatLng(16.7054, 74.2400), // Shahupuri
        LatLng(16.7020, 74.2365), // Intermediate point 1
        LatLng(16.6980, 74.2320), // Intermediate point 2
        LatLng(16.6940, 74.2265), // Intermediate point 3
        LatLng(16.6875, 74.2187)  // DYP College
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_bus2)

        busDetailsTextView = findViewById(R.id.busDetailsTextView)

        findViewById<FloatingActionButton>(R.id.homeButton).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        drawGeofences()

        if (hasLocationPermission()) {
            startBusRouteSimulation()
        } else {
            requestLocationPermission()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE && hasLocationPermission()) {
            startBusRouteSimulation()
        } else {
            Toast.makeText(this, "❗ Location permission required to show map", Toast.LENGTH_LONG).show()
        }
    }

    private fun drawGeofences() {
        val boundsBuilder = LatLngBounds.builder()
        for ((location, id) in geofenceList) {
            mMap.addCircle(
                CircleOptions()
                    .center(location)
                    .radius(300.0)
                    .strokeColor(0xFF00695C.toInt())
                    .fillColor(0x3065D6BA)
                    .strokeWidth(3f)
            )
            mMap.addMarker(MarkerOptions().position(location).title("Zone: $id"))
            boundsBuilder.include(location)
        }
        // Add polyline for the bus route
        mMap.addPolyline(
            PolylineOptions()
                .addAll(busRoute)
                .color(0xFF2196F3.toInt())
                .width(5f)
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
    }

    private fun startBusRouteSimulation() {
        job = CoroutineScope(Dispatchers.Main).launch {
            var currentSegment = 0
            var progress = 0.0
            val segmentDuration = 5000L // 5 seconds per segment

            while (isActive && currentSegment < busRoute.size - 1) {
                val start = busRoute[currentSegment]
                val end = busRoute[currentSegment + 1]

                // Interpolate position
                val lat = start.latitude + (end.latitude - start.latitude) * progress
                val lng = start.longitude + (end.longitude - start.longitude) * progress
                val currentPos = LatLng(lat, lng)

                // Update bus marker
                busMarker?.remove()
                busMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(currentPos)
                        .title("🚌 Bus Live")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                )
                mMap.animateCamera(CameraUpdateFactory.newLatLng(currentPos))

                // Update TextView
                busDetailsTextView.text = "Bus Location:\nLat: %.4f\nLng: %.4f".format(lat, lng)

                // Check geofence transitions
                if (isAppInForeground) {
                    checkGeofenceTransition(currentPos)
                }

                // Update progress
                progress += 0.05 // Adjust for smoother movement (20 updates per segment)
                if (progress >= 1.0) {
                    progress = 0.0
                    currentSegment++
                }

                delay(segmentDuration / 20) // Update 20 times per segment
            }

            // Bus has reached DYP College
            if (currentSegment == busRoute.size - 1) {
                busMarker?.remove()
                busMarker = mMap.addMarker(
                    MarkerOptions()
                        .position(busRoute.last())
                        .title("🚌 Bus at DYP_College")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                )
                busDetailsTextView.text = "Bus has reached DYP College"
                if (isActive && isAppInForeground) {
                    sendNotification("Geofence Alert", "🟢 Bus has arrived at DYP_College")
                }
            }
        }
    }

    private fun checkGeofenceTransition(busLocation: LatLng) {
        for ((center, id) in geofenceList) {
            val distance = FloatArray(1)
            android.location.Location.distanceBetween(
                busLocation.latitude, busLocation.longitude,
                center.latitude, center.longitude, distance
            )
            val isInside = distance[0] <= 300f

            // Store previous state to detect transitions
            val wasInsideKey = "wasInside_$id"
            val wasInside = getSharedPreferences("BusTrackingPrefs", MODE_PRIVATE)
                .getBoolean(wasInsideKey, false)

            if (isInside && !wasInside) {
                sendNotification("Geofence Alert", "🟢 Bus entered zone: $id")
                getSharedPreferences("BusTrackingPrefs", MODE_PRIVATE).edit()
                    .putBoolean(wasInsideKey, true).apply()
            } else if (!isInside && wasInside) {
                sendNotification("Geofence Alert", "🔴 Bus exited zone: $id")
                getSharedPreferences("BusTrackingPrefs", MODE_PRIVATE).edit()
                    .putBoolean(wasInsideKey, false).apply()
            }
        }
    }

    private fun sendNotification(title: String, message: String) {
        if (isAppInForeground) {
            val intent = Intent(this, GeofenceBroadcastReceiver::class.java).apply {
                putExtra("title", title)
                putExtra("message", message)
            }
            sendBroadcast(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        isAppInForeground = true
    }

    override fun onPause() {
        super.onPause()
        isAppInForeground = false
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }
}