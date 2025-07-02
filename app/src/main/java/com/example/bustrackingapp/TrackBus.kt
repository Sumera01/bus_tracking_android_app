package com.example.bustrackingapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TrackBus : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var busDetailsTextView: TextView
    private lateinit var busInfoTextView: TextView
    private var busMarker: Marker? = null
    private var routeJob: Job? = null

    companion object {
        private const val LOCATION_PERMISSION_CODE = 100
        private const val GEOFENCE_RADIUS = 300f
        private const val API_KEY = "AIzaSyDjWXHa4cpYsQk01UBQUi6WtLtaZRRm1RI"
    }

    private val zones = listOf(
        LatLng(16.6999, 74.2323) to "Rajarampuri",
        LatLng(16.7288, 74.2443) to "DY Patil College"
    )

    private val busId = "bus_201"
    private val busName = "rajarampuri nus"
    private val driverName = "Ramesh Patil"
    private val route = "Rajarampuri → DY Patil College"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_bus2)

        busDetailsTextView = findViewById(R.id.busDetailsTextView)
        busInfoTextView = findViewById(R.id.busInfoTextView)

        busInfoTextView.text = """
             Bus ID: $busId
             Name: $busName
            ️ Driver: $driverName
             Route: $route
        """.trimIndent()

        findViewById<FloatingActionButton>(R.id.homeButton).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zones.first().first, 13f))

        drawZones()

        if (hasLocationPermission()) {
            lifecycleScope.launch {
                drawRouteViaRoads(zones[0].first, zones[1].first, route)
            }
        } else {
            requestLocationPermission()
        }
    }

    private suspend fun drawRouteViaRoads(origin: LatLng, destination: LatLng, label: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(DirectionsService::class.java)
        val response = service.getRoute(
            "${origin.latitude},${origin.longitude}",
            "${destination.latitude},${destination.longitude}",
            API_KEY
        )

        if (response.routes.isNotEmpty()) {
            val points = response.routes[0].overview_polyline.points
            val path = PolyUtil.decode(points)

            mMap.addPolyline(PolylineOptions().addAll(path).color(Color.BLUE).width(6f))
            mMap.addMarker(MarkerOptions().position(origin).title("Start: $label"))
            mMap.addMarker(MarkerOptions().position(destination).title("End: $label"))

            simulateRoute(path)
        }
    }

    private fun drawZones() {
        zones.forEach { (center, id) ->
            mMap.addCircle(
                CircleOptions()
                    .center(center)
                    .radius(GEOFENCE_RADIUS.toDouble())
                    .strokeColor(Color.MAGENTA)
                    .fillColor(0x30FF00FF)
                    .strokeWidth(4f)
            )
            mMap.addMarker(MarkerOptions().position(center).title("Zone: $id"))
        }
    }

    private fun hasLocationPermission() = ActivityCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(rq: Int, perms: Array<String>, grants: IntArray) {
        super.onRequestPermissionsResult(rq, perms, grants)
        if (rq == LOCATION_PERMISSION_CODE && hasLocationPermission()) {
            lifecycleScope.launch {
                drawRouteViaRoads(zones[0].first, zones[1].first, route)
            }
        }
    }

    private fun simulateRoute(route: List<LatLng>) {
        routeJob?.cancel()
        routeJob = CoroutineScope(Dispatchers.Main).launch {
            val zoneStates = mutableMapOf<String, Boolean>()
            for (pos in route) {
                busMarker?.remove()
                busMarker = mMap.addMarker(
                    MarkerOptions().position(pos)
                        .title("🚌 Bus: %.4f, %.4f".format(pos.latitude, pos.longitude))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                )
                mMap.animateCamera(CameraUpdateFactory.newLatLng(pos))
                busDetailsTextView.text = "Bus at:\nLat: %.4f\nLng: %.4f".format(pos.latitude, pos.longitude)

                handleZoneTransitions(pos, zoneStates)
                delay(2000L)
            }
        }
    }

    private fun handleZoneTransitions(pos: LatLng, zoneStates: MutableMap<String, Boolean>) {
        zones.forEach { (center, zoneId) ->
            val dist = FloatArray(1)
            Location.distanceBetween(pos.latitude, pos.longitude, center.latitude, center.longitude, dist)
            val currentlyInside = dist[0] <= GEOFENCE_RADIUS
            val wasInside = zoneStates[zoneId] ?: false
            if (currentlyInside && !wasInside) {
                sendNotification("🟢 Entered $zoneId")
                zoneStates[zoneId] = true
            } else if (!currentlyInside && wasInside) {
                sendNotification("🔴 Exited $zoneId")
                zoneStates[zoneId] = false
            }
        }
    }

    private fun sendNotification(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        routeJob?.cancel()
    }
}
