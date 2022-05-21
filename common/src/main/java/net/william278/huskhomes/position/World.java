package net.william278.huskhomes.position;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a world on a server
 */
public class World {

    /**
     * The name of this world, as defined by the world directory name
     */
    @NotNull
    public String name;

    /**
     * UUID of this world, as defined by the {@code uid.dat} file in the world directory
     */
    @NotNull
    public UUID uuid;

    public World(@NotNull String name, @NotNull UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

}
