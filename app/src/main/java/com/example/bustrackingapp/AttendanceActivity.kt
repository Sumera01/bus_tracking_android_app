package com.example.bustrackingapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AttendanceActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AttendanceAdapter
    private val attendanceList = mutableListOf<AttendanceLog>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

        // Home button
        val homeButton = findViewById<FloatingActionButton>(R.id.homeButton)
        homeButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        // RecyclerView setup
        recyclerView = findViewById(R.id.attendanceRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AttendanceAdapter(attendanceList)
        recyclerView.adapter = adapter

        loadDemoAttendance()
    }

    private fun loadDemoAttendance() {
        attendanceList.add(AttendanceLog("2025-06-20", "Present"))
        attendanceList.add(AttendanceLog("2025-06-19", "Absent"))
        attendanceList.add(AttendanceLog("2025-06-18", "Present"))
        attendanceList.add(AttendanceLog("2025-06-17", "Present"))
        attendanceList.add(AttendanceLog("2025-06-16", "Absent"))
        adapter.notifyDataSetChanged()
    }
}
