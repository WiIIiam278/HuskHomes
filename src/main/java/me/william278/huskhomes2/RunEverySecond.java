package me.william278.huskhomes2;

import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.integrations.VaultIntegration;
import me.william278.huskhomes2.teleport.RandomPoint;
import me.william278.huskhomes2.teleport.TimedTeleport;
import me.william278.huskhomes2.teleport.TeleportManager;
import me.william278.huskhomes2.teleport.TeleportRequestHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashSet;

import static org.bukkit.Bukkit.getServer;

public class RunEverySecond {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    // Cancel expired teleport requests
    private static void clearExpiredRequests(HashSet<Player> expiredTeleportRequests) {
        // Check if any requests have expired
        if (!TeleportRequestHandler.teleportRequests.isEmpty()) {
            for (Player p : TeleportRequestHandler.teleportRequests.keySet()) {
                if (TeleportRequestHandler.teleportRequests.get(p).getExpired()) {
                    expiredTeleportRequests.add(p);
                }
            }
        }

        // Clear expired requests
        if (!expiredTeleportRequests.isEmpty()) {
            for (Player p : expiredTeleportRequests) {
                TeleportRequestHandler.teleportRequests.remove(p);
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
            if (!TeleportManager.queuedTeleports.isEmpty()) {
                for (TimedTeleport timedTeleport : TeleportManager.queuedTeleports) {
                    Player teleporter = Bukkit.getPlayer(timedTeleport.getTeleporter().getUniqueId());
                    if (teleporter != null) {
                        if (timedTeleport.getTimeRemaining() > 0) {
                            if (!timedTeleport.hasMoved(teleporter)) {
                                if (!timedTeleport.hasLostHealth(teleporter)) {
                                    teleporter.playSound(teleporter.getLocation(), HuskHomes.settings.getTeleportWarmupSound(), 2, 1);
                                    MessageManager.sendActionBarMessage(teleporter, "teleporting_action_bar_countdown",
                                            Integer.toString(timedTeleport.getTimeRemaining()));
                                    timedTeleport.decrementTimeRemaining();
                                } else {
                                    MessageManager.sendActionBarMessage(teleporter, "teleporting_action_bar_cancelled");
                                    MessageManager.sendMessage(teleporter, "teleporting_cancelled_damage");
                                    teleporter.playSound(teleporter.getLocation(), HuskHomes.settings.getTeleportCancelledSound(), 1, 1);
                                    completedTeleports.add(timedTeleport);
                                }
                            } else {
                                MessageManager.sendActionBarMessage(teleporter, "teleporting_action_bar_cancelled");
                                MessageManager.sendMessage(teleporter, "teleporting_cancelled_movement");
                                teleporter.playSound(teleporter.getLocation(), HuskHomes.settings.getTeleportCancelledSound(), 1, 1);
                                completedTeleports.add(timedTeleport);
                            }
                        } else {
                            // Execute the teleport
                            String targetType = timedTeleport.getTargetType();
                            switch (targetType) {
                                case "point":
                                    TeleportManager.teleportPlayer(teleporter, timedTeleport.getTargetPoint());
                                    break;
                                case "player":
                                    TeleportManager.teleportPlayer(teleporter, timedTeleport.getTargetPlayerName());
                                    break;
                                case "random":
                                    if (HuskHomes.settings.doEconomy()) {
                                        double rtpCost = HuskHomes.settings.getRtpCost();
                                        if (rtpCost > 0) {
                                            if (!VaultIntegration.takeMoney(teleporter, rtpCost)) {
                                                MessageManager.sendMessage(teleporter, "error_insufficient_funds", VaultIntegration.format(rtpCost));
                                                break;
                                            } else {
                                                MessageManager.sendMessage(teleporter, "rtp_spent_money", VaultIntegration.format(rtpCost));
                                            }
                                        }
                                    }
                                    TeleportManager.teleportPlayer(teleporter, new RandomPoint(teleporter));
                                    DataManager.updateRtpCooldown(teleporter);
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
                    TeleportManager.queuedTeleports.remove(timedTeleport);
                }
            }
            completedTeleports.clear();

            // Clear expired teleport requests
            clearExpiredRequests(expiredTeleportRequests);
        }, 0L, 20L);
    }
}