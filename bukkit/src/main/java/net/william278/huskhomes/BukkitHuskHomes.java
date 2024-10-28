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
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.william278.desertwell.util.Version;
import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.command.BukkitCommand;
import net.william278.huskhomes.command.Command;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.config.Server;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.config.Spawn;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.event.BukkitEventDispatcher;
import net.william278.huskhomes.hook.BukkitHookProvider;
import net.william278.huskhomes.hook.Hook;
import net.william278.huskhomes.listener.BukkitEventListener;
import net.william278.huskhomes.listener.EventListener;
import net.william278.huskhomes.manager.Manager;
import net.william278.huskhomes.network.Broker;
import net.william278.huskhomes.network.PluginMessageBroker;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.random.RandomTeleportEngine;
import net.william278.huskhomes.user.*;
import net.william278.huskhomes.util.BukkitSavePositionProvider;
import net.william278.huskhomes.util.BukkitTask;
import net.william278.huskhomes.util.UnsafeBlocks;
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
import space.arim.morepaperlib.scheduling.AsynchronousScheduler;
import space.arim.morepaperlib.scheduling.AttachedScheduler;
import space.arim.morepaperlib.scheduling.GracefulScheduling;
import space.arim.morepaperlib.scheduling.RegionalScheduler;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

@Getter
@NoArgsConstructor
public class BukkitHuskHomes extends JavaPlugin implements HuskHomes, BukkitTask.Supplier, BukkitEventDispatcher,
        PluginMessageListener, BukkitHookProvider, BukkitSavePositionProvider, BukkitUserProvider {

    private AudienceProvider audiences;
    private AsynchronousScheduler asyncScheduler;
    private RegionalScheduler regionalScheduler;
    private MorePaperLib morePaperLib;

    private final Set<SavedUser> savedUsers = Sets.newHashSet();
    private final Set<UUID> currentlyOnWarmup = Sets.newConcurrentHashSet();
    private final Set<UUID> currentlyInvulnerable = Sets.newConcurrentHashSet();
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

    // Super constructor for unit testing
    @TestOnly
    protected BukkitHuskHomes(@NotNull JavaPluginLoader loader, @NotNull PluginDescriptionFile description,
                              @NotNull File dataFolder, @NotNull File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onLoad() {
        this.load();
    }

    @Override
    public void onEnable() {
        this.audiences = BukkitAudiences.create(this);
        this.morePaperLib = new MorePaperLib(this);
        this.enable();
        this.loadCommands();
    }

    @Override
    public void onDisable() {
        this.shutdown();
        if (this.audiences != null) {
            this.audiences.close();
        }
    }

    @Override
    public void loadAPI() {
        HuskHomesAPI.register(this);
    }

    @Override
    public void loadMetrics() {
        try {
            final Metrics metrics = new Metrics(this, BSTATS_BUKKIT_PLUGIN_ID);
            metrics.addCustomChart(new SimplePie("bungee_mode",
                    () -> Boolean.toString(getSettings().getCrossServer().isEnabled()))
            );
            metrics.addCustomChart(new SimplePie("language",
                    () -> getSettings().getLanguage().toLowerCase())
            );
            metrics.addCustomChart(new SimplePie("database_type",
                    () -> getSettings().getDatabase().getType().getDisplayName())
            );
            metrics.addCustomChart(new SimplePie("using_economy",
                    () -> Boolean.toString(getSettings().getEconomy().isEnabled()))
            );
            metrics.addCustomChart(new SimplePie("using_map",
                    () -> Boolean.toString(getSettings().getMapHook().isEnabled()))
            );
            getBroker().ifPresent(broker -> metrics.addCustomChart(new SimplePie("messenger_type",
                    () -> settings.getCrossServer().getBrokerType().getDisplayName()
            )));
        } catch (Throwable e) {
            log(Level.WARNING, "Failed to register plugin metrics", e);
        }
    }

    // Register the event listener
    @Override
    @NotNull
    public BukkitEventListener createListener() {
        return new BukkitEventListener(this);
    }

    @Override
    public void disablePlugin() {
        log(Level.INFO, "Disabling HuskHomes...");
        getServer().getPluginManager().disablePlugin(this);
    }

    @NotNull
    @Override
    public Version getPluginVersion() {
        return Version.fromString(getDescription().getVersion());
    }

    @Override
    @NotNull
    public String getServerType() {
        return String.format("%s/%s", getServer().getName(), getServer().getVersion());
    }

    @Override
    @NotNull
    public Version getMinecraftVersion() {
        return Version.fromString(getServer().getBukkitVersion());
    }

    @Override
    public boolean isDependencyAvailable(@NotNull String name) {
        return Bukkit.getPluginManager().getPlugin(name) != null;
    }

    @Override
    public Optional<Broker> getBroker() {
        return Optional.ofNullable(broker);
    }

    @Override
    public void closeBroker() {
        if (broker != null) {
            broker.close();
        }
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
        Objects.requireNonNull(Adapter.adapt(position).getWorld()).setSpawnLocation(Adapter.adapt(position));
    }

    @Override
    public void registerCommands(@NotNull List<Command> commands) {
        commands.stream().peek(this.commands::add)
                .map(c -> new BukkitCommand(c, getPlugin()))
                .forEach(BukkitCommand::register);
    }

    @Override
    public Optional<Spawn> getServerSpawn() {
        return Optional.ofNullable(serverSpawn);
    }

    @Override
    @NotNull
    public String getServerName() {
        return serverName != null ? serverName.getName() : "server";
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
    public void setupPluginMessagingChannels() {
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
        if (broker != null && broker instanceof PluginMessageBroker pluginMessenger &&
            getSettings().getCrossServer().getBrokerType() == Broker.Type.PLUGIN_MESSAGE) {
            pluginMessenger.onReceive(channel, getOnlineUser(player), message);
        }
    }

    @NotNull
    public GracefulScheduling getScheduler() {
        return morePaperLib.scheduling();
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

    @Override
    public void closeDatabase() {
        if (database != null) {
            database.close();
        }
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
