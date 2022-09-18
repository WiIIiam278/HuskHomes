package net.william278.huskhomes;

import com.google.inject.Inject;
import net.william278.annotaml.Annotaml;
import net.william278.desertwell.Version;
import net.william278.huskhomes.command.CommandBase;
import net.william278.huskhomes.command.DisabledCommand;
import net.william278.huskhomes.config.CachedServer;
import net.william278.huskhomes.config.CachedSpawn;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.database.MySqlDatabase;
import net.william278.huskhomes.database.SqLiteDatabase;
import net.william278.huskhomes.event.EventDispatcher;
import net.william278.huskhomes.hook.BlueMapHook;
import net.william278.huskhomes.hook.PlanHook;
import net.william278.huskhomes.hook.PluginHook;
import net.william278.huskhomes.listener.EventListener;
import net.william278.huskhomes.messenger.NetworkMessenger;
import net.william278.huskhomes.migrator.Migrator;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.SpongePlayer;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.SavedPositionManager;
import net.william278.huskhomes.position.Server;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.random.NormalDistributionEngine;
import net.william278.huskhomes.random.RandomTeleportEngine;
import net.william278.huskhomes.request.RequestManager;
import net.william278.huskhomes.teleport.TeleportManager;
import net.william278.huskhomes.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Plugin("huskhomes")
public class SpongeHuskHomes implements HuskHomes {

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path pluginDirectory;
    @Inject
    private PluginContainer pluginContainer;
    @Inject
    private Game game;
    private Settings settings;
    private Locales locales;
    private Logger logger;
    private ResourceReader resourceReader;
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
    @Nullable
    private NetworkMessenger networkMessenger;
    @Nullable
    private Server server;

    // Instance of the plugin
    private static SpongeHuskHomes instance;

    public static SpongeHuskHomes getInstance() {
        return instance;
    }

    @Listener
    public void onConstructPlugin(final ConstructPluginEvent event) {
        instance = this;

        // Initialize HuskHomes
        final AtomicBoolean initialized = new AtomicBoolean(true);
        try {
            // Set the logging and resource reading adapter
            this.logger = new SpongeLogger(pluginContainer.logger());
            //this.resourceReader = new SpongeResourceReader(this);

            // Load settings and locales
            getLoggingAdapter().log(Level.INFO, "Loading plugin configuration settings & locales...");
            initialized.set(reload().join());
            if (initialized.get()) {
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
//                networkMessenger = switch (settings.messengerType) {
//                    case PLUGIN_MESSAGE -> new BukkitPluginMessenger();
//                    case REDIS -> new RedisMessenger();
//                };
                networkMessenger.initialize(this);
                getLoggingAdapter().log(Level.INFO, "Successfully initialized the network messenger.");
            }

            // Prepare the teleport manager
            this.teleportManager = new TeleportManager(this);

            // Prepare the request manager
            this.requestManager = new RequestManager(this);

            // Prepare the event dispatcher
            //this.eventDispatcher = new BukkitEventDispatcher(this);

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
            if (settings.doMapHook) {
                if (settings.mappingPlugin == Settings.MappingPlugin.BLUEMAP) {
                    game.pluginManager().plugin("bluemap").ifPresent(blueMapPlugin -> {
                        pluginHooks.add(new BlueMapHook(this));
                        getLoggingAdapter().log(Level.INFO, "Successfully hooked into BlueMap");
                    });
                }
                getMapHook().ifPresent(mapHook -> savedPositionManager.setMapHook(mapHook));
            }
            game.pluginManager().plugin("bluemap").ifPresent(planPlugin -> {
                pluginHooks.add(new BlueMapHook(this));
                getLoggingAdapter().log(Level.INFO, "Successfully hooked into BlueMap");
            });

            if (pluginHooks.size() > 0) {
                pluginHooks.forEach(PluginHook::initialize);
                getLoggingAdapter().log(Level.INFO, "Registered " + pluginHooks.size() + " plugin hooks: " +
                                                    pluginHooks.stream().map(PluginHook::getHookName)
                                                            .collect(Collectors.joining(", ")));
            }

            // Register events
            getLoggingAdapter().log(Level.INFO, "Registering events...");
            //this.eventListener = new BukkitEventListener(this);
            getLoggingAdapter().log(Level.INFO, "Successfully registered events listener");

            // Register permissions
            getLoggingAdapter().log(Level.INFO, "Registering permissions & commands...");
//            Arrays.stream(Permission.values()).forEach(permission -> getServer().getPluginManager().addPermission(
//                    new org.bukkit.permissions.Permission(permission.node,
//                            switch (permission.defaultAccess) {
//                                case EVERYONE -> PermissionDefault.TRUE;
//                                case NOBODY -> PermissionDefault.FALSE;
//                                case OPERATORS -> PermissionDefault.OP;
//                            })));

            // Register commands
            this.registeredCommands = new ArrayList<>();
//            Arrays.stream(BukkitCommandType.values()).forEach(commandType -> {
//                final PluginCommand pluginCommand = getCommand(commandType.commandBase.command);
//                if (pluginCommand == null) {
//                    return;
//                }
//
//                // If the command is disabled, use the disabled CommandBase
//                if (settings.disabledCommands.stream().anyMatch(disabledCommand -> {
//                    final String command = (disabledCommand.startsWith("/") ? disabledCommand.substring(1) : disabledCommand);
//                    return command.equalsIgnoreCase(commandType.commandBase.command) ||
//                           Arrays.stream(commandType.commandBase.aliases)
//                                   .anyMatch(alias -> alias.equalsIgnoreCase(command));
//                })) {
//                    new BukkitCommand(new DisabledCommand(this), this).register(pluginCommand);
//                    return;
//                }
//
//                // Otherwise, register the command
//                final CommandBase commandBase = commandType.commandBase;
//                this.registeredCommands.add(commandBase);
//                new BukkitCommand(commandBase, this).register(pluginCommand);
//            });
            getLoggingAdapter().log(Level.INFO, "Successfully registered permissions & commands.");

            // Check for updates
            if (settings.checkForUpdates) {
                getLoggingAdapter().log(Level.INFO, "Checking for updates...");
                getLatestVersionIfOutdated().thenAccept(newestVersion ->
                        newestVersion.ifPresent(newVersion -> getLoggingAdapter().log(Level.WARNING,
                                "An update is available for HuskHomes, v" + newVersion
                                + " (Currently running v" + getPluginVersion() + ")")));
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
                getLoggingAdapter().log(Level.SEVERE, "Failed to initialize HuskHomes. The plugin will not be fully functional and you should restart your server.");
            }
        }
    }

