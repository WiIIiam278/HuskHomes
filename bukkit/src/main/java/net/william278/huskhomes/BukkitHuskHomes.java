/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.huskhomes;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.william278.annotaml.Annotaml;
import net.william278.desertwell.util.Version;
import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.command.BukkitCommand;
import net.william278.huskhomes.command.Command;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.config.Server;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.config.Spawn;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.database.H2Database;
import net.william278.huskhomes.database.MySqlDatabase;
import net.william278.huskhomes.database.SqLiteDatabase;
import net.william278.huskhomes.event.BukkitEventDispatcher;
import net.william278.huskhomes.hook.Hook;
import net.william278.huskhomes.hook.PlaceholderAPIHook;
import net.william278.huskhomes.hook.VaultEconomyHook;
import net.william278.huskhomes.importer.EssentialsXImporter;
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
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import space.arim.morepaperlib.MorePaperLib;
import space.arim.morepaperlib.commands.CommandRegistration;
import space.arim.morepaperlib.scheduling.GracefulScheduling;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BukkitHuskHomes extends JavaPlugin implements HuskHomes, BukkitTask.Supplier, BukkitEventDispatcher,
        PluginMessageListener, BukkitSafetyResolver {

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
    private BukkitAudiences audiences;
    private MorePaperLib paperLib;
    @Nullable
    private Broker broker;

    // Default public constructor
    public BukkitHuskHomes() {
        super();
    }

    // Super constructor for unit testing
    @TestOnly
    protected BukkitHuskHomes(@NotNull JavaPluginLoader loader, @NotNull PluginDescriptionFile description,
                              @NotNull File dataFolder, @NotNull File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onEnable() {
        // Create adventure audience
        this.audiences = BukkitAudiences.create(this);
        this.paperLib = new MorePaperLib(this);
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
        initialize(getSettings().getDatabaseType().getDisplayName() + " database connection", (plugin) -> {
            this.database = switch (getSettings().getDatabaseType()) {
                case MYSQL, MARIADB -> new MySqlDatabase(this);
                case SQLITE -> new SqLiteDatabase(this);
                case H2 -> new H2Database(this);
            };

            database.initialize();
        });

        // Initialize the manager
        this.manager = new Manager(this);

        // Initialize the network messenger if proxy mode is enabled
        if (getSettings().doCrossServer()) {
            initialize(settings.getBrokerType().getDisplayName() + " message broker", (plugin) -> {
                broker = switch (settings.getBrokerType()) {
                    case PLUGIN_MESSAGE -> new PluginMessageBroker(this);
                    case REDIS -> new RedisBroker(this);
                };
                broker.initialize();
            });
        }

        // Set the random teleport engine
        setRandomTeleportEngine(new NormalDistributionEngine(this));

        // Register plugin hooks (Economy, Maps, Plan)
        initialize("hooks", (plugin) -> {
            this.registerHooks();

            if (!hooks.isEmpty()) {
                hooks.forEach(hook -> {
                    try {
                        hook.initialize();
                    } catch (Throwable e) {
                        log(Level.WARNING, "Failed to initialize " + hook.getName() + " hook", e);
                    }
                });
                log(Level.INFO, "Registered " + hooks.size() + " plugin hooks: " + hooks.stream()
                        .map(Hook::getName)
                        .collect(Collectors.joining(", ")));
            }
        });

        // Register event listener
        initialize("events", (plugin) -> this.eventListener = getListener().register(this));

        // Register commands
        initialize("commands", (plugin) -> this.commands = BukkitCommand.Type.getCommands(this));

        // Initialize the API
        initialize("API", (plugin) -> HuskHomesAPI.register(this));

        // Hook into bStats and check for updates
        initialize("metrics", (plugin) -> this.registerMetrics(METRICS_ID));
        this.checkForUpdates();
    }

    // Register the event listener
    @NotNull
    protected BukkitEventListener getListener() {
        return new BukkitEventListener(this);
    }

    @Override
    public void registerHooks() {
        HuskHomes.super.registerHooks();

        // Hooks
        if (getSettings().doEconomy() && isDependencyLoaded("Vault")) {
            getHooks().add(new VaultEconomyHook(this));
        }
        if (isDependencyLoaded("PlaceholderAPI")) {
            getHooks().add(new PlaceholderAPIHook(this));
        }
    }

    @Override
    public void registerImporters() {
        HuskHomes.super.registerImporters();

        // Importers
        if (isDependencyLoaded("Essentials")) {
            getHooks().add(new EssentialsXImporter(this));
        }
    }

    @Override
    public boolean isDependencyLoaded(@NotNull String name) {
        return Bukkit.getPluginManager().getPlugin(name) != null;
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
        cancelTasks();
    }

    @Override
    @NotNull
    public ConsoleUser getConsole() {
        return new ConsoleUser(audiences.console());
    }

    @NotNull
    @Override
    public List<OnlineUser> getOnlineUsers() {
        return Bukkit.getOnlinePlayers().stream()
                .map(player -> (OnlineUser) BukkitUser.adapt(player, this))
                .toList();
    }

    @NotNull
    @Override
    public Audience getAudience(@NotNull UUID user) {
        return audiences.player(user);
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

    @Override
    public void setSettings(@NotNull Settings settings) {
        this.settings = settings;
    }

    @NotNull
    @Override
    public Locales getLocales() {
        return locales;
    }

    @Override
    public void setLocales(@NotNull Locales locales) {
        this.locales = locales;
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
            throw new IllegalStateException("Attempted to access message broker when it was not initialized");
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
    public Optional<Spawn> getServerSpawn() {
        return Optional.ofNullable(serverSpawn);
    }

    @Override
    public void setServerSpawn(@NotNull Spawn spawn) {
        this.serverSpawn = spawn;
    }

    @Override
    public void setServerSpawn(@NotNull Location location) {
        try {
            // Create or update the spawn.yml file
            final File spawnFile = new File(getDataFolder(), "spawn.yml");
            if (spawnFile.exists() && !spawnFile.delete()) {
                log(Level.WARNING, "Failed to delete the existing spawn.yml file");
            }
            this.serverSpawn = Annotaml.create(spawnFile, new Spawn(location)).get();

            // Update the world spawn location, too
            BukkitAdapter.adaptLocation(location).ifPresent(bukkitLocation -> {
                assert bukkitLocation.getWorld() != null : "World was null when setting server spawn";
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
    public void setHooks(@NotNull List<Hook> hooks) {
        this.hooks = hooks;
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
    public void setServer(@NotNull Server server) {
        this.server = server;
    }

    @Override
    public void setUnsafeBlocks(@NotNull UnsafeBlocks unsafeBlocks) {
        this.unsafeBlocks = unsafeBlocks;
    }

    @Override
    @NotNull
    public UnsafeBlocks getUnsafeBlocks() {
        return unsafeBlocks;
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
    public void registerMetrics(int metricsId) {
        if (!getVersion().getMetadata().isBlank()) {
            return;
        }

        try {
            final Metrics metrics = new Metrics(this, metricsId);
            metrics.addCustomChart(new SimplePie("bungee_mode",
                    () -> Boolean.toString(getSettings().doCrossServer())));

            if (getSettings().doCrossServer()) {
                metrics.addCustomChart(new SimplePie("messenger_type",
                        () -> getSettings().getBrokerType().getDisplayName()));
            }

            metrics.addCustomChart(new SimplePie("language",
                    () -> getSettings().getLanguage().toLowerCase()));
            metrics.addCustomChart(new SimplePie("database_type",
                    () -> getSettings().getDatabaseType().getDisplayName()));
            metrics.addCustomChart(new SimplePie("using_economy",
                    () -> Boolean.toString(getSettings().doEconomy())));
            metrics.addCustomChart(new SimplePie("using_map",
                    () -> Boolean.toString(getSettings().doMapHook())));

            getMapHook().ifPresent(hook -> metrics.addCustomChart(new SimplePie("map_type", hook::getName)));
        } catch (Throwable e) {
            log(Level.WARNING, "Failed to register bStats metrics (" + e.getMessage() + ")");
        }
    }

    @Override
    public void initializePluginChannels() {
        final String channelId = PluginMessageBroker.BUNGEE_CHANNEL_ID;
        Bukkit.getMessenger().registerIncomingPluginChannel(this, channelId, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, channelId);
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
            pluginMessenger.onReceive(channel, BukkitUser.adapt(player, this), message);
        }
    }

    @NotNull
    public AudienceProvider getAudiences() {
        return audiences;
    }

    @NotNull
    public GracefulScheduling getScheduler() {
        return paperLib.scheduling();
    }

    @NotNull
    public CommandRegistration getCommandRegistrar() {
        return paperLib.commandRegistration();
    }

    @Override
    @NotNull
    public HuskHomes getPlugin() {
        return this;
    }

}
