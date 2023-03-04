package net.william278.huskhomes.teleport;

import org.jetbrains.annotations.NotNull;

public interface Teleportable {

    @NotNull
    static Teleportable username(@NotNull String teleporter) {
        return new Username(teleporter);
    }

}
