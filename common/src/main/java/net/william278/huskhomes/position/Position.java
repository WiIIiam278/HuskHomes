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

    @SuppressWarnings("unused")
    public Position() {
    }

    /**
     * Parses the position from a set of values
     *
     * @param values The values to parse
     * @return The position if it could be parsed, otherwise an empty optional
     */
    public static Optional<Position> parse(@NotNull Position currentPosition, @NotNull String... values) {
        System.out.println(String.join(" ", values));

        // Validate argument length
        if (values.length < 3 || values.length > 5) {
            System.out.println("Invalid syntax");
            return Optional.empty();
        }

        // Parse, handle relatives, and return, catching NumberFormatExceptions and returning an empty optional
        try {
            final double x = parseCoordinate(values[0], currentPosition.x);
            final double y = parseCoordinate(values[1], currentPosition.y);
            final double z = parseCoordinate(values[2], currentPosition.z);
            final World world = values.length > 3 ? new World(values[3], UUID.randomUUID()) : currentPosition.world;
            final Server server = values.length > 4 ? new Server(values[4]) : currentPosition.server;
            return Optional.of(new Position(x, y, z, currentPosition.yaw, currentPosition.pitch, world, server));
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
