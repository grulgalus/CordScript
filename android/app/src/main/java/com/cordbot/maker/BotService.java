package com.cordbot.maker;

import com.cordbot.core.CordCore;
import android.app.*;
import android.content.Intent;
import android.os.*;
import androidx.core.app.NotificationCompat;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.util.Map;

public class BotService extends Service {
    private JDA jda;
    private PowerManager.WakeLock wakeLock;

    private void sendLog(String msg) {
        Intent intent = new Intent("com.cordbot.LOG");
        intent.putExtra("LOG", msg);
        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String code = intent.getStringExtra("CODE"); String token = intent.getStringExtra("TOKEN");
        String lang = intent.getStringExtra("LANG"); boolean useSlash = intent.getBooleanExtra("SLASH", true);
        String prefix = intent.getStringExtra("PREFIX");

        NotificationChannel channel = new NotificationChannel("BotChannel", "CordBot", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        startForeground(1, new NotificationCompat.Builder(this, "BotChannel").setContentTitle("Bot běží").setSmallIcon(android.R.drawable.ic_dialog_info).build());

        wakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CordBot::Lock");
        wakeLock.acquire();

        new Thread(() -> {
            try {
                CordScriptParser parser = new CordScriptParser(code, lang, prefix, useSlash, this);
                jda = JDABuilder.createDefault(token).enableIntents(GatewayIntent.MESSAGE_CONTENT).addEventListeners(parser).build();
                jda.awaitReady();

                if (useSlash) {
                    for (String cmdName : parser.getCommands().keySet()) jda.upsertCommand(cmdName, "Příkaz bota").queue();
                    sendLog("✅ Slash příkazy (/" + String.join(", /", parser.getCommands().keySet()) + ") nahrány!");
                }
            } catch (Exception e) { sendLog("❌ CHYBA: Zkontroluj token a Message Content Intent!"); }
        }).start();

        return START_STICKY;
    }

    @Override public void onDestroy() {
        if (jda != null) jda.shutdownNow();
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
    
    // Parser teď může posílat logy
    class CordScriptParser extends ListenerAdapter {
        private String prefix; private boolean useSlash; private Map<String, String> commands; private BotService service;

        public CordScriptParser(String code, String lang, String prefix, boolean useSlash, BotService service) {
            this.prefix = prefix; this.useSlash = useSlash; this.commands = CordCore.getCommands(code, lang); this.service = service;
        }
        public Map<String, String> getCommands() { return commands; }
        
        @Override public void onReady(ReadyEvent event) { service.sendLog("✅ Bot " + event.getJDA().getSelfUser().getName() + " se úspěšně připojil!"); }

        @Override public void onMessageReceived(MessageReceivedEvent event) {
            if (useSlash || event.getAuthor().isBot()) return;
            String msg = event.getMessage().getContentRaw();
            if (msg.startsWith(prefix)) {
                String cmd = msg.substring(prefix.length()).trim().split(" ")[0];
                if (commands.containsKey(cmd)) {
                    service.sendLog("📩 " + event.getAuthor().getName() + " napsal(a): " + msg);
                    event.getChannel().sendMessage(commands.get(cmd)).queue();
                }
            }
        }
        @Override public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
            if (useSlash && commands.containsKey(event.getName())) {
                service.sendLog("🚀 " + event.getUser().getName() + " použil(a) příkaz /" + event.getName());
                event.reply(commands.get(event.getName())).queue();
            }
        }
    }
}
