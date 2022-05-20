package net.william278.huskhomes.position;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a position somewhere on the proxy network or server
 */
public class Position {

    /**
     * The double-precision defined x coordinate of the position
     */
    public double x;

    /**
     * The double-precision defined y coordinate of the position
     */
    public double y;

    /**
     * The double-precision defined z coordinate of the position
     */
    public double z;

    /**
     * The float-precision defined yaw facing of the position
     */
    public float yaw;

    /**
     * The float-precision defined pitch facing of the position
     */
    public float pitch;

    /**
     * The {@link World} the position is within
     */
    @NotNull
    public World world;

    /**
     * The {@link Server} the position is on
     */
    @NotNull
    public Server server;

    public Position(double x, double y, double z, float yaw, float pitch, @NotNull World world, @NotNull Server server) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = world;
        this.server = server;
    }

}
