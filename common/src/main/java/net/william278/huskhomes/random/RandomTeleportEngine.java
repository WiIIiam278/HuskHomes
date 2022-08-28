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
 * An engine for random teleportation, that generates and caches random locations and serves them as requested
 * <p>
 * See {@link #generateRandomLocation(Location, String...)} for details on how to generate positions
 *
 * @since 3.0
 */
public abstract class RandomTeleportEngine {

    /**
     * Name of the engine
     */
    @NotNull
    protected final String name;
    /**
     * Number of random positions to cache on each world
     */
    protected final int cacheSize;
    /**
     * {@link World}s to serve random positions in
     */
    @NotNull
    private final List<World> worlds;
    /**
     * The cache of random positions for each world
     */
    @NotNull
    private final Map<World, Queue<Location>> cachedRandomLocations;

    /**
     * <b>(Internal use only)</b> Create a new random teleport engine, using HuskHomes config settings
     *
     * @param implementor The HuskHomes instance
     * @param name        The name of the engine
     */
    protected RandomTeleportEngine(@NotNull HuskHomes implementor, @NotNull String name) {
        this.name = name;
        this.cacheSize = implementor.getSettings().rtpLocationCacheSize;
        this.worlds = implementor.getWorlds().stream()
                .filter(world -> world.environment != World.Environment.NETHER)
                .filter(world -> !implementor.getSettings().rtpRestrictedWorlds.contains(world.name))
                .collect(Collectors.toList());
        this.cachedRandomLocations = new HashMap<>();
    }

    /**
     * Create a new random teleport engine
     *
     * @param name   The name of the engine
     * @param worlds The worlds to serve random locations for. Random locations will be generated using
     *               {@link #generateRandomLocation(Location, String...)} for each world in this list when
     *               {@link #initialize()} is called.
     * @since 3.0
     */
    @SuppressWarnings("unused")
    protected RandomTeleportEngine(@NotNull String name, @NotNull List<World> worlds) {
        this.name = name;
        this.cacheSize = 10;
        this.worlds = worlds;
        this.cachedRandomLocations = new HashMap<>();
    }

    /**
     * Initializes the RTP engine, populating the cache with random positons for each world.
     * This is called after the engine is registered.
     *
     * @since 3.0
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
     * Populates the cache with new random locations, by calling {@link #generateRandomLocation(Location, String...)}
     *
     * @param world     The world to populate the cache for
     * @param cacheSize The size of the cache
     * @since 3.0
     */
    private void populateCache(@NotNull World world, final int cacheSize) {
        final int amountToPopulate = Math.max(1, cacheSize) - cachedRandomLocations.get(world).size();
        for (final AtomicInteger i = new AtomicInteger(); i.get() < amountToPopulate; i.getAndIncrement()) {
            generateRandomLocation(new Location(0, 0, 0, 0f, 0f, world)).join()
                    .ifPresentOrElse(location -> cachedRandomLocations.get(world).add(location), i::getAndDecrement);
        }
    }

    /**
     * Generates a new random {@link Location} on this server within the radius of the origin position.
     * <p>
     * The default implementation of a {@link RandomTeleportEngine} will only invoke this message in two occasions:
     * <ul>
     *     <li>If the cache is empty (the arguments specified by the permission request command will be passed).</li>
     *     <li>When a position has been requested from the cache ({@link #getRandomLocation(World, String...)}, this will
     *     be called as many times as needed to replenish the cache. (no arguments will be passed).</li>
     *     <li>When {@link #initialize()} is called while the plugin is enabling (no arguments will be passed).</li>
     * </ul>
     * Locations returned by this method should be safe for players to teleport to. This means that the location has
     * solid ground to stand on and that the player will not die by standing still at the location.
     * Implementations of HuskHomes validate location safety using {@link HuskHomes#getSafeGroundLocation(Location)}.
     * <p>
     * RandomTeleportEngines that wish to return cross-server random {@link Position}s can use this method to cache
     * locations on this server, and override {@link #getRandomPosition(Position, String...)} to return a random position
     * from the caches of the engine on other servers.
     * <p>
     * If you'd prefer not to use caching for whatever reason, you can simply override {@link #getRandomLocation(World, String...)}
     * to call this method directly, though this will affect the speed at which positions can be served to users.
     *
     * @param origin The origin {@link Location} to generate a random position around (i.e. the server spawn, or
     *               {@code x: 0, z: 0})
     * @param args   Arguments specified by the random location request. These may be used to alter which locations are
     *               generated by the randomizer.
     * @return a generated {@link Location} if one was found; an {@link Optional#empty()} otherwise
     * @since 3.0
     */
    @NotNull
    protected abstract CompletableFuture<Optional<Location>> generateRandomLocation(@NotNull Location origin,
                                                                                    @NotNull String... args);

    /**
     * Returns a random {@link Position} from the cache, generating if the cache is empty
     *
     * @param userPosition The {@link Position} of the requesting user
     * @param args         Arguments specified by the random location request. These may be used to alter which locations are
     *                     generated by the randomizer.
     * @return a generated {@link Location} if one was found; an {@link Optional#empty()} otherwise
     * @since 3.0
     */
    public CompletableFuture<Optional<Position>> getRandomPosition(@NotNull Position userPosition,
                                                                   @NotNull String... args) {
        return getRandomLocation(userPosition.world, args).thenApply(location -> {
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
     * @param args  Arguments specified by the random location request. These may be used to alter which locations are
     *              generated by the randomizer.
     * @return a generated {@link Location} if one was found; an {@link Optional#empty()} otherwise
     * @since 3.0
     */
    private CompletableFuture<Optional<Location>> getRandomLocation(@NotNull World world, @NotNull String... args) {
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
            return generateRandomLocation(new Location(0, 0, 0, 0f, 0f, world), args).join();
        }).orTimeout(5, TimeUnit.SECONDS).exceptionally(throwable -> Optional.empty());
    }

}
