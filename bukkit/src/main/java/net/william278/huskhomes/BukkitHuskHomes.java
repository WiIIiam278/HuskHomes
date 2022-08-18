package net.william278.huskhomes;

import net.william278.annotaml.Annotaml;
import net.william278.annotaml.AnnotamlException;
import net.william278.huskhomes.command.BukkitCommand;
import net.william278.huskhomes.command.BukkitCommandType;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.database.MySqlDatabase;
import net.william278.huskhomes.database.SqLiteDatabase;
import net.william278.huskhomes.listener.BukkitEventListener;
import net.william278.huskhomes.listener.EventListener;
import net.william278.huskhomes.messenger.NetworkMessenger;
import net.william278.huskhomes.messenger.PluginMessenger;
import net.william278.huskhomes.messenger.RedisMessenger;
import net.william278.huskhomes.player.BukkitPlayer;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.SavedPositionManager;
import net.william278.huskhomes.position.Server;
import net.william278.huskhomes.request.RequestManager;
import net.william278.huskhomes.teleport.TeleportManager;
import net.william278.huskhomes.util.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class BukkitHuskHomes extends JavaPlugin implements HuskHomes {

    /**
     * Metrics ID for <a href="https://bstats.org/plugin/bukkit/HuskHomes/8430">HuskHomes on Bukkit</a>.
     */
    private static final int METRICS_ID = 8430;
    private Settings settings;
    private Locales locales;
    private BukkitLogger logger;
    private BukkitResourceReader resourceReader;
    private Database database;
    private Cache cache;
    private TeleportManager teleportManager;
    private RequestManager requestManager;
    private SavedPositionManager savedPositionManager;
    private EventListener eventListener;

    @Nullable
    private NetworkMessenger networkMessenger;

    @Nullable
    private Server server;

    /**
     * Returns the {@link Server} the plugin is on
     *
     * @param onlineUser {@link OnlineUser} to request the server
     * @return The {@link Server} object
     */
    @Override
    public CompletableFuture<Server> getServer(@NotNull OnlineUser onlineUser) {
        if (server != null) {
            return CompletableFuture.supplyAsync(() -> server);
        }
        if (!getSettings().crossServer) {
            server = new Server("server");
            return CompletableFuture.supplyAsync(() -> server);
        }
        assert networkMessenger != null;
        return networkMessenger.getServerName(onlineUser).thenApplyAsync(server -> {
            this.server = new Server(server);
            return this.server;
        });
    }

    // Instance of the plugin
    private static BukkitHuskHomes instance;

    public static BukkitHuskHomes getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        // Set the instance
        instance = this;
    }

    @Override
    public void onEnable() {
        // Initialize HuskHomes
        final AtomicBoolean initialized = new AtomicBoolean(true);
        try {
            // Set the logging and resource reading adapter
            this.logger = new BukkitLogger(getLogger());
            this.resourceReader = new BukkitResourceReader(this);

            // Load settings and locales
            getLoggingAdapter().log(Level.INFO, "Loading plugin configuration settings & locales...");
            initialized.set(reload().join());
            if (initialized.get()) {
                logger.showDebugLogs(settings.debugLogging);
                getLoggingAdapter().log(Level.INFO, "Successfully loaded plugin configuration settings & locales");
            } else {
                throw new HuskHomesInitializationException("Failed to load plugin configuration settings and/or locales");
            }

            // Initialize the database
            getLoggingAdapter().log(Level.INFO, "Attempting to establish connection to the database...");
            final Settings.DatabaseType databaseType = settings.databaseType;
            this.database = switch (databaseType == null ? Settings.DatabaseType.MYSQL : databaseType) {
                case MYSQL -> new MySqlDatabase(settings, logger, resourceReader);
                case SQLITE -> new SqLiteDatabase(settings, logger, resourceReader);
            };
            initialized.set(this.database.initialize());
            if (initialized.get()) {
                getLoggingAdapter().log(Level.INFO, "Successfully established a connection to the database");
            } else {
                throw new HuskHomesInitializationException("Failed to establish a connection to the database. " +
                                                           "Please check the supplied database credentials in the config file");
            }

            // Initialize the network messenger if proxy mode is enabled
            if (getSettings().crossServer) {
                getLoggingAdapter().log(Level.INFO, "Initializing the network messenger...");
                networkMessenger = switch (settings.messengerType) {
                    case PLUGIN_MESSAGE -> new PluginMessenger();
                    case REDIS -> new RedisMessenger();
                };
                networkMessenger.initialize(this);
                getLoggingAdapter().log(Level.INFO, "Successfully initialized the network messenger.");
            }

            // Initialize the cache
            cache = new Cache();
            cache.initialize(database);

            // Prepare the teleport manager
            this.teleportManager = new TeleportManager(this);

            // Prepare the request manager
            this.requestManager = new RequestManager(this);

            // Prepare the home and warp position manager
            this.savedPositionManager = new SavedPositionManager(database, cache);

            // Register events
            getLoggingAdapter().log(Level.INFO, "Registering events...");
            this.eventListener = new BukkitEventListener(this);
            getLoggingAdapter().log(Level.INFO, "Successfully registered events listener");

            // Register permissions
            getLoggingAdapter().log(Level.INFO, "Registering permissions & commands...");
            Arrays.stream(Permission.values()).forEach(permission -> getServer().getPluginManager().addPermission(
                    new org.bukkit.permissions.Permission(permission.node,
                            switch (permission.defaultAccess) {
                                case EVERYONE -> PermissionDefault.TRUE;
                                case NOBODY -> PermissionDefault.FALSE;
                                case OPERATORS -> PermissionDefault.OP;
                            })));

            // Register commands
            for (final BukkitCommandType bukkitCommandType : BukkitCommandType.values()) {
                final PluginCommand pluginCommand = getCommand(bukkitCommandType.commandBase.command);
                if (pluginCommand != null) {
                    new BukkitCommand(bukkitCommandType.commandBase, this).register(pluginCommand);
                }
            }
            getLoggingAdapter().log(Level.INFO, "Successfully registered permissions & commands.");

            // Hook into bStats metrics
            try {
                new Metrics(this, METRICS_ID);
            } catch (final Exception e) {
                getLoggingAdapter().log(Level.WARNING, "Skipped bStats metrics initialization.");
            }

            // Check for updates
            if (settings.checkForUpdates) {
                getLoggingAdapter().log(Level.INFO, "Checking for updates...");
                CompletableFuture.runAsync(() -> new UpdateChecker(getPluginVersion(), getLoggingAdapter()).logToConsole());
            }
        } catch (HuskHomesInitializationException exception) {
            getLoggingAdapter().log(Level.SEVERE, exception.getMessage());
            initialized.set(false);
        } catch (Exception exception) {
            getLoggingAdapter().log(Level.SEVERE, "An unhandled exception occurred initializing HuskHomes!", exception);
            initialized.set(false);
        } finally {
            // Validate initialization
            if (initialized.get()) {
                getLoggingAdapter().log(Level.INFO, "Successfully enabled HuskHomes v" + getPluginVersion());
            } else {
                getLoggingAdapter().log(Level.SEVERE, "Failed to initialize HuskHomes. The plugin will now be disabled");
                getServer().getPluginManager().disablePlugin(this);
            }
        }
    }

    @Override
    public void onDisable() {
        if (this.eventListener != null) {
            this.eventListener.handlePluginDisable();
        }
        if (database != null) {
            database.terminate();
        }
        if (networkMessenger != null) {
            networkMessenger.terminate();
        }
    }

    @NotNull
    @Override
    public Logger getLoggingAdapter() {
        return logger;
    }

    @NotNull
    @Override
    public List<OnlineUser> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream().map(
                player -> (OnlineUser) BukkitPlayer.adapt(player)).toList();
    }

    @NotNull
    @Override
    public Settings getSettings() {
        return settings;
    }

    @NotNull
    @Override
    public Locales getLocales() {
        return locales;
    }

    @Override
    public @NotNull Database getDatabase() {
        return database;
    }

    @NotNull
    @Override
    public Cache getCache() {
        return cache;
    }

    @NotNull
    @Override
    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    @Override
    public @NotNull RequestManager getRequestManager() {
        return requestManager;
    }

    @NotNull
    @Override
    public SavedPositionManager getSavedPositionManager() {
        return savedPositionManager;
    }

    @Override
    public @Nullable NetworkMessenger getNetworkMessenger() {
        return networkMessenger;
    }

    @Override
    public boolean isValidPositionOnServer(Position position) {
        final Optional<Location> adaptedLocation = BukkitAdapter.adaptLocation(position);
        if (adaptedLocation.isEmpty()) {
            return false;
        }
        final Location location = adaptedLocation.get();
        assert location.getWorld() != null;
        return location.getWorld().getWorldBorder().isInside(location);
    }

    @Override
    public @NotNull Version getPluginVersion() {
        return Version.pluginVersion(getDescription().getVersion());
    }

    @Override
    public @NotNull Version getMinecraftVersion() {
        return Version.minecraftVersion(Bukkit.getBukkitVersion());
    }

    @Override
    public @NotNull String getPlatformType() {
        return getServer().getName();
    }

    @Override
    public CompletableFuture<Boolean> reload() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                this.settings = Annotaml.reload(new File(getDataFolder(), "config.yml"),
                        new Settings(), Annotaml.LoaderOptions.builder().copyDefaults(true));

                this.locales = Annotaml.reload(new File(getDataFolder(), "messages-" + settings.language + ".yml"),
                        Objects.requireNonNull(resourceReader.getResource("locales/" + settings.language + ".yml")),
                        Locales.class, Annotaml.LoaderOptions.builder().copyDefaults(true));
                return true;
            } catch (AnnotamlException e) {
                getLoggingAdapter().log(Level.SEVERE, "Failed to load data from the config", e);
                return false;
            } catch (NullPointerException e) {
                getLoggingAdapter().log(Level.SEVERE, "An unsupported language code was supplied, could not load locales", e);
                return false;
            }
        });
    }

    // Default constructor
    public BukkitHuskHomes()
    {
        super();
    }

    // Super constructor for unit testing
    protected BukkitHuskHomes(@NotNull JavaPluginLoader loader, @NotNull PluginDescriptionFile description,
                              @NotNull File dataFolder, @NotNull File file)
    {
        super(loader, description, dataFolder, file);
    }

    public static final class BukkitAdapter {

        public static Optional<Location> adaptLocation(net.william278.huskhomes.position.Location location) {
            World world = Bukkit.getWorld(location.world.name);
            if (world == null) {
                world = Bukkit.getWorld(location.world.uuid);
            }
            if (world == null) {
                return Optional.empty();
            }
            return Optional.of(new Location(world, location.x, location.y, location.z,
                    location.yaw, location.pitch));
        }

        @Nullable
        public static net.william278.huskhomes.position.Location adaptLocation(@NotNull Location location) {
            if (location.getWorld() == null) return null;
            return new net.william278.huskhomes.position.Location(location.getX(), location.getY(), location.getZ(),
                    location.getYaw(), location.getPitch(),
                    new net.william278.huskhomes.position.World(location.getWorld().getName(), location.getWorld().getUID()));
        }

    }

}
