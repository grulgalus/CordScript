package com.cordbot.maker;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.cordbot.core.CordCore;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 33) ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);

        EditText codeInput = findViewById(R.id.codeInput);
        
        findViewById(R.id.btnTutorial).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Návod / Tutorial")
                .setMessage(CordCore.getTutorial(codeInput.getText().toString()))
                .setPositiveButton("OK", null).show();
        });

        findViewById(R.id.btnStart).setOnClickListener(v -> {
            String code = codeInput.getText().toString();
            String errorMsg = CordCore.validateCode(code);
            if (errorMsg != null) {
                new AlertDialog.Builder(this).setTitle("Chyba / Error 🕵️").setMessage(errorMsg).setPositiveButton("OK", null).show();
                return;
            }

            Intent intent = new Intent(this, BotService.class);
            intent.putExtra("CODE", code);
            intent.putExtra("TOKEN", CordCore.getSetting(code, "token", ""));
            
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent); else startService(intent);
            Toast.makeText(this, "🚀 Startuji...", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnStop).setOnClickListener(v -> stopService(new Intent(this, BotService.class)));
    }
}
