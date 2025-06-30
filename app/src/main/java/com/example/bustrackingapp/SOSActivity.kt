package com.example.bustrackingapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class SOSActivity : AppCompatActivity() {

    private lateinit var delayButton: Button
    private lateinit var emergencyButton: Button
    private lateinit var statusText: TextView

    private val busId = "bus_202" // Updated example bus ID
    private val apiUrl = "https://bus-tracking-app-wt0f.onrender.com/api/alerts"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sosactivity)

        // Initialize UI elements
        delayButton = findViewById(R.id.delayButton)
        emergencyButton = findViewById(R.id.emergencyButton)
        statusText = findViewById(R.id.statusText)

        // Set button click listeners
        delayButton.setOnClickListener {
            sendNotification(
                type = "Delay",
                message = "Bus $busId is delayed by 10 minutes",
                severity = "warning",
                audience = "admin,parent"
            )
        }

        emergencyButton.setOnClickListener {
            sendNotification(
                type = "Emergency",
                message = "SOS triggered by driver on Bus $busId",
                severity = "danger",
                audience = "admin"
            )
        }
    }

    private fun sendNotification(type: String, message: String, severity: String, audience: String) {
        val queue = Volley.newRequestQueue(this)
        val jsonBody = JSONObject().apply {
            put("busId", busId)
            put("type", type)
            put("message", message)
            put("latitude", 19.0760) // Hardcoded for now
            put("longitude", 72.8777) // Hardcoded for now
            put("severity", severity)
            put("audience", audience)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, apiUrl, jsonBody,
            { response ->
                statusText.text = "Notification sent: ${response.getString("message")}"
                Toast.makeText(this, "Notification sent successfully", Toast.LENGTH_SHORT).show()
            },
            { error ->
                val errorMsg = error.networkResponse?.let {
                    String(it.data)
                } ?: error.message ?: "Unknown error"
                statusText.text = "Failed to send notification: $errorMsg"
                Toast.makeText(this, "Error: $errorMsg", Toast.LENGTH_LONG).show()
            }
        )

        queue.add(request)
    }
}