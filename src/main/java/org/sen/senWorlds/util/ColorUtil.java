package org.sen.senWorlds.util;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ColorUtil {
    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");
    public static String color(String message){
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexColor = matcher.group();
            matcher.appendReplacement(buffer, ChatColor.of(hexColor).toString());
        }

        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}
