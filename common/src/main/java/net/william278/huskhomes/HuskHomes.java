package net.william278.huskhomes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.key.Key;
import net.william278.annotaml.Annotaml;
import net.william278.desertwell.UpdateChecker;
import net.william278.desertwell.Version;
import net.william278.huskhomes.command.Command;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.config.Server;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.config.Spawn;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.event.EventDispatcher;
import net.william278.huskhomes.hook.*;
import net.william278.huskhomes.manager.Manager;
import net.william278.huskhomes.network.Broker;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.random.RandomTeleportEngine;
import net.william278.huskhomes.user.ConsoleUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.util.TaskRunner;
import net.william278.huskhomes.util.ThrowingConsumer;
import net.william278.huskhomes.util.UnsafeBlocks;
import net.william278.huskhomes.util.Validator;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * Represents a cross-platform instance of the plugin
 */
public interface HuskHomes extends TaskRunner, EventDispatcher {

    int SPIGOT_RESOURCE_ID = 83767;

    @NotNull
    ConsoleUser getConsole();

    /**
     * The {@link Set} of online {@link OnlineUser}s on this server
     *
     * @return a {@link Set} of currently online {@link OnlineUser}s
     */
    @NotNull
    List<OnlineUser> getOnlineUsers();

    @NotNull
    Set<SavedUser> getSavedUsers();

    default Optional<SavedUser> getSavedUser(@NotNull User user) {
        return getSavedUsers().stream()
                .filter(savedUser -> savedUser.getUser().equals(user))
                .findFirst();
    }

    default void editUserData(@NotNull User user, @NotNull Consumer<SavedUser> editor) {
        runAsync(() -> getSavedUser(user)
                .ifPresent(result -> {
                    editor.accept(result);
                    getDatabase().updateUserData(result);
                }));
    }

    /**
     * Initialize a faucet of the plugin
     *
     * @param name   the name of the faucet
     * @param runner a runnable for initializing the faucet
     */
    default void initialize(@NotNull String name, @NotNull ThrowingConsumer<HuskHomes> runner) {
        log(Level.INFO, "Initializing " + name + "...");
        try {
            runner.accept(this);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize " + name, e);
        }
        log(Level.INFO, "Successfully initialized " + name);
    }

