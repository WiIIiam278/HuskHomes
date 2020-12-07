package me.william278.huskhomes2;

import me.william278.huskhomes2.Objects.TeleportationPoint;
import org.bukkit.entity.Player;

public class teleportManager {

    private static void teleportPlayer(Player p, TeleportationPoint teleportationPoint) {
        if (teleportationPoint.getServer().equals(Main.server)) {
            p.teleport(teleportationPoint.getLocation());
            messageHandler.sendMessage(p, "teleport_success");
        }
    }

}
