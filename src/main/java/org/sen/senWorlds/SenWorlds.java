package org.sen.senWorlds;

import org.bukkit.Color;
import org.bukkit.plugin.java.JavaPlugin;
import org.sen.senWorlds.util.ColorUtil;
import org.sen.senWorlds.world.WorldManager;
import org.sen.senWorlds.world.commands.WorldCommand;

public final class SenWorlds extends JavaPlugin {

    private WorldManager worldManager; //

    public static String c(String msg){
        return ColorUtil.color(msg);
    }

    // Na prefix aby som mohol vsade pouzivat
    public static String PREFIX;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("world.yml", false);
        PREFIX = getConfig().getString("spravy.prefix", "#00C7EESenWorlds &8▸ ");

        getLogger().info("SenWorlds sa nacital");

        worldManager = new WorldManager(this);
        worldManager.loadWorlds();


        WorldCommand worldCommand = new WorldCommand(worldManager, this);
        getCommand("sw").setExecutor(new WorldCommand(worldManager, this));
        getCommand("sw").setTabCompleter(worldCommand);

    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    @Override
    public void onDisable() {
        getLogger().info("Server sa vypol");
    }
}
