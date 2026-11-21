package com.example.instantcolourclicker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var status: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("clicker", MODE_PRIVATE)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 28, 28, 28)
        }
        status = TextView(this).apply {
            textSize = 18f
            text = "1) Enable the Accessibility service.\n2) Return here and press START."
        }
        val settings = Button(this).apply {
            text = "Open Accessibility Settings"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }
        val start = Button(this).apply {
            text = "START AUTO CLICK"
            setOnClickListener {
                prefs.edit().putBoolean("enabled", true).apply()
                status.text = "Running. Switch to the target screen. Press STOP here to pause."
            }
        }
        val stop = Button(this).apply {
            text = "STOP"
            setOnClickListener {
                prefs.edit().putBoolean("enabled", false).apply()
                status.text = "Stopped."
            }
        }
        val note = TextView(this).apply {
            textSize = 14f
            text = "\nThis starter detects the largest bright, non-black region on screen and taps its centre. Keep it for apps you own or have permission to automate."
        }
        layout.addView(status)
        layout.addView(settings)
        layout.addView(start)
        layout.addView(stop)
        layout.addView(note)
        setContentView(layout)
    }
    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) {
            val enabled = getSharedPreferences("clicker", MODE_PRIVATE).getBoolean("enabled", false)
            status.text = if (enabled) "Running. Switch to the target screen. Press STOP here to pause."
                          else "1) Enable the Accessibility service.\n2) Return here and press START."
        }
    }
}
