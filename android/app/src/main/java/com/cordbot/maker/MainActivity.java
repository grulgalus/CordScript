package com.cordbot.maker;

import android.os.Bundle;
import android.text.InputType;
import android.view.WindowManager;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.cordbot.core.CordCore;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    String botToken = ""; 
    String botLang = "cz"; 
    boolean botSlash = true; 
    String botPrefix = "!";
    
    TextView consoleOut;
    JDA jda = null; 

    public void logToConsole(String msg) {
        runOnUiThread(() -> {
            if (consoleOut != null) consoleOut.append(msg + "\n");
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_main);
        
        consoleOut = findViewById(R.id.consoleOutput);

        // ========================================================
        // GLOBAL CRASH HANDLER: Zabrání tomu, aby se aplikace zavřela!
        // ========================================================
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logToConsole("💥 FATÁLNÍ CRASH (ZACHYCEN): " + throwable.toString());
            // Tohle zachrání appku před úplným zavřením (pádem na domovskou obrazovku)
        });

        EditText codeInput = findViewById(R.id.codeInput);
        
        findViewById(R.id.btnTutorial).setOnClickListener(v -> {
            new AlertDialog.Builder(this).setTitle("Návod").setMessage(CordCore.getTutorial(botLang)).setPositiveButton("OK", null).show();
        });

        findViewById(R.id.btnSettings).setOnClickListener(v -> openSettings());

        findViewById(R.id.btnStart).setOnClickListener(v -> {
            String code = codeInput.getText().toString();
            String errorMsg = CordCore.validateCode(code, botLang, botToken);
            
            if (errorMsg != null) { 
                new AlertDialog.Builder(this).setTitle("Chyba v kódu 🕵️").setMessage(errorMsg).setPositiveButton("OK", null).show(); 
                return; 
            }

            if (jda != null) {
                logToConsole("⚠️ Bot už běží! Nejdřív ho zastav.");
                return;
            }

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            logToConsole("🚀 Spouštím bota...");
            
            new Thread(() -> {
                try {
                    CordScriptParser parser = new CordScriptParser(code, botLang, botPrefix, botSlash);
                    jda = JDABuilder.createDefault(botToken)
                        .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                        .addEventListeners(parser)
                        .build();
                    jda.awaitReady();

                    if (botSlash) {
                        for (String cmdName : parser.getCommands().keySet()) {
                            jda.upsertCommand(cmdName, "Příkaz bota").queue();
                        }
                        logToConsole("✅ Slash příkazy uloženy!");
                    }
                // ========================================================
                // MAGICKÉ SLOVO THROWABLE: Chytí i to, co normálně zabíjí appky
                // ========================================================
                } catch (Throwable t) { 
                    logToConsole("❌ KATASTROFICKÁ CHYBA: " + t.toString());
                    if (t.getCause() != null) {
                        logToConsole("Důvod: " + t.getCause().toString());
                    }
                    jda = null;
                    runOnUiThread(() -> getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON));
                }
            }).start();
        });

        findViewById(R.id.btnStop).setOnClickListener(v -> {
            if (jda != null) {
                jda.shutdownNow();
                jda = null;
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                logToConsole("🛑 Bot byl zastaven.");
            } else {
                logToConsole("⚠️ Bot momentálně neběží.");
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
                botToken = tokenInput.getText().toString().trim(); 
                botSlash = slashSwitch.isChecked(); 
                botPrefix = prefixInput.getText().toString().trim(); 
                botLang = langSwitch.isChecked() ? "en" : "cz";
                logToConsole("⚙️ Nastavení uloženo.");
            }).show();
    }
    
    @Override 
    protected void onDestroy() { 
        if (jda != null) jda.shutdownNow();
        super.onDestroy(); 
    }

    class CordScriptParser extends ListenerAdapter {
        private String prefix; private boolean useSlash; private Map<String, String> commands; 

        public CordScriptParser(String code, String lang, String prefix, boolean useSlash) {
            this.prefix = prefix; this.useSlash = useSlash; this.commands = CordCore.getCommands(code, lang); 
        }
        public Map<String, String> getCommands() { return commands; }
        
        @Override public void onReady(ReadyEvent event) { 
            logToConsole("✅ Bot " + event.getJDA().getSelfUser().getName() + " je ONLINE!"); 
        }

        @Override public void onMessageReceived(MessageReceivedEvent event) {
            if (useSlash || event.getAuthor().isBot()) return;
            String msg = event.getMessage().getContentRaw();
            if (msg.startsWith(prefix)) {
                String cmd = msg.substring(prefix.length()).trim().split(" ")[0];
                if (commands.containsKey(cmd)) {
                    logToConsole("📩 " + event.getAuthor().getName() + " napsal(a): " + msg);
                    event.getChannel().sendMessage(commands.get(cmd)).queue();
                }
            }
        }
        @Override public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
            if (useSlash && commands.containsKey(event.getName())) {
                logToConsole("🚀 " + event.getUser().getName() + " použil(a) /" + event.getName());
                event.reply(commands.get(event.getName())).queue();
            }
        }
    }
}
