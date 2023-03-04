package net.william278.huskhomes.teleport;

import org.jetbrains.annotations.NotNull;

public class TeleportationException extends IllegalArgumentException {

    public TeleportationException(@NotNull Type error) {
        super("Error during teleport operation: " + error.name());
    }

    public enum Type {
        TELEPORTER_NOT_FOUND,
        TARGET_NOT_FOUND,
        ALREADY_WARMING_UP, ECONOMY_ACTION_FAILED, WARMUP_ALREADY_MOVING, CANNOT_TELEPORT_TO_SELF
    }
}
