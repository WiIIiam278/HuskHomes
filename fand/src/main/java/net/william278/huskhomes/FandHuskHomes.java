/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 */

package net.william278.huskhomes;

import io.fand.api.messaging.PluginMessageDirection;
import io.fand.api.permission.PermissionDefault;
import io.fand.api.permission.PermissionDescriptor;
import io.fand.api.plugin.Plugin;
import io.fand.api.plugin.PluginContext;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.william278.desertwell.util.Version;
import net.william278.huskhomes.api.FandHuskHomesAPI;
import net.william278.huskhomes.command.Command;
import net.william278.huskhomes.command.FandCommand;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.config.Server;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.config.Spawn;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.event.FandEventDispatcher;
import net.william278.huskhomes.hook.FandHookProvider;
import net.william278.huskhomes.hook.Hook;
import net.william278.huskhomes.listener.EventListener;
import net.william278.huskhomes.listener.FandEventListener;
import net.william278.huskhomes.manager.Manager;
import net.william278.huskhomes.network.Broker;
import net.william278.huskhomes.network.PluginMessageBroker;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.random.RandomTeleportEngine;
import net.william278.huskhomes.user.FandUserProvider;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.user.ConsoleUser;
import net.william278.huskhomes.util.FandSavePositionProvider;
import net.william278.huskhomes.util.FandTask;
import net.william278.huskhomes.util.UnsafeBlocks;
import net.william278.toilet.Toilet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class FandHuskHomes implements Plugin, HuskHomes, FandTask.Supplier, FandEventDispatcher,
        FandSavePositionProvider, FandUserProvider, FandHookProvider {

    private final Set<SavedUser> savedUsers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> currentlyOnWarmup = ConcurrentHashMap.newKeySet();
    private final Set<UUID> warmupDamagedUsers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, OnlineUser> onlineUserMap = new ConcurrentHashMap<>();
    private final Map<String, List<User>> globalUserList = new ConcurrentHashMap<>();
    private final Map<String, Boolean> permissions = new ConcurrentHashMap<>();
    private final List<Command> commands = new ArrayList<>();
    private final Audience consoleAudience = new Audience() {
        @Override
        public void sendMessage(@NotNull Component message) {
            context.logger().info("{}", message);
        }
    };

    private PluginContext context;
    private Toilet toilet;
    private Set<Hook> hooks = new HashSet<>();
    private Settings settings;
    private Locales locales;
    private Database database;
    private Manager manager;
    private EventListener eventListener;
    private RandomTeleportEngine randomTeleportEngine;
    private Spawn serverSpawn;
    private UnsafeBlocks unsafeBlocks;
    private @Nullable Broker broker;
    private @Nullable Server serverName;
    private boolean active;
    private boolean failed;

    @Override
    public void onLoad(@NotNull PluginContext context) {
        this.context = context;
        this.load();
        if (failed) {
            throw new IllegalStateException("HuskHomes failed to load");
        }
    }

    @Override
    public void onEnable(@NotNull PluginContext context) {
        if (failed) {
            return;
        }
        this.toilet = FandToilet.create(getDumpOptions(), this);
        this.active = true;
        this.enable();
        if (failed) {
            throw new IllegalStateException("HuskHomes failed to enable");
        }
        try {
            this.loadCommands();
        } catch (RuntimeException | Error failure) {
            log(Level.SEVERE, "Failed to register HuskHomes commands", failure);
            disablePlugin();
            throw failure;
        }
    }

    @Override
    public void onDisable(@NotNull PluginContext context) {
        if (active) {
            this.shutdown();
            this.active = false;
        }
    }

    @NotNull
    public PluginContext getContext() {
        return context;
    }

    @Override
    public void loadAPI() {
        FandHuskHomesAPI.register(this);
    }

    @Override
    public void loadMetrics() {
    }

    @Override
    public void disablePlugin() {
        failed = true;
        if (active) {
            shutdown();
            active = false;
        }
    }

    @Override
    public void setWorldSpawn(@NotNull Position position) {
        FandAdapter.adapt(position.getWorld(), server()).ifPresent(world ->
                world.setSpawnLocation(world.at(
                        position.getX(), position.getY(), position.getZ(), position.getYaw(), position.getPitch()
                ))
        );
    }

    @Override
    public void setServerName(@NotNull Server serverName) {
        this.serverName = serverName;
    }

    @Override
    @Nullable
    public InputStream getResource(@NotNull String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
    }

    @Override
    @NotNull
    public Path getConfigDirectory() {
        return context.dataDirectory();
    }

    @Override
    @NotNull
    public List<World> getWorlds() {
        return server().worlds().stream().map(FandAdapter::adapt).toList();
    }

    @Override
    public void registerCommands(@NotNull List<Command> toRegister) {
        toRegister.forEach(command -> {
            commands.add(command);
            registerPermission(command.getPermission(), command.isOperatorCommand());
            command.getAdditionalPermissions().forEach(this::registerPermission);
            new FandCommand(command, this).register();
        });
    }

    private void registerPermission(@NotNull String permission, boolean operatorOnly) {
        if (permissions.putIfAbsent(permission, operatorOnly) == null) {
            context.permissions().register(new PermissionDescriptor(
                    permission, operatorOnly ? PermissionDefault.OPERATOR : PermissionDefault.TRUE
            ));
        }
    }

    @Override
    @NotNull
    public EventListener createListener() {
        this.eventListener = new FandEventListener(this);
        return eventListener;
    }

    @Override
    public void setupPluginMessagingChannels() {
        context.pluginMessaging().register(
                Key.key("bungeecord", "main"),
                PluginMessageDirection.BIDIRECTIONAL,
                (player, channel, payload) -> {
                    if (broker instanceof PluginMessageBroker messenger
                            && settings.getCrossServer().getBrokerType() == Broker.Type.PLUGIN_MESSAGE) {
                        messenger.onReceive(PluginMessageBroker.BUNGEE_CHANNEL_ID, getOnlineUser(player), payload);
                    }
                }
        );
    }

    @Override
    public void log(@NotNull Level level, @NotNull String message, Throwable... exceptions) {
        final Throwable failure = exceptions.length == 0 ? null : exceptions[0];
        if (level.intValue() >= Level.SEVERE.intValue()) {
            if (failure == null) {
                context.logger().error(message);
            } else {
                context.logger().error(message, failure);
            }
        } else if (level.intValue() >= Level.WARNING.intValue()) {
            if (failure == null) {
                context.logger().warn(message);
            } else {
                context.logger().warn(message, failure);
            }
        } else {
            if (failure == null) {
                context.logger().info(message);
            } else {
                context.logger().info(message, failure);
            }
        }
    }

    @Override
    public void closeDatabase() {
        if (database != null) {
            database.close();
        }
    }

    @Override
    public void closeBroker() {
        if (broker != null) {
            broker.close();
        }
    }

    @Override
    @NotNull
    public Version getPluginVersion() {
        return Version.fromString(context.descriptor().version(), "-");
    }

    @Override
    @NotNull
    public String getServerType() {
        return "fand %s/%s".formatted(server().version(), server().minecraftVersion());
    }

    @Override
    @NotNull
    public Version getMinecraftVersion() {
        return Version.fromString(server().minecraftVersion());
    }

    @Override
    @NotNull
    public Audience getAudience(@NotNull UUID user) {
        return server().player(user).map(player -> (Audience) player).orElse(Audience.empty());
    }

    @Override
    @NotNull
    public ConsoleUser getConsole() {
        return ConsoleUser.wrap(consoleAudience);
    }

    @Override
    @NotNull
    public Toilet getToilet() {
        return toilet;
    }

    @Override
    @NotNull
    public FandHuskHomes getPlugin() {
        return this;
    }

    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    @Override
    public Map<UUID, OnlineUser> getOnlineUserMap() {
        return onlineUserMap;
    }

    @Override
    public Map<String, List<User>> getGlobalUserList() {
        return globalUserList;
    }

    @Override
    public Set<SavedUser> getSavedUsers() {
        return savedUsers;
    }

    @Override
    public Set<UUID> getCurrentlyOnWarmup() {
        return currentlyOnWarmup;
    }

    @Override
    public Set<UUID> getWarmupDamagedUsers() {
        return warmupDamagedUsers;
    }

    @Override
    public List<Command> getCommands() {
        return commands;
    }

    @Override
    public Set<Hook> getHooks() {
        return hooks;
    }

    @Override
    public void setHooks(@NotNull Set<Hook> hooks) {
        this.hooks = hooks;
    }

    @Override
    public Settings getSettings() {
        return settings;
    }

    @Override
    public void setSettings(@NotNull Settings settings) {
        this.settings = settings;
    }

    @Override
    public Locales getLocales() {
        return locales;
    }

    @Override
    public void setLocales(@NotNull Locales locales) {
        this.locales = locales;
    }

    @Override
    public Database getDatabase() {
        return database;
    }

    @Override
    public void setDatabase(@NotNull Database database) {
        this.database = database;
    }

    @Override
    public Manager getManager() {
        return manager;
    }

    @Override
    public void setManager(@NotNull Manager manager) {
        this.manager = manager;
    }

    public EventListener getEventListener() {
        return eventListener;
    }

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
    public void setServerSpawn(@NotNull Spawn serverSpawn) {
        this.serverSpawn = serverSpawn;
    }

    @Override
    public UnsafeBlocks getUnsafeBlocks() {
        return unsafeBlocks;
    }

    @Override
    public void setUnsafeBlocks(@NotNull UnsafeBlocks unsafeBlocks) {
        this.unsafeBlocks = unsafeBlocks;
    }

    @Override
    public Optional<Broker> getBroker() {
        return Optional.ofNullable(broker);
    }

    @Override
    public void setBroker(@Nullable Broker broker) {
        this.broker = broker;
    }

    @Override
    @NotNull
    public String getServerName() {
        return serverName == null ? "server" : serverName.getName();
    }
}
