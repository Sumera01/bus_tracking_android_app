package com.example.bustrackingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.content.Intent

class NextActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_next)

        val btnNext = findViewById<Button>(R.id.nextButton)
        btnNext.setOnClickListener {
            // Example: Navigate back to TrackBusActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }
}