package me.william278.huskhomes2;

import me.william278.huskhomes2.Objects.TeleportationPoint;
import org.bukkit.entity.Player;

public class teleportManager {

    private static void teleportPlayer(Player p, TeleportationPoint teleportationPoint) {
        String server = teleportationPoint.getServer();
        if (!Main.settings.doBungee() || server.equals(Main.settings.getServerID())) {
            p.teleport(teleportationPoint.getLocation());
            messageManager.sendMessage(p, "teleport_success");
        } else if (Main.settings.doBungee()) {
            dataManager.setPlayerDestinationLocation(p, teleportationPoint);
            pluginMessageHandler.sendPlayer(p, server);
        } else {
            return;
        }
    }

}
