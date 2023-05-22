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

import com.google.inject.Inject;
import net.william278.annotaml.Annotaml;
import net.william278.desertwell.util.Version;
import net.william278.huskhomes.command.Command;
import net.william278.huskhomes.command.SpongeCommand;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.config.Server;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.config.Spawn;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.database.MySqlDatabase;
import net.william278.huskhomes.database.SqLiteDatabase;
import net.william278.huskhomes.event.SpongeEventDispatcher;
import net.william278.huskhomes.hook.Hook;
import net.william278.huskhomes.hook.SpongeEconomyHook;
import net.william278.huskhomes.listener.SpongeEventListener;
import net.william278.huskhomes.manager.Manager;
import net.william278.huskhomes.network.Broker;
import net.william278.huskhomes.network.PluginMessageBroker;
import net.william278.huskhomes.network.RedisBroker;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.random.NormalDistributionEngine;
import net.william278.huskhomes.random.RandomTeleportEngine;
import net.william278.huskhomes.user.ConsoleUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.user.SpongeUser;
import net.william278.huskhomes.util.*;
import org.bstats.charts.SimplePie;
import org.bstats.sponge.Metrics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Game;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.Command.Raw;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.api.network.channel.ChannelBuf;
import org.spongepowered.api.network.channel.raw.RawDataChannel;
import org.spongepowered.api.network.channel.raw.play.RawPlayDataChannel;
import org.spongepowered.api.network.channel.raw.play.RawPlayDataHandler;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Plugin("huskhomes")
public class SpongeHuskHomes implements HuskHomes, SpongeTaskRunner, SpongeSafetyResolver, SpongeEventDispatcher, RawPlayDataHandler<EngineConnection> {

    /**
     * Metrics ID for <a href="https://bstats.org/plugin/sponge/HuskHomes/18423">HuskHomes on Sponge</a>.
     */
    private static final int METRICS_ID = 18423;
    private static final ResourceKey PLUGIN_MESSAGE_CHANNEL_KEY = ResourceKey.of("bungeecord", "main");

    // Instance of the plugin
    private static SpongeHuskHomes instance;

    public static SpongeHuskHomes getInstance() {
        return instance;
    }

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path pluginDirectory;
    @Inject
    private PluginContainer pluginContainer;
    @Inject
    private Game game;
    @Inject
    private Metrics.Factory metricsFactory;

    private ConcurrentHashMap<Integer, CancellableRunnable> tasks;
    private Set<SavedUser> savedUsers;
    private Settings settings;
    private Locales locales;
    private Database database;
    private Validator validator;
    private Manager manager;
    private RandomTeleportEngine randomTeleportEngine;
    private Spawn serverSpawn;
    private UnsafeBlocks unsafeBlocks;
    private List<Hook> hooks;
    private List<SpongeCommand> commands;
    private Map<String, List<String>> globalPlayerList;
    private Set<UUID> currentlyOnWarmup;
    private Server server;
    @Nullable
    private Broker broker;
    private RawPlayDataChannel channel;

    @Listener
    public void onConstructPlugin(final ConstructPluginEvent event) {
        instance = this;

        // Get plugin version from mod container
        this.tasks = new ConcurrentHashMap<>();
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
                case MYSQL -> new MySqlDatabase(this);
                case SQLITE -> new SqLiteDatabase(this);
            };

