package com.cordbot.core;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CordCore {
    // Společná funkce pro získání prefixu
    public static String parsePrefix(String code) {
        Matcher pMatcher = Pattern.compile("prefix:\\s*\"([^\"]+)\"").matcher(code);
        return pMatcher.find() ? pMatcher.group(1) : "!";
    }

    // Společná funkce pro získání všech příkazů a odpovědí
    public static Map<String, String> parseCommands(String code) {
        Map<String, String> commands = new HashMap<>();
        Matcher cMatcher = Pattern.compile("command\\s+(\\w+)\\s*\\{\\s*reply\\s+\"([^\"]+)\"\\s*\\}").matcher(code);
        while (cMatcher.find()) {
            commands.put(cMatcher.group(1), cMatcher.group(2));
        }
        return commands;
    }
}
