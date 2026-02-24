package org.sen.senWorlds.world.commands;

import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import org.bukkit.Sound;
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


import java.util.ArrayList;
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


        // Sekcie v config.yml
        ConfigurationSection invalidenvconf = plugin.getConfig().getConfigurationSection("errory");
        ConfigurationSection usage = plugin.getConfig().getConfigurationSection("usage");




        if (args.length == 0) {


            Player p = (Player) sender;
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1f);

            List<String> helpmessage = plugin.getConfig().getStringList("usage.helpmessage");
            for (String line : helpmessage){
                sender.sendMessage(SenWorlds.c(line));
            }
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "create" -> {

                if (args.length < 3) {
                    String noargscreate = usage.getString("noargscreate", SenWorlds.c("{prefix} &cPouzitie: /sw create <meno> <normal/nether/end/flat/void>"))
                                    .replace("{prefix}", SenWorlds.PREFIX);

                    sender.sendMessage(SenWorlds.c(noargscreate));
                    return true;
                }

                String name = args[1];
                String envType = args[2].toLowerCase();

                if (!List.of("normal","nether","end","flat","void").contains(envType)) {

                    String invalidenv = invalidenvconf.getString("invalidenv", SenWorlds.c("{prefix} &7Neplatný Enviroment. Použi: Normal, End, Nether, Flat, Void"))
                                    .replace("{prefix}", SenWorlds.PREFIX);
                    sender.sendMessage(SenWorlds.c(invalidenv));
                    return true;
                }

                if (worldManager.worldExists(name)) {
                    String worldexists = invalidenvconf.getString("worldexists", "{prefix} &7Svet už existuje!")
                                    .replace("{prefix}", SenWorlds.PREFIX);
                    sender.sendMessage((SenWorlds.c(worldexists)));
                    return true;
                }

                // Name je meno sveta, tak dame placeholder {name} do plugin.yml
                // Prefix je tiez v PREFIX String v Main class a je to placeholder {prefix}

                String creatingworld = plugin.getConfig().getString("spravy.creatingworld", "{prefix} §eVytvara sa svet {name} ...")
                                .replace("{prefix}", SenWorlds.PREFIX)
                                        .replace("{name}", name);

                sender.sendMessage(SenWorlds.c(creatingworld));

                worldManager.createWorldAsync(name, envType, () -> {

                    String worldcreated = plugin.getConfig().getString("spravy.worldcreated", "{prefix} &eSvet {name} bol vytvoreny")
                                    .replace("{prefix}", SenWorlds.PREFIX)
                                            .replace("{name}", name);

                    sender.sendMessage(SenWorlds.c(worldcreated));
                });
            }



            case "tp" -> {

                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /sw tp <world>");

                    return true;
                }

                if (!(sender instanceof Player player)) {
                    String onlyplayer = plugin.getConfig().getString("errory.onlyplayer", "{prefix} §cLen hraci mozu pouzit tento prikaz!")
                                    .replace("{prefix}", SenWorlds.PREFIX);
                    sender.sendMessage(SenWorlds.c(onlyplayer));
                    return true;
                }

                String worldName = args[1];


                World targetWorld = Bukkit.getWorld(worldName);

                if (targetWorld == null) {
                    String notfound = plugin.getConfig().getString("errory.notfound", "{prefix} &eSvet nebol najdeny, alebo nacitany")
                                    .replace("{prefix}", SenWorlds.PREFIX);
                    sender.sendMessage(SenWorlds.c(notfound));
                    return true;
                }

                WorldData data = worldManager.getWorldData(worldName);
                if (data != null) {
                    String teleportingcustom = plugin.getConfig().getString("spravy.teleportingcustom", "{prefix} &eTeleportujem do sveta {menosveta}")
                                    .replace("{prefix}", SenWorlds.PREFIX)
                                            .replace("{menosveta}", worldName);

                    sender.sendMessage(SenWorlds.c(teleportingcustom));
                }

                player.teleport(targetWorld.getSpawnLocation());

                String targetworldgetname = targetWorld.getName();

                String teleportnuty = plugin.getConfig().getString("spravy.teleported", "{prefix} &aTeleportnuty do sveta: {menosveta}")
                                .replace("{prefix}", SenWorlds.PREFIX)
                                        .replace("{menosveta}", targetworldgetname);


                sender.sendMessage(SenWorlds.c(teleportnuty));
            }

            case "delete" -> {

                if (args.length < 2) {

                    String deleteusage = plugin.getConfig().getString("usage.deleteusage", "{prefix} &cPouzitie: /sw delete <meno>")
                                    .replace("{prefix}", SenWorlds.PREFIX);

                    sender.sendMessage(SenWorlds.c(deleteusage));
                    return true;
                }

                String name = args[1];

                if (!worldManager.worldExists(name)) {
                    String worldnotexist = plugin.getConfig().getString("errory.worldnotexist", "&cSvet neexistuje!")
                                    .replace("{prefix}", SenWorlds.PREFIX);
                    sender.sendMessage(SenWorlds.c(worldnotexist));
                    return true;
                }

                Bukkit.unloadWorld(name, false);
                worldManager.deleteWorld(name);

                String worlddeleted = plugin.getConfig().getString("spravy.worlddeleted", "{prefix} &aSvet zmazany!")
                                .replace("{prefix}", SenWorlds.PREFIX);
                sender.sendMessage(SenWorlds.c(worlddeleted));

            }

            case "reload" -> {
                if (args.length < 2){
                    plugin.reloadConfig();
                    String reloadmsg = plugin.getConfig().getString("spravy.reloadmsg", "{prefix} &aKonfiguracia uspesne obnovena")
                                    .replace("{prefix}", SenWorlds.PREFIX);
                    sender.sendMessage(SenWorlds.c(reloadmsg));
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



            default -> sender.sendMessage(SenWorlds.c(("&cNeznamy priakz!")));
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
