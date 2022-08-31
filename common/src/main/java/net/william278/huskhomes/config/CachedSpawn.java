package net.william278.huskhomes.config;

import net.william278.annotaml.YamlFile;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.Server;
import net.william278.huskhomes.position.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * Used to store the server spawn location
 */
@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃ Server /spawn location cache ┃
        ┃ Edit in-game using /setspawn ┃
        ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛""")
public class CachedSpawn {

    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public String worldName;
    public String worldUuid;

    /**
     * Returns the {@link Position} of the spawn
     *
     * @return The {@link Position} of the spawn
     */
    public Optional<Position> getLocation(@NotNull Server server) {
        try {
            return Optional.of(new Position(x, y, z, yaw, pitch,
                    new World(worldName, UUID.fromString(worldUuid)), server));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Set the {@link Location} of the spawn
     *
     * @param location The {@link Location} of the spawn
     */
    public CachedSpawn(@NotNull Location location) {
        this.x = location.x;
        this.y = location.y;
        this.z = location.z;
        this.yaw = location.yaw;
        this.pitch = location.pitch;
        this.worldName = location.world.name;
        this.worldUuid = location.world.uuid.toString();
    }

    @SuppressWarnings("unused")
    public CachedSpawn() {
    }
}
