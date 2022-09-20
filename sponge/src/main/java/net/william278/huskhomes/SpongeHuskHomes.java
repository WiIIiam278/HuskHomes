package net.william278.huskhomes;

import com.google.inject.Inject;
import net.william278.annotaml.Annotaml;
import net.william278.desertwell.Version;
import net.william278.huskhomes.command.CommandBase;
import net.william278.huskhomes.command.SpongeCommand;
import net.william278.huskhomes.command.SpongeCommandType;
import net.william278.huskhomes.config.CachedServer;
import net.william278.huskhomes.config.CachedSpawn;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.database.MySqlDatabase;
import net.william278.huskhomes.database.SqLiteDatabase;
import net.william278.huskhomes.event.EventDispatcher;
import net.william278.huskhomes.event.SpongeEventDispatcher;
import net.william278.huskhomes.hook.BlueMapHook;
import net.william278.huskhomes.hook.PluginHook;
import net.william278.huskhomes.listener.SpongeEventListener;
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
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
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
    private RandomTeleportEngine randomTeleportEngine;
    private CachedSpawn serverSpawn;
    private EventDispatcher eventDispatcher;
    private Set<PluginHook> pluginHooks;
    private List<CommandBase> registeredCommands;
    @Nullable
    private NetworkMessenger networkMessenger; //todo
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
            this.resourceReader = new SpongeResourceReader(pluginContainer, pluginDirectory.toFile());

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
                //networkMessenger.initialize(this);
                getLoggingAdapter().log(Level.INFO, "Successfully initialized the network messenger.");
            }

            // Prepare the teleport manager
            this.teleportManager = new TeleportManager(this);

            // Prepare the request manager
            this.requestManager = new RequestManager(this);

            // Prepare the event dispatcher
            this.eventDispatcher = new SpongeEventDispatcher(this);

            // Initialize the cache
            cache = new Cache(eventDispatcher);
            cache.initialize(database);

            // Prepare the home and warp position manager
            this.savedPositionManager = new SavedPositionManager(database, cache, eventDispatcher,
                    settings.allowUnicodeNames, settings.allowUnicodeDescriptions);

            // Initialize the RTP engine with the default normal distribution engine
            //setRandomTeleportEngine(new NormalDistributionEngine(this));

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
            new SpongeEventListener(this, pluginContainer);
            getLoggingAdapter().log(Level.INFO, "Successfully registered events listener");

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

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Command.Raw> event) {
        registeredCommands = new ArrayList<>();
        Arrays.stream(SpongeCommandType.values()).forEach(commandType -> {
            new SpongeCommand(commandType.commandBase, this).register(event, pluginContainer);
            registeredCommands.add(commandType.commandBase);
        });
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
        return CompletableFuture.supplyAsync(() -> {
            // Load settings and locales
            this.settings = Annotaml.reload(new File(pluginDirectory.toFile(), "config.yml"),
                    new Settings(), Annotaml.LoaderOptions.builder().copyDefaults(true));
            this.locales = Annotaml.reload(new File(pluginDirectory.toFile(), "messages_" + settings.language + ".yml"),
                    Objects.requireNonNull(resourceReader.getResource("locales/" + settings.language + ".yml")),
                    Locales.class, Annotaml.LoaderOptions.builder().copyDefaults(true));

            // Load cached server from file
            if (settings.crossServer) {
                final File serverFile = new File(pluginDirectory.toFile(), "server.yml");
                if (serverFile.exists()) {
                    this.server = Annotaml.load(serverFile, CachedServer.class).getServer();
                }
            } else {
                this.server = new Server("server");
            }

            // Load spawn location from file
            final File spawnFile = new File(pluginDirectory.toFile(), "spawn.yml");
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
        // todo
    }

}
