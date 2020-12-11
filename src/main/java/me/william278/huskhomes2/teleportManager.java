package me.william278.huskhomes2;

import me.william278.huskhomes2.Objects.TeleportationPoint;
import me.william278.huskhomes2.Objects.TimedTeleport;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class teleportManager {

    public static HashSet<TimedTeleport> queuedTeleports = new HashSet<>();

    public static void teleportPlayer(Player p) {
        TeleportationPoint teleportationPoint = dataManager.getPlayerDestination(p);
        String server = teleportationPoint.getServer();
        if (!Main.settings.doBungee() || server.equals(Main.settings.getServerID())) {
            p.teleport(teleportationPoint.getLocation());
            messageManager.sendMessage(p, "teleport_success");
        } else if (Main.settings.doBungee()) {
            dataManager.setPlayerDestinationLocation(p, teleportationPoint);
            pluginMessageHandler.sendPlayer(p, server);
        }
    }

    public static void queueTimedTeleport(Player player, String targetPlayer) {
        if (player.hasPermission("huskhomes.bypass_warmup_timers")) {
            dataManager.setPlayerLastPosition(player, new TeleportationPoint(player.getLocation(), Main.settings.getServerID()));
            teleportPlayer(player);
            return;
        }

        queuedTeleports.add(new TimedTeleport(player, targetPlayer));
    }

    public static void queueTimedTeleport(Player player, TeleportationPoint point) {
        if (player.hasPermission("huskhomes.bypass_warmup_timers")) {
            dataManager.setPlayerLastPosition(player, new TeleportationPoint(player.getLocation(), Main.settings.getServerID()));
            teleportPlayer(player);
            return;
        }

        queuedTeleports.add(new TimedTeleport(player, point));
    }

    private static void setTeleportationDestinationCrossServer(Player requester, String targetPlayerName) {
        pluginMessageHandler.sendPluginMessage(requester, targetPlayerName, "set_teleportation_destination", requester.getName());
    }

    public static void setPlayerDestinationFromTargetPlayer(Player requester, String targetPlayerName) {
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            dataManager.setPlayerDestinationLocation(requester,
                    new TeleportationPoint(targetPlayer.getLocation(), Main.settings.getServerID()));
        } else {
            if (Main.settings.doBungee()) {
                setTeleportationDestinationCrossServer(requester, targetPlayerName);
            } else {
                messageManager.sendMessage(requester, "error_player_not_found", targetPlayerName);
            }
        }
    }

}
