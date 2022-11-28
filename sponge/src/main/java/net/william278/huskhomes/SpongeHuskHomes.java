package net.william278.huskhomes;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
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
import net.william278.huskhomes.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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
    private Database database;
    private Cache cache;
    private RequestManager requestManager;
    private SavedPositionManager savedPositionManager;
    private RandomTeleportEngine randomTeleportEngine;
    private CachedSpawn serverSpawn;
    private UnsafeBlocks unsafeBlocks;
    private EventDispatcher eventDispatcher;
    private Set<PluginHook> pluginHooks;
    private List<CommandBase> registeredCommands;
    private Map<String, PermissionDescription> registeredPermissions;
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
            // Prepare the logging adapter
            this.logger = new SpongeLogger(pluginContainer.logger());

            // Load settings and locales
            getLoggingAdapter().log(Level.INFO, "Loading plugin configuration settings & locales...");
            initialized.set(reload());
            if (initialized.get()) {
                getLoggingAdapter().log(Level.INFO, "Successfully loaded plugin configuration settings & locales");
            } else {
                throw new HuskHomesInitializationException("Failed to load plugin configuration settings and/or locales");
            }

            // Initialize the database
            getLoggingAdapter().log(Level.INFO, "Attempting to establish connection to the database...");
            final Settings.DatabaseType databaseType = settings.databaseType;
            this.database = switch (databaseType == null ? Settings.DatabaseType.MYSQL : databaseType) {
                case MYSQL -> new MySqlDatabase(this);
                case SQLITE -> new SqLiteDatabase(this);
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
                //todo
                /*networkMessenger = switch (settings.messengerType) {
                    case PLUGIN_MESSAGE -> new BukkitPluginMessenger();
                    case REDIS -> new RedisMessenger();
                };
                networkMessenger.initialize(this);*/
                getLoggingAdapter().log(Level.INFO, "Successfully initialized the network messenger.");
            }

            // Prepare the request manager
            this.requestManager = new RequestManager(this);

            // Prepare the event dispatcher
            this.eventDispatcher = new SpongeEventDispatcher(this);

            // Initialize the cache
            cache = new Cache(eventDispatcher);
            cache.initialize(database);

            // Prepare the home and warp position manager
            this.savedPositionManager = new SavedPositionManager(database, cache, eventDispatcher,
                    settings.allowUnicodeNames, settings.allowUnicodeDescriptions,
                    settings.overwriteExistingHomesWarps);

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

            // Register permission nodes
            registeredPermissions = new HashMap<>();
            Sponge.serviceProvider().provide(PermissionService.class).ifPresent(service ->
                    Arrays.stream(Permission.values()).forEach(permission -> {
                        final PermissionDescription.Builder builder = service
                                .newDescriptionBuilder(getPluginContainer())
                                .description(Component.text("https://william278.net/docs/huskhomes/commands/")
                                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL,
                                                "https://william278.net/docs/huskhomes/commands/")))
                                .id(permission.node)
                                .defaultValue(switch (permission.defaultAccess) {
                                    case EVERYONE -> Tristate.TRUE;
                                    case OPERATORS -> Tristate.UNDEFINED;
                                    case NOBODY -> Tristate.FALSE;
                                });
                        if (permission.defaultAccess == Permission.DefaultAccess.OPERATORS) {
                            builder.assign(PermissionDescription.ROLE_ADMIN, true);
                        }
                        registeredPermissions.put(permission.node, builder.register());
                    }));

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

    @NotNull
    public PluginContainer getPluginContainer() {
        return pluginContainer;
    }

    @Override
    @NotNull
    public Logger getLoggingAdapter() {
        return logger;
    }

    @Override
    @NotNull
    public List<OnlineUser> getOnlinePlayers() {
        return game.server().onlinePlayers().stream()
                .map(SpongePlayer::adapt)
                .collect(Collectors.toList());
    }

    @Override
    @NotNull
    public Settings getSettings() {
        return settings;
    }

    @Override
    @NotNull
    public Locales getLocales() {
        return locales;
    }

    @Override
    @NotNull
    public Database getDatabase() {
        return database;
    }

    @Override
    @NotNull
    public Cache getCache() {
        return cache;
    }

    @Override
    @NotNull
    public RequestManager getRequestManager() {
        return requestManager;
    }

    @Override
    @NotNull
    public SavedPositionManager getSavedPositionManager() {
        return savedPositionManager;
    }

    @Override
    @NotNull
    public NetworkMessenger getNetworkMessenger() throws HuskHomesException {
        if (networkMessenger == null) {
            throw new HuskHomesException("Attempted to access network messenger when it was not initialized");
        }
        return networkMessenger;
    }

    @Override
    @NotNull
    public RandomTeleportEngine getRandomTeleportEngine() {
        return randomTeleportEngine;
    }

    @Override
    public void setRandomTeleportEngine(@NotNull RandomTeleportEngine randomTeleportEngine) {
        this.randomTeleportEngine = randomTeleportEngine;
    }

    @Override
    @NotNull
    public EventDispatcher getEventDispatcher() {
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
        try {
            Annotaml.create(new File(getDataFolder(), "spawn.yml"), newSpawn);
        } catch (IOException e) {
            getLoggingAdapter().log(Level.WARNING, "Failed to save server spawn to disk", e);
        }

        // Update the world spawn location, too
        SpongeAdapter.adaptLocation(location).ifPresent(spongeLocation ->
                spongeLocation.world().properties().setSpawnPosition(spongeLocation.blockPosition()));
    }

    @Override
    @NotNull
    public Set<PluginHook> getPluginHooks() {
        return pluginHooks;
    }

    @Override
    public CompletableFuture<Optional<Location>> resolveSafeGroundLocation(@NotNull Location location) {
        return null;
    }

    @Override
    @NotNull
    public Server getPluginServer() throws HuskHomesException {
        if (server == null) {
            throw new HuskHomesException("Attempted to access server when it was not initialized");
        }
        return server;
    }

    @Override
    public CompletableFuture<Void> fetchServer(@NotNull OnlineUser requester) {
        if (!getSettings().crossServer || this.server != null) {
            return CompletableFuture.completedFuture(null);
        }
        return getNetworkMessenger().fetchServerName(requester).orTimeout(5, TimeUnit.SECONDS).exceptionally(throwable -> null).thenAccept(serverName -> {
            if (serverName == null) {
                throw new HuskHomesException("GetServer plugin message call operation timed out");
            }
            try {
                this.server = new Server(serverName);
                Annotaml.create(new File(getDataFolder(), "server.yml"), new CachedServer(serverName));
                getLoggingAdapter().log(Level.INFO, "Successfully cached server name to disk (" + serverName + ")");
            } catch (IOException e) {
                throw new HuskHomesException("Failed to write cached server name to disk", e);
            }
        }).exceptionally(throwable -> {
            getLoggingAdapter().log(Level.SEVERE, "Failed to fetch and cache server name", throwable);
            return null;
        });
    }

    @Override
    @Nullable
    public InputStream getResource(@NotNull String name) {
        return pluginContainer.openResource(URI.create(name))
                .orElse(null);
    }

    @Override
    @NotNull
    public File getDataFolder() {
        return pluginDirectory.toFile();
    }

    @Override
    @NotNull
    public List<World> getWorlds() {
        return game.server().worldManager().worlds()
                .stream()
                .map(world -> new World(world.key().toString(), world.uniqueId()))
                .collect(Collectors.toList());
    }

    @Override
    @NotNull
    public Version getPluginVersion() {
        return Version.fromString(pluginContainer.metadata().version().toString(), "-");
    }

    @Override
    @NotNull
    public List<CommandBase> getCommands() {
        return registeredCommands;
    }

    @Override
    public boolean reload() {
        try {
            // Load settings
            this.settings = Annotaml.create(new File(getDataFolder(), "config.yml"), new Settings()).get();

            // Load locales from language preset default
            final Locales languagePresets = Annotaml.create(Locales.class, Objects.requireNonNull(getResource("locales/" + settings.language + ".yml"))).get();
            this.locales = Annotaml.create(new File(getDataFolder(), "messages_" + settings.language + ".yml"), languagePresets).get();

            // Load cached server from file
            if (settings.crossServer) {
                final File serverFile = new File(getDataFolder(), "server.yml");
                if (serverFile.exists()) {
                    this.server = Annotaml.create(serverFile, CachedServer.class).get().getServer();
                }
            } else {
                this.server = new Server("server");
            }

            // Load spawn location from file
            final File spawnFile = new File(getDataFolder(), "spawn.yml");
            if (spawnFile.exists()) {
                this.serverSpawn = Annotaml.create(spawnFile, CachedSpawn.class).get();
            }

            // Load unsafe blocks from resources
            final InputStream blocksResource = getResource("safety/unsafe_blocks.yml");
            this.unsafeBlocks = Annotaml.create(new UnsafeBlocks(), Objects.requireNonNull(blocksResource)).get();

            return true;
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            getLoggingAdapter().log(Level.SEVERE, "Failed to reload HuskHomes config or messages file", e);
        }
        return false;
    }

    @Override
    public boolean isBlockUnsafe(@NotNull String blockId) {
        return unsafeBlocks.isUnsafe(blockId);
    }

    @Override
    public void registerMetrics(int metricsId) {
        // todo
    }

}