            database.initialize();
        });

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

        // Check for updates
        this.checkForUpdates();
    }

    @Listener
    public void onServerStarted(final StartedEngineEvent<org.spongepowered.api.Server> event) {
        // Initialize the manager
        this.manager = new Manager(this);

        // Setup RTP engine
        setRandomTeleportEngine(new NormalDistributionEngine(this));

        // Register events
        initialize("events", (plugin) -> new SpongeEventListener(this));

        // Register permissions
        initialize("permissions", (plugin) -> registerPermissions());

        // Initialize hooks
        initialize("hooks", (plugin) -> {
            this.registerHooks();

            if (hooks.size() > 0) {
                hooks.forEach(Hook::initialize);
                log(Level.INFO, "Registered " + hooks.size() + " mod hooks: " + hooks.stream()
                        .map(Hook::getName)
                        .collect(Collectors.joining(", ")));
            }
        });

        // Hook into bStats
        initialize("metrics", (plugin) -> this.registerMetrics(METRICS_ID));
    }

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Raw> event) {
        initialize("commands", (plugin) -> this.commands = registerCommands(event));
    }

    @NotNull
    @Override
    public ConsoleUser getConsole() {
        return new ConsoleUser(game.server());
    }

    @NotNull
    @Override
    public List<OnlineUser> getOnlineUsers() {
        return game.server().onlinePlayers().stream()
                .map(SpongeUser::adapt)
                .collect(Collectors.toList());
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
    public Optional<Spawn> getServerSpawn() {
        return Optional.ofNullable(serverSpawn);
    }

    @Override
    public void setServerSpawn(@NotNull Spawn spawn) {
        this.serverSpawn = spawn;
    }

    @NotNull
    @Override
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

    @NotNull
    @Override
    public UnsafeBlocks getUnsafeBlocks() {
        return unsafeBlocks;
    }

    @NotNull
    @Override
    public Database getDatabase() {
        return database;
    }

    @NotNull
    @Override
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
    public void setServerSpawn(@NotNull Location location) {
        try {
            this.serverSpawn = Annotaml.create(new File(getDataFolder(), "spawn.yml"), new Spawn(location)).get();

            // Update the world spawn location, too
            game.server().worldManager().worlds().forEach(world -> {
                if (world.properties().key().asString().equals(location.getWorld().getName())) {
                    world.properties().setSpawnPosition(Vector3i.from(
                            (int) location.getX(), (int) location.getY(), (int) location.getZ())
                    );
                }
            });
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            log(Level.WARNING, "Failed to save the server spawn.yml file", e);
        }
    }

    @NotNull
    @Override
    public List<Hook> getHooks() {
        return hooks;
    }

    @Override
    public void setHooks(@NotNull List<Hook> hooks) {
        this.hooks = hooks;
    }

    @Override
    public void registerHooks() {
        HuskHomes.super.registerHooks();

        // Register the sponge economy service if it is available
        if (getSettings().doEconomy() && getGame().server().serviceProvider().economyService().isPresent()) {
            getHooks().add(new SpongeEconomyHook(this));
        }
    }

    @Nullable
    @Override
    public InputStream getResource(@NotNull String name) {
        return pluginContainer.openResource(URI.create(name))
                .orElse(null);
    }

    @NotNull
    @Override
    public File getDataFolder() {
        return pluginDirectory.toFile();
    }

    @NotNull
    @Override
    public List<World> getWorlds() {
        return game.server().worldManager().worlds()
                .stream()
                .map(world -> World.from(world.key().toString(), world.uniqueId()))
                .collect(Collectors.toList());
    }

    @NotNull
    @Override
    public Version getVersion() {
        return Version.fromString(pluginContainer.metadata().version().toString(), "-");
    }

    @NotNull
    @Override
    public List<Command> getCommands() {
        return commands.stream().map(SpongeCommand::getCommand).collect(Collectors.toList());
    }

    @NotNull
    public List<SpongeCommand> registerCommands(@NotNull RegisterCommandEvent<Raw> event) {
        final List<SpongeCommand> commands = new ArrayList<>();
        for (SpongeCommand.Type type : SpongeCommand.Type.values()) {
            final SpongeCommand command = new SpongeCommand(type.getCommand(), this);
            commands.add(command);
            command.registerCommand(event);
        }
        return commands;
    }

    public void registerPermissions() {
        commands.forEach(SpongeCommand::registerPermissions);
    }

    @Override
    public boolean isDependencyLoaded(@NotNull String name) {
        return game.pluginManager().plugin(name).isPresent();
    }

    @NotNull
    @Override
    public Map<String, List<String>> getGlobalPlayerList() {
        return globalPlayerList;
    }

    @NotNull
    @Override
    public Set<UUID> getCurrentlyOnWarmup() {
        return currentlyOnWarmup;
    }

    @Override
    public void registerMetrics(int metricsId) {
        if (!getVersion().getMetadata().isBlank()) {
            return;
        }

        try {
            final Metrics metrics = metricsFactory.make(METRICS_ID);
            metrics.addCustomChart(new SimplePie("bungee_mode", () -> Boolean.toString(getSettings().doCrossServer())));
            if (getSettings().doCrossServer()) {
                metrics.addCustomChart(new SimplePie("messenger_type", () -> getSettings().getBrokerType().getDisplayName()));
            }
            metrics.addCustomChart(new SimplePie("language", () -> getSettings().getLanguage().toLowerCase()));
            metrics.addCustomChart(new SimplePie("database_type", () -> getSettings().getDatabaseType().getDisplayName()));
            metrics.addCustomChart(new SimplePie("using_economy", () -> Boolean.toString(getSettings().doEconomy())));
            metrics.addCustomChart(new SimplePie("using_map", () -> Boolean.toString(getSettings().doMapHook())));
            getMapHook().ifPresent(hook -> metrics.addCustomChart(new SimplePie("map_type", hook::getName)));
        } catch (Throwable e) {
            log(Level.WARNING, "Failed to register bStats metrics (" + e.getMessage() + ")");
        }
    }

    @Override
    public void initializePluginChannels() {
        this.channel = game.channelManager().ofType(PLUGIN_MESSAGE_CHANNEL_KEY, RawDataChannel.class).play();
        this.channel.addHandler(this);
    }

    @Override
    public void handlePayload(@NotNull ChannelBuf pluginMessage, @NotNull EngineConnection connection) {
        final Optional<OnlineUser> playerConnection = getOnlineUsers().stream()
                .filter(onlineUser -> ((SpongeUser) onlineUser).getPlayer().connection().equals(connection))
                .findFirst();

        // Get the associated engine connection
        if (playerConnection.isEmpty()) {
            log(Level.WARNING, "Received a message from a player that is not online");
            return;
        }

        // Read the message and handle
        final SpongeUser user = (SpongeUser) playerConnection.get();
        final String channel = pluginMessage.readUTF();
        if (broker instanceof PluginMessageBroker messenger && getSettings().getBrokerType() == Broker.Type.PLUGIN_MESSAGE) {
            messenger.onReceive(channel, user, pluginMessage.readBytes(pluginMessage.available()));
        }
    }

    @Override
    public void log(@NotNull Level level, @NotNull String message, Throwable... exceptions) {
        final org.apache.logging.log4j.Level adaptedLevel = Optional
                .ofNullable(org.apache.logging.log4j.Level.getLevel(level.getName()))
                .orElse(org.apache.logging.log4j.Level.INFO);

        if (exceptions.length > 0) {
            pluginContainer.logger().log(adaptedLevel, message, exceptions[0]);
            return;
        }
        pluginContainer.logger().log(adaptedLevel, message);
    }

    @NotNull
    public PluginContainer getPluginContainer() {
        return pluginContainer;
    }

    @NotNull
    public Game getGame() {
        return game;
    }

    @NotNull
    public RawPlayDataChannel getPluginMessageChannel() {
        return channel;
    }

    @NotNull
    @Override
    public ConcurrentHashMap<Integer, CancellableRunnable> getTasks() {
        return tasks;
    }

    @NotNull
    @Override
    public SpongeHuskHomes getPlugin() {
        return this;
    }
}
