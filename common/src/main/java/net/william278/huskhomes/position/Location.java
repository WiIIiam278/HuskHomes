package net.william278.huskhomes.position;

import org.jetbrains.annotations.NotNull;

public class Location {

    /**
     * The double-precision defined x coordinate of the location
     */
    public double x;

    /**
     * The double-precision defined y coordinate of the location
     */
    public double y;

    /**
     * The double-precision defined z coordinate of the location
     */
    public double z;

    /**
     * The float-precision defined yaw facing of the location
     */
    public float yaw;

    /**
     * The float-precision defined pitch facing of the location
     */
    public float pitch;

    /**
     * The {@link World} the location is on
     */
    @NotNull
    public World world;

    public Location(double x, double y, double z, float yaw, float pitch,
                    @NotNull World world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = world;
    }

}
