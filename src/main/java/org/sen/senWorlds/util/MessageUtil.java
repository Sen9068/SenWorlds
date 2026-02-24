package org.sen.senWorlds.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class MessageUtil {
    private static final MiniMessage minimessage = MiniMessage.miniMessage();
    private final JavaPlugin plugin;
    public MessageUtil(JavaPlugin plugin){
        this.plugin = plugin;
    }

    public Component get(String path, Map<String, String> placeholders){
        String message = plugin.getConfig().getString(path, "");
        for (Map.Entry<String, String> entry : placeholders.entrySet()){
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return minimessage.deserialize(message);
    }
}
