package net.william278.huskhomes.position;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents a world on a server
 */
public class World {

    private String name;
    private UUID uuid;
    @Nullable
    private Environment environment;

    private World(@NotNull String name, @NotNull UUID uuid, @Nullable Environment environment) {
        this.setName(name);
        this.setUuid(uuid);
        this.setEnvironment(environment);
    }

    @SuppressWarnings("unused")
    public World() {
    }

    @NotNull
    public static World from(@NotNull String name, @NotNull UUID uuid, @NotNull Environment environment) {
        return new World(name, uuid, environment);
    }

    @NotNull
    public static World from(@NotNull String name, @NotNull UUID uuid) {
        return new World(name, uuid, null);
    }

    /**
     * The name of this world, as defined by the world directory name
     */
    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    /**
     * UUID of this world, as defined by the {@code uid.dat} file in the world directory
     */
    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Environment of the world ({@link Environment#OVERWORLD}, {@link Environment#NETHER}, {@link Environment#THE_END},
     * or {@link Environment#CUSTOM})
     * <p>
     * Will return {@link Environment#OVERWORLD} if the environment is null
     */
    @NotNull
    public Environment getEnvironment() {
        return Optional.ofNullable(environment).orElse(Environment.OVERWORLD);
    }

    public void setEnvironment(@Nullable Environment environment) {
        this.environment = environment;
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
            return world.getUuid().equals(getUuid());
        }
        return super.equals(obj);
    }
}
