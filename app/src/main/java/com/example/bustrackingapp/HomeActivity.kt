package com.example.bustrackingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.widget.Toolbar
import com.google.android.material.navigation.NavigationView
import android.content.Intent
import androidx.appcompat.app.ActionBarDrawerToggle
import android.widget.Toast

class HomeActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle // Declare toggle as a class-level variable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.hamburger)

        // Initialize the toggle
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }
        }
    }

    // Click handlers for each card
    fun onTrackBusClick(view: android.view.View) {
        startActivity(Intent(this, TrackBus::class.java))
    }

    fun onAttendanceClick(view: android.view.View) {
        startActivity(Intent(this, AttendanceActivity::class.java))
        // Replace with: startActivity(Intent(this, AttendanceActivity::class.java))
    }

    fun onSOSClick(view: android.view.View) {
        startActivity(Intent(this, SOSActivity::class.java))
        // Replace with: startActivity(Intent(this, SOSActivity::class.java))
    }

    fun onNotificationClick(view: android.view.View) {
        startActivity(Intent(this, NotificationActivity::class.java))
        // Replace with: startActivity(Intent(this, NotificationActivity::class.java))
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) { // Now toggle is accessible
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}