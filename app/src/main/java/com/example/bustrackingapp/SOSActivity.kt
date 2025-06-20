package com.example.bustrackingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.content.Intent
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SOSActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sosactivity)
        val homeButton = findViewById<FloatingActionButton>(R.id.homeButton)
        homeButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish() // Optional: Close TrackBusActivity
        }
    }
}