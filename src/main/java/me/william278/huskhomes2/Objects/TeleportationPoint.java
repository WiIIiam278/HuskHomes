package me.william278.huskhomes2.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class TeleportationPoint {

    String server;
    String worldName;
    double x;
    double y;
    double z;
    float yaw;
    float pitch;

    private void setLocation(Location location, String server) {
        worldName = location.getWorld().getName();
        x = location.getX();
        y = location.getY();
        z = location.getZ();
        yaw = location.getYaw();
        pitch = location.getPitch();
        this.server = server;
    }

    public TeleportationPoint(Location location, String server) {
        if (location.getWorld() == null) {
            throw new IllegalStateException("The location provided has an invalid world");
        }
        setLocation(location, server);
    }

    public TeleportationPoint(String worldName, double x, double y, double z, float yaw, float pitch, String server) {
        this.server = server;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public void updateLocation(Location location, String server) {
        setLocation(location, server);
    }

    public String getServer() {
        return server;
    }

    public Location getLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            return new Location(world, x, y, z, yaw, pitch);
        } else {
            throw new IllegalStateException("The world \"" + worldName + "\" could not be found on the server; could not return location");
        }
    }

}
