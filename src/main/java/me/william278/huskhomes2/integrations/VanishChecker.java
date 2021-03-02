package me.william278.huskhomes2.integrations;

import me.william278.huskhomes2.HuskHomes;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

public class VanishChecker {

    public static boolean isVanished(Player player) {
        if (HuskHomes.getSettings().isCheckVanishedPlayers()) {
            for (MetadataValue meta : player.getMetadata("vanished")) {
                if (meta.asBoolean()) return true;
            }
        }
        return false;
    }

}
