package net.william278.huskhomes.util;

import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.WorldTypeTemplate;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldManager;

import java.util.Optional;

public class SpongeAdapter {

    /**
     * Adapt a Sponge {@link ServerLocation} to a HuskHomes {@link Location}
     *
     * @param location the Sponge {@link ServerLocation} to adapt
     * @return the adapted {@link Location}
     */
    public static Optional<ServerLocation> adaptLocation(@NotNull Location location) {
        final WorldManager worldManager = Sponge.server().worldManager();
        return worldManager.world(ResourceKey.resolve(location.world.name))
                .map(world -> ServerLocation.of(world, location.x, location.y, location.z)).or(() -> {
                    final Optional<ResourceKey> worldKey = worldManager.worldKey(location.world.uuid);
                    if (worldKey.isEmpty()) {
                        return Optional.empty();
                    }
                    return worldManager.world(worldKey.get())
                            .map(world -> ServerLocation.of(world, location.x, location.y, location.z));
                });
    }

    /**
     * Adapt a HuskHomes {@link Location} to a Sponge {@link ServerLocation}
     *
     * @param location the HuskHomes {@link Location} to adapt
     * @return the adapted {@link ServerLocation}
     */
    public static Optional<Location> adaptLocation(@NotNull ServerLocation location) {
        return Optional.of(new Location(location.x(), location.y(), location.z(),
                0f, 0f,
                adaptWorld(location.world()).orElse(new World())));
    }

    /**
     * Adapt a Sponge {@link ServerWorld} to a HuskHomes {@link World}
     *
     * @param world the Sponge {@link ServerWorld} to adapt
     * @return the adapted {@link World}
     */
    public static Optional<World> adaptWorld(@Nullable ServerWorld world) {
        if (world == null) {
            return Optional.empty();
        }
        final String worldType = world.properties().worldType().asTemplate().key().asString();
        return Optional.of(new World(world.key().toString(), world.uniqueId(),
                (worldType.equals(WorldTypeTemplate.theNether().key().toString()) ? World.Environment.NETHER
                        : worldType.equals(WorldTypeTemplate.theEnd().key().toString()) ? World.Environment.THE_END
                        : worldType.equals(WorldTypeTemplate.overworld().key().toString()) ? World.Environment.OVERWORLD
                        : World.Environment.CUSTOM)));
    }
}