    @Override
    public @NotNull Logger getLoggingAdapter() {
        return logger;
    }

    @Override
    public @NotNull List<OnlineUser> getOnlinePlayers() {
        return game.server().onlinePlayers().stream()
                .map(SpongePlayer::adapt)
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull Settings getSettings() {
        return settings;
    }

    @Override
    public @NotNull Locales getLocales() {
        return locales;
    }

    @Override
    public @NotNull Database getDatabase() {
        return database;
    }

    @Override
    public @NotNull Cache getCache() {
        return cache;
    }

    @Override
    public @NotNull TeleportManager getTeleportManager() {
        return teleportManager;
    }

    @Override
    public @NotNull RequestManager getRequestManager() {
        return requestManager;
    }

    @Override
    public @NotNull SavedPositionManager getSavedPositionManager() {
        return savedPositionManager;
    }

    @Override
    public @NotNull NetworkMessenger getNetworkMessenger() throws HuskHomesException {
        if (networkMessenger == null) {
            throw new HuskHomesException("Attempted to access network messenger when it was not initialized");
        }
        return networkMessenger;
    }

    @Override
    public @NotNull RandomTeleportEngine getRandomTeleportEngine() {
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


    // No migrators on the Sponge platform!
    @Override
    public List<Migrator> getMigrators() {
        return Collections.emptyList();
    }

    @Override
    public Optional<CachedSpawn> getLocalCachedSpawn() {
        return Optional.ofNullable(serverSpawn);
    }

    @Override
    public void setServerSpawn(@NotNull Location location) {
        final CachedSpawn newSpawn = new CachedSpawn(location);
        this.serverSpawn = newSpawn;
        Annotaml.save(newSpawn, new File(pluginDirectory.toFile(), "spawn.yml"));

        // Update the world spawn location, too
        SpongeAdapter.adaptLocation(location).ifPresent(spongeLocation ->
                spongeLocation.world().properties().setSpawnPosition(spongeLocation.blockPosition()));
    }

    @Override
    public @NotNull Set<PluginHook> getPluginHooks() {
        return pluginHooks;
    }

    @Override
    public CompletableFuture<Optional<Location>> getSafeGroundLocation(@NotNull Location location) {
        return null;
    }

    @Override
    public @NotNull Server getPluginServer() throws HuskHomesException {
        if (server == null) {
            throw new HuskHomesException("Attempted to access server when it was not initialized");
        }
        return server;
    }

    @Override
    public CompletableFuture<Void> fetchServer(@NotNull OnlineUser requester) {
        if (!getSettings().crossServer) {
            return CompletableFuture.completedFuture(null);
        }
        if (server == null) {
            return getNetworkMessenger().getServerName(requester).thenAcceptAsync(serverName -> {
                // Set the server name
                this.server = new Server(serverName);

                // Cache the server to the server.yml file
                Annotaml.save(new CachedServer(serverName), new File(pluginDirectory.toFile(), "server.yml"));
            });
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @Nullable InputStream getResource(@NotNull String name) {
        return pluginContainer.openResource(URI.create(name))
                .orElse(null);
    }

    @Override
    public @NotNull List<World> getWorlds() {
        return game.server().worldManager().worlds()
                .stream()
                .map(world -> new World(world.key().toString(), world.uniqueId()))
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull Version getPluginVersion() {
        return Version.fromString(pluginContainer.metadata().version().toString(), "-");
    }

    @Override
    public @NotNull List<CommandBase> getCommands() {
        return registeredCommands;
    }

    @Override //todo
    public CompletableFuture<Boolean> reload() {
        return null;
    }

    @Override
    public void registerMetrics(int metricsId) {
        // todo
    }

}
