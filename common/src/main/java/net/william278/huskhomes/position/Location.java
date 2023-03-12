package net.william278.huskhomes.position;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a local position on this server
 */
public class Location {

    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private World world;

    public Location(double x, double y, double z, @NotNull World world) {
        this.setX(x);
        this.setY(y);
        this.setZ(z);
        this.setYaw(0);
        this.setPitch(0);
        this.setWorld(world);
    }

    public Location(double x, double y, double z, float yaw, float pitch,
                    @NotNull World world) {
        this.setX(x);
        this.setY(y);
        this.setZ(z);
        this.setYaw(yaw);
        this.setPitch(pitch);
        this.setWorld(world);
    }

    public Location() {
    }

    /**
     * Update the position to match that of another position
     *
     * @param position The position to update to
     */
    public void update(@NotNull Position position) {
        this.setX(position.getX());
        this.setY(position.getY());
        this.setZ(position.getZ());
        this.setYaw(position.getYaw());
        this.setPitch(position.getPitch());
        this.setWorld(position.getWorld());
    }

    /**
     * The double-precision defined x coordinate of the location
     */
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    /**
     * The double-precision defined y coordinate of the location
     */
    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    /**
     * The double-precision defined z coordinate of the location
     */
    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    /**
     * The float-precision defined yaw facing of the location
     */
    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    /**
     * The float-precision defined pitch facing of the location
     */
    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    /**
     * The {@link World} the location is on
     */
    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }
}
