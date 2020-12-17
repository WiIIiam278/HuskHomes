package me.william278.huskhomes2.Integrations;

import me.william278.huskhomes2.HuskHomes;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

public class vanishChecker {

    public static boolean isVanished(Player player) {
        if (HuskHomes.settings.isCheckVanishedPlayers()) {
            for (MetadataValue meta : player.getMetadata("vanished")) {
                if (meta.asBoolean()) return true;
            }
        }
        return false;
    }

}
