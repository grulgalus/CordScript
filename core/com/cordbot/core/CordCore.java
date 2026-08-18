package com.cordbot.core;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CordCore {
    // Čtení nastavení (jazyk, token, prefix...)
    public static String getSetting(String code, String key, String defaultValue) {
        Matcher m = Pattern.compile(key + "\\s*=\\s*([a-zA-Z0-9_\\-\\.]+)").matcher(code);
        return m.find() ? m.group(1).trim() : defaultValue;
    }

    // Čtení příkazů BEZ ZÁVOREK (Dynamicky podle jazyka!)
    public static Map<String, String> getCommands(String code) {
        String lang = getSetting(code, "jazyk", "cz").toLowerCase();
        String cmdWord = lang.equals("en") ? "command" : "prikaz";
        String repWord = lang.equals("en") ? "reply" : "odpoved";

        Map<String, String> commands = new HashMap<>();
        Matcher m = Pattern.compile(cmdWord + "\\s+(.+?)\\s+" + repWord + "\\s+(.+?)(?=\\s+" + cmdWord + "|\\z)", Pattern.DOTALL).matcher(code);
        while (m.find()) {
            commands.put(m.group(1).trim(), m.group(2).trim());
        }
        return commands;
    }

    // Návod, který se mění podle jazyka v kódu
    public static String getTutorial(String code) {
        String lang = getSetting(code, "jazyk", "cz").toLowerCase();
        if (lang.equals("en")) {
            return "STEP BY STEP (How to get a Token):\n\n" +
                   "1. Go to: discord.com/developers/applications\n" +
                   "2. Log in and click 'New Application'\n" +
                   "3. Choose a name and confirm\n" +
                   "4. Go to the 'Bot' tab on the left\n" +
                   "5. ⚠️ IMPORTANT: Scroll down and ENABLE 'Message Content Intent'!\n" +
                   "6. Click 'Reset Token', COPY it and paste it instead of YOUR_TOKEN_HERE";
        } else {
            return "KROK ZA KROKEM (Jak získat Token):\n\n" +
                   "1. Běž na: discord.com/developers/applications\n" +
                   "2. Dej 'New Application' a jméno bota\n" +
                   "3. Vlevo vyber 'Bot'\n" +
                   "4. ⚠️ DŮLEŽITÉ: Sjeď dolů a zapni 'Message Content Intent' (Jinak to spadne)!\n" +
                   "5. Dej 'Reset Token', zkopíruj ho a vlož místo TVUJ_TOKEN_ZDE";
        }
    }

    // Kontrola chyb v daném jazyce!
    public static String validateCode(String code) {
        String lang = getSetting(code, "jazyk", "cz").toLowerCase();
        boolean isEn = lang.equals("en");
        
        String token = getSetting(code, "token", "");
        if (token.isEmpty() || token.equals(isEn ? "YOUR_TOKEN_HERE" : "TVUJ_TOKEN_ZDE")) {
            return isEn ? "❌ ERROR: You forgot to insert your Token!\n\n💡 HINT: Click 'TUTORIAL', get your token on Discord and paste it here." 
                        : "❌ CHYBA: Zapomněl jsi vložit svůj Token!\n\n💡 NÁPOVĚDA: Klikni na 'NÁVOD', zjisti token na Discordu a vlož ho sem.";
        }

        String lowerCode = code.toLowerCase();
        if (!isEn) {
            if (lowerCode.contains("přikaz") || lowerCode.contains("příkaz")) return "❌ CHYBA: Překlep 'přikaz'.\n\n💡 NÁPOVĚDA: Napiš to bez háčků a čárek jako 'prikaz'.";
            if (lowerCode.contains("odpověd") || lowerCode.contains("odpověď")) return "❌ CHYBA: Překlep 'odpověď'.\n\n💡 NÁPOVĚDA: Napiš to bez háčků a čárek jako 'odpoved'.";
        }

        String cmdWord = isEn ? "command" : "prikaz";
        String repWord = isEn ? "reply" : "odpoved";

        int cmdCount = code.split(cmdWord + "\\s").length - 1;
        int repCount = code.split(repWord + "\\s").length - 1;

        if (cmdCount > repCount) {
            return isEn ? "❌ ERROR: More commands than replies (" + cmdCount + " vs " + repCount + ")!\n\n💡 HINT: Ensure every 'command' has a 'reply'."
                        : "❌ CHYBA: Máš tam víc příkazů než odpovědí (" + cmdCount + " vs " + repCount + ")!\n\n💡 NÁPOVĚDA: Zkontroluj, jestli má každý 'prikaz' i svou 'odpoved'.";
        }
        if (repCount > cmdCount) {
            return isEn ? "❌ ERROR: More replies than commands (" + repCount + " vs " + cmdCount + ")!\n\n💡 HINT: You might have missed the 'command' keyword."
                        : "❌ CHYBA: Máš tam víc odpovědí než příkazů (" + repCount + " vs " + cmdCount + ")!\n\n💡 NÁPOVĚDA: Možná jsi zapomněl napsat slovo 'prikaz' před odpovědí.";
        }
        
        if (cmdCount == 0) {
            return isEn ? "⚠️ WARNING: Your bot does nothing yet!\n\n💡 HINT: Add something like:\ncommand hello\nreply Hi there!"
                        : "⚠️ VAROVÁNÍ: Tvůj bot zatím nic neumí!\n\n💡 NÁPOVĚDA: Zkus do kódu přidat třeba toto:\nprikaz ahoj\nodpoved Čau!";
        }
        return null;
    }
}
