package me.william278.huskhomes2;

import me.william278.huskhomes2.Integrations.economy;
import me.william278.huskhomes2.Objects.RandomPoint;
import me.william278.huskhomes2.Objects.TimedTeleport;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashSet;

import static org.bukkit.Bukkit.getServer;

public class runEverySecond {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    // Cancel expired teleport requests
    private static void clearExpiredRequests(HashSet<Player> expiredTeleportRequests) {
        // Check if any requests have expired
        if (!teleportRequestHandler.teleportRequests.isEmpty()) {
            for (Player p : teleportRequestHandler.teleportRequests.keySet()) {
                if (teleportRequestHandler.teleportRequests.get(p).getExpired()) {
                    expiredTeleportRequests.add(p);
                }
            }
        }

        // Clear expired requests
        if (!expiredTeleportRequests.isEmpty()) {
            for (Player p : expiredTeleportRequests) {
                teleportRequestHandler.teleportRequests.remove(p);
            }
        }
        expiredTeleportRequests.clear();
    }

    public static void startLoop() {
        BukkitScheduler scheduler = getServer().getScheduler();

        HashSet<Player> expiredTeleportRequests = new HashSet<>();
        HashSet<TimedTeleport> completedTeleports = new HashSet<>();

        // Run every second
        scheduler.scheduleSyncRepeatingTask(plugin, () -> {

            // Update active timed teleports
            if (!teleportManager.queuedTeleports.isEmpty()) {
                for (TimedTeleport timedTeleport : teleportManager.queuedTeleports) {
                    Player teleporter = Bukkit.getPlayer(timedTeleport.getTeleporter().getUniqueId());
                    if (teleporter != null) {
                        if (timedTeleport.getTimeRemaining() > 0) {
                            if (!timedTeleport.hasMoved(teleporter)) {
                                if (!timedTeleport.hasLostHealth(teleporter)) {
                                    teleporter.playSound(teleporter.getLocation(), HuskHomes.settings.getTeleportWarmupSound(), 2, 1);
                                    messageManager.sendActionBarMessage(teleporter, "teleporting_action_bar_countdown",
                                            Integer.toString(timedTeleport.getTimeRemaining()));
                                    timedTeleport.decrementTimeRemaining();
                                } else {
                                    messageManager.sendActionBarMessage(teleporter, "teleporting_action_bar_cancelled");
                                    messageManager.sendMessage(teleporter, "teleporting_cancelled_damage");
                                    teleporter.playSound(teleporter.getLocation(), HuskHomes.settings.getTeleportCancelledSound(), 1, 1);
                                    completedTeleports.add(timedTeleport);
                                }
                            } else {
                                messageManager.sendActionBarMessage(teleporter, "teleporting_action_bar_cancelled");
                                messageManager.sendMessage(teleporter, "teleporting_cancelled_movement");
                                teleporter.playSound(teleporter.getLocation(), HuskHomes.settings.getTeleportCancelledSound(), 1, 1);
                                completedTeleports.add(timedTeleport);
                            }
                        } else {
                            // Execute the teleport
                            String targetType = timedTeleport.getTargetType();
                            switch (targetType) {
                                case "point":
                                    teleportManager.teleportPlayer(teleporter, timedTeleport.getTargetPoint());
                                    break;
                                case "player":
                                    teleportManager.teleportPlayer(teleporter, timedTeleport.getTargetPlayerName());
                                    break;
                                case "random":
                                    if (HuskHomes.settings.doEconomy()) {
                                        double rtpCost = HuskHomes.settings.getRtpCost();
                                        if (rtpCost > 0) {
                                            if (!economy.takeMoney(teleporter, rtpCost)) {
                                                messageManager.sendMessage(teleporter, "error_insufficient_funds", economy.format(rtpCost));
                                                break;
                                            } else {
                                                messageManager.sendMessage(teleporter, "rtp_spent_money", economy.format(rtpCost));
                                            }
                                        }
                                    }
                                    teleportManager.teleportPlayer(teleporter, new RandomPoint(teleporter));
                                    dataManager.updateRtpCooldown(teleporter);
                                    break;
                            }
                            completedTeleports.add(timedTeleport);
                        }
                    } else {
                        completedTeleports.add(timedTeleport);
                    }
                }
            }

            // Clear completed teleports
            if (!completedTeleports.isEmpty()) {
                for (TimedTeleport timedTeleport : completedTeleports) {
                    teleportManager.queuedTeleports.remove(timedTeleport);
                }
            }
            completedTeleports.clear();

            // Clear expired teleport requests
            clearExpiredRequests(expiredTeleportRequests);
        }, 0L, 20L);
    }
}