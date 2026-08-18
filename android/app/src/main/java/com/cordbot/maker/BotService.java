package com.cordbot.maker;

import com.cordbot.core.CordCore; // IMPORTOVÁNÍ SDÍLENÉHO JÁDRA!

import android.app.*;
import android.content.Intent;
import android.os.*;
import androidx.core.app.NotificationCompat;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.util.Map;

public class BotService extends Service {
    private JDA jda;
    private PowerManager.WakeLock wakeLock;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String token = intent.getStringExtra("TOKEN");
        String code = intent.getStringExtra("CODE");

        NotificationChannel channel = new NotificationChannel("BotChannel", "CordBot Status", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification notification = new NotificationCompat.Builder(this, "BotChannel")
            .setContentTitle("CordBot Běží")
            .setContentText("Jádro přeloženo. Bot je aktivní.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build();
        startForeground(1, notification);

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CordBot::Lock");
        wakeLock.acquire();

        new Thread(() -> {
            try {
                jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(new CordScriptParser(code))
                    .build();
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
        // VOLÁME SDÍLENÉ JÁDRO!
        this.prefix = CordCore.parsePrefix(code);
        this.commands = CordCore.parseCommands(code);
    }

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
}
