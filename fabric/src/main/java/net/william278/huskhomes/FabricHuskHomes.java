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

import io.netty.buffer.ByteBufUtil;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.william278.annotaml.Annotaml;
import net.william278.desertwell.util.Version;
import net.william278.huskhomes.command.Command;
import net.william278.huskhomes.command.FabricCommand;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.config.Server;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.config.Spawn;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.database.MySqlDatabase;
import net.william278.huskhomes.database.SqLiteDatabase;
import net.william278.huskhomes.event.FabricEventDispatcher;
import net.william278.huskhomes.hook.Hook;
import net.william278.huskhomes.listener.EventListener;
import net.william278.huskhomes.listener.FabricEventListener;
import net.william278.huskhomes.manager.Manager;
import net.william278.huskhomes.network.Broker;
import net.william278.huskhomes.network.PluginMessageBroker;
import net.william278.huskhomes.network.RedisBroker;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.random.NormalDistributionEngine;
import net.william278.huskhomes.random.RandomTeleportEngine;
import net.william278.huskhomes.user.ConsoleUser;
import net.william278.huskhomes.user.FabricUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.util.FabricSafetyResolver;
import net.william278.huskhomes.util.FabricTask;
import net.william278.huskhomes.util.UnsafeBlocks;
import net.william278.huskhomes.util.Validator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggingEventBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class FabricHuskHomes implements DedicatedServerModInitializer, HuskHomes, FabricTask.Supplier,
        FabricEventDispatcher, FabricSafetyResolver, ServerPlayNetworking.PlayChannelHandler {

    public static final Logger LOGGER = LoggerFactory.getLogger("HuskHomes");
    private final ModContainer modContainer = FabricLoader.getInstance().getModContainer("huskhomes")
            .orElseThrow(() -> new RuntimeException("Failed to get Mod Container"));

    private MinecraftServer minecraftServer;
    private Map<String, Boolean> permissions;
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
    private ConcurrentHashMap<String, List<String>> globalPlayerList;
    private Set<UUID> currentlyOnWarmup;
    private Server server;
    @Nullable
    private Broker broker;
    private FabricServerAudiences audiences;

    @Override
    public void onInitializeServer() {
        // Get plugin version from mod container
        this.permissions = new HashMap<>();
        this.savedUsers = new HashSet<>();
        this.globalPlayerList = new ConcurrentHashMap<>();
        this.currentlyOnWarmup = new HashSet<>();
        this.validator = new Validator(this);

        // Load settings and locales
        initialize("plugin config & locale files", (plugin) -> {
            if (!loadConfigs()) {
                throw new IllegalStateException("Failed to load config files. Please check the console for errors");
            }
        });

        // Pre-register commands
        initialize("commands", (plugin) -> this.commands = registerCommands());

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            this.minecraftServer = server;
            this.onEnable();
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> this.onDisable());
    }

    private void onEnable() {
        // Create adventure audience
        this.audiences = FabricServerAudiences.of(minecraftServer);

        // Temporarily log about sounds being disabled - todo: Remove when adventure-platform-fabric is updated
        if (getSettings().doPlaySoundEffects()) {
            log(Level.WARNING, "Sound effects are currently disabled for HuskHomes v" + getVersion()
                    + " on Fabric servers running Minecraft " + getMinecraftServer().getVersion());
        }

        // Initialize the database
        initialize(getSettings().getDatabaseType().getDisplayName() + " database connection", (plugin) -> {
            this.database = switch (getSettings().getDatabaseType()) {
                case MYSQL, MARIADB -> new MySqlDatabase(this);
                case SQLITE -> new SqLiteDatabase(this);
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

        setRandomTeleportEngine(new NormalDistributionEngine(this));

        // Register plugin hooks (Economy, Maps, Plan)
        initialize("hooks", (plugin) -> {
            this.registerHooks();

            if (hooks.size() > 0) {
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

        // Register events
        initialize("events", (plugin) -> this.eventListener = new FabricEventListener(this));

        this.checkForUpdates();
    }

    private void onDisable() {
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

    @Override
    @NotNull
    public List<OnlineUser> getOnlineUsers() {
        return minecraftServer.getPlayerManager().getPlayerList()
                .stream().map(user -> (OnlineUser) FabricUser.adapt(user, this))
                .toList();
    }

    @Override
    @NotNull
    public Set<SavedUser> getSavedUsers() {
        return savedUsers;
    }

    @Override
    @NotNull
    public Settings getSettings() {
        return settings;
    }

    @Override
    public void setSettings(@NotNull Settings settings) {
        this.settings = settings;
    }

    @Override
    @NotNull
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
            minecraftServer.getWorlds().forEach(world -> {
                if (world.getRegistryKey().getValue().asString().equals(location.getWorld().getName())) {
                    world.setSpawnPos(BlockPos.ofFloored(location.getX(), location.getY(), location.getZ()), 0);
                }
            });
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            log(Level.WARNING, "Failed to save the server spawn.yml file", e);
        }
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

    @NotNull
    @Override
    public UnsafeBlocks getUnsafeBlocks() {
        return unsafeBlocks;
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

    @Override
    @NotNull
    public Manager getManager() {
        return manager;
    }

    @Override
    @NotNull
    public Broker getMessenger() {
        if (broker == null) {
            throw new IllegalStateException("Attempted to access message broker when it was not initialized");
        }
        return broker;
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
    public List<Hook> getHooks() {
        return hooks;
    }

    @Override
    public void setHooks(@NotNull List<Hook> hooks) {
        this.hooks = hooks;
    }

    @Override
    @Nullable
    public InputStream getResource(@NotNull String name) {
        return this.modContainer.findPath(name)
                .map(path -> {
                    try {
                        return Files.newInputStream(path);
                    } catch (IOException e) {
                        log(Level.WARNING, "Failed to load resource: " + name, e);
                    }
                    return null;
                })
                .orElse(this.getClass().getClassLoader().getResourceAsStream(name));
    }

    @Override
    @NotNull
    public File getDataFolder() {
        return FabricLoader.getInstance().getConfigDir().resolve("huskhomes").toFile();
    }

    @Override
    @NotNull
    public List<World> getWorlds() {
        final List<World> worlds = new ArrayList<>();
        minecraftServer.getWorlds().forEach(world -> worlds.add(World.from(
                world.getRegistryKey().getValue().asString(),
                UUID.nameUUIDFromBytes(world.getRegistryKey().getValue().asString().getBytes())
        )));
        return worlds;
    }

    @Override
    @NotNull
    public Version getVersion() {
        return Version.fromString(modContainer.getMetadata().getVersion().getFriendlyString(), "-");
    }

    @Override
    @NotNull
    public List<Command> getCommands() {
        return commands;
    }

    @NotNull
    public List<Command> registerCommands() {
        final List<Command> commands = FabricCommand.Type.getCommands(getPlugin());
        CommandRegistrationCallback.EVENT.register((dispatcher, ignored, ignored2) ->
                commands.forEach(command -> new FabricCommand(command, this).register(dispatcher)));
        return commands;
    }

    @Override
    public boolean isDependencyLoaded(@NotNull String name) {
        return FabricLoader.getInstance().isModLoaded(name)
                || FabricLoader.getInstance().isModLoaded(name.toLowerCase(Locale.ENGLISH));
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
    public void registerMetrics(int metricsId) {
        // No metrics for Fabric
    }

    @Override
    public void initializePluginChannels() {
        ServerPlayNetworking.registerGlobalReceiver(new Identifier("bungeecord", "main"), this);
    }

    // When the server receives a plugin message
    @Override
    public void receive(@NotNull MinecraftServer server, @NotNull ServerPlayerEntity player,
                        @NotNull ServerPlayNetworkHandler handler, @NotNull PacketByteBuf buf,
                        @NotNull PacketSender responseSender) {
        if (broker instanceof PluginMessageBroker messenger
                && getSettings().getBrokerType() == Broker.Type.PLUGIN_MESSAGE) {
            messenger.onReceive(
                    PluginMessageBroker.BUNGEE_CHANNEL_ID,
                    FabricUser.adapt(player, this),
                    ByteBufUtil.getBytes(buf)
            );
        }
    }

    @Override
    public void log(@NotNull Level level, @NotNull String message, @NotNull Throwable... exceptions) {
        LoggingEventBuilder logEvent = LOGGER.makeLoggingEventBuilder(
                switch (level.getName()) {
                    case "WARNING" -> org.slf4j.event.Level.WARN;
                    case "SEVERE" -> org.slf4j.event.Level.ERROR;
                    default -> org.slf4j.event.Level.INFO;
                }
        );
        if (exceptions.length >= 1) {
            logEvent = logEvent.setCause(exceptions[0]);
        }
        logEvent.log(message);
    }

    @NotNull
    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    @NotNull
    public MinecraftServer getMinecraftServer() {
        return minecraftServer;
    }

    @Override
    @NotNull
    public FabricHuskHomes getPlugin() {
        return this;
    }

}
