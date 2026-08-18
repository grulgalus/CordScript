package com.cordbot.core;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CordCore {
    // Univerzální čtečka nastavení z kódu
    public static String getSetting(String code, String key, String defaultValue) {
        Matcher m = Pattern.compile(key + "\\s*=\\s*([a-zA-Z0-9_\\-\\.]+)").matcher(code);
        return m.find() ? m.group(1).trim() : defaultValue;
    }

    // Čtečka příkazů - Hledá "prikaz(slovo)" a "odpoved(text)"
    public static Map<String, String> getCommands(String code) {
        Map<String, String> commands = new HashMap<>();
        Matcher m = Pattern.compile("prikaz\$(.*?)\$\\s*odpoved\$(.*?)\$", Pattern.DOTALL).matcher(code);
        while (m.find()) {
            // Odstraní případné uvozovky pro maximální blbuvzdornost
            String cmdName = m.group(1).trim().replace("\"", "");
            String response = m.group(2).trim().replace("\"", "");
            commands.put(cmdName, response);
        }
        return commands;
    }
}
