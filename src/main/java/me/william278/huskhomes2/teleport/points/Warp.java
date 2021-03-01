package me.william278.huskhomes2.teleport.points;

import org.bukkit.Location;

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
        this.description = "A publicly accessible warp";
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
        this.name = description;
    }

}
