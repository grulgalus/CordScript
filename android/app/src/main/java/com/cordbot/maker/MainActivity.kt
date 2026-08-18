package com.cordbot.maker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // FIX PRO ANDROID 13+: Vyžádání práv na notifikace hned po startu
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        val tokenInput = findViewById<EditText>(R.id.tokenInput)
        val codeInput = findViewById<EditText>(R.id.codeInput)
        
        findViewById<Button>(R.id.btnStart).setOnClickListener {
            val token = tokenInput.text.toString().trim()
            
            // Kontrola prázdného tokenu
            if (token.isEmpty()) {
                Toast.makeText(this, "CHYBA: Musíš zadat Discord Token!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val serviceIntent = Intent(this, BotService::class.java).apply {
                putExtra("TOKEN", token)
                putExtra("CODE", codeInput.text.toString())
            }
            
            // Bezpečné spuštění služby napříč Android verzemi
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            
            Toast.makeText(this, "🚀 Bot se spouští na pozadí...", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            stopService(Intent(this, BotService::class.java))
            Toast.makeText(this, "🛑 Bot byl zastaven.", Toast.LENGTH_SHORT).show()
        }
    }
}
