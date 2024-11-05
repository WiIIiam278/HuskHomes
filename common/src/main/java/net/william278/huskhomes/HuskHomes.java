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

import net.kyori.adventure.key.Key;
import net.william278.huskhomes.api.BaseHuskHomesAPI;
import net.william278.huskhomes.command.Command;
import net.william278.huskhomes.command.CommandProvider;
import net.william278.huskhomes.config.ConfigProvider;
import net.william278.huskhomes.config.Server;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.database.DatabaseProvider;
import net.william278.huskhomes.event.EventDispatcher;
import net.william278.huskhomes.hook.HookProvider;
import net.william278.huskhomes.hook.PluginHook;
import net.william278.huskhomes.listener.ListenerProvider;
import net.william278.huskhomes.manager.ManagerProvider;
import net.william278.huskhomes.network.BrokerProvider;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.random.RandomTeleportProvider;
import net.william278.huskhomes.user.ConsoleUser;
import net.william278.huskhomes.user.UserProvider;
import net.william278.huskhomes.util.*;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Represents a cross-platform instance of the plugin.
 */
public interface HuskHomes extends Task.Supplier, EventDispatcher, SavePositionProvider, TransactionResolver,
        ConfigProvider, DatabaseProvider, BrokerProvider, MetaProvider, HookProvider, RandomTeleportProvider,
        AudiencesProvider, UserProvider, TextValidator, ManagerProvider, ListenerProvider, CommandProvider,
        GsonProvider {

    int BSTATS_BUKKIT_PLUGIN_ID = 8430;

    /**
     * Load plugin systems.
     *
     * @since 4.8
     */
    default void load() {
        try {
            loadConfigs();
            loadHooks();
            registerHooks(PluginHook.Register.ON_LOAD);
        } catch (Throwable e) {
            log(Level.SEVERE, "An error occurred whilst loading HuskHomes", e);
            disablePlugin();
            return;
        }
        log(Level.INFO, String.format("Successfully loaded HuskHomes v%s", getPluginVersion()));
    }

    /**
     * Enable all plugin systems.
     *
     * @since 4.8
     */
    default void enable() {
        try {
            loadDatabase();
            loadBroker();
            loadManager();
            loadRandomTeleportEngine();
            loadListeners();
            registerHooks(PluginHook.Register.ON_ENABLE);
            loadAPI();
            if (getPluginVersion().getMetadata().isBlank()) {
                loadMetrics();
            }
        } catch (Throwable e) {
            log(Level.SEVERE, "An error occurred whilst enabling HuskHomes", e);
            disablePlugin();
            return;
        }
        log(Level.INFO, String.format("Successfully enabled HuskHomes v%s", getPluginVersion()));
        checkForUpdates();
    }

    /**
     * Shutdown plugin subsystems.
     *
     * @since 4.8
     */
    default void shutdown() {
        log(Level.INFO, String.format("Disabling HuskHomes v%s...", getPluginVersion()));
        try {
            unloadHooks();
            closeDatabase();
            closeBroker();
            cancelTasks();
            unloadAPI();
        } catch (Throwable e) {
            log(Level.SEVERE, "An error occurred whilst disabling HuskHomes", e);
        }
        log(Level.INFO, String.format("Successfully disabled HuskHomes v%s", getPluginVersion()));
    }

    /**
     * Register the API instance.
     *
     * @since 4.8
     */
    void loadAPI();

    /**
     * Unregister the API instance.
     *
     * @since 4.8
     */
    default void unloadAPI() {
        BaseHuskHomesAPI.unregister();
    }

    /**
     * Load plugin metrics.
     *
     * @since 4.8
     */
    void loadMetrics();

    /**
     * Disable the plugin.
     *
     * @since 4.8
     */
    void disablePlugin();

    /**
     * Get the user representing the server console.
     *
     * @return the {@link ConsoleUser}
     */
    @NotNull
    ConsoleUser getConsole();

    /**
     * The canonical spawn {@link Position} of this server, if it has been set.
     *
     * @return the {@link Position} of the spawn, or an empty {@link Optional} if it has not been set
     */
    default Optional<Position> getSpawn() {
        final Settings.CrossServerSettings crossServer = getSettings().getCrossServer();
        return crossServer.isEnabled() && crossServer.getGlobalSpawn().isEnabled()
                ? getDatabase().getWarp(crossServer.getGlobalSpawn().getWarpName()).map(warp -> (Position) warp)
                : getServerSpawn().map(spawn -> spawn.getPosition(getServerName()));
    }

    /**
     * Update the spawn position of a world on the server.
     *
     * @param position The new spawn world and coordinates.
     */
    void setWorldSpawn(@NotNull Position position);

    void setServerName(@NotNull Server serverName);

    /**
     * Returns a resource read from the plugin resources folder.
     *
     * @param name the name of the resource
     * @return the resource read as an {@link InputStream}
     */
    @Nullable
    InputStream getResource(@NotNull String name);

    /**
     * Returns a list of worlds on the server.
     *
     * @return a list of worlds on the server
     */
    @NotNull
    List<World> getWorlds();

    /**
     * Returns a list of enabled commands.
     *
     * @return A list of registered and enabled {@link Command}s
     */
    @NotNull
    List<Command> getCommands();

    default <T extends Command> Optional<T> getCommand(@NotNull Class<T> type) {
        return getCommands().stream()
                .filter(command -> command.getClass() == type)
                .findFirst()
                .map(type::cast);
    }

    @NotNull
    Set<UUID> getCurrentlyOnWarmup();

    /**
     * Returns if the given user is currently warming up to teleport to a home.
     *
     * @param userUuid The user to check.
     * @return If the user is currently warming up.
     */
    default boolean isWarmingUp(@NotNull UUID userUuid) {
        return this.getCurrentlyOnWarmup().contains(userUuid);
    }

    /**
     * Log a message to the console.
     *
     * @param level      the level to log at
     * @param message    the message to log
     * @param exceptions any exceptions to log
     */
    void log(@NotNull Level level, @NotNull String message, Throwable... exceptions);

    /**
     * Create a resource key namespaced with the plugin id.
     *
     * @param data the string ID elements to join
     * @return the key
     */
    @NotNull
    default Key getKey(@NotNull String... data) {
        if (data.length == 0) {
            throw new IllegalArgumentException("Cannot create a key with no data");
        }
        @Subst("foo") final String joined = String.join("/", data);
        return Key.key("huskhomes", joined);
    }

}
