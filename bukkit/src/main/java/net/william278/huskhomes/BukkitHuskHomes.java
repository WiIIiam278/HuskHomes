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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.william278.desertwell.util.Version;
import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.command.BukkitCommand;
import net.william278.huskhomes.command.Command;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.config.Server;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.config.Spawn;
import net.william278.huskhomes.database.*;
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
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.random.NormalDistributionEngine;
import net.william278.huskhomes.random.RandomTeleportEngine;
import net.william278.huskhomes.user.BukkitUser;
import net.william278.huskhomes.user.ConsoleUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.util.BukkitSafetyResolver;
import net.william278.huskhomes.util.BukkitTask;
import net.william278.huskhomes.util.UnsafeBlocks;
import net.william278.huskhomes.util.Validator;
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
import space.arim.morepaperlib.scheduling.AsynchronousScheduler;
import space.arim.morepaperlib.scheduling.AttachedScheduler;
import space.arim.morepaperlib.scheduling.GracefulScheduling;
import space.arim.morepaperlib.scheduling.RegionalScheduler;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class BukkitHuskHomes extends JavaPlugin implements HuskHomes, BukkitTask.Supplier, BukkitEventDispatcher,
        PluginMessageListener, BukkitSafetyResolver {

    /**
     * Metrics ID for <a href="https://bstats.org/plugin/bukkit/HuskHomes/8430">HuskHomes on Bukkit</a>.
     */
    private static final int METRICS_ID = 8430;

    private final Set<SavedUser> savedUsers = Sets.newHashSet();
    private final Map<String, List<String>> globalPlayerList = Maps.newConcurrentMap();
    private final Set<UUID> currentlyOnWarmup = Sets.newConcurrentHashSet();
    private final Set<UUID> currentlyInvulnerable = Sets.newConcurrentHashSet();

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
    @Getter(AccessLevel.NONE)
    private Server server;
    @Getter(AccessLevel.NONE)
    private BukkitAudiences audiences;
    @Getter(AccessLevel.NONE)
    private MorePaperLib paperLib;
    private AsynchronousScheduler asyncScheduler;
    private RegionalScheduler regionalScheduler;
    @Nullable
    private Broker broker;

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
        this.validator = new Validator(this);

        // Load settings and locales
        initialize("plugin config & locale files", (plugin) -> loadConfigs());

        // Initialize the database
        final Database.Type type = getSettings().getDatabase().getType();
        initialize(type.getDisplayName() + " database connection", (plugin) -> {
            this.database = switch (type) {
                case MYSQL, MARIADB -> new MySqlDatabase(this);
                case SQLITE -> new SqLiteDatabase(this);
                case H2 -> new H2Database(this);
                case POSTGRESQL -> new PostgreSqlDatabase(this);
            };

            database.initialize();
        });

        // Initialize the manager
        this.manager = new Manager(this);

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
        if (getSettings().getEconomy().isEnabled() && isDependencyLoaded("Vault")) {
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
        HuskHomesAPI.unregister();
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

    @Override
    public void setWorldSpawn(@NotNull Position position) {
        Objects.requireNonNull(Adapter.adapt(position).getWorld()).setSpawnLocation(Adapter.adapt(position));
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
    public Optional<Spawn> getServerSpawn() {
        return Optional.ofNullable(serverSpawn);
    }

    @Override
    public void setServerSpawn(@NotNull Spawn spawn) {
        this.serverSpawn = spawn;
    }

    @Override
    @NotNull
    public Version getVersion() {
        return Version.fromString(getDescription().getVersion(), "-");
    }

    @Override
    @NotNull
    public String getServerName() {
        return server != null ? server.getName() : "server";
    }

    @Override
    public void setServerName(@NotNull Server server) {
        this.server = server;
    }

    @Override
    @NotNull
    public Path getConfigDirectory() {
        return getDataFolder().toPath();
    }

    @Override
    @NotNull
    public List<World> getWorlds() {
        return getServer().getWorlds().stream().map(Adapter::adapt).toList();
    }

    @Override
    public void registerMetrics(int metricsId) {
        if (!getVersion().getMetadata().isBlank()) {
            return;
        }

        try {
            final Metrics metrics = new Metrics(this, metricsId);
            metrics.addCustomChart(new SimplePie("bungee_mode",
                    () -> Boolean.toString(getSettings().getCrossServer().isEnabled())
            ));

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
        final String channelId = PluginMessageBroker.BUNGEE_CHANNEL_ID;
        getServer().getMessenger().registerIncomingPluginChannel(this, channelId, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, channelId);
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
                && getSettings().getCrossServer().getBrokerType() == Broker.Type.PLUGIN_MESSAGE) {
            pluginMessenger.onReceive(channel, BukkitUser.adapt(player, this), message);
        }
    }

    @NotNull
    public GracefulScheduling getScheduler() {
        return paperLib.scheduling();
    }

    @NotNull
    public AsynchronousScheduler getAsyncScheduler() {
        return asyncScheduler == null
                ? asyncScheduler = getScheduler().asyncScheduler() : asyncScheduler;
    }

    @NotNull
    public RegionalScheduler getSyncScheduler() {
        return regionalScheduler == null
                ? regionalScheduler = getScheduler().globalRegionalScheduler() : regionalScheduler;
    }

    @NotNull
    public AttachedScheduler getUserSyncScheduler(@NotNull OnlineUser user) {
        return getScheduler().entitySpecificScheduler(((BukkitUser) user).getPlayer());
    }

    @NotNull
    public CommandRegistration getCommandRegistrar() {
        return paperLib.commandRegistration();
    }

    @Override
    @NotNull
    public BukkitHuskHomes getPlugin() {
        return this;
    }

    public static class Adapter {

        @NotNull
        public static Location adapt(@NotNull org.bukkit.Location location) {
            return Position.at(
                    location.getX(), location.getY(), location.getZ(),
                    location.getYaw(), location.getPitch(),
                    adapt(Objects.requireNonNull(location.getWorld(), "Location world is null"))
            );
        }

        @NotNull
        public static Position adapt(@NotNull org.bukkit.Location location, @NotNull String server) {
            return Position.at(adapt(location), server);
        }

        @NotNull
        public static org.bukkit.Location adapt(@NotNull Location position) {
            return new org.bukkit.Location(
                    adapt(position.getWorld()),
                    position.getX(), position.getY(), position.getZ(),
                    position.getYaw(), position.getPitch()
            );
        }

        @Nullable
        public static org.bukkit.World adapt(@NotNull World world) {
            return Optional.ofNullable(Bukkit.getWorld(world.getUuid()))
                    .or(() -> Optional.ofNullable(Bukkit.getWorld(world.getName())))
                    .orElse(null);
        }

        @NotNull
        public static World adapt(@NotNull org.bukkit.World world) {
            return World.from(world.getName(), world.getUID(), World.Environment.match(world.getEnvironment().name()));
        }

    }
}
