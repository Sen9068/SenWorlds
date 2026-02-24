package org.sen.senWorlds.world;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.sen.senWorlds.SenWorlds;

import java.io.*;
import java.util.*;

public class WorldManager {


    private JavaPlugin plugin;
    private final Map<String, WorldData> worlds = new HashMap<>();

    private final File worldsFile;
    private final FileConfiguration worldsConfig;

    public WorldData getWorldData(String name) {
        return worlds.get(name.toLowerCase());
    }


    public WorldManager(JavaPlugin plugin) {
        this.plugin = plugin;


        worldsFile = new File(plugin.getDataFolder(), "worlds.yml");
        if (!worldsFile.exists()) plugin.saveResource("worlds.yml", false);

        worldsConfig = YamlConfiguration.loadConfiguration(worldsFile);
    }

    public String getPrefix(){
        String prefix = plugin.getConfig().getString("spravy.prefix", "#00C7EESenWorlds ");
        prefix = prefix.replace("{prefix}", "#00C7EESenWorlds ");
        return ChatColor.translateAlternateColorCodes('&', prefix);
    }

    public void loadWorlds() {
        if (!worldsConfig.contains("worlds")) return;

        for (String worldName : worldsConfig.getConfigurationSection("worlds").getKeys(false)) {
            String env = worldsConfig.getString("worlds." + worldName + ".environment", "normal");
            createWorldInternal(worldName, env);
        }

        plugin.getLogger().info("Loaded " + worlds.size() + " worlds.");
    }

// Skusanie Async World Generacie
    public void createWorldAsync(String name, String envType, Runnable callback) {

        if (worlds.containsKey(name.toLowerCase())) return;

        plugin.getLogger().info("Preparing world: " + name);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {

                // Template folder copy (Volitelne) TO DO!
                File templateFolder = new File(plugin.getDataFolder(), "templates/" + envType.toLowerCase());
                File targetFolder = new File(Bukkit.getWorldContainer(), name);

                if (templateFolder.exists()) copyFolder(templateFolder, targetFolder);

                // Main Thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    WorldCreator creator = new WorldCreator(name);

                    switch (envType.toLowerCase()) {
                        case "normal" -> creator.environment(World.Environment.NORMAL);
                        case "nether" -> creator.environment(World.Environment.NETHER);
                        case "end" -> creator.environment(World.Environment.THE_END);
                        case "flat" -> {
                            creator.environment(World.Environment.NORMAL);
                            creator.type(WorldType.FLAT);
                        }
                        case "void" -> {
                            creator.environment(World.Environment.NORMAL);
                            creator.generator(new VoidChunkGenerator());
                        }
                        default -> creator.environment(World.Environment.NORMAL);
                    }

                    World world = Bukkit.createWorld(creator);

                    // Main thread - Ulozenie
                    worlds.put(name.toLowerCase(), new WorldData(name, envType));
                    saveWorld(name, envType);

                    plugin.getLogger().info("World created: " + name);

                    if (callback != null) callback.run();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public boolean deleteWorld(String worldName) {

        worlds.remove(worldName.toLowerCase());

        World world = Bukkit.getWorld(worldName);
        if (world == null) return false;

        World safeWorld = Bukkit.getWorlds().get(0);
        Location safeLocation = safeWorld.getSpawnLocation();

        // Teleportne hracov prec pri zmazani worldu
        for (Player player : world.getPlayers()) {
            player.teleport(safeLocation);



            String zmazanysvet = plugin.getConfig().getString("errors.zmazanysvettp", "&7Svet bol zmazaný. Bol si teleportovaný.");
            player.sendMessage((SenWorlds.c(zmazanysvet)));
        }

        // Unloadnut svet
        Bukkit.unloadWorld(world, false);

        // Zmazat world folder
        File worldFolder = world.getWorldFolder();
        deleteFolder(worldFolder);

        return true;
    }



    private void createWorldInternal(String name, String envType) {
        WorldCreator creator = new WorldCreator(name);

        switch (envType.toLowerCase()) {
            case "normal" -> creator.environment(World.Environment.NORMAL);
            case "nether" -> creator.environment(World.Environment.NETHER);
            case "end" -> creator.environment(World.Environment.THE_END);
            case "flat" -> {
                creator.environment(World.Environment.NORMAL);
                creator.type(WorldType.FLAT);
            }
            case "void" -> {
                creator.environment(World.Environment.NORMAL);
                creator.generator(new VoidChunkGenerator());
            }
            default -> creator.environment(World.Environment.NORMAL);
        }

        World world = Bukkit.createWorld(creator);
        worlds.put(name.toLowerCase(), new WorldData(name, envType));
    }

    private void saveWorld(String name, String envType) {
        worldsConfig.set("worlds." + name + ".environment", envType.toLowerCase());
        saveFile();
    }

    private void saveFile() {
        try {
            worldsConfig.save(worldsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteFolder(File folder) {
        if (!folder.exists()) return;

        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isDirectory()) deleteFolder(file);
            else file.delete();
        }
        folder.delete();
    }

    private void copyFolder(File source, File target) throws IOException {
        if (!source.exists()) return;

        if (source.isDirectory()) {
            if (!target.exists()) target.mkdirs();
            for (String child : source.list()) {
                copyFolder(new File(source, child), new File(target, child));
            }
        } else {
            try (InputStream in = new FileInputStream(source);
                 OutputStream out = new FileOutputStream(target)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
        }
    }

    public boolean worldExists(String name) {
        return worlds.containsKey(name.toLowerCase());
    }

    public Set<String> getWorldNames() {
        return Collections.unmodifiableSet(worlds.keySet());
    }
}
