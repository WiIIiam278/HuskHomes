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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.pokeskies.fabricpluginmessaging.FabricPluginMessaging;
import com.pokeskies.fabricpluginmessaging.PluginMessageEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.kyori.adventure.audience.Audience;
//? if >=1.21.4 {
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences;
//?} else {
/*import net.kyori.adventure.platform.fabric.FabricServerAudiences;
*///?}
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.LevelData;
import net.william278.desertwell.util.Version;
import net.william278.huskhomes.api.FabricHuskHomesAPI;
import net.william278.huskhomes.command.Command;
import net.william278.huskhomes.command.FabricCommand;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.config.Server;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.config.Spawn;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.event.FabricEventDispatcher;
import net.william278.huskhomes.hook.FabricHookProvider;
import net.william278.huskhomes.hook.Hook;
import net.william278.huskhomes.listener.EventListener;
import net.william278.huskhomes.listener.FabricEventListener;
import net.william278.huskhomes.manager.Manager;
import net.william278.huskhomes.network.Broker;
import net.william278.huskhomes.network.PluginMessageBroker;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.random.RandomTeleportEngine;
import net.william278.huskhomes.user.*;
import net.william278.huskhomes.util.FabricSavePositionProvider;
import net.william278.huskhomes.util.FabricTask;
import net.william278.huskhomes.util.UnsafeBlocks;
import net.william278.toilet.Toilet;
import net.william278.toilet.fabric.FabricToilet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggingEventBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;


