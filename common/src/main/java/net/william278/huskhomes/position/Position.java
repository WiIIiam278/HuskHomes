package net.william278.huskhomes.position;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents a position - a {@link Location} somewhere on the proxy network or server
 */
public class Position extends Location {

    /**
     * The {@link Server} the position is on
     */
    public Server server;

    public Position(double x, double y, double z, float yaw, float pitch,
                    @NotNull World world, @NotNull Server server) {
        super(x, y, z, yaw, pitch, world);
        this.server = server;
    }

    public Position(@NotNull Location location, @NotNull Server server) {
        super(location.x, location.y, location.z, location.yaw, location.pitch, location.world);
        this.server = server;
    }

    @SuppressWarnings("unused")
    public Position() {
    }

    /**
     * Update the position to match that of another position
     *
     * @param newPosition The position to update to
     */
    public void update(@NotNull Position newPosition) {
        this.x = newPosition.x;
        this.y = newPosition.y;
        this.z = newPosition.z;
        this.yaw = newPosition.yaw;
        this.pitch = newPosition.pitch;
        this.world = newPosition.world;
        this.server = newPosition.server;
    }

    /**
     * Parses the position from a set of values
     *
     * @param args       The values to parse
     * @param relativeTo The {@link Position} to use as a reference for relative coordinates
     * @return The position if it could be parsed, otherwise an empty optional
     */
    public static Optional<Position> parse(@NotNull String[] args, @NotNull Position relativeTo) {
        // Validate argument length
        if (args.length < 3 || args.length > 5) {
            return Optional.empty();
        }

        // Parse, handle relatives, and return, catching NumberFormatExceptions and returning an empty optional
        try {
            final double x = parseCoordinate(args[0], relativeTo.x);
            final double y = parseCoordinate(args[1], relativeTo.y);
            final double z = parseCoordinate(args[2], relativeTo.z);
            final World world = args.length > 3 ? new World(args[3], UUID.randomUUID()) : relativeTo.world;
            final Server server = args.length > 4 ? new Server(args[4]) : relativeTo.server;
            return Optional.of(new Position(x, y, z, relativeTo.yaw, relativeTo.pitch, world, server));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Parses a coordinate double value from a string
     *
     * @param value        The string to parse
     * @param currentValue The current value of the coordinate, for handling relatives
     * @return The parsed coordinate
     * @throws NumberFormatException If the string could not be parsed
     */
    private static double parseCoordinate(@NotNull String value, double currentValue) throws NumberFormatException {
        // Future: Consider supporting ^ ^ ^ (position-relative-to-facing)
        if (value.startsWith("~")) {
            if (value.length() == 1) {
                return currentValue;
            } else {
                return currentValue + Double.parseDouble(value.substring(1));
            }
        } else {
            return Double.parseDouble(value);
        }
    }
}
