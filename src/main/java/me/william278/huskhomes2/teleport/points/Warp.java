package me.william278.huskhomes2.teleport.points;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

/**
 * An object representing an in-game warp
 * @author William
 */
public class Warp extends TeleportationPoint {

    private String name;
    private String description;

    /**
     * An object representing an in-game warp
     * @param location the Bukkit location of the Warp
     * @param server The Bungee server ID of the Warp
     * @param name The name of the Warp
     */
    public Warp(Location location, String server, String name) {
        super(location, server);
        this.name = name;
        this.description = MessageManager.getRawMessage("warp_default_description");
    }

    /**
     * An object representing an in-game warp
     * @param teleportationPoint The position of the Warp
     * @param name The name of the Warp
     * @param description The description of the Warp
     */
    public Warp(TeleportationPoint teleportationPoint, String name, String description) {
        super(teleportationPoint.worldName, teleportationPoint.x, teleportationPoint.y, teleportationPoint.z, teleportationPoint.yaw, teleportationPoint.pitch, teleportationPoint.server);
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the description of the Warp
     * @return the Warp's description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the name of the Warp
     * @return the Warp's name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the Warp
     * @param name the new Warp name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the description of the Warp
     * @param description the new Warp description
     */
    public void setDescription(String description) {
        this.description = description;
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
