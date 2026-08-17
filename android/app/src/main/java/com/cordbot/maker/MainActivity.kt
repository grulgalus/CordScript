package com.cordbot.maker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tokenInput = findViewById<EditText>(R.id.tokenInput)
        val codeInput = findViewById<EditText>(R.id.codeInput)
        
        findViewById<Button>(R.id.btnStart).setOnClickListener {
            val serviceIntent = Intent(this, BotService::class.java).apply {
                putExtra("TOKEN", tokenInput.text.toString().trim())
                putExtra("CODE", codeInput.text.toString())
            }
            startForegroundService(serviceIntent)
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            stopService(Intent(this, BotService::class.java))
        }
    }
}
