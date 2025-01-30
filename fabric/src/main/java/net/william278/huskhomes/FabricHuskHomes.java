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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.kyori.adventure.audience.Audience;
//#if MC==12104
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences;
//#else
//$$ import net.kyori.adventure.platform.fabric.FabricServerAudiences;
//#endif
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
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
import net.william278.huskhomes.network.FabricPluginMessage;
import net.william278.huskhomes.network.PluginMessageBroker;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.random.RandomTeleportEngine;
import net.william278.huskhomes.user.*;
import net.william278.huskhomes.util.FabricSavePositionProvider;
import net.william278.huskhomes.util.FabricTask;
import net.william278.huskhomes.util.UnsafeBlocks;
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
        FabricEventDispatcher, FabricSavePositionProvider, FabricUserProvider, FabricHookProvider,
        ServerPlayNetworking.PlayPayloadHandler<FabricPluginMessage> {

    public static final Logger LOGGER = LoggerFactory.getLogger("HuskHomes");
    private final ModContainer modContainer = FabricLoader.getInstance().getModContainer("huskhomes")
            .orElseThrow(() -> new RuntimeException("Failed to get Mod Container"));
    private final Map<String, Boolean> permissions = Maps.newHashMap();

    //#if MC==12104
    private MinecraftServerAudiences audiences;
    //#else
    //$$ private FabricServerAudiences audiences;
    //#endif
    private MinecraftServer minecraftServer;

    private final Set<SavedUser> savedUsers = Sets.newHashSet();
    private final Set<UUID> currentlyOnWarmup = Sets.newConcurrentHashSet();
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
        //#if MC==12104
        this.audiences = MinecraftServerAudiences.of(minecraftServer);
        //#else
        //$$ this.audiences = FabricServerAudiences.of(minecraftServer);
        //#endif
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
        final ServerWorld world = Adapter.adapt(position, minecraftServer);
        if (world != null) {
            world.setSpawnPos(
                    BlockPos.ofFloored(position.getX(), position.getY(), position.getZ()),
                    position.getYaw()
            );
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
    public boolean isDependencyAvailable(@NotNull String name) {
        return FabricLoader.getInstance().isModLoaded(name);
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
        minecraftServer.getWorlds().forEach(world -> worlds.add(Adapter.adapt(world)));
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
                .orElse("unknown"), minecraftServer.getVersion());
    }

    @Override
    @NotNull
    public Version getMinecraftVersion() {
        return Version.fromString(minecraftServer.getVersion());
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
        PayloadTypeRegistry.playC2S().register(FabricPluginMessage.CHANNEL_ID, FabricPluginMessage.CODEC);
        PayloadTypeRegistry.playS2C().register(FabricPluginMessage.CHANNEL_ID, FabricPluginMessage.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FabricPluginMessage.CHANNEL_ID, this);
    }

    // When the server receives a plugin message
    @Override
    public void receive(@NotNull FabricPluginMessage payload, @NotNull ServerPlayNetworking.Context context) {
        if (broker instanceof PluginMessageBroker messenger
            && getSettings().getCrossServer().getBrokerType() == Broker.Type.PLUGIN_MESSAGE) {
            messenger.onReceive(
                    PluginMessageBroker.BUNGEE_CHANNEL_ID,
                    getOnlineUser(context.player()),
                    payload.getData()
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
        public static Location adapt(@NotNull Vec3d pos, @NotNull net.minecraft.world.World world,
                                     float yaw, float pitch) {
            return Position.at(
                    pos.getX(), pos.getY(), pos.getZ(),
                    yaw, pitch,
                    adapt(world)
            );
        }

        @NotNull
        public static Position adapt(@NotNull Vec3d pos, @NotNull net.minecraft.world.World world,
                                     float yaw, float pitch, @NotNull String server) {
            return Position.at(adapt(pos, world, yaw, pitch), server);
        }

        @NotNull
        public static TeleportTarget adapt(@NotNull Location location, @NotNull MinecraftServer server,
                                           @NotNull Consumer<Entity> runAfterTeleport) {
            return new TeleportTarget(
                    adapt(location.getWorld(), server),
                    new Vec3d(location.getX(), location.getY(), location.getZ()),
                    Vec3d.ZERO,
                    location.getYaw(),
                    location.getPitch(),
                    runAfterTeleport::accept
            );
        }

        @Nullable
        public static ServerWorld adapt(@NotNull World world, @NotNull MinecraftServer server) {
            return server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.tryParse(world.getName())));
        }

        @Nullable
        public static ServerWorld adapt(@NotNull Position position, @NotNull MinecraftServer server) {
            return adapt(position.getWorld(), server);
        }

        @NotNull
        public static World adapt(@NotNull net.minecraft.world.World world) {
            return World.from(
                    world.getRegistryKey().getValue().asMinimalString(),
                    UUID.nameUUIDFromBytes(world.getRegistryKey().getValue().asString().getBytes())
            );
        }

    }

}
