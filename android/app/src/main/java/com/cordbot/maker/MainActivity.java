package com.cordbot.maker;

import android.Manifest;
import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 33) ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);

        EditText codeInput = findViewById(R.id.codeInput);
        
        findViewById(R.id.btnTutorial).setOnClickListener(v -> {
            new AlertDialog.Builder(this).setTitle("Návod").setMessage(CordCore.getTutorial(botLang)).setPositiveButton("OK", null).show();
        });

        findViewById(R.id.btnSettings).setOnClickListener(v -> openSettings());

        findViewById(R.id.btnStart).setOnClickListener(v -> {
            String code = codeInput.getText().toString();
            String errorMsg = CordCore.validateCode(code, botLang, botToken);
            if (errorMsg != null) {
                new AlertDialog.Builder(this).setTitle("Chyba 🕵️").setMessage(errorMsg).setPositiveButton("OK", null).show();
                return;
            }

            Intent intent = new Intent(this, BotService.class);
            intent.putExtra("CODE", code);
            intent.putExtra("TOKEN", botToken);
            intent.putExtra("LANG", botLang);
            intent.putExtra("SLASH", botSlash);
            intent.putExtra("PREFIX", botPrefix);
            
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent); else startService(intent);
            Toast.makeText(this, "🚀 Startuji...", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnStop).setOnClickListener(v -> stopService(new Intent(this, BotService.class)));
    }

    private void openSettings() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        EditText tokenInput = new EditText(this);
        tokenInput.setHint("Zkopíruj Token sem");
        tokenInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); // Zatečkovaný!
        tokenInput.setText(botToken);
        layout.addView(tokenInput);

        Switch slashSwitch = new Switch(this);
        slashSwitch.setText("Použít Slash (/) příkazy");
        slashSwitch.setChecked(botSlash);
        layout.addView(slashSwitch);

        EditText prefixInput = new EditText(this);
        prefixInput.setHint("Prefix (např. !)");
        prefixInput.setText(botPrefix);
        prefixInput.setEnabled(!botSlash); // Zablokuje se, pokud je zapnutý slash
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
                Toast.makeText(this, "Nastavení uloženo", Toast.LENGTH_SHORT).show();
            })
            .show();
    }
}
