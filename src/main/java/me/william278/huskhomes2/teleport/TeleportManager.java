package me.william278.huskhomes2.teleport;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.data.pluginmessage.PluginMessage;
import me.william278.huskhomes2.data.pluginmessage.PluginMessageHandler;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.data.pluginmessage.PluginMessageType;
import me.william278.huskhomes2.integrations.VaultIntegration;
import me.william278.huskhomes2.teleport.points.RandomPoint;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class TeleportManager {

    private static final Set<TimedTeleport> queuedTeleports = new HashSet<>();

    private static TeleportationPoint spawnLocation;

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
                PluginMessage.sendPlayer(p, server);
            }
        }
    }

    public static void queueTimedTeleport(Player player, String targetPlayer) {
        if (player.hasPermission("huskhomes.bypass_timer")) {
            teleportPlayer(player, targetPlayer);
            return;
        }

        getQueuedTeleports().add(new TimedTeleport(player, targetPlayer));
    }

    public static void queueTimedTeleport(Player player, TeleportationPoint point) {
        if (player.hasPermission("huskhomes.bypass_timer")) {
            teleportPlayer(player, point);
            return;
        }

        getQueuedTeleports().add(new TimedTeleport(player, point, "point"));
    }

    public static void queueBackTeleport(Player player) {
        TeleportationPoint lastPosition = DataManager.getPlayerLastPosition(player);
        if (lastPosition != null) {
            if (HuskHomes.getSettings().doEconomy()) {
                double backCost = HuskHomes.getSettings().getBackCost();
                if (backCost > 0) {
                    if (!VaultIntegration.hasMoney(player, backCost)) {
                        MessageManager.sendMessage(player, "error_insufficient_funds", VaultIntegration.format(backCost));
                        return;
                    }
                }
            }
            if (player.hasPermission("huskhomes.bypass_timer")) {
                if (HuskHomes.getSettings().doEconomy()) {
                    double backCost = HuskHomes.getSettings().getRtpCost();
                    if (backCost > 0) {
                        VaultIntegration.takeMoney(player, backCost);
                        MessageManager.sendMessage(player, "rtp_spent_money", VaultIntegration.format(backCost));
                    }
                }
                teleportPlayer(player, lastPosition);
                return;
            }

            getQueuedTeleports().add(new TimedTeleport(player, lastPosition, "back"));
        } else {
            MessageManager.sendMessage(player, "error_no_last_position");
        }
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

        getQueuedTeleports().add(new TimedTeleport(player));
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
                return;
            }
            MessageManager.sendMessage(requester, "error_player_not_found", targetPlayerName);
        }
    }

    private static void teleportHereCrossServer(Player requester, String targetPlayerName) {
        new PluginMessage(targetPlayerName, PluginMessageType.TELEPORT_TO_ME, requester.getName()).send(requester);
    }

    private static void setTeleportationDestinationCrossServer(Player requester, String targetPlayerName) {
        new PluginMessage(targetPlayerName, PluginMessageType.SET_TP_DESTINATION, requester.getName()).send(requester);
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
                return;
            }
            MessageManager.sendMessage(requester, "error_player_not_found", targetPlayerName);
        }
    }

    public static Set<TimedTeleport> getQueuedTeleports() {
        return queuedTeleports;
    }

    public static TeleportationPoint getSpawnLocation() {
        return spawnLocation;
    }

    public static void setSpawnLocation(TeleportationPoint spawnLocation) {
        TeleportManager.spawnLocation = spawnLocation;
    }
}
