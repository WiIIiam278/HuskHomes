package net.william278.huskhomes.position;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a position - a location somewhere on the proxy network or server
 */
public class Position extends Location {

    /**
     * The {@link Server} the position is on
     */
    @NotNull
    public Server server;

    public Position(double x, double y, double z, float yaw, float pitch,
                    @NotNull World world, @NotNull Server server) {
        super(x, y, z, yaw, pitch, world);
        this.server = server;
    }

}