    /**
     * Finds a local {@link OnlineUser} by their name. Auto-completes partially typed names for the closest match
     *
     * @param playerName the name of the player to find
     * @return an {@link Optional} containing the {@link OnlineUser} if found, or an empty {@link Optional} if not found
     */
    @NotNull
    default Optional<OnlineUser> findOnlinePlayer(@NotNull String playerName) {
        return getOnlineUsers().stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(playerName))
                .findFirst()
                .or(() -> getOnlineUsers().stream()
                        .filter(user -> user.getUsername().toLowerCase().startsWith(playerName.toLowerCase()))
                        .findFirst());
    }

    /**
     * The plugin {@link Settings} loaded from file
     *
     * @return the plugin {@link Settings}
     */
    @NotNull
    Settings getSettings();

    void setSettings(@NotNull Settings settings);

    /**
     * The plugin messages loaded from file
     *
     * @return The plugin {@link Locales}
     */
    @NotNull
    Locales getLocales();

    void setLocales(@NotNull Locales locales);

    /**
     * The local {@link Spawn} location of this server, as cached to disk
     *
     * @return the {@link Spawn} location data
     * @see #getSpawn() for the canonical spawn point to use
     */
    Optional<Spawn> getServerSpawn();

    void setServerSpawn(@NotNull Spawn spawn);

    /**
     * The canonical spawn {@link Position} of this server, if it has been set
     *
     * @return the {@link Position} of the spawn, or an empty {@link Optional} if it has not been set
     */
    default Optional<Position> getSpawn() {
        return getSettings().doCrossServer() && getSettings().isGlobalSpawn()
                ? getDatabase().getWarp(getSettings().getGlobalSpawnName()).map(warp -> (Position) warp)
                : getServerSpawn().map(spawn -> spawn.getPosition(getServerName()));
    }

    /**
     * Returns the {@link Server} the plugin is on
     *
     * @return The {@link Server} object
     */
    @NotNull
    String getServerName();

    void setServer(@NotNull Server server);

    void setUnsafeBlocks(@NotNull UnsafeBlocks unsafeBlocks);

    /**
     * The {@link Database} that stores persistent plugin data
     *
     * @return the {@link Database} implementation for accessing data
     */
    @NotNull
    Database getDatabase();

    /**
     * The {@link Validator} for validating thome names and descriptions
     *
     * @return the {@link Validator} instance
     */
    @NotNull
    Validator getValidator();

    /**
     * The {@link Manager} that manages home, warp and user data
     *
     * @return the {@link Manager} implementation
     */
    @NotNull
    Manager getManager();

    /**
     * The {@link Broker} that sends cross-network messages
     *
     * @return the {@link Broker} implementation
     */
    @NotNull
    Broker getMessenger();

    /**
     * The {@link RandomTeleportEngine} that manages random teleports
     *
     * @return the {@link RandomTeleportEngine} implementation
     */
    @NotNull
    RandomTeleportEngine getRandomTeleportEngine();

    /**
     * Sets the {@link RandomTeleportEngine} to be used for processing random teleports
     *
     * @param randomTeleportEngine the {@link RandomTeleportEngine} to use
     */
    void setRandomTeleportEngine(@NotNull RandomTeleportEngine randomTeleportEngine);


    /**
     * Update the {@link Spawn} position to a location on the server
     *
     * @param location the new {@link Spawn} location
     */
    void setServerSpawn(@NotNull Location location);

    /**
     * Set of active {@link Hook}s running on the server
     *
     * @return the {@link Set} of active {@link Hook}s
     */
    @NotNull
    List<Hook> getHooks();

    void setHooks(@NotNull List<Hook> hooks);

    default <T extends Hook> Optional<T> getHook(@NotNull Class<T> hookClass) {
        return getHooks().stream()
                .filter(hook -> hookClass.isAssignableFrom(hook.getClass()))
                .map(hookClass::cast)
                .findFirst();
    }

    default Optional<EconomyHook> getEconomyHook() {
        return getHook(EconomyHook.class);
    }

    default Optional<MapHook> getMapHook() {
        return getHook(MapHook.class);
    }

    /**
     * Perform an economy check on the {@link OnlineUser}; returning {@code true} if it passes the check
     *
     * @param player the player to perform the check on
     * @param action the action to perform
     * @return {@code true} if the action passes the check, {@code false} if the user has insufficient funds
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    default boolean validateEconomyCheck(@NotNull OnlineUser player, @NotNull EconomyHook.Action action) {
        final Optional<Double> cost = getSettings().getEconomyCost(action).map(Math::abs);
        if (cost.isPresent() && !player.hasPermission(EconomyHook.BYPASS_PERMISSION)) {
            final Optional<EconomyHook> hook = getEconomyHook();
            if (hook.isPresent()) {
                if (cost.get() > hook.get().getPlayerBalance(player)) {
                    getLocales().getLocale("error_insufficient_funds", hook.get().formatCurrency(cost.get()))
                            .ifPresent(player::sendMessage);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Execute an economy transaction if needed, updating the player's balance
     *
     * @param player the player to deduct the cost from if needed
     * @param action the action to deduct the cost from if needed
     */
    default void performEconomyTransaction(@NotNull OnlineUser player, @NotNull EconomyHook.Action action) {
        if (!getSettings().doEconomy()) return;
        final Optional<Double> cost = getSettings().getEconomyCost(action).map(Math::abs);

        if (cost.isPresent() && !player.hasPermission(EconomyHook.BYPASS_PERMISSION)) {
            final Optional<EconomyHook> hook = getEconomyHook();
            if (hook.isPresent()) {
                hook.get().changePlayerBalance(player, -cost.get());
                getLocales().getLocale(action.confirmationLocaleId, hook.get().formatCurrency(cost.get()))
                        .ifPresent(player::sendMessage);
            }
        }
    }

    /**
     * Returns a safe ground location for the specified {@link Location} if possible
     *
     * @param location the {@link Location} to find a safe ground location for
     * @return a {@link CompletableFuture} that will complete with an optional of the safe ground position, if it is
     * possible to find one
     */
    CompletableFuture<Optional<Location>> findSafeGroundLocation(@NotNull Location location);

    /**
     * Returns a resource read from the plugin resources folder
     *
     * @param name the name of the resource
     * @return the resource read as an {@link InputStream}
     */
    @Nullable
    InputStream getResource(@NotNull String name);

    /**
     * Returns the plugin data folder containing the plugin config, etc
     *
     * @return the plugin data folder
     */
    @NotNull
    File getDataFolder();

    /**
     * Returns a list of worlds on the server
     *
     * @return a list of worlds on the server
     */
    @NotNull
    List<World> getWorlds();

    /**
     * Returns the plugin version
     *
     * @return the plugin {@link Version}
     */
    @NotNull
    Version getVersion();

    /**
     * Returns a list of enabled commands
     *
     * @return A list of registered and enabled {@link Command}s
     */
    @NotNull
    List<Command> getCommands();

    default <T extends Command> Optional<T> getCommand(@NotNull Class<T> type) {
        return getCommands().stream()
                .filter(command -> command.getClass() == type)
                .findFirst()
                .map(type::cast);
    }

    @NotNull
    List<Command> registerCommands();

    default void registerHooks() {
        setHooks(new ArrayList<>());

        if (getSettings().doMapHook()) {
            if (isDependencyLoaded("Dynmap")) {
                getHooks().add(new DynmapHook(this));
            } else if (isDependencyLoaded("BlueMap")) {
                getHooks().add(new BlueMapHook(this));
            }
        }
        if (isDependencyLoaded("Plan")) {
            getHooks().add(new PlanHook(this));
        }
    }

    boolean isDependencyLoaded(@NotNull String name);

    @NotNull
    Map<String, List<String>> getGlobalPlayerList();

    @NotNull
    default List<String> getPlayerList() {
        return Stream.concat(
                getGlobalPlayerList().values().stream().flatMap(Collection::stream),
                getLocalPlayerList().stream()
        ).distinct().sorted().toList();
    }

    default void setPlayerList(@NotNull String server, @NotNull List<String> players) {
        getGlobalPlayerList().values().forEach(list -> {
            list.removeAll(players);
            list.removeAll(getLocalPlayerList());
        });
        getGlobalPlayerList().put(server, players);
    }

    @NotNull
    default List<String> getLocalPlayerList() {
        return getOnlineUsers().stream()
                .map(OnlineUser::getUsername)
                .toList();
    }

    @NotNull
    Set<UUID> getCurrentlyOnWarmup();

    /**
     * Returns if the given user is currently warming up to teleport to a home.
     *
     * @param userUuid The user to check.
     * @return If the user is currently warming up.
     */
    default boolean isWarmingUp(@NotNull UUID userUuid) {
        return this.getCurrentlyOnWarmup().contains(userUuid);
    }

    /**
     * Reloads the {@link Settings} and {@link Locales} from their respective config files
     *
     * @return {@code true} if the reload was successful, {@code false} otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    default boolean loadConfigs() {
        try {
            // Load settings
            setSettings(Annotaml.create(new File(getDataFolder(), "config.yml"), Settings.class).get());

            // Load locales from language preset default
            final Locales languagePresets = Annotaml.create(Locales.class, Objects.requireNonNull(getResource("locales/" + getSettings().getLanguage() + ".yml"))).get();
            setLocales(Annotaml.create(new File(getDataFolder(), "messages_" + getSettings().getLanguage() + ".yml"), languagePresets).get());

            // Load server from file
            if (getSettings().doCrossServer()) {
                setServer(Annotaml.create(new File(getDataFolder(), "server.yml"), Server.class).get());
            } else {
                setServer(new Server(Server.getDefaultServerName()));
            }

            // Load spawn location from file
            final File spawnFile = new File(getDataFolder(), "spawn.yml");
            if (spawnFile.exists()) {
                setServerSpawn(Annotaml.create(spawnFile, Spawn.class).get());
            }

            // Load unsafe blocks from resources
            final InputStream blocksResource = getResource("safety/unsafe_blocks.yml");
            setUnsafeBlocks(Annotaml.create(new UnsafeBlocks(), Objects.requireNonNull(blocksResource)).get());

            return true;
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            log(Level.SEVERE, "Failed to reload HuskHomes config or messages file", e);
        }
        return false;
    }

    @NotNull
    default UpdateChecker getUpdateChecker() {
        return UpdateChecker.create(getVersion(), SPIGOT_RESOURCE_ID);
    }

    default void checkForUpdates() {
        if (getSettings().doCheckForUpdates()) {
            getUpdateChecker().isUpToDate().thenAccept(updated -> {
                if (!updated) {
                    getUpdateChecker().getLatestVersion().thenAccept(latest -> log(Level.WARNING,
                            "A new version of HuskTowns is available: v" + latest + " (running v" + getVersion() + ")"));
                }
            });
        }
    }

    /**
     * Returns if the block, by provided identifier, is unsafe
     *
     * @param blockId The block identifier (e.g. {@code minecraft:stone})
     * @return {@code true} if the block is on the unsafe blocks list, {@code false} otherwise
     */
    boolean isBlockUnsafe(@NotNull String blockId);

    /**
     * Registers the plugin with bStats metrics
     *
     * @param metricsId the bStats id for the plugin
     */
    void registerMetrics(int metricsId);

    /**
     * Initialize plugin messaging channels
     */
    void initializePluginChannels();

    /**
     * Log a message to the console
     *
     * @param level      the level to log at
     * @param message    the message to log
     * @param exceptions any exceptions to log
     */
    void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... exceptions);

    /**
     * Create a resource key namespaced with the plugin id
     *
     * @param data the string ID elements to join
     * @return the key
     */
    @NotNull
    default Key getKey(@NotNull String... data) {
        if (data.length == 0) {
            throw new IllegalArgumentException("Cannot create a key with no data");
        }
        @Subst("foo") final String joined = String.join("/", data);
        return Key.key("huskhomes", joined);
    }

    @NotNull
    default Gson getGson() {
        return new GsonBuilder().create();
    }

}
