package net.william278.huskhomes.random;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * An engine for random teleportation, that generates and caches random points
 */
public abstract class RtpEngine {

    @NotNull
    protected final String name;
    protected final int radius;
    protected final int spawnRadius;
    protected final int cacheSize;
    @NotNull
    private final List<World> worlds;
    @NotNull
    private final Map<World, Queue<Location>> cachedRandomLocations;

    protected RtpEngine(@NotNull HuskHomes implementor, @NotNull String name) {
        this.name = name;
        this.radius = implementor.getSettings().rtpRadius;
        this.spawnRadius = implementor.getSettings().rtpSpawnRadius;
        this.cacheSize = implementor.getSettings().rtpLocationCacheSize;
        this.worlds = implementor.getWorlds().stream()
                .filter(world -> world.environment != World.Environment.NETHER)
                .filter(world -> !implementor.getSettings().rtpRestrictedWorlds.contains(world.name))
                .collect(Collectors.toList());
        this.cachedRandomLocations = new HashMap<>();
    }

    //todo better API for this?
    protected RtpEngine(@NotNull String name, int radius, int spawnRadius, int cacheSize, @NotNull List<World> worlds) {
        this.name = name;
        this.radius = radius;
        this.spawnRadius = spawnRadius;
        this.cacheSize = cacheSize;
        this.worlds = worlds;
        this.cachedRandomLocations = new HashMap<>();
    }

    /**
     * Initializes the RTP engine, populating the cache. This is to be called after the engine has been created.
     */
    public void initialize() {
        CompletableFuture.runAsync(() -> {
            for (final World world : worlds) {
                cachedRandomLocations.put(world, new LinkedList<>());
                populateCache(world, cacheSize);
            }
        });
    }

    /**
     * Populates the cache with new random locations
     *
     * @param world     The world to populate the cache for
     * @param cacheSize The size of the cache
     */
    private void populateCache(@NotNull World world, final int cacheSize) {
        final int amountToPopulate = Math.max(1, cacheSize) - cachedRandomLocations.get(world).size();
        for (final AtomicInteger i = new AtomicInteger(); i.get() < amountToPopulate; i.getAndIncrement()) {
            generateRandomPosition(new Location(0, 0, 0, 0f, 0f, world)).join()
                    .ifPresentOrElse(location -> cachedRandomLocations.get(world).add(location), i::getAndDecrement);
        }
    }

    /**
     * Generates a new random position within the radius of the origin position.
     *
     * @param origin The origin position
     * @return A generated location
     */
    @NotNull
    protected abstract CompletableFuture<Optional<Location>> generateRandomPosition(@NotNull Location origin);

    /**
     * Returns a random {@link Position} from the cache, generating only if needed
     *
     * @param userPosition The user's position
     * @return A random {@link Position}
     */
    public CompletableFuture<Optional<Position>> getRandomPosition(@NotNull Position userPosition) {
        return getRandomLocation(userPosition.world).thenApply(location -> {
            if (location.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new Position(location.get().x, location.get().y, location.get().z, location.get().yaw,
                    location.get().pitch, location.get().world, userPosition.server));
        });
    }

    /**
     * Returns a random {@link Location} on this server from the cache, only generating if necessary
     *
     * @param world the {@link World} to search for a random {@link Location}
     * @return a {@link CompletableFuture} containing an {@link Optional} containing a {@link Location} if one was found;
     * an {@link Optional#empty()} otherwise
     */
    private CompletableFuture<Optional<Location>> getRandomLocation(@NotNull World world) {
        return CompletableFuture.supplyAsync(() -> {
            final Optional<World> keyedWorld = cachedRandomLocations.keySet().stream()
                    .filter(cachedWorld -> cachedWorld.uuid.equals(world.uuid)).findFirst();
            if (keyedWorld.isPresent()) {
                final Queue<Location> cachedWorldLocations = cachedRandomLocations.get(keyedWorld.get());
                if (!cachedWorldLocations.isEmpty()) {
                    populateCache(keyedWorld.get(), cacheSize);
                    return Optional.of(cachedWorldLocations.remove());
                } else {
                    populateCache(keyedWorld.get(), cacheSize);
                }
            }
            return generateRandomPosition(world.spawn != null ? world.spawn : new Location(0, 0, 0, world))
                    .join();
        }).orTimeout(5, TimeUnit.SECONDS).exceptionally(throwable -> Optional.empty());
    }

}
