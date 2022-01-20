package me.william278.huskhomes2.teleport.points;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.util.MessageManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

/**
 * An object representing an in-game warp
 * @author William
 */
public class Warp extends SetPoint {

    /**
     * An object representing an in-game warp
     * @param location the Bukkit location of the Warp
     * @param server The Bungee server ID of the Warp
     * @param name The name of the Warp
     */
    public Warp(Location location, String server, String name) {
        super(location, server, name, MessageManager.getRawMessage("warp_default_description"));
    }

    /**
     * An object representing an in-game warp
     * @param teleportationPoint The position of the Warp
     * @param name The name of the Warp
     * @param description The description of the Warp
     */
    public Warp(TeleportationPoint teleportationPoint, String name, String description, long creationTime) {
        super(teleportationPoint.worldName, teleportationPoint.x, teleportationPoint.y, teleportationPoint.z, teleportationPoint.yaw, teleportationPoint.pitch, teleportationPoint.server, name, description, creationTime);
    }

    /**
     * Whether the player can use the warp
     * @param player The player
     * @return boolean; whether they can use the warp
     */
    public boolean canUse(Player player) {
        return getWarpCanUse(player, getName());
    }

    // Returns if the player has permission to access the warp
    public static boolean getWarpCanUse(Player p, String warpName) {
        if (!HuskHomes.getSettings().doPermissionRestrictedWarps()) {
            return true;
        }
        p.recalculatePermissions();
        String permissionFormat = HuskHomes.getSettings().getWarpRestrictionPermissionFormat();
        if (p.hasPermission("huskhomes.warp.*") || p.hasPermission(permissionFormat + "*")) { return true; } // Always return true if they have the wildcard
        for (PermissionAttachmentInfo permissionAI : p.getEffectivePermissions()) {
            String permission = permissionAI.getPermission();
            if (permission.contains(permissionFormat)) {
                try {
                    if (permission.split("\\.")[StringUtils.countMatches(permissionFormat, ".")].equalsIgnoreCase(warpName)) {
                        return permissionAI.getValue();
                    }
                } catch (Exception ignored) {}
            }
        }
        return false;
    }

}
