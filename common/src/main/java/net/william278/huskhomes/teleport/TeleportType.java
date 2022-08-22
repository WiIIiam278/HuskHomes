package net.william278.huskhomes.teleport;

import java.util.Arrays;
import java.util.Optional;

/**
 * Represents the type of teleport being used
 */
public enum TeleportType {

    TELEPORT(0),
    RESPAWN(1);

    public final int typeId;

    TeleportType(final int typeId) {
        this.typeId = typeId;
    }

    /**
     * Returns a {@link TeleportType} by its type id.
     *
     * @param typeId The type id of the {@link TeleportType} to return.
     * @return The {@link TeleportType} of the given type id.
     */
    public static Optional<TeleportType> getTeleportType(int typeId) {
        return Arrays.stream(values())
                .filter(type -> type.typeId == typeId)
                .findFirst();
    }
}
