package org.sen.senWorlds.world.commands;

import org.bukkit.configuration.ConfigurationSection;
import org.sen.senWorlds.util.ColorUtil;


import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sen.senWorlds.SenWorlds;
import org.sen.senWorlds.world.VoidChunkGenerator;
import org.sen.senWorlds.world.WorldData;
import org.sen.senWorlds.world.WorldManager;

import java.util.List;



public class WorldCommand implements CommandExecutor, TabCompleter {


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {


        if (args.length == 1){
            return List.of("create", "delete", "list", "pomoc", "reload", "tp").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2) {
            String firstArgs = args[0].toLowerCase();

            if (firstArgs.equals("tp") || firstArgs.equals("delete")) {
                worldManager.getWorldNames().stream()
                        .filter(w -> w.startsWith(args[1].toLowerCase()))
                        .toList();
            }
        }


        if(args.length == 3){
            if (args[0].equalsIgnoreCase("create")) {
                return List.of("normal", "nether", "end", "void", "flat").stream()
                        .filter(env -> env.startsWith(args[2].toLowerCase()))
                        .toList();
            }
            }


        return List.of();
    }

    private WorldManager worldManager;
    private final JavaPlugin plugin;

    public WorldCommand(WorldManager worldManager, JavaPlugin plugin) {
        this.worldManager = worldManager;
        this.plugin = plugin;
    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        ConfigurationSection invalidenvconf = plugin.getConfig().getConfigurationSection("errory");




        if (args.length == 0) {
            sender.sendMessage("§e/sw create <name> <normal|nether|end>");
            sender.sendMessage("§e/sw delete <name>");
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "create" -> {

                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /sw create <name> <normal|nether|end|flat|void>");
                    return true;
                }

                String name = args[1];
                String envType = args[2].toLowerCase();

                if (!List.of("normal","nether","end","flat","void").contains(envType)) {

                    String invalidenv = invalidenvconf.getString("invalidenv", SenWorlds.c("&7Neplatný Enviroment. Použi: Normal, End, Nether, Flat, Void"));
                    sender.sendMessage(SenWorlds.c(SenWorlds.PREFIX + invalidenv));
                    return true;
                }

                if (worldManager.worldExists(name)) {
                    String worldexists = invalidenvconf.getString("worldexists", "&7Svet už existuje!");
                    sender.sendMessage((SenWorlds.c(SenWorlds.PREFIX + worldexists)));
                    return true;
                }

                sender.sendMessage("§eCreating world " + name + "...");

                worldManager.createWorldAsync(name, envType, () -> {
                    sender.sendMessage("§aWorld " + name + " has been created!");
                });
            }



            case "tp" -> {

                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /sw tp <world>");

                    return true;
                }

                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can use this command!");
                    return true;
                }

                String worldName = args[1];


                World targetWorld = Bukkit.getWorld(worldName);

                if (targetWorld == null) {
                    sender.sendMessage("§cWorld not found or not loaded!");
                    return true;
                }

                WorldData data = worldManager.getWorldData(worldName);
                if (data != null) {
                    sender.sendMessage("§aTeleporting to custom world: " + worldName);
                } else {
                    sender.sendMessage("§aTeleporting to world: " + worldName);
                }

                player.teleport(targetWorld.getSpawnLocation());

                sender.sendMessage("§aTeleported to world: " + targetWorld.getName());
            }

            case "delete" -> {

                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /sw delete <name>");
                    return true;
                }

                String name = args[1];

                if (!worldManager.worldExists(name)) {
                    sender.sendMessage("§cWorld does not exist.");
                    return true;
                }

                Bukkit.unloadWorld(name, false);
                worldManager.deleteWorld(name);

                sender.sendMessage("§aWorld deleted successfully!");

            }

            case "reload" -> {
                if (args.length < 2){
                    plugin.reloadConfig();
                    sender.sendMessage("Plugin reloadnuty");
                }
            }

            case "list" -> {
                sender.sendMessage(" ");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eLoadnute svety"));

                for (org.bukkit.World world : org.bukkit.Bukkit.getWorlds()) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', world.getName()));
                }

                sender.sendMessage(" ");
            }

            case "pomoc" -> {
                sender.sendMessage(" ");
                sender.sendMessage(SenWorlds.c("&r &r #00C7EE&lSenWorlds &7- #00C7EE&lCommandy:"));
                sender.sendMessage(" ");
                sender.sendMessage(SenWorlds.c("&r &r #FF2929&l<> &7- Povinné, #16EC00&l[] &7- Voliteľné"));
                sender.sendMessage(" ");
                sender.sendMessage(SenWorlds.c("&r &r #00C7EE/sw create <meno> <env> &8▸ &7Vytvorí Svet"));
                sender.sendMessage(SenWorlds.c("&r &r #00C7EE/sw delete <meno> &8▸ &7Zmaže Svet"));
                sender.sendMessage(SenWorlds.c("&r &r #00C7EE/sw reload &8▸ &7Reloadne Plugin"));
                sender.sendMessage(SenWorlds.c("&r &r #00C7EE/sw list &8▸ &7Zobrazí Svety"));
                sender.sendMessage(" ");
            }




            default -> sender.sendMessage("§cUnknown subcommand.");
        }

        return true;
    }

    private World.Environment parseEnvironment(String input) {

        return switch (input.toLowerCase()) {
            case "normal" -> World.Environment.NORMAL;
            case "nether" -> World.Environment.NETHER;
            case "end" -> World.Environment.THE_END;
            default -> null;
        };
    }
}
