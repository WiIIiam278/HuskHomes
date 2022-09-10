package net.william278.huskhomes;

import io.papermc.lib.PaperLib;
import net.william278.annotaml.Annotaml;
import net.william278.desertwell.Version;
import net.william278.huskhomes.command.BukkitCommand;
import net.william278.huskhomes.command.BukkitCommandType;
import net.william278.huskhomes.command.CommandBase;
import net.william278.huskhomes.command.DisabledCommand;
import net.william278.huskhomes.config.CachedServer;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.config.CachedSpawn;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.database.MySqlDatabase;
import net.william278.huskhomes.database.SqLiteDatabase;
import net.william278.huskhomes.event.BukkitEventDispatcher;
import net.william278.huskhomes.event.EventDispatcher;
import net.william278.huskhomes.hook.*;
import net.william278.huskhomes.listener.BukkitEventListener;
import net.william278.huskhomes.listener.EventListener;
import net.william278.huskhomes.messenger.NetworkMessenger;
import net.william278.huskhomes.messenger.PluginMessenger;
import net.william278.huskhomes.messenger.RedisMessenger;
import net.william278.huskhomes.migrator.LegacyMigrator;
import net.william278.huskhomes.migrator.Migrator;
import net.william278.huskhomes.player.BukkitPlayer;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.SavedPositionManager;
import net.william278.huskhomes.position.Server;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.random.NormalDistributionEngine;
import net.william278.huskhomes.random.RandomTeleportEngine;
import net.william278.huskhomes.request.RequestManager;
import net.william278.huskhomes.teleport.TeleportManager;
import net.william278.huskhomes.util.*;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Collectors;

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
    private RandomTeleportEngine randomTeleportEngine;
    private CachedSpawn serverSpawn;
    private EventDispatcher eventDispatcher;
    private Set<PluginHook> pluginHooks;
    private List<CommandBase> registeredCommands;

    private List<Migrator> migrators;

    @Nullable
    private NetworkMessenger networkMessenger;

    @Nullable
    private Server server;

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

            // Detect if an upgrade is needed
            final BukkitUpgradeUtil upgradeData = BukkitUpgradeUtil.detect(this);

            // Load settings and locales
            getLoggingAdapter().log(Level.INFO, "Loading plugin configuration settings & locales...");
            initialized.set(reload().join());
            if (initialized.get()) {
                logger.showDebugLogs(settings.debugLogging);
                if (upgradeData != null) {
                    upgradeData.upgradeSettings(settings);
                }
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

            // Prepare the teleport manager
            this.teleportManager = new TeleportManager(this);

            // Prepare the request manager
            this.requestManager = new RequestManager(this);

            // Prepare the event dispatcher
            this.eventDispatcher = new BukkitEventDispatcher(this);

            // Initialize the cache
            cache = new Cache(eventDispatcher);
            cache.initialize(database);

            // Prepare the home and warp position manager
            this.savedPositionManager = new SavedPositionManager(database, cache, eventDispatcher,
                    settings.allowUnicodeNames, settings.allowUnicodeDescriptions);

            // Initialize the RTP engine with the default normal distribution engine
            setRandomTeleportEngine(new NormalDistributionEngine(this));

            // Register plugin hooks (Economy, Maps, Plan)
            this.pluginHooks = new HashSet<>();
            if (settings.economy && Bukkit.getPluginManager().getPlugin("Vault") != null) {
                pluginHooks.add(new VaultEconomyHook(this));
            }
            if (settings.doMapHook) {
                switch (settings.mappingPlugin) {
                    case DYNMAP -> {
                        final Plugin dynmapPlugin = Bukkit.getPluginManager().getPlugin("Dynmap");
                        if (dynmapPlugin != null) {
                            pluginHooks.add(new DynMapHook(this, dynmapPlugin));
                        }
                    }
                    case BLUEMAP -> {
                        if (Bukkit.getPluginManager().getPlugin("BlueMap") != null) {
                            pluginHooks.add(new BlueMapHook(this));
                        }
                    }
                }
                getMapHook().ifPresent(mapHook -> savedPositionManager.setMapHook(mapHook));
            }
            if (Bukkit.getPluginManager().getPlugin("Plan") != null) {
                pluginHooks.add(new PlanHook(this));
            }

            if (pluginHooks.size() > 0) {
                pluginHooks.forEach(PluginHook::initialize);
                getLoggingAdapter().log(Level.INFO, "Registered " + pluginHooks.size() + " plugin hooks: " +
                        pluginHooks.stream().map(PluginHook::getHookName)
                                .collect(Collectors.joining(", ")));
            }

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
            this.registeredCommands = new ArrayList<>();
            Arrays.stream(BukkitCommandType.values()).forEach(commandType -> {
                final PluginCommand pluginCommand = getCommand(commandType.commandBase.command);
                if (pluginCommand == null) {
                    return;
                }

                // If the command is disabled, use the disabled CommandBase
                if (settings.disabledCommands.stream().anyMatch(disabledCommand -> {
                    final String command = (disabledCommand.startsWith("/") ? disabledCommand.substring(1) : disabledCommand);
                    return command.equalsIgnoreCase(commandType.commandBase.command) ||
                            Arrays.stream(commandType.commandBase.aliases)
                                    .anyMatch(alias -> alias.equalsIgnoreCase(command));
                })) {
                    new BukkitCommand(new DisabledCommand(this), this).register(pluginCommand);
                    return;
                }

                // Otherwise, register the command
                final CommandBase commandBase = commandType.commandBase;
                this.registeredCommands.add(commandBase);
                new BukkitCommand(commandBase, this).register(pluginCommand);
            });
            getLoggingAdapter().log(Level.INFO, "Successfully registered permissions & commands.");

            // Prepare migrators
            this.migrators = new ArrayList<>();
            this.migrators.add(new LegacyMigrator(this));
            //todo EssentialsX

            // Hook into bStats metrics
            registerMetrics(METRICS_ID);

            // Check for updates
            if (settings.checkForUpdates) {
                getLoggingAdapter().log(Level.INFO, "Checking for updates...");
                getLatestVersionIfOutdated().thenAccept(newestVersion ->
                        newestVersion.ifPresent(newVersion -> getLoggingAdapter().log(Level.WARNING,
                                "An update is available for HuskHomes, v" + newVersion
                                        + " (Currently running v" + getPluginVersion() + ")")));
            }

            // Perform automatic upgrade if detected
            if (upgradeData != null) {
                getLoggingAdapter().log(Level.INFO, "Performing automatic upgrade...");
                new LegacyMigrator(this, upgradeData).start().thenAccept(result -> {
                    if (result) {
                        getLoggingAdapter().log(Level.INFO, "Successfully performed automatic upgrade.");
                    } else {
                        getLoggingAdapter().log(Level.WARNING, "Failed to perform automatic upgrade.");
                    }
                });
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

    @NotNull
    @Override
    public NetworkMessenger getNetworkMessenger() {
        if (networkMessenger == null) {
            throw new HuskHomesException("Attempted to access network messenger when it was not initialized");
        }
        return networkMessenger;
    }

    @NotNull
    @Override
    public RandomTeleportEngine getRandomTeleportEngine() {
        return randomTeleportEngine;
    }

    @Override
    public void setRandomTeleportEngine(@NotNull RandomTeleportEngine randomTeleportEngine) {
        this.randomTeleportEngine = randomTeleportEngine;
        this.randomTeleportEngine.initialize();
    }

    @Override
    public @NotNull EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    @Override
    public List<Migrator> getMigrators() {
        return migrators;
    }

    @Override
    public Optional<CachedSpawn> getLocalCachedSpawn() {
        return Optional.ofNullable(serverSpawn);
    }

    @Override
    public void setServerSpawn(@NotNull Location location) {
        final CachedSpawn newSpawn = new CachedSpawn(location);
        this.serverSpawn = newSpawn;
        Annotaml.save(newSpawn, new File(getDataFolder(), "spawn.yml"));

        // Update the world spawn location, too
        BukkitAdapter.adaptLocation(location).ifPresent(bukkitLocation -> {
            assert bukkitLocation.getWorld() != null;
            bukkitLocation.getWorld().setSpawnLocation(bukkitLocation);
        });
    }

    @Override
    public @NotNull Set<PluginHook> getPluginHooks() {
        return pluginHooks;
    }

    @Override
    public CompletableFuture<Optional<Location>> getSafeGroundLocation(@NotNull Location location) {
        final CompletableFuture<Optional<Location>> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(getInstance(), () -> BukkitAdapter.adaptLocation(location).ifPresentOrElse(
                bukkitLocation -> {
                    // Validate the location is within the world's border
                    assert bukkitLocation.getWorld() != null;
                    if (!bukkitLocation.getWorld().getWorldBorder().isInside(bukkitLocation)) {
                        future.complete(Optional.empty());
                        return;
                    }

                    // Find a safe position and return
                    PaperLib.getChunkAtAsync(bukkitLocation).thenAcceptAsync(chunk ->
                            future.complete(BukkitSafetyUtil.findSafeLocation(location.world, bukkitLocation,
                                    chunk.getChunkSnapshot()))).exceptionally(throwable -> {
                        throwable.printStackTrace();
                        return null;
                    });
                },
                () -> future.complete(Optional.empty())));
        return future;
    }

    @Override
    public @NotNull Version getPluginVersion() {
        return Version.fromString(getDescription().getVersion(), "-");
    }

    @Override
    public @NotNull List<CommandBase> getCommands() {
        return registeredCommands;
    }

    @Override
    @NotNull
    public Server getPluginServer() throws HuskHomesException {
        if (server == null) {
            throw new HuskHomesException("Attempted to access server when it was not initialized");
        }
        return server;
    }

    public CompletableFuture<Void> fetchServer(@NotNull OnlineUser requester) {
        if (!getSettings().crossServer) {
            return CompletableFuture.completedFuture(null);
        }
        if (server == null) {
            return getNetworkMessenger().getServerName(requester).thenAccept(serverName -> {
                // Set the server name
                this.server = new Server(serverName);

                // Cache the server to the server.yml file
                Annotaml.save(new CachedServer(serverName), new File(getDataFolder(), "server.yml"));
            });
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull List<World> getWorlds() {
        return getServer().getWorlds().stream().filter(world -> BukkitAdapter.adaptWorld(world).isPresent())
                .map(world -> BukkitAdapter.adaptWorld(world).orElse(null)).collect(Collectors.toList());
    }

    @Override
    public CompletableFuture<Boolean> reload() {
        return CompletableFuture.supplyAsync(() -> {
            // Load settings and locales
            this.settings = Annotaml.reload(new File(getDataFolder(), "config.yml"),
                    new Settings(), Annotaml.LoaderOptions.builder().copyDefaults(true));
            this.locales = Annotaml.reload(new File(getDataFolder(), "messages_" + settings.language + ".yml"),
                    Objects.requireNonNull(resourceReader.getResource("locales/" + settings.language + ".yml")),
                    Locales.class, Annotaml.LoaderOptions.builder().copyDefaults(true));

            // Load cached server from file
            if (settings.crossServer) {
                final File serverFile = new File(getDataFolder(), "server.yml");
                if (serverFile.exists()) {
                    this.server = Annotaml.load(serverFile, CachedServer.class).getServer();
                }
            } else {
                this.server = new Server("server");
            }

            // Load spawn location from file
            final File spawnFile = new File(getDataFolder(), "spawn.yml");
            if (spawnFile.exists()) {
                this.serverSpawn = Annotaml.load(spawnFile, CachedSpawn.class);
            }
            return true;
        }).exceptionally(throwable -> {
            getLoggingAdapter().log(Level.SEVERE, "Failed to load data from the config", throwable);
            return false;
        });
    }

    @Override
    public void registerMetrics(int metricsId) {
        try {
            final Metrics metrics = new Metrics(this, metricsId);
            metrics.addCustomChart(new SimplePie("bungee_mode", () -> Boolean.toString(getSettings().crossServer)));
            if (getSettings().crossServer) {
                metrics.addCustomChart(new SimplePie("messenger_type", () -> getSettings().messengerType.displayName));
            }
            metrics.addCustomChart(new SimplePie("language", () -> getSettings().language.toLowerCase()));
            metrics.addCustomChart(new SimplePie("database_type", () -> getSettings().databaseType.displayName));
            metrics.addCustomChart(new SimplePie("using_economy", () -> Boolean.toString(getSettings().economy)));
            metrics.addCustomChart(new SimplePie("using_map", () -> Boolean.toString(getSettings().doMapHook)));
            if (getSettings().doMapHook) {
                metrics.addCustomChart(new SimplePie("map_type", () -> getSettings().mappingPlugin.displayName));
            }
        } catch (Exception e) {
            getLoggingAdapter().log(Level.WARNING, "Failed to register metrics", e);
        }
    }

    // Default constructor
    @SuppressWarnings("unused")
    public BukkitHuskHomes() {
        super();
    }

    // Super constructor for unit testing
    @SuppressWarnings("unused")
    protected BukkitHuskHomes(@NotNull JavaPluginLoader loader, @NotNull PluginDescriptionFile description,
                              @NotNull File dataFolder, @NotNull File file) {
        super(loader, description, dataFolder, file);
    }

}
