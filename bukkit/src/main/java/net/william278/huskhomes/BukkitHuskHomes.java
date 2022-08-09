package net.william278.huskhomes;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.william278.huskhomes.cache.Cache;
import net.william278.huskhomes.command.*;
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
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.BukkitPlayer;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.Server;
import net.william278.huskhomes.position.SavedPositionManager;
import net.william278.huskhomes.teleport.BukkitTeleportManager;
import net.william278.huskhomes.teleport.TeleportManager;
import net.william278.huskhomes.util.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
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
    private SavedPositionManager savedPositionManager;
    private EventListener eventListener;

    @Nullable
    private NetworkMessenger networkMessenger;

    @Nullable
    private Server server;

    private static BukkitHuskHomes instance;

    public static BukkitHuskHomes getInstance() {
        return instance;
    }

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
        if (!getSettings().getBooleanValue(Settings.ConfigOption.ENABLE_PROXY_MODE)) {
            server = new Server("server");
            return CompletableFuture.supplyAsync(() -> server);
        }
        assert networkMessenger != null;
        return networkMessenger.getServerName(onlineUser).thenApplyAsync(server -> {
            this.server = new Server(server);
            return this.server;
        });
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
                logger.showDebugLogs(settings.getBooleanValue(Settings.ConfigOption.DEBUG_LOGGING));
                getLoggingAdapter().log(Level.INFO, "Successfully loaded plugin configuration settings & locales");
            } else {
                throw new HuskHomesInitializationException("Failed to load plugin configuration settings and/or locales");
            }

            // Initialize the database
            getLoggingAdapter().log(Level.INFO, "Attempting to establish connection to the database...");
            final String databaseType = settings.getStringValue(Settings.ConfigOption.DATA_STORAGE_TYPE);
            this.database = switch (databaseType == null ? "MYSQL" : databaseType.toUpperCase()) {
                case "MYSQL" -> new MySqlDatabase(settings, logger, resourceReader);
                case "SQLITE" -> new SqLiteDatabase(settings, logger, resourceReader);
                default -> null;
            };
            if (database == null) {
                throw new HuskHomesInitializationException("An invalid database type was specified. " +
                                                           "Please check the database type in the configuration file.");
            }
            initialized.set(this.database.initialize());
            if (initialized.get()) {
                getLoggingAdapter().log(Level.INFO, "Successfully established a connection to the database");
            } else {
                throw new HuskHomesInitializationException("Failed to establish a connection to the database. " +
                                                           "Please check the supplied database credentials in the config file");
            }

            // Initialize the network messenger if proxy mode is enabled
            if (getSettings().getBooleanValue(Settings.ConfigOption.ENABLE_PROXY_MODE)) {
                getLoggingAdapter().log(Level.INFO, "Initializing the network messenger...");
                networkMessenger = switch (settings.getStringValue(Settings.ConfigOption.MESSENGER_TYPE).toUpperCase()) {
                    case "PLUGIN_MESSAGE", "PLUGINMESSAGE" -> new PluginMessenger();
                    case "REDIS", "JEDIS" -> new RedisMessenger();
                    default -> null;
                };
                if (networkMessenger == null) {
                    throw new HuskHomesInitializationException("An invalid network messenger type was specified. " +
                                                               "Please check the network messenger type in the configuration file.");
                }
                networkMessenger.initialize(this);
                getLoggingAdapter().log(Level.INFO, "Successfully initialized the network messenger.");
            }

            // Initialize the cache
            cache = new Cache();
            cache.initialize(database);

            // Prepare the teleport manager
            this.teleportManager = new BukkitTeleportManager(this);

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
                getLoggingAdapter().log(Level.WARNING, "Skipped bStats metrics initialization due to an exception.");
            }

            // Check for updates
            if (settings.getBooleanValue(Settings.ConfigOption.CHECK_FOR_UPDATES)) {
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
                this.settings = Settings.load(YamlDocument.create(new File(getDataFolder(), "config.yml"), Objects.requireNonNull(resourceReader.getResource("config.yml")), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.builder().setEncoding(DumperSettings.Encoding.UNICODE).build(), UpdaterSettings.builder().setVersioning(new BasicVersioning("config_version")).build()));

                this.locales = Locales.load(YamlDocument.create(new File(getDataFolder(), "messages-" + settings.getStringValue(Settings.ConfigOption.LANGUAGE) + ".yml"), Objects.requireNonNull(resourceReader.getResource("locales/" + settings.getStringValue(Settings.ConfigOption.LANGUAGE) + ".yml"))));
                return true;
            } catch (IOException | NullPointerException e) {
                getLoggingAdapter().log(Level.SEVERE, "Failed to load data from the config", e);
                return false;
            }
        });
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
