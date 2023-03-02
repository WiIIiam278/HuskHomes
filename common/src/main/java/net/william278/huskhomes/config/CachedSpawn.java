package net.william278.huskhomes.config;

import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
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
    @YamlKey("world_name")
    public String worldName;
    @YamlKey("world_uuid")
    public String worldUuid;

    /**
     * Returns the {@link Position} of the spawn
     *
     * @param server The server the spawn is on
     * @return The {@link Position} of the spawn
     */
    @NotNull
    public Position getPosition(@NotNull String server) {
        return new Position(x, y, z, yaw, pitch, new World(worldName, UUID.fromString(worldUuid)), server);
    }

    /**
     * Set the {@link Location} of the spawn
     *
     * @param location The {@link Location} of the spawn
     */
    public CachedSpawn(@NotNull Location location) {
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
        this.worldName = location.getWorld().getName();
        this.worldUuid = location.getWorld().getUuid().toString();
    }

    @SuppressWarnings("unused")
    public CachedSpawn() {
    }
}
