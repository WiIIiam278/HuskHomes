package me.william278.huskhomes2.teleport;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.PluginMessageHandler;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.integrations.VaultIntegration;
import me.william278.huskhomes2.teleport.points.RandomPoint;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.HashSet;

public class TeleportManager {

    public static HashSet<TimedTeleport> queuedTeleports = new HashSet<>();

    public static TeleportationPoint spawnLocation = null;

    public static void teleportPlayer(Player player, String targetPlayer) {
        DataManager.setPlayerLastPosition(player, new TeleportationPoint(player.getLocation(), HuskHomes.getSettings().getServerID()));
        setPlayerDestinationFromTargetPlayer(player, targetPlayer);
    }

    public static void teleportPlayer(Player player, TeleportationPoint point) {
        DataManager.setPlayerLastPosition(player, new TeleportationPoint(player.getLocation(), HuskHomes.getSettings().getServerID()));
        DataManager.setPlayerDestinationLocation(player, point);
        teleportPlayer(player);
    }

    public static void teleportPlayer(Player p) {
        TeleportationPoint teleportationPoint = DataManager.getPlayerDestination(p);
        if (teleportationPoint != null) {
            String server = teleportationPoint.getServer();
            if (!HuskHomes.getSettings().doBungee() || server.equals(HuskHomes.getSettings().getServerID())) {
                p.teleport(teleportationPoint.getLocation());
                p.playSound(p.getLocation(), HuskHomes.getSettings().getTeleportationCompleteSound(), 1, 1);
                MessageManager.sendMessage(p, "teleporting_complete");
                DataManager.setPlayerTeleporting(p, false);
                DataManager.clearPlayerDestination(p.getName());
            } else if (HuskHomes.getSettings().doBungee()) {
                DataManager.setPlayerDestinationLocation(p, teleportationPoint);
                DataManager.setPlayerTeleporting(p, true);
                PluginMessageHandler.sendPlayer(p, server);
            }
        }
    }

    public static void queueTimedTeleport(Player player, String targetPlayer) {
        if (player.hasPermission("huskhomes.bypass_timer")) {
            teleportPlayer(player, targetPlayer);
            return;
        }

        queuedTeleports.add(new TimedTeleport(player, targetPlayer));
    }

    public static void queueTimedTeleport(Player player, TeleportationPoint point) {
        if (player.hasPermission("huskhomes.bypass_timer")) {
            teleportPlayer(player, point);
            return;
        }

        queuedTeleports.add(new TimedTeleport(player, point));
    }

    public static void queueRandomTeleport(Player player) {
        if (!player.hasPermission("huskhomes.rtp.bypass_cooldown")) {
            long currentTime = Instant.now().getEpochSecond();
            long cooldownTime = DataManager.getPlayerRtpCooldown(player);
            if (currentTime < cooldownTime) {
                long timeRemaining = cooldownTime - currentTime;
                long timeRemainingMinutes = timeRemaining / 60;
                MessageManager.sendMessage(player, "error_rtp_cooldown", Long.toString(timeRemainingMinutes));
                return;
            }
        }
        if (HuskHomes.getSettings().doEconomy()) {
            double rtpCost = HuskHomes.getSettings().getRtpCost();
            if (rtpCost > 0) {
                if (!VaultIntegration.hasMoney(player, rtpCost)) {
                    MessageManager.sendMessage(player, "error_insufficient_funds", VaultIntegration.format(rtpCost));
                    return;
                }
            }
        }
        if (player.hasPermission("huskhomes.bypass_timer")) {
            if (HuskHomes.getSettings().doEconomy()) {
                double rtpCost = HuskHomes.getSettings().getRtpCost();
                if (rtpCost > 0) {
                    VaultIntegration.takeMoney(player, rtpCost);
                    MessageManager.sendMessage(player, "rtp_spent_money", VaultIntegration.format(rtpCost));
                }
            }
            teleportPlayer(player, new RandomPoint(player));
            DataManager.updateRtpCooldown(player);
            return;
        }

        queuedTeleports.add(new TimedTeleport(player));
    }

    public static void teleportHere(Player requester, String targetPlayerName) {
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            if (targetPlayer.getUniqueId() != requester.getUniqueId()) {
                teleportPlayer(targetPlayer, requester.getName());
            } else {
                MessageManager.sendMessage(requester, "error_tp_self");
            }
        } else {
            if (HuskHomes.getSettings().doBungee()) {
                teleportHereCrossServer(requester, targetPlayerName);
            } else {
                MessageManager.sendMessage(requester, "error_player_not_found", targetPlayerName);
            }
        }
    }

    private static void teleportHereCrossServer(Player requester, String targetPlayerName) {
        PluginMessageHandler.sendPluginMessage(requester, targetPlayerName, "teleport_to_me", requester.getName());
    }

    private static void setTeleportationDestinationCrossServer(Player requester, String targetPlayerName) {
        PluginMessageHandler.sendPluginMessage(requester, targetPlayerName, "set_tp_destination", requester.getName());
    }

    public static void setPlayerDestinationFromTargetPlayer(Player requester, String targetPlayerName) {
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            if (requester.getUniqueId() != targetPlayer.getUniqueId()) {
                DataManager.setPlayerDestinationLocation(requester,
                        new TeleportationPoint(targetPlayer.getLocation(), HuskHomes.getSettings().getServerID()));
                teleportPlayer(requester);
            } else {
                MessageManager.sendMessage(requester, "error_tp_self");
            }
        } else {
            if (HuskHomes.getSettings().doBungee()) {
                setTeleportationDestinationCrossServer(requester, targetPlayerName);
            } else {
                MessageManager.sendMessage(requester, "error_player_not_found", targetPlayerName);
            }
        }
    }

}
