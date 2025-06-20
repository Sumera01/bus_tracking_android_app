package com.example.bustrackingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.content.Intent

class AttendanceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

        // Find the home button and set click listener
        val homeButton = findViewById<FloatingActionButton>(R.id.homeButton)
        homeButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish() // Optional: Close TrackBusActivity
        }
    }
}