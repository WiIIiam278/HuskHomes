package me.william278.huskhomes2;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class permissionHomeLimits {

    private static final Main plugin = Main.getInstance();

    // Returns the maximum number of set homes a player can make
    public static int getSetHomeLimit(Player p) {
        p.recalculatePermissions();
        for (PermissionAttachmentInfo permissionAI : p.getEffectivePermissions()) {
            String permission = permissionAI.getPermission();
            if (permission.contains("huskhomes.max_sethomes.")) {
                try {
                    return Integer.parseInt(permission.split("\\.")[2]);
                } catch(Exception e) {
                    return Main.settings.getMaximumHomes();
                }
            }
        }
        return Main.settings.getMaximumHomes();
    }

    // Returns the number of set homes a player can set for free
    public static int getFreeHomes(Player p) {
        for (PermissionAttachmentInfo permissionAI : p.getEffectivePermissions()) {
            String permission = permissionAI.getPermission();
            if (permission.contains("huskhomes.free_sethomes.")) {
                try {
                    return Integer.parseInt(permission.split("\\.")[2]);
                } catch(Exception e) {
                    return Main.settings.getFreeHomeSlots();
                }
            }
        }
        return Main.settings.getFreeHomeSlots();
    }
}
