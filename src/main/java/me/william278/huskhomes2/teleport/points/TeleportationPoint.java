package me.william278.huskhomes2.teleport.points;

import me.william278.huskhomes2.HuskHomes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.StringJoiner;

/**
 * @author William
 * HuskHomes' position representation object; represents an in-game location on a server
 */
public class TeleportationPoint {

    protected String server;
    protected String worldName;
    protected double x;
    protected double y;
    protected double z;
    protected float yaw;
    protected float pitch;

    /**
     * Update the location of a TeleportationPoint
     *
     * @param location a new Bukkit location
     * @param server   a new Bungee server ID
     */
    public void setLocation(Location location, String server) {
        worldName = location.getWorld().getName();
        x = location.getX();
        y = location.getY();
        z = location.getZ();
        yaw = location.getYaw();
        pitch = location.getPitch();
        this.server = server;
    }

    /**
     * HuskHomes' position representation object; represents an in-game location on a server
     *
     * @param location The Bukkit location
     * @param server   The Bungee server ID which the location is on
     */
    public TeleportationPoint(Location location, String server) {
        if (location.getWorld() == null) {
            throw new IllegalStateException("The location provided has an invalid world");
        }
        setLocation(location, server);
    }

    /**
     * HuskHomes' position representation object; represents an in-game location on a server
     *
     * @param worldName The name of the location's world
     * @param x         The location's x-coordinate
     * @param y         The location's y-coordinate
     * @param z         The location's z-coordinate
     * @param yaw       The location's yaw angle
     * @param pitch     The location's pitch angle
     * @param server    The Bungee server ID which the location is on
     */
    public TeleportationPoint(String worldName, double x, double y, double z, float yaw, float pitch, String server) {
        this.server = server;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Get the Bukkit location from a TeleportationPoint
     *
     * @return the Bukkit location on the server from a TeleportationPoint
     * @throws IllegalStateException if the location is not valid on the server
     */
    public Location getLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            return new Location(world, x, y, z, yaw, pitch);
        } else {
            throw new IllegalStateException(worldName + " is not a valid world on the server; failed to return location");
        }
    }

    /**
     * Get the name of the world from a TeleportationPoint
     *
     * @return the name of the world the position is on from a TeleportationPoint
     */
    public String getWorldName() {
        return worldName;
    }

    /**
     * Get the Bungee server ID from a TeleportationPoint
     *
     * @return the TeleportationPoint's Bungee server ID
     */
    public String getServer() {
        return server;
    }

    /**
     * Get the x-coordinate of a TeleportationPoint
     *
     * @return the x-coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Get the y-coordinate of a TeleportationPoint
     *
     * @return the y-coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Get the z-coordinate of a TeleportationPoint
     *
     * @return the z-coordinate
     */
    public double getZ() {
        return z;
    }

    /**
     * Get the yaw angle of a TeleportationPoint
     *
     * @return the yaw angle
     */
    public float getYaw() {
        return yaw;
    }

    /**
     * Get the pitch angle of a TeleportationPoint
     *
     * @return the pitch angle
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Returns the TeleportationPoint as a formatted string
     *
     * @return the TeleportationPoint
     */
    @Override
    public String toString() {
        return x + ", " + y + ", " + z + " (" + worldName + (HuskHomes.getSettings().doBungee() ? "/" + server : "") + ")";
    }
}
