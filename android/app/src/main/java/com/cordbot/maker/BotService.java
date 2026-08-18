package com.cordbot.maker;

import com.cordbot.core.CordCore;
import android.app.*;
import android.content.Intent;
import android.os.*;
import androidx.core.app.NotificationCompat;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.util.Map;

public class BotService extends Service {
    private JDA jda;
    private PowerManager.WakeLock wakeLock;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String code = intent.getStringExtra("CODE");
        String token = intent.getStringExtra("TOKEN");
        boolean useSlash = CordCore.getSetting(code, "slash", "ano").equalsIgnoreCase("ano");

        NotificationChannel channel = new NotificationChannel("BotChannel", "CordBot Status", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification notification = new NotificationCompat.Builder(this, "BotChannel")
            .setContentTitle("Bot je aktivní 24/7")
            .setContentText("Kód běží perfektně.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build();
        startForeground(1, notification);

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CordBot::Lock");
        wakeLock.acquire();

        new Thread(() -> {
            try {
                CordScriptParser parser = new CordScriptParser(code);
                jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(parser)
                    .build();
                jda.awaitReady();

                // AUTOMATICKÁ REGISTRACE SLASH PŘÍKAZŮ
                if (useSlash) {
                    for (String cmdName : parser.getCommands().keySet()) {
                        jda.upsertCommand(cmdName, "CordBot automatický příkaz").queue();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (jda != null) jda.shutdownNow();
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}

class CordScriptParser extends ListenerAdapter {
    private String prefix;
    private Map<String, String> commands;

    public CordScriptParser(String code) {
        this.prefix = CordCore.getSetting(code, "prefix", "!");
        this.commands = CordCore.getCommands(code);
    }

    public Map<String, String> getCommands() { return commands; }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        String msg = event.getMessage().getContentRaw();
        if (msg.startsWith(prefix)) {
            String cmd = msg.substring(prefix.length()).trim().split(" ")[0];
            if (commands.containsKey(cmd)) {
                event.getChannel().sendMessage(commands.get(cmd)).queue();
            }
        }
    }

    // OBLUHA SLASH PŘÍKAZŮ PRO ANDROID!
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String cmd = event.getName();
        if (commands.containsKey(cmd)) {
            event.reply(commands.get(cmd)).queue();
        }
    }
}
