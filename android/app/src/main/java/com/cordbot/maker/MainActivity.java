package com.cordbot.maker;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.cordbot.core.CordCore;

public class MainActivity extends AppCompatActivity {
    String botToken = ""; 
    String botLang = "cz"; 
    boolean botSlash = true; 
    String botPrefix = "!";
    TextView consoleOut;

    private BroadcastReceiver logReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String log = intent.getStringExtra("LOG");
                if(log != null && consoleOut != null) consoleOut.append(log + "\n");
            } catch (Exception e) { /* Ignorovat chyby při výpisu */ }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState); 
            setContentView(R.layout.activity_main);
        } catch (Exception e) { return; }
        
        try {
            if (Build.VERSION.SDK_INT >= 33) ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        } catch (Exception e) { /* Štít chytil chybu */ }
        
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                registerReceiver(logReceiver, new IntentFilter("com.cordbot.LOG"), Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(logReceiver, new IntentFilter("com.cordbot.LOG"));
            }
        } catch (Exception e) { /* Štít chytil chybu */ }

        EditText codeInput = findViewById(R.id.codeInput);
        consoleOut = findViewById(R.id.consoleOutput);
        
        findViewById(R.id.btnTutorial).setOnClickListener(v -> {
            try {
                new AlertDialog.Builder(this).setTitle("Návod").setMessage(CordCore.getTutorial(botLang)).setPositiveButton("OK", null).show();
            } catch (Exception e) {}
        });

        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            try { openSettings(); } catch (Exception e) {}
        });

        findViewById(R.id.btnStart).setOnClickListener(v -> {
            try {
                String code = codeInput.getText().toString();
                String errorMsg = CordCore.validateCode(code, botLang, botToken);
                if (errorMsg != null) { 
                    new AlertDialog.Builder(this).setTitle("Chyba v kódu 🕵️").setMessage(errorMsg).setPositiveButton("OK", null).show(); 
                    return; 
                }

                consoleOut.setText("Startuji bota...\n");
                Intent intent = new Intent(this, BotService.class);
                intent.putExtra("CODE", code); 
                intent.putExtra("TOKEN", botToken); 
                intent.putExtra("LANG", botLang); 
                intent.putExtra("SLASH", botSlash); 
                intent.putExtra("PREFIX", botPrefix);
                
                // TADY TO PŘEDTÍM PADALO - TEĎ TO CHYTÍME!
                if (Build.VERSION.SDK_INT >= 26) {
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }
            } catch (Exception e) {
                // APLIKACE SE NEZAVŘE, CHYBA SE VYPÍŠE SEM:
                consoleOut.append("❌ SYSTÉMOVÁ CHYBA (ZACHYCENA): " + e.getMessage() + "\n");
                consoleOut.append("⚠️ Android zablokoval spuštění na pozadí. Zkontroluj oprávnění.\n");
            }
        });

        findViewById(R.id.btnStop).setOnClickListener(v -> {
            try {
                stopService(new Intent(this, BotService.class));
                consoleOut.append("[INFO] Bot byl zastaven.\n");
            } catch (Exception e) {
                consoleOut.append("❌ Chyba při zastavování: " + e.getMessage() + "\n");
            }
        });
    }

    private void openSettings() {
        LinearLayout layout = new LinearLayout(this); 
        layout.setOrientation(LinearLayout.VERTICAL); 
        layout.setPadding(40, 20, 40, 20);
        
        EditText tokenInput = new EditText(this); 
        tokenInput.setHint("Zkopíruj Token sem"); 
        tokenInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); 
        tokenInput.setText(botToken); 
        layout.addView(tokenInput);
        
        Switch slashSwitch = new Switch(this); 
        slashSwitch.setText("Použít Slash (/) příkazy"); 
        slashSwitch.setChecked(botSlash); 
        layout.addView(slashSwitch);
        
        EditText prefixInput = new EditText(this); 
        prefixInput.setHint("Prefix (např. !)"); 
        prefixInput.setText(botPrefix); 
        prefixInput.setEnabled(!botSlash); 
        layout.addView(prefixInput);
        
        slashSwitch.setOnCheckedChangeListener((btn, isChecked) -> prefixInput.setEnabled(!isChecked));
        
        Switch langSwitch = new Switch(this); 
        langSwitch.setText("Jazyk kódu: Angličtina (EN)"); 
        langSwitch.setChecked(botLang.equals("en")); 
        layout.addView(langSwitch);
        
        new AlertDialog.Builder(this)
            .setTitle("⚙️ Nastavení Bota")
            .setView(layout)
            .setPositiveButton("ULOŽIT", (dialog, which) -> {
                botToken = tokenInput.getText().toString(); 
                botSlash = slashSwitch.isChecked(); 
                botPrefix = prefixInput.getText().toString(); 
                botLang = langSwitch.isChecked() ? "en" : "cz";
            }).show();
    }
    
    @Override 
    protected void onDestroy() { 
        try { super.onDestroy(); unregisterReceiver(logReceiver); } catch (Exception e) {} 
    }
}
