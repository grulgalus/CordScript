package com.cordbot.core;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CordCore {
    public static String getSetting(String code, String key, String defaultValue) {
        Matcher m = Pattern.compile(key + "\\s*=\\s*([a-zA-Z0-9_\\-\\.]+)").matcher(code);
        return m.find() ? m.group(1).trim() : defaultValue;
    }

    public static Map<String, String> getCommands(String code) {
        Map<String, String> commands = new HashMap<>();
        Matcher m = Pattern.compile("prikaz\$(.*?)\$\\s*odpoved\$(.*?)\$", Pattern.DOTALL).matcher(code);
        while (m.find()) {
            String cmdName = m.group(1).trim().replace("\"", "");
            String response = m.group(2).trim().replace("\"", "");
            commands.put(cmdName, response);
        }
        return commands;
    }

    // NOVINKA: Inteligentní kontrola kódu
    public static String validateCode(String code) {
        // 1. Kontrola Tokenu
        String token = getSetting(code, "token", "");
        if (token.isEmpty() || token.equals("TVUJ_TOKEN_ZDE")) {
            return "❌ CHYBA: Zapomněl jsi vložit svůj Token!\n\n💡 NÁPOVĚDA: Klikni na tlačítko 'NÁVOD', zjisti svůj token na Discordu a vlož ho místo 'TVUJ_TOKEN_ZDE'.";
        }

        // 2. Kontrola častých překlepů a diakritiky
        String lowerCode = code.toLowerCase();
        if (lowerCode.contains("přikaz") || lowerCode.contains("příkaz")) {
            return "❌ CHYBA: Našel jsem překlep ve slově 'prikaz'.\n\n💡 NÁPOVĚDA: Programování nemá rádo háčky a čárky. Přepiš to prosím čistě jako 'prikaz(...)'.";
        }
        if (lowerCode.contains("odpověd") || lowerCode.contains("odpověď")) {
            return "❌ CHYBA: Našel jsem překlep ve slově 'odpoved'.\n\n💡 NÁPOVĚDA: Přepiš to prosím bez háčků a čárek jako 'odpoved(...)'.";
        }

        // 3. Kontrola párů (Každý příkaz musí mít odpověď)
        int prikazCount = code.split("prikaz\$").length - 1;
        int odpovedCount = code.split("odpoved\$").length - 1;

        if (prikazCount > odpovedCount) {
            return "❌ CHYBA: Máš tam víc příkazů než odpovědí (" + prikazCount + " vs " + odpovedCount + ")!\n\n💡 NÁPOVĚDA: Zkontroluj, jestli jsi pod každý 'prikaz(...)' napsal i 'odpoved(...)'.";
        }
        if (odpovedCount > prikazCount) {
            return "❌ CHYBA: Máš tam víc odpovědí než příkazů (" + odpovedCount + " vs " + prikazCount + ")!\n\n💡 NÁPOVĚDA: Možná jsi zapomněl napsat slovo 'prikaz(...)' před nějakou odpovědí.";
        }
        
        if (prikazCount == 0) {
            return "⚠️ VAROVÁNÍ: Tvůj bot zatím nic neumí!\n\n💡 NÁPOVĚDA: Zkus do kódu přidat třeba toto:\nprikaz(ahoj)\nodpoved(Čau!)";
        }

        // Vše je v pořádku
        return null;
    }
}
