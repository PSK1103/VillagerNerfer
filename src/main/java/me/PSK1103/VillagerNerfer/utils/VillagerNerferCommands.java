package me.PSK1103.VillagerNerfer.utils;

import me.PSK1103.VillagerNerfer.VillagerNerfer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class VillagerNerferCommands implements CommandExecutor {

    private VillagerNerfer plugin;

    public VillagerNerferCommands(VillagerNerfer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] strings) {

        if(label.equals("vnerfer") || label.equals("vn")) {
            if(strings.length == 1) {
                switch (strings[0]) {
                    case "count":
                    case "c":

                        int[] vcount = new int[2];

                        Bukkit.getWorlds().forEach(world ->
                                world.getEntities().forEach(entity -> {
                                    if (entity instanceof Villager) {
                                        Villager v = (Villager) entity;
                                        if (v.isAware()) {
                                            vcount[1]++;
                                        } else {
                                            vcount[0]++;
                                        }
                                    }
                                }));

                        if (commandSender instanceof Player) {
                            Player player = (Player) commandSender;

                            if(!player.hasPermission("VNerfer.count")) {
                                player.sendMessage(ChatColor.RED + "You do not have permission to use this command");
                                return true;
                            }

                            player.sendMessage(ChatColor.YELLOW + "Villager Count:" + (vcount[0] + vcount[1]));
                            player.sendMessage("Active villagers: " + vcount[1]);
                            player.sendMessage("Inactive villagers: " + vcount[0]);
                            return true;
                        }
                        System.out.println("Villager Count:" + (vcount[0] + vcount[1]));
                        System.out.println("Active villagers: " + vcount[1]);
                        System.out.println("Inactive villagers: " + vcount[0]);
                        return true;
                    case "highlight":
                    case "hl":

                        if(commandSender instanceof Player && !commandSender.hasPermission("VNerfer.highlight")) {
                            commandSender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
                            return true;
                        }

                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            int [] count = {0};
                            Bukkit.getWorlds().forEach(world ->
                                    world.getEntities().forEach(entity -> {
                                    if (entity instanceof Villager) {
                                        Villager v = (Villager) entity;
                                        if (!v.isAware()) {
                                            v.setGlowing(true);
                                            count[0]++;
                                        }
                                    }
                                    }));
                            if(commandSender instanceof Player) {
                                commandSender.sendMessage(ChatColor.LIGHT_PURPLE + "Highlighted " + count[0] + " villagers");
                            }
                            else {
                                System.out.println("Highlighted " + count[0] + " villagers");
                            }
                        }, 1);

                        Bukkit.getScheduler().runTaskLater(plugin, () ->
                                Bukkit.getWorlds().forEach(world ->
                                        world.getEntities().forEach(entity -> {
                                            if (entity instanceof Villager) {
                                                Villager v = (Villager) entity;
                                                if (!v.isAware()) {
                                                    v.setGlowing(false);
                                                }
                                            }
                                        })), 200);
                        return true;
                    case "reload":
                    case "r":
                        if (commandSender instanceof Player) {
                            Player player = (Player) commandSender;

                            if (player.hasPermission("vnerfer.use")) {
                                Bukkit.getWorlds().forEach(world -> {
                                    world.getEntities().forEach(e -> {
                                        if (e instanceof Villager) {
                                            Villager v = (Villager) e;
                                            v.setAI(true);
                                            v.setAware(true);
                                        }
                                    });
                                });

                                plugin.getStorage().clearStorage();
                                plugin.reloadCustomConfig();

                                Bukkit.getWorlds().forEach(world ->
                                        world.getEntities().forEach(entity -> {
                                            if (entity instanceof Villager) {
                                                Villager v = (Villager) entity;
                                                plugin.getStorage().addVillager(v);
                                            }
                                        }));

                                player.sendMessage(ChatColor.LIGHT_PURPLE + "Villager data reloaded!");
                                return true;
                            }
                            else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to use this command");
                                return true;
                            }
                        }

                        Bukkit.getWorlds().forEach(world ->
                                world.getEntities().forEach(e -> {
                                    if (e instanceof Villager) {
                                        Villager v = (Villager) e;
                                        v.setAI(true);
                                        v.setAware(true);
                                    }
                                }));

                        plugin.getStorage().clearStorage();
                        plugin.reloadCustomConfig();

                        Bukkit.getWorlds().forEach(world ->
                                world.getEntities().forEach(entity -> {
                                    if (entity instanceof Villager) {
                                        Villager v = (Villager) entity;
                                        plugin.getStorage().addVillager(v);
                                    }
                                }));

                        System.out.println("Villager data reloaded!");
                        return true;
                    case "force":
                    case "f":

                        if (commandSender instanceof Player) {

                            if(!commandSender.hasPermission("VNerfer.force")) {
                                commandSender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
                                return true;
                            }

                            Player player = (Player) commandSender;
                            Location playerLocation = player.getLocation();

                            final String[] uid = {""};
                            final double[] distance = {14142000};

                            Bukkit.getWorlds().forEach(world -> {
                                if(world.getEnvironment() == player.getWorld().getEnvironment()) {
                                    world.getEntities().forEach(entity -> {
                                        if (entity instanceof Villager) {
                                            Villager villager = (Villager) entity;
                                            if (playerLocation.distance(villager.getLocation()) < distance[0]) {
                                                uid[0] = villager.getUniqueId().toString();
                                                distance[0] = playerLocation.distance(villager.getLocation());
                                            }
                                        }
                                    });
                                }
                            });

                            if (distance[0] < 14142000) {
                                plugin.getStorage().addVillager(((Villager) Bukkit.getEntity(UUID.fromString(uid[0]))));
                                player.sendMessage(ChatColor.YELLOW + "Nerfed nearest villager");
                            } else {
                                player.sendMessage(ChatColor.RED + "No villager found on the server");
                            }
                            return true;
                        }
                        break;

                    case "help":

                        if(commandSender instanceof Player) {
                            Player player = (Player) commandSender;
                            if(!player.hasPermission("VNerfer.help")) {
                                player.sendMessage(ChatColor.RED + "You do not have permission to use this command");
                                return true;
                            }
                            player.sendMessage(ChatColor.LIGHT_PURPLE + "===============Villager Nerfer v" + plugin.getDescription().getVersion() + "===============");
                            player.sendMessage(ChatColor.GOLD + "/vnerfer count (active/inactive): " + ChatColor.GREEN + "Display number of villagers (active or inactive) being tracked by the plugin");
                            player.sendMessage(ChatColor.GOLD + "/vnerfer  highlight: " + ChatColor.GREEN + "Highlights all nerfed villagers");
                            player.sendMessage(ChatColor.GOLD + "/vnerfer force (count): " + ChatColor.GREEN + "Forces the plugin to track nearest (count) villager(s)");
                            player.sendMessage(ChatColor.GOLD + "/vnerfer reload: " + ChatColor.GREEN + "Refreshes the plugin and reloads all tracked villagers");
                        }
                        else {
                            System.out.println("===============Villager Nerfer v" + plugin.getDescription().getVersion() + "===============");
                            System.out.println("/vnerfer count (active/inactive): Display number of villagers (active or inactive) being tracked by the plugin");
                            System.out.println("/vnerfer  highlight: Highlights all nerfed villagers");
                            System.out.println("/vnerfer force (count): Forces the plugin to track nearest (count) villager(s)");
                            System.out.println("/vnerfer reload: Refreshes the plugin and reloads all tracked villagers");
                        }

                        return true;
                }
            } else if(strings.length==2) {
                switch (strings[0]) {
                    case "count":
                    case "c":

                        if(commandSender instanceof Player && !commandSender.hasPermission("VNerfer.count")) {
                            commandSender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
                            return true;
                        }

                        int[] vcount = new int[2];

                        Bukkit.getWorlds().forEach(world ->
                                world.getEntities().forEach(entity -> {
                                    if (entity instanceof Villager) {
                                        Villager v = (Villager) entity;
                                        if (v.isAware()) {
                                            vcount[1]++;
                                        } else {
                                            vcount[0]++;
                                        }
                                    }
                                }));

                        if (commandSender instanceof Player) {
                            Player player = (Player) commandSender;

                            switch (strings[1]) {

                                case "active":
                                case "a":
                                    player.sendMessage("Active villagers: " + vcount[1]);
                                    return true;
                                case "inactive":
                                case "i":
                                    player.sendMessage("Inactive villagers: " + vcount[0]);
                                    return true;

                            }

                            player.sendMessage(ChatColor.RED + "Incorrect usage, specify active(a) or inactive(i) or nothing for both variants");
                            return true;
                        }

                        switch (strings[1]) {

                            case "active":
                            case "a":
                                System.out.println("Active villagers: " + vcount[1]);
                                return true;
                            case "inactive":
                            case "i":
                                System.out.println("Inactive villagers: " + vcount[0]);
                                return true;
                        }
                        System.out.println("Incorrect usage, specify active(a) or inactive(i) or nothing for both variants");
                        return true;
                    case "force":
                    case "f":
                        if(commandSender instanceof Player) {
                            Player player = (Player) commandSender;
                            if(!player.hasPermission("VNerfer.force")) {
                                player.sendMessage(ChatColor.RED + "You do not have permission to use this command");
                                return true;
                            }
                            int[] count = {0};
                            try {
                                count[0] = Integer.parseInt(strings[1]);
                                if(count[0] <=0)
                                    throw  new IllegalArgumentException();
                            }
                            catch (NumberFormatException e) {
                                player.sendMessage(ChatColor.RED + "Error! Integer expected as second argument");
                                return true;
                            }
                            catch (IllegalArgumentException e) {
                                player.sendMessage(ChatColor.RED + "Positive integer value expected");
                                return true;
                            }

                            List<String> u = new ArrayList<>();

                            List<Double> d = new ArrayList<>();

                            Bukkit.getWorlds().forEach(world -> {
                                if(player.getWorld().getEnvironment() == world.getEnvironment()) {
                                    world.getEntities().forEach(entity -> {
                                        if (entity instanceof Villager) {
                                            Villager villager = (Villager) entity;
                                            int index = Collections.binarySearch(d, player.getLocation().distance(villager.getLocation()));
                                            if (index < 0) index = ~index;
                                            d.add(index, player.getLocation().distance(villager.getLocation()));
                                            u.add(index, villager.getUniqueId().toString());
                                        }
                                    });
                                }
                            });

                            if(u.size()==0) {
                                player.sendMessage(ChatColor.RED + "No villager found");
                            }

                            for(int i = 0;i < Math.min(count[0],u.size()); i++) {
                                plugin.getStorage().addVillager((Villager)Bukkit.getEntity(UUID.fromString(u.get(i))));
                            }
                            player.sendMessage(ChatColor.YELLOW + "Nerfed " + Math.min(count[0],u.size()) + " villagers");
                            return true;

                        }
                }
            }
        }

        return false;
    }
}
