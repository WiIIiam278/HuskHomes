package me.william278.huskhomes2;

import me.william278.huskhomes2.Integrations.economy;
import me.william278.huskhomes2.Objects.RandomPoint;
import me.william278.huskhomes2.Objects.TeleportationPoint;
import me.william278.huskhomes2.Objects.TimedTeleport;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.HashSet;

public class teleportManager {

    public static HashSet<TimedTeleport> queuedTeleports = new HashSet<>();

    public static TeleportationPoint spawnLocation = null;

    public static void teleportPlayer(Player player, String targetPlayer) {
        dataManager.setPlayerLastPosition(player, new TeleportationPoint(player.getLocation(), HuskHomes.settings.getServerID()));
        setPlayerDestinationFromTargetPlayer(player, targetPlayer);
    }

    public static void teleportPlayer(Player player, TeleportationPoint point) {
        dataManager.setPlayerLastPosition(player, new TeleportationPoint(player.getLocation(), HuskHomes.settings.getServerID()));
        dataManager.setPlayerDestinationLocation(player, point);
        teleportPlayer(player);
    }

    public static void teleportPlayer(Player p) {
        TeleportationPoint teleportationPoint = dataManager.getPlayerDestination(p);
        if (teleportationPoint != null) {
            String server = teleportationPoint.getServer();
            if (!HuskHomes.settings.doBungee() || server.equals(HuskHomes.settings.getServerID())) {
                p.teleport(teleportationPoint.getLocation());
                messageManager.sendMessage(p, "teleporting_complete");
                dataManager.setPlayerTeleporting(p, false);
                dataManager.clearPlayerDestination(p.getName());
            } else if (HuskHomes.settings.doBungee()) {
                dataManager.setPlayerDestinationLocation(p, teleportationPoint);
                dataManager.setPlayerTeleporting(p, true);
                pluginMessageHandler.sendPlayer(p, server);
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
            long cooldownTime = dataManager.getPlayerRtpCooldown(player);
            if (currentTime < cooldownTime) {
                long timeRemaining = cooldownTime - currentTime;
                long timeRemainingMinutes = timeRemaining / 60;
                messageManager.sendMessage(player, "error_rtp_cooldown", Long.toString(timeRemainingMinutes));
                return;
            }
        }
        if (HuskHomes.settings.doEconomy()) {
            double rtpCost = HuskHomes.settings.getRtpCost();
            if (rtpCost > 0) {
                if (!economy.hasMoney(player, rtpCost)) {
                    messageManager.sendMessage(player, "error_insufficient_funds", economy.format(rtpCost));
                    return;
                }
            }
        }
        if (player.hasPermission("huskhomes.bypass_timer")) {
            if (HuskHomes.settings.doEconomy()) {
                double rtpCost = HuskHomes.settings.getRtpCost();
                if (rtpCost > 0) {
                    economy.takeMoney(player, rtpCost);
                    messageManager.sendMessage(player, "rtp_spent_money", economy.format(rtpCost));
                }
            }
            teleportPlayer(player, new RandomPoint(player));
            dataManager.updateRtpCooldown(player);
            return;
        }

        queuedTeleports.add(new TimedTeleport(player));
    }

    public static void teleportHere(Player requester, String targetPlayerName) {
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            teleportPlayer(targetPlayer, requester.getName());
        } else {
            if (HuskHomes.settings.doBungee()) {
                teleportHereCrossServer(requester, targetPlayerName);
            } else {
                messageManager.sendMessage(requester, "error_player_not_found", targetPlayerName);
            }
        }
    }

    private static void teleportHereCrossServer(Player requester, String targetPlayerName) {
        pluginMessageHandler.sendPluginMessage(requester, targetPlayerName, "teleport_to_me", requester.getName());
    }

    private static void setTeleportationDestinationCrossServer(Player requester, String targetPlayerName) {
        pluginMessageHandler.sendPluginMessage(requester, targetPlayerName, "set_tp_destination", requester.getName());
    }

    public static void setPlayerDestinationFromTargetPlayer(Player requester, String targetPlayerName) {
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            dataManager.setPlayerDestinationLocation(requester,
                    new TeleportationPoint(targetPlayer.getLocation(), HuskHomes.settings.getServerID()));
            teleportPlayer(requester);
        } else {
            if (HuskHomes.settings.doBungee()) {
                setTeleportationDestinationCrossServer(requester, targetPlayerName);
            } else {
                messageManager.sendMessage(requester, "error_player_not_found", targetPlayerName);
            }
        }
    }

}
