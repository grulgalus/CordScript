package com.cordbot.core;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CordCore {
    // Přijímá jazyk z nastavení, ne z textu!
    public static Map<String, String> getCommands(String code, String lang) {
        String cmdWord = lang.equals("en") ? "command" : "prikaz";
        String repWord = lang.equals("en") ? "reply" : "odpoved";

        Map<String, String> commands = new HashMap<>();
        Matcher m = Pattern.compile(cmdWord + "\\s+(.+?)\\s+" + repWord + "\\s+(.+?)(?=\\s+" + cmdWord + "|\\z)", Pattern.DOTALL).matcher(code);
        while (m.find()) {
            commands.put(m.group(1).trim(), m.group(2).trim());
        }
        return commands;
    }

    public static String getTutorial(String lang) {
        if (lang.equals("en")) {
            return "STEP BY STEP:\n1. Go to discord.com/developers/applications\n2. Click 'New Application'\n3. Go to 'Bot' tab\n4. ⚠️ ENABLE 'Message Content Intent'\n5. Copy Token and paste it into SETTINGS.";
        } else {
            return "KROK ZA KROKEM:\n1. Běž na discord.com/developers/applications\n2. Dej 'New Application'\n3. Vlevo vyber 'Bot'\n4. ⚠️ ZAPNI 'Message Content Intent'!\n5. Zkopíruj Token a vlož ho do ⚙️ NASTAVENÍ.";
        }
    }

    public static String validateCode(String code, String lang, String token) {
        boolean isEn = lang.equals("en");
        
        if (token == null || token.trim().isEmpty() || token.equals("TVUJ_TOKEN_ZDE")) {
            return isEn ? "❌ ERROR: Token is missing!\n💡 HINT: Click 'SETTINGS' and paste your token." 
                        : "❌ CHYBA: Chybí Token!\n💡 NÁPOVĚDA: Klikni na '⚙️ NASTAVENÍ' a vlož ho tam.";
        }

        String lowerCode = code.toLowerCase();
        if (!isEn) {
            if (lowerCode.contains("přikaz") || lowerCode.contains("příkaz")) return "❌ CHYBA: Překlep 'přikaz'. Napiš to čistě jako 'prikaz'.";
            if (lowerCode.contains("odpověd") || lowerCode.contains("odpověď")) return "❌ CHYBA: Překlep 'odpověď'. Napiš to čistě jako 'odpoved'.";
        }

        String cmdWord = isEn ? "command" : "prikaz";
        String repWord = isEn ? "reply" : "odpoved";

        int cmdCount = code.split(cmdWord + "\\s").length - 1;
        int repCount = code.split(repWord + "\\s").length - 1;

        if (cmdCount > repCount) return isEn ? "❌ ERROR: Missing reply!" : "❌ CHYBA: Chybí odpověď k nějakému příkazu!";
        if (repCount > cmdCount) return isEn ? "❌ ERROR: Missing command keyword!" : "❌ CHYBA: Chybí slovo 'prikaz'!";
        if (cmdCount == 0) return isEn ? "⚠️ WARNING: Bot is empty!" : "⚠️ VAROVÁNÍ: Kód je prázdný!";
        
        return null;
    }
}
