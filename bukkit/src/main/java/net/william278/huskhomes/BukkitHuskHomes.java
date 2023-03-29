package net.william278.huskhomes;

import io.papermc.lib.PaperLib;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.william278.annotaml.Annotaml;
import net.william278.desertwell.Version;
import net.william278.huskhomes.command.BukkitCommand;
import net.william278.huskhomes.command.Command;
import net.william278.huskhomes.command.DisabledCommand;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.config.Server;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.config.Spawn;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.database.MySqlDatabase;
import net.william278.huskhomes.database.SqLiteDatabase;
import net.william278.huskhomes.event.BukkitEventDispatcher;
import net.william278.huskhomes.hook.*;
import net.william278.huskhomes.listener.BukkitEventListener;
import net.william278.huskhomes.listener.EventListener;
import net.william278.huskhomes.manager.Manager;
import net.william278.huskhomes.network.Broker;
import net.william278.huskhomes.network.PluginMessageBroker;
import net.william278.huskhomes.network.RedisBroker;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.random.NormalDistributionEngine;
import net.william278.huskhomes.random.RandomTeleportEngine;
import net.william278.huskhomes.user.BukkitUser;
import net.william278.huskhomes.user.ConsoleUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.util.*;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BukkitHuskHomes extends JavaPlugin implements HuskHomes, BukkitTaskRunner, BukkitEventDispatcher, PluginMessageListener {

    /**
     * Metrics ID for <a href="https://bstats.org/plugin/bukkit/HuskHomes/8430">HuskHomes on Bukkit</a>.
     */
    private static final int METRICS_ID = 8430;
    private Set<SavedUser> savedUsers;
    private Settings settings;
    private Locales locales;
    private Database database;
    private Validator validator;
    private Manager manager;
    private EventListener eventListener;
    private RandomTeleportEngine randomTeleportEngine;
    private Spawn serverSpawn;
    private UnsafeBlocks unsafeBlocks;
    private List<Hook> hooks;
    private List<Command> commands;
    private Map<String, List<String>> globalPlayerList;
    private Set<UUID> currentlyOnWarmup;
    private Server server;
    @Nullable
    private Broker broker;
    private BukkitAudiences audiences;

    private static BukkitHuskHomes instance;

    public static BukkitHuskHomes getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // Create adventure audience
        this.audiences = BukkitAudiences.create(this);
        this.savedUsers = new HashSet<>();
        this.globalPlayerList = new HashMap<>();
        this.currentlyOnWarmup = new HashSet<>();
        this.validator = new Validator(this);

        // Load settings and locales
        initialize("plugin config & locale files", (plugin) -> {
            if (!loadConfigs()) {
                throw new IllegalStateException("Failed to load config files. Please check the console for errors");
            }
        });

        // Initialize the database
        final Database.Type databaseType = getSettings().getDatabaseType();
        initialize(databaseType.getDisplayName() + " database connection", (plugin) -> {
            this.database = switch (databaseType) {
                case MYSQL -> new MySqlDatabase(this);
                case SQLITE -> new SqLiteDatabase(this);
            };

            database.initialize();
        });

        // Initialize the manager
        this.manager = new Manager(this);

        // Initialize the network messenger if proxy mode is enabled
        if (getSettings().doCrossServer()) {
            initialize("message broker", (plugin) -> {
                broker = switch (settings.getBrokerType()) {
                    case PLUGIN_MESSAGE -> new PluginMessageBroker(this);
                    case REDIS -> new RedisBroker(this);
                };
                broker.initialize();
            });
        }

        setRandomTeleportEngine(new NormalDistributionEngine(this));

        // Register plugin hooks (Economy, Maps, Plan)
        initialize("hooks", (plugin) -> {
            this.prepareHooks();

            if (hooks.size() > 0) {
                hooks.forEach(Hook::initialize);
                log(Level.INFO, "Registered " + hooks.size() + " plugin hooks: " + hooks.stream()
                        .map(Hook::getName)
                        .collect(Collectors.joining(", ")));
            }
        });

        // Register events
        initialize("events", (plugin) -> this.eventListener = new BukkitEventListener(this));

        // Register commands
        initialize("commands", (plugin) -> this.commands = registerCommands());

        // Hook into bStats and check for updates
        initialize("metrics", (plugin) -> this.registerMetrics(METRICS_ID));
        this.checkForUpdates();
    }

    private void initialize(@NotNull String name, @NotNull ThrowingConsumer<HuskHomes> runner) {
        log(Level.INFO, "Initializing " + name + "...");
        try {
            runner.accept(this);
        } catch (Exception e) {
            Bukkit.getPluginManager().disablePlugin(this);
            throw new IllegalStateException("Failed to initialize " + name, e);
        }
        log(Level.INFO, "Successfully initialized " + name);
    }

    @NotNull
    protected List<Command> registerCommands() {
        return Arrays.stream(BukkitCommand.Type.values())
                .map(type -> {
                    final Command command = type.getCommand();
                    if (settings.isCommandDisabled(command)) {
                        new BukkitCommand(new DisabledCommand(command.getName(), this), this).register();
                        return null;
                    } else {
                        new BukkitCommand(command, this).register();
                        return command;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    protected void prepareHooks() {
        this.hooks = new ArrayList<>();
        if (getSettings().doEconomy()) {
            if (Bukkit.getPluginManager().getPlugin("RedisEconomy") != null) {
                hooks.add(new RedisEconomyHook(this));
            } else if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
                hooks.add(new VaultEconomyHook(this));
            }
        }
        if (getSettings().doMapHook()) {
            if (Bukkit.getPluginManager().getPlugin("Dynmap") != null) {
                hooks.add(new DynmapHook(this));
            } else if (Bukkit.getPluginManager().getPlugin("BlueMap") != null) {
                hooks.add(new BlueMapHook(this));
            }
        }
        if (Bukkit.getPluginManager().getPlugin("Plan") != null) {
            hooks.add(new PlanHook(this));
        }
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            hooks.add(new PlaceholderAPIHook(this));
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
        if (broker != null) {
            broker.close();
        }
        if (audiences != null) {
            audiences.close();
            audiences = null;
        }
        cancelAllTasks();
    }

    /**
     * Returns the adventure Bukkit audiences
     *
     * @return The adventure Bukkit audiences
     */
    public BukkitAudiences getAudiences() {
        return audiences;
    }

    @Override
    @NotNull
    public ConsoleUser getConsole() {
        return new ConsoleUser(audiences.console());
    }

    @NotNull
    @Override
    public List<OnlineUser> getOnlineUsers() {
        return Bukkit.getOnlinePlayers().stream().map(player -> (OnlineUser) BukkitUser.adapt(player)).toList();
    }

    @NotNull
    @Override
    public Set<SavedUser> getSavedUsers() {
        return savedUsers;
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
    @NotNull
    public Database getDatabase() {
        return database;
    }

    @Override
    @NotNull
    public Validator getValidator() {
        return validator;
    }

    @NotNull
    @Override
    public Manager getManager() {
        return manager;
    }

    @NotNull
    @Override
    public Broker getMessenger() {
        if (broker == null) {
            throw new IllegalStateException("Attempted to access network messenger when it was not initialized");
        }
        return broker;
    }

    @NotNull
    @Override
    public RandomTeleportEngine getRandomTeleportEngine() {
        return randomTeleportEngine;
    }

    @Override
    public void setRandomTeleportEngine(@NotNull RandomTeleportEngine randomTeleportEngine) {
        this.randomTeleportEngine = randomTeleportEngine;
    }

    @Override
    public Optional<Spawn> getLocalCachedSpawn() {
        return Optional.ofNullable(serverSpawn);
    }

    @Override
    public void setServerSpawn(@NotNull Location location) {
        try {
            this.serverSpawn = Annotaml.create(new File(getDataFolder(), "spawn.yml"), new Spawn(location)).get();

            // Update the world spawn location, too
            BukkitAdapter.adaptLocation(location).ifPresent(bukkitLocation -> {
                assert bukkitLocation.getWorld() != null;
                bukkitLocation.getWorld().setSpawnLocation(bukkitLocation);
            });
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            log(Level.WARNING, "Failed to save the server spawn.yml file", e);
        }
    }

    @Override
    @NotNull
    public List<Hook> getHooks() {
        return hooks;
    }

    @Override
    public CompletableFuture<Optional<Location>> resolveSafeGroundLocation(@NotNull Location location) {
        final org.bukkit.Location bukkitLocation = BukkitAdapter.adaptLocation(location).orElse(null);
        if (bukkitLocation == null || bukkitLocation.getWorld() == null) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        final CompletableFuture<Optional<Location>> locationFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(this, () -> PaperLib.getChunkAtAsync(bukkitLocation).thenApply(chunk -> {
            final ChunkSnapshot snapshot = chunk.getChunkSnapshot();
            final int chunkX = bukkitLocation.getBlockX() & 0xF;
            final int chunkZ = bukkitLocation.getBlockZ() & 0xF;

            for (int dX = -1; dX <= 2; dX++) {
                for (int dZ = -1; dZ <= 2; dZ++) {
                    final int x = chunkX + dX;
                    final int z = chunkZ + dZ;
                    if (x < 0 || x >= 16 || z < 0 || z >= 16) {
                        continue;
                    }
                    final int y = snapshot.getHighestBlockYAt(x, z);
                    final Material blockType = snapshot.getBlockType(chunkX, y, chunkZ);
                    if (!isBlockUnsafe(blockType.getKey().toString())) {
                        return Location.at((location.getX() + dX) + 0.5d, y + 1.25d, (location.getZ() + dZ) + 0.5d, location.getWorld());
                    }
                }
            }
            return null;
        }).thenAccept(resolved -> locationFuture.complete(Optional.ofNullable(resolved))));
        return locationFuture;
    }

    @Override
    @NotNull
    public Version getVersion() {
        return Version.fromString(getDescription().getVersion(), "-");
    }

    @Override
    @NotNull
    public List<Command> getCommands() {
        return commands;
    }

    @Override
    @NotNull
    public Map<String, List<String>> getGlobalPlayerList() {
        return globalPlayerList;
    }

    @Override
    @NotNull
    public Set<UUID> getCurrentlyOnWarmup() {
        return currentlyOnWarmup;
    }

    @Override
    @NotNull
    public String getServerName() {
        return server.getName();
    }

    @Override
    @NotNull
    public List<World> getWorlds() {
        return getServer().getWorlds().stream()
                .filter(world -> BukkitAdapter.adaptWorld(world).isPresent())
                .map(world -> BukkitAdapter.adaptWorld(world).orElse(null))
                .toList();
    }

    @Override
    public boolean loadConfigs() {
        try {
            // Load settings
            this.settings = Annotaml.create(new File(getDataFolder(), "config.yml"), Settings.class).get();

            // Load locales from language preset default
            final Locales languagePresets = Annotaml.create(Locales.class, Objects.requireNonNull(getResource("locales/" + settings.getLanguage() + ".yml"))).get();
            this.locales = Annotaml.create(new File(getDataFolder(), "messages_" + settings.getLanguage() + ".yml"), languagePresets).get();

            // Load server from file
            if (settings.doCrossServer()) {
                this.server = Annotaml.create(new File(getDataFolder(), "server.yml"), Server.class).get();
            } else {
                this.server = new Server(Server.getDefaultServerName());
            }

            // Load spawn location from file
            final File spawnFile = new File(getDataFolder(), "spawn.yml");
            if (spawnFile.exists()) {
                this.serverSpawn = Annotaml.create(spawnFile, Spawn.class).get();
            }

            // Load unsafe blocks from resources
            final InputStream blocksResource = getResource("safety/unsafe_blocks.yml");
            this.unsafeBlocks = Annotaml.create(new UnsafeBlocks(), Objects.requireNonNull(blocksResource)).get();

            return true;
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            log(Level.SEVERE, "Failed to reload HuskHomes config or messages file", e);
        }
        return false;
    }

    @Override
    public boolean isBlockUnsafe(@NotNull String blockId) {
        return unsafeBlocks.isUnsafe(blockId);
    }

    @Override
    public void registerMetrics(int metricsId) {
        try {
            final Metrics metrics = new Metrics(this, metricsId);
            metrics.addCustomChart(new SimplePie("bungee_mode", () -> Boolean.toString(getSettings().doCrossServer())));
            if (getSettings().doCrossServer()) {
                metrics.addCustomChart(new SimplePie("messenger_type", () -> getSettings().getBrokerType().getDisplayName()));
            }
            metrics.addCustomChart(new SimplePie("language", () -> getSettings().getLanguage().toLowerCase()));
            metrics.addCustomChart(new SimplePie("database_type", () -> getSettings().getDatabaseType().getDisplayName()));
            metrics.addCustomChart(new SimplePie("using_economy", () -> Boolean.toString(getSettings().doEconomy())));
            metrics.addCustomChart(new SimplePie("using_map", () -> Boolean.toString(getSettings().doMapHook())));
            getMapHook().ifPresent(hook -> metrics.addCustomChart(new SimplePie("map_type", hook::getName)));
        } catch (Exception e) {
            log(Level.WARNING, "Failed to register metrics", e);
        }
    }

    @Override
    public void initializePluginChannels() {
        Bukkit.getMessenger().registerIncomingPluginChannel(this, PluginMessageBroker.BUNGEE_CHANNEL_ID, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, PluginMessageBroker.BUNGEE_CHANNEL_ID);
    }

    @Override
    public void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... exceptions) {
        if (exceptions.length > 0) {
            getLogger().log(level, message, exceptions[0]);
            return;
        }
        getLogger().log(level, message);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (broker != null && broker instanceof PluginMessageBroker pluginMessenger
            && getSettings().getBrokerType() == Broker.Type.PLUGIN_MESSAGE) {
            pluginMessenger.onReceive(channel, BukkitUser.adapt(player), message);
        }
    }

    @Override
    @NotNull
    public HuskHomes getPlugin() {
        return this;
    }

    // Default constructor
    @SuppressWarnings("unused")
    public BukkitHuskHomes() {
        super();
    }

    // Super constructor for unit testing
    @SuppressWarnings("unused")
    protected BukkitHuskHomes(@NotNull JavaPluginLoader loader, @NotNull PluginDescriptionFile description, @NotNull File dataFolder, @NotNull File file) {
        super(loader, description, dataFolder, file);
    }

}