@Getter
@NoArgsConstructor
public class FabricHuskHomes implements DedicatedServerModInitializer, HuskHomes, FabricTask.Supplier,
        FabricEventDispatcher, FabricSavePositionProvider, FabricUserProvider, FabricHookProvider {

    public static final Logger LOGGER = LoggerFactory.getLogger("HuskHomes");
    private final ModContainer modContainer = FabricLoader.getInstance().getModContainer("huskhomes")
            .orElseThrow(() -> new RuntimeException("Failed to get Mod Container"));
    private final Map<String, Boolean> permissions = Maps.newHashMap();

    //? if >=1.21.4 {
    private MinecraftServerAudiences audiences;
    //?} else {
    /*private FabricServerAudiences audiences;
    *///?}
    private MinecraftServer minecraftServer;
    private Toilet toilet;

    private final Set<SavedUser> savedUsers = Sets.newHashSet();
    private final Set<UUID> currentlyOnWarmup = Sets.newConcurrentHashSet();
    private final Set<UUID> warmupDamagedUsers = Sets.newConcurrentHashSet();
    private final Map<UUID, OnlineUser> onlineUserMap = Maps.newHashMap();
    private final Map<String, List<User>> globalUserList = Maps.newConcurrentMap();
    private final List<Command> commands = Lists.newArrayList();

    @Setter
    private Set<Hook> hooks = Sets.newHashSet();
    @Setter
    private Settings settings;
    @Setter
    private Locales locales;
    @Setter
    private Database database;
    @Setter
    private Manager manager;
    @Setter
    private EventListener eventListener;
    @Setter
    private RandomTeleportEngine randomTeleportEngine;
    @Setter
    private Spawn serverSpawn;
    @Setter
    private UnsafeBlocks unsafeBlocks;
    @Setter
    @Nullable
    private Broker broker;
    @Setter
    @Nullable
    private Server serverName;

    @Override
    public void onInitializeServer() {
        this.load();
        this.loadCommands();

        ServerLifecycleEvents.SERVER_STARTING.register(this::onEnable);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onDisable);
    }

    private void onEnable(@NotNull MinecraftServer server) {
        this.minecraftServer = server;
        this.toilet = FabricToilet.create(getDumpOptions(), server);
        //? if >=1.21.4 {
        this.audiences = MinecraftServerAudiences.of(minecraftServer);
        //?} else {
        /*this.audiences = FabricServerAudiences.of(minecraftServer);
        *///?}
        this.enable();
    }

    private void onDisable(@NotNull MinecraftServer server) {
        this.shutdown();
        if (audiences != null) {
            audiences.close();
            audiences = null;
        }
    }

    @Override
    public void loadAPI() {
        FabricHuskHomesAPI.register(this);
    }

    @Override
    public void loadMetrics() {
        // No metrics on Fabric
    }

    @Override
    public void disablePlugin() {
        onDisable(minecraftServer);
    }

    @Override
    @NotNull
    public ConsoleUser getConsole() {
        return new ConsoleUser(audiences.console());
    }

    @NotNull
    @Override
    public Audience getAudience(@NotNull UUID user) {
        return audiences.player(user);
    }

    @Override
    public void setWorldSpawn(@NotNull Position position) {
        final ServerLevel world = Adapter.adapt(position, minecraftServer);

        if (world != null) {
            //? if >=26.1.2 {
            world.setRespawnData(LevelData.RespawnData.of(
                    world.getLevel().dimension(),
                    new BlockPos(
                            (int) Math.floor(position.getX()),
                            (int) Math.floor(position.getY()),
                            (int) Math.floor(position.getZ())
                    ),
                    position.getYaw(),
                    0
            ));
            //?} elif >=1.21.11 {
            /*world.setRespawnData(LevelData.RespawnData.of(
                   world.dimension(),
                   BlockPos.containing(position.getX(), position.getY(), position.getZ()),
                   position.getYaw(),
                   0
            ));
            *///?} else {
            /*world.setDefaultSpawnPos(
                   BlockPos.containing(position.getX(), position.getY(), position.getZ()),
                   position.getYaw()
            );
            *///?}
        }
    }

    @Override
    public Optional<Spawn> getServerSpawn() {
        return Optional.ofNullable(serverSpawn);
    }

    @Override
    @NotNull
    public String getServerName() {
        return serverName == null ? "server" : serverName.getName();
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
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir().resolve("huskhomes");
    }

    @Override
    @NotNull
    public List<World> getWorlds() {
        final List<World> worlds = Lists.newArrayList();
        minecraftServer.getAllLevels().forEach(level -> worlds.add(Adapter.adapt(level)));
        return worlds;
    }

    @Override
    @NotNull
    public Version getPluginVersion() {
        return Version.fromString(modContainer.getMetadata().getVersion().getFriendlyString(), "-");
    }

    @Override
    @NotNull
    public String getServerType() {
        return String.format("fabric %s/%s", FabricLoader.getInstance()
                .getModContainer("fabricloader").map(l -> l.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown"),
                minecraftServer.getServerVersion()
        );
    }

    @Override
    @NotNull
    public Version getMinecraftVersion() {
        return Version.fromString(
                minecraftServer.getServerVersion()
        );
    }

    @Override
    public void registerCommands(@NotNull List<Command> toRegister) {
        CommandRegistrationCallback.EVENT.register((dispatcher, i1, i2) -> toRegister.stream().peek(commands::add)
                .forEach((command) -> new FabricCommand(command, this).register(dispatcher)));
    }

    @Override
    @NotNull
    public EventListener createListener() {
        return new FabricEventListener(this);
    }

    @Override
    public Optional<Broker> getBroker() {
        return Optional.ofNullable(broker);
    }

    @Override
    public void setupPluginMessagingChannels() {
        try {
            FabricPluginMessaging.class.getMethod("initialize").invoke(null);
        } catch (ReflectiveOperationException ignored) {
            // Older mapped runtimes initialize this dependency via its own ModInitializer.
        }
        PluginMessageEvent.EVENT.register((payload, context) -> {
            if (broker instanceof PluginMessageBroker messenger
                && getSettings().getCrossServer().getBrokerType() == Broker.Type.PLUGIN_MESSAGE) {
                messenger.onReceive(
                        PluginMessageBroker.BUNGEE_CHANNEL_ID,
                        getOnlineUser(context.player()),
                        payload.getData()
                );
            }
        });
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
    public FabricHuskHomes getPlugin() {
        return this;
    }

    public static class Adapter {

        @NotNull
        public static Location adapt(
                //? if >=26.1.2 {
                @NotNull Vec3 pos,
                @NotNull ServerLevel world,
                //?} else {
                /*@NotNull Vec3 pos,
                @NotNull net.minecraft.world.level.Level world,
                *///?}
                float yaw, float pitch
        ) {
            //? if >=26.1.2 {
            double x = pos.get(Direction.Axis.X);
            double y = pos.get(Direction.Axis.Y);
            double z = pos.get(Direction.Axis.Z);
            //?} else {
            /*double x = pos.x();
            double y = pos.y();
            double z = pos.z();
            *///?}
            return Position.at(
                    x, y, z,
                    yaw, pitch,
                    adapt(world)
            );
        }

        @NotNull
        public static Position adapt(
                //? if >=26.1.2 {
                @NotNull Vec3 pos,
                @NotNull ServerLevel world,
                //?} else {
                /*@NotNull Vec3 pos,
                @NotNull net.minecraft.world.level.Level world,
                *///?}
                float yaw, float pitch,
                @NotNull String server
        ) {
            return Position.at(adapt(pos, world, yaw, pitch), server);
        }

        @NotNull
        public static TeleportTransition adapt(@NotNull Location location, @NotNull MinecraftServer server,
                                               @NotNull Consumer<Entity> runAfterTeleport) {
            Vec3 pos = new Vec3(location.getX(), location.getY(), location.getZ());
            return new TeleportTransition(
                    Objects.requireNonNull(adapt(location.getWorld(), server)),
                    pos,
                    Vec3.ZERO,
                    location.getYaw(),
                    location.getPitch(),
                    runAfterTeleport::accept
            );
        }

        @Nullable
        public static ServerLevel adapt(@NotNull World world, @NotNull MinecraftServer server) {
            //? if >=26.1.2 {
            return server.getLevel(ResourceKey.create(Registries.DIMENSION,
                    Objects.requireNonNull(Identifier.tryParse(world.getName()))));
            //?} else {
            /*return server.getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.tryParse(world.getName())));
            *///?}
        }

        @Nullable
        public static ServerLevel adapt(@NotNull Position position, @NotNull MinecraftServer server) {
            return adapt(position.getWorld(), server);
        }

        @NotNull
        public static World adapt(
                //? if >=26.1.2 {
                @NotNull ServerLevel world
                //?} else {
                /*@NotNull net.minecraft.world.level.Level world
                *///?}
        ) {
            //? if >=26.1.2 {
            String name = world.getLevel().dimension().identifier().asString().replace("minecraft:", "");
            //?} else {
            /*String name = world.dimension().identifier().asMinimalString();
            *///?}
            return World.from(
                    name,
                    UUID.nameUUIDFromBytes(name.getBytes()),
                    World.Environment.match(name)
            );
        }

    }

}
