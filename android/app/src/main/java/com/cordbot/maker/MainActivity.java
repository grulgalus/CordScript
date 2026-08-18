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
            String token = CordCore.getSetting(code, "token", "");
            
            if (token.isEmpty() || token.equals("TVUJ_TOKEN_ZDE")) {
                Toast.makeText(this, "CHYBA: Musíš vložit svůj Token místo TVUJ_TOKEN_ZDE!", Toast.LENGTH_LONG).show();
                return;
            }

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
        String msg = "1. Běž na stránku: discord.com/developers/applications\n\n" +
                     "2. Přihlaš se a klikni na 'New Application'\n\n" +
                     "3. Napiš jméno a potvrď\n\n" +
                     "4. Vlevo vyber 'Bot'\n\n" +
                     "5. ⚠️ DŮLEŽITÉ: Sjeď dolů a zapni 'Message Content Intent' (Jinak appka spadne!)\n\n" +
                     "6. Nahoře klikni na 'Reset Token', dej COPY a vlož ho do kódu k nápisu 'token ='";
                     
        new AlertDialog.Builder(this)
            .setTitle("Jak získat Token (Návod)")
            .setMessage(msg)
            .setPositiveButton("ROZUMÍM", null)
            .show();
    }
}
