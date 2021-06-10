package me.william278.huskhomes2.teleport;

import io.papermc.lib.PaperLib;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.data.pluginmessage.PluginMessage;
import me.william278.huskhomes2.data.pluginmessage.PluginMessageType;
import me.william278.huskhomes2.integrations.VaultIntegration;
import me.william278.huskhomes2.teleport.points.RandomPoint;
import me.william278.huskhomes2.teleport.points.TeleportationPoint;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.logging.Level;

public class TeleportManager {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    private static TeleportationPoint spawnLocation;

    // todo this doesnt seem to work cross server
    public static void teleportPlayer(Player player, String targetPlayer, Connection connection) throws SQLException {
        DataManager.setPlayerLastPosition(player, new TeleportationPoint(player.getLocation(),
                HuskHomes.getSettings().getServerID()), connection);
        setPlayerDestinationFromTargetPlayer(player, targetPlayer, connection);
    }

    public static void teleportPlayer(Player player, TeleportationPoint point, Connection connection) throws SQLException {
        DataManager.setPlayerLastPosition(player, new TeleportationPoint(player.getLocation(),
                HuskHomes.getSettings().getServerID()), connection);
        DataManager.setPlayerDestinationLocation(player, point, connection);
        teleportPlayer(player);
    }

    public static void teleportPlayer(Player p) {
        Connection connection = HuskHomes.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                TeleportationPoint teleportationPoint = DataManager.getPlayerDestination(p, connection);
                if (teleportationPoint != null) {
                    String server = teleportationPoint.getServer();
                    if (!HuskHomes.getSettings().doBungee() || server.equals(HuskHomes.getSettings().getServerID())) {
                        DataManager.setPlayerTeleporting(p, false, connection);
                        DataManager.deletePlayerDestination(p.getName(), connection);
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            PaperLib.teleportAsync(p, teleportationPoint.getLocation());
                            p.playSound(p.getLocation(), HuskHomes.getSettings().getTeleportationCompleteSound(), 1, 1);
                            MessageManager.sendMessage(p, "teleporting_complete");
                        });
                    } else if (HuskHomes.getSettings().doBungee()) {
                        DataManager.setPlayerDestinationLocation(p, teleportationPoint, connection);
                        DataManager.setPlayerTeleporting(p, true, connection);
                        PluginMessage.sendPlayer(p, server);
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred!", e);
            }
        });
    }

    public static void queueTimedTeleport(Player player, String targetPlayer, Connection connection) throws SQLException {
        if (player.hasPermission("huskhomes.bypass_timer")) {
            teleportPlayer(player, targetPlayer, connection);
            return;
        }

        new TimedTeleport(player, targetPlayer).begin();
    }

    public static void queueTimedTeleport(Player player, TeleportationPoint point, Connection connection) throws SQLException {
        if (player.hasPermission("huskhomes.bypass_timer")) {
            teleportPlayer(player, point, connection);
            return;
        }

        new TimedTeleport(player, point, TimedTeleport.TargetType.POINT).begin();
    }

    public static void queueBackTeleport(Player player, Connection connection) throws SQLException {
        TeleportationPoint lastPosition = DataManager.getPlayerLastPosition(player, connection);
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
                teleportPlayer(player, lastPosition, connection);
                return;
            }

            new TimedTeleport(player, lastPosition, TimedTeleport.TargetType.BACK).begin();
        } else {
            MessageManager.sendMessage(player, "error_no_last_position");
        }
    }

    public static void queueRandomTeleport(Player player, Connection connection) throws SQLException {
        if (!player.hasPermission("huskhomes.rtp.bypass_cooldown")) {
            long currentTime = Instant.now().getEpochSecond();
            long coolDownTime = DataManager.getPlayerRtpCooldown(player, connection);
            if (currentTime < coolDownTime) {
                long timeRemaining = coolDownTime - currentTime;
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
            RandomPoint randomPoint = new RandomPoint(player);
            if (randomPoint.hasFailed()) {
                return;
            }
            if (HuskHomes.getSettings().doEconomy()) {
                double rtpCost = HuskHomes.getSettings().getRtpCost();
                if (rtpCost > 0) {
                    VaultIntegration.takeMoney(player, rtpCost);
                    MessageManager.sendMessage(player, "rtp_spent_money", VaultIntegration.format(rtpCost));
                }
            }
            teleportPlayer(player, randomPoint, connection);
            DataManager.updateRtpCooldown(player, connection);
            return;
        }
        new TimedTeleport(player).begin();
    }

    public static void teleportHere(Player requester, String targetPlayerName, Connection connection) throws SQLException {
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            if (targetPlayer.getUniqueId() != requester.getUniqueId()) {
                teleportPlayer(targetPlayer, requester.getName(), connection);
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

    public static void setPlayerDestinationFromTargetPlayer(Player requester, String targetPlayerName, Connection connection) throws SQLException {
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            if (requester.getUniqueId() != targetPlayer.getUniqueId()) {
                DataManager.setPlayerDestinationLocation(requester,
                        new TeleportationPoint(targetPlayer.getLocation(), HuskHomes.getSettings().getServerID()), connection);
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

    public static TeleportationPoint getSpawnLocation() {
        return spawnLocation;
    }

    public static void setSpawnLocation(TeleportationPoint spawnLocation) {
        TeleportManager.spawnLocation = spawnLocation;
    }
}
