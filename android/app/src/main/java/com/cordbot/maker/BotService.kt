package com.cordbot.maker

import android.app.*
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.regex.Pattern

class BotService : Service() {
    private var jda: JDA? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val token = intent?.getStringExtra("TOKEN") ?: return START_NOT_STICKY
        val code = intent.getStringExtra("CODE") ?: ""

        // 1. Udržení procesu 24/7 přes notifikaci
        val channelId = "BotServiceChannel"
        val channel = NotificationChannel(channelId, "Bot Status", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("CordBot Běží 24/7")
            .setContentText("Bot je aktivní na pozadí.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        startForeground(1, notification)

        // 2. WakeLock (nedovolí CPU usnout)
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CordBot::WakeLock")
        wakeLock?.acquire()

        // 3. Spuštění JDA (Nativní Discord Bot)
        Thread {
            try {
                jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(CordScriptParser(code))
                    .build()
            } catch (e: Exception) { e.printStackTrace() }
        }.start()

        return START_STICKY
    }

    override fun onDestroy() {
        jda?.shutdownNow()
        wakeLock?.let { if (it.isHeld) it.release() }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

// Nativní CordScript Parser pro Kotlin
class CordScriptParser(private val code: String) : ListenerAdapter() {
    private var prefix = "!"
    private val commands = mutableMapOf<String, String>()

    init {
        val prefixMatcher = Pattern.compile("prefix:\\s*\"([^\"]+)\"").matcher(code)
        if (prefixMatcher.find()) prefix = prefixMatcher.group(1)!!

        val cmdMatcher = Pattern.compile("command\\s+(\\w+)\\s*\\{\\s*reply\\s+\"([^\"]+)\"\\s*\\}").matcher(code)
        while (cmdMatcher.find()) {
            commands[cmdMatcher.group(1)!!] = cmdMatcher.group(2)!!
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return
        val msg = event.message.contentRaw
        if (msg.startsWith(prefix)) {
            val cmd = msg.removePrefix(prefix).trim().split(" ")[0]
            commands[cmd]?.let { reply ->
                event.channel.sendMessage(reply).queue()
            }
        }
    }
}
