package me.william278.huskhomes2;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class PermissionHomeLimits {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    // Returns the maximum number of set homes a player can make
    public static int getSetHomeLimit(Player p) {
        p.recalculatePermissions();
        for (PermissionAttachmentInfo permissionAI : p.getEffectivePermissions()) {
            String permission = permissionAI.getPermission();
            if (permission.contains("huskhomes.max_sethomes.")) {
                try {
                    return Integer.parseInt(permission.split("\\.")[2]);
                } catch(Exception e) {
                    return HuskHomes.settings.getMaximumHomes();
                }
            }
        }
        return HuskHomes.settings.getMaximumHomes();
    }

    // Returns the number of set homes a player can set for free
    public static int getFreeHomes(Player p) {
        for (PermissionAttachmentInfo permissionAI : p.getEffectivePermissions()) {
            String permission = permissionAI.getPermission();
            if (permission.contains("huskhomes.free_sethomes.")) {
                try {
                    return Integer.parseInt(permission.split("\\.")[2]);
                } catch(Exception e) {
                    return HuskHomes.settings.getFreeHomeSlots();
                }
            }
        }
        return HuskHomes.settings.getFreeHomeSlots();
    }
}
