package com.ashkiano.interestingplaces;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class InterestingPlaces extends JavaPlugin {
    private List<Location> locations = new ArrayList<>();
    private List<Integer> times = new ArrayList<>();
    private int currentIndex = 0;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        getCommand("startTour").setExecutor(this);
        getCommand("addLocation").setExecutor(this);
        Metrics metrics = new Metrics(this, 22239);
    }

    @Override
    public void onDisable() {
        saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("startTour")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                startTour(player);
                return true;
            } else {
                sender.sendMessage("This command can only be used by a player.");
                return true;
            }
        } else if (command.getName().equalsIgnoreCase("addLocation")) {
            if (sender.hasPermission("interestingplaces.addlocation")) {
                if (args.length == 1 && sender instanceof Player) {
                    Player player = (Player) sender;
                    int time;
                    try {
                        time = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        player.sendMessage("Invalid time. Please enter a valid number of seconds.");
                        return true;
                    }
                    addLocation(player.getLocation(), time);
                    player.sendMessage("Location added with a time of " + time + " seconds.");
                    return true;
                } else {
                    sender.sendMessage("Usage: /addLocation <time>");
                    return true;
                }
            } else {
                sender.sendMessage("You do not have permission to use this command.");
                return true;
            }
        }
        return false;
    }

    private void loadConfig() {
        locations.clear();
        times.clear();
        List<String> locList = getConfig().getStringList("locations");
        List<Integer> timeList = getConfig().getIntegerList("times");
        for (String loc : locList) {
            String[] parts = loc.split(",");
            if (parts.length == 4) {
                String world = parts[0];
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double z = Double.parseDouble(parts[3]);
                locations.add(new Location(Bukkit.getWorld(world), x, y, z));
            }
        }
        times.addAll(timeList);
    }

    private void addLocation(Location location, int time) {
        locations.add(location);
        times.add(time);
        List<String> locList = getConfig().getStringList("locations");
        List<Integer> timeList = getConfig().getIntegerList("times");
        locList.add(location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ());
        timeList.add(time);
        getConfig().set("locations", locList);
        getConfig().set("times", timeList);
        saveConfig();
    }

    private void startTour(Player player) {
        Location startLocation = player.getLocation();
        currentIndex = 0;
        teleportNext(player, startLocation);
    }

    private void teleportNext(Player player, Location startLocation) {
        if (currentIndex < locations.size()) {
            Location location = locations.get(currentIndex);
            int time = times.get(currentIndex);

            player.teleport(location);
            player.sendMessage("You will be teleported to the next location in " + time + " seconds.");

            new BukkitRunnable() {
                @Override
                public void run() {
                    currentIndex++;
                    teleportNext(player, startLocation);
                }
            }.runTaskLater(this, time * 20L); // time * 20L to convert seconds to ticks
        } else {
            player.teleport(startLocation);
            player.sendMessage("Tour finished! You have been returned to your starting location.");
        }
    }
}