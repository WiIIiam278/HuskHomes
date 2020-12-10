package me.william278.huskhomes2;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;

import static org.bukkit.Bukkit.getServer;

public class runEverySecond {

    private static Main plugin = Main.getInstance();

    // HashMap mapping teleportation times to players
    public static HashMap<Player,Integer> teleportationTimer = new HashMap<>();

    public runEverySecond() {
        BukkitScheduler scheduler = getServer().getScheduler();

        // Run every second
        scheduler.scheduleSyncRepeatingTask(plugin, () -> {
            for (Player p : teleportationTimer.keySet()) {

            }
        }, 0L, 20L);
    }
}