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

        if (Build.VERSION.SDK_INT >= 33) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }

        EditText codeInput = findViewById(R.id.codeInput);
        findViewById(R.id.btnTutorial).setOnClickListener(v -> showTutorial());

        findViewById(R.id.btnStart).setOnClickListener(v -> {
            String code = codeInput.getText().toString();
            
            // INTELIGENTNÍ KONTROLA CHYB
            String errorMsg = CordCore.validateCode(code);
            if (errorMsg != null) {
                new AlertDialog.Builder(this)
                    .setTitle("Chyba v kódu 🕵️")
                    .setMessage(errorMsg)
                    .setPositiveButton("OPRAVIT KÓD", null)
                    .show();
                return; // Zastaví start bota
            }

            String token = CordCore.getSetting(code, "token", "");
            Intent intent = new Intent(this, BotService.class);
            intent.putExtra("CODE", code);
            intent.putExtra("TOKEN", token);
            
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            Toast.makeText(this, "🚀 Startuji...", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnStop).setOnClickListener(v -> {
            stopService(new Intent(this, BotService.class));
            Toast.makeText(this, "🛑 Zastaveno.", Toast.LENGTH_SHORT).show();
        });
    }

    private void showTutorial() {
        String msg = "1. Běž na: discord.com/developers/applications\n\n" +
                     "2. Dej 'New Application' a jméno bota\n\n" +
                     "3. Vlevo vyber 'Bot'\n\n" +
                     "4. ⚠️ DŮLEŽITÉ: Sjeď dolů a zapni 'Message Content Intent'!\n\n" +
                     "5. Dej 'Reset Token', zkopíruj ho a vlož k nápisu 'token ='";
                     
        new AlertDialog.Builder(this)
            .setTitle("Jak získat Token")
            .setMessage(msg)
            .setPositiveButton("ROZUMÍM", null)
            .show();
    }
}
