package net.william278.huskhomes.position;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents a world on a server
 */
public class World {

    /**
     * The name of this world, as defined by the world directory name
     */
    public String name;

    /**
     * UUID of this world, as defined by the {@code uid.dat} file in the world directory
     */
    public UUID uuid;

    /**
     * Environment of the world ({@link Environment#OVERWORLD}, {@link Environment#NETHER}, {@link Environment#THE_END},
     * or {@link Environment#CUSTOM})
     */
    @Nullable
    public Environment environment;

    public World(@NotNull String name, @NotNull UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public World(@NotNull String name, @NotNull UUID uuid, @Nullable Environment environment) {
        this.name = name;
        this.uuid = uuid;
        this.environment = environment;
    }

    @SuppressWarnings("unused")
    public World() {
    }

    /**
     * Identifies the environment of the world
     */
    public enum Environment {
        OVERWORLD,
        NETHER,
        THE_END,
        CUSTOM
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        if (obj instanceof World world) {
            return world.uuid.equals(uuid);
        }
        return super.equals(obj);
    }
}
