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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.william278.desertwell.util.Version;
import net.william278.huskhomes.command.Command;
import net.william278.huskhomes.command.SpongeCommand;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.config.Server;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.config.Spawn;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.event.SpongeEventDispatcher;
import net.william278.huskhomes.hook.Hook;
import net.william278.huskhomes.hook.SpongeEconomyHook;
import net.william278.huskhomes.listener.SpongeEventListener;
import net.william278.huskhomes.manager.Manager;
import net.william278.huskhomes.network.Broker;
import net.william278.huskhomes.network.PluginMessageBroker;
import net.william278.huskhomes.network.RedisBroker;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.random.NormalDistributionEngine;
import net.william278.huskhomes.random.RandomTeleportEngine;
import net.william278.huskhomes.user.ConsoleUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.user.SpongeUser;
import net.william278.huskhomes.util.SpongeSafetyResolver;
import net.william278.huskhomes.util.SpongeTask;
import net.william278.huskhomes.util.UnsafeBlocks;
import net.william278.huskhomes.util.Validator;
import org.bstats.charts.SimplePie;
import org.bstats.sponge.Metrics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Game;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command.Raw;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.api.network.channel.ChannelBuf;
import org.spongepowered.api.network.channel.raw.RawDataChannel;
import org.spongepowered.api.network.channel.raw.play.RawPlayDataChannel;
import org.spongepowered.api.network.channel.raw.play.RawPlayDataHandler;
import org.spongepowered.api.world.WorldTypes;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@Plugin("huskhomes")
public class SpongeHuskHomes implements HuskHomes, SpongeTask.Supplier, SpongeSafetyResolver, SpongeEventDispatcher,
        RawPlayDataHandler<EngineConnection> {

    /**
     * Metrics ID for <a href="https://bstats.org/plugin/sponge/HuskHomes/18423">HuskHomes on Sponge</a>.
     */
    private static final int METRICS_ID = 18423;
    private static final ResourceKey PLUGIN_MESSAGE_CHANNEL_KEY = ResourceKey.of("bungeecord", "main");

    private final Set<SavedUser> savedUsers = Sets.newHashSet();
    private final Map<String, List<String>> globalPlayerList = Maps.newConcurrentMap();
    private final Set<UUID> currentlyOnWarmup = Sets.newHashSet();
    private final Set<UUID> currentlyInvulnerable = Sets.newHashSet();

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDirectory;
    @Inject
    private PluginContainer pluginContainer;
    @Inject
    private Game game;
    @Inject
    private Metrics.Factory metricsFactory;
    private RawPlayDataChannel pluginMessageChannel;

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
    private Server server;
    @Nullable
    private Broker broker;

    @Listener
    public void onConstructPlugin(final ConstructPluginEvent event) {
        // Get plugin version from mod container
        this.validator = new Validator(this);

        // Load settings and locales
        initialize("plugin config & locale files", (plugin) -> loadConfigs());

        // Initialize the database
        initialize("database", (plugin) -> loadDatabase());

        // Initialize the network messenger if proxy mode is enabled
        final Settings.CrossServerSettings crossServer = getSettings().getCrossServer();
        if (crossServer.isEnabled()) {
            initialize(crossServer.getBrokerType().getDisplayName() + " message broker", (plugin) -> {
                broker = switch (crossServer.getBrokerType()) {
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

            if (!hooks.isEmpty()) {
                hooks.forEach(hook -> {
                    try {
                        hook.initialize();
                    } catch (Throwable e) {
                        log(Level.WARNING, "Failed to initialize " + hook.getName() + " hook", e);
                    }
                });
                log(Level.INFO, "Registered " + hooks.size() + " mod hooks: " + hooks.stream()
                        .map(Hook::getName)
                        .collect(Collectors.joining(", ")));
            }
        });

        // Hook into bStats
        initialize("metrics", (plugin) -> this.registerMetrics(METRICS_ID));
    }

    @Listener
    public void onShutdown(final StoppingEngineEvent<org.spongepowered.api.Server> event) {
        this.closeDatabase();
        if (broker != null) {
            broker.close();
        }
        cancelTasks();
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
                .map(user -> SpongeUser.adapt(user, this))
                .collect(Collectors.toList());
    }

    @NotNull
    @Override
    public Audience getAudience(@NotNull UUID user) {
        return game.server().player(user).map(player -> (Audience) player).orElse(Audience.empty());
    }

    @Override
    public void setWorldSpawn(@NotNull Position position) {
        final ServerLocation loc = Adapter.adapt(position);
        loc.world().properties().setSpawnPosition(loc.blockPosition());
    }

    @Override
    public Optional<Spawn> getServerSpawn() {
        return Optional.ofNullable(serverSpawn);
    }

    @NotNull
    @Override
    public String getServerName() {
        return server == null ? "server" : server.getName();
    }

    @Override
    public void setServerName(@NotNull Server server) {
        this.server = server;
    }

    @NotNull
    @Override
    public Broker getMessenger() {
        if (broker == null) {
            throw new IllegalStateException("Attempted to access message broker when it was not initialized");
        }
        return broker;
    }

    @Override
    public void registerHooks() {
        HuskHomes.super.registerHooks();

        // Register the sponge economy service if it is available
        if (getSettings().getEconomy().isEnabled()
                && getGame().server().serviceProvider().economyService().isPresent()) {
            getHooks().add(new SpongeEconomyHook(this));
        }
    }

    @Nullable
    @Override
    public InputStream getResource(@NotNull String name) {
        return pluginContainer.openResource(URI.create(name)).orElse(null);
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
        return SpongeCommand.Type.getCommands(getPlugin()).stream()
                .map(command -> new SpongeCommand(command, this))
                .peek(command -> command.registerCommand(event))
                .toList();
    }

    public void registerPermissions() {
        commands.forEach(SpongeCommand::registerPermissions);
    }

    @Override
    public boolean isDependencyLoaded(@NotNull String name) {
        return game.pluginManager().plugin(name).isPresent();
    }

    @Override
    public void registerMetrics(int metricsId) {
        if (!getVersion().getMetadata().isBlank()) {
            return;
        }

        try {
            final Metrics metrics = metricsFactory.make(METRICS_ID);
            metrics.addCustomChart(new SimplePie("bungee_mode",
                    () -> Boolean.toString(getSettings().getCrossServer().isEnabled())));
            if (getSettings().getCrossServer().isEnabled()) {
                metrics.addCustomChart(new SimplePie("messenger_type",
                        () -> getSettings().getCrossServer().getBrokerType().getDisplayName())
                );
            }
            metrics.addCustomChart(new SimplePie("language",
                    () -> getSettings().getLanguage().toLowerCase()));
            metrics.addCustomChart(new SimplePie("database_type",
                    () -> getSettings().getDatabase().getType().getDisplayName()));
            metrics.addCustomChart(new SimplePie("using_economy",
                    () -> Boolean.toString(getSettings().getEconomy().isEnabled())));
            metrics.addCustomChart(new SimplePie("using_map",
                    () -> Boolean.toString(getSettings().getMapHook().isEnabled())));
            getMapHook().ifPresent(hook -> metrics.addCustomChart(new SimplePie("map_type", hook::getName)));
        } catch (Throwable e) {
            log(Level.WARNING, "Failed to register bStats metrics (" + e.getMessage() + ")");
        }
    }

    @Override
    public void initializePluginChannels() {
        this.pluginMessageChannel = game.channelManager().ofType(
                PLUGIN_MESSAGE_CHANNEL_KEY, RawDataChannel.class
        ).play();
        this.pluginMessageChannel.addHandler(this);
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
        if (broker instanceof PluginMessageBroker messenger
                && getSettings().getCrossServer().getBrokerType() == Broker.Type.PLUGIN_MESSAGE) {
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

    @Override
    public void closeDatabase() {
        if (database != null) {
            database.close();
        }
    }

    @NotNull
    @Override
    public SpongeHuskHomes getPlugin() {
        return this;
    }

    public static final class Adapter {

        @NotNull
        public static Location adapt(@NotNull ServerLocation location) {
            return Location.at(
                    location.x(), location.y(), location.z(),
                    adapt(location.world())
            );
        }

        @NotNull
        public static Position adapt(@NotNull ServerLocation location, @NotNull String server) {
            return Position.at(adapt(location), server);
        }

        @NotNull
        public static ServerLocation adapt(@NotNull Location location) {
            return ServerLocation.of(
                    Objects.requireNonNull(adapt(location.getWorld())),
                    location.getX(), location.getY(), location.getZ()
            );
        }

        @Nullable
        public static ServerWorld adapt(@NotNull World world) {
            return Sponge.server().worldManager().world(ResourceKey.resolve(world.getName())).orElse(null);
        }

        @NotNull
        public static World adapt(@NotNull ServerWorld world) {
            final String worldType = world.properties().worldType().key(WorldTypes.registry().type()
                    .asDefaultedType(world::engine)).value();
            return World.from(
                    world.properties().name(),
                    world.uniqueId(),
                    World.Environment.match(worldType)
            );
        }

    }
}
