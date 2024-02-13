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

package net.william278.huskhomes.api;

import de.themoep.minedown.adventure.MineDown;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.random.RandomTeleportEngine;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportBuilder;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.util.TransactionResolver;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The base implementation of the HuskHomes API, containing cross-platform API calls.
 * </p>
 * This class should not be used directly, but rather through platform-specific extending API classes.
 */
@SuppressWarnings("unused")
public class BaseHuskHomesAPI {

    /**
     * <b>(Internal use only)</b> - API instance.
     */
    protected static BaseHuskHomesAPI instance;
    /**
     * <b>(Internal use only)</b> - Instance of the implementing plugin.
     */
    protected final HuskHomes plugin;

    /**
     * <b>(Internal use only)</b> - Constructor, instantiating the base API class.
     */
    @ApiStatus.Internal
    protected BaseHuskHomesAPI(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns saved {@link SavedUser} for the given player's account {@link UUID}, if they exist.
     *
     * @param uuid The {@link UUID} of the user to get data for.
     * @return The {@link SavedUser} of the user.
     * @since 3.0
     */
    public final CompletableFuture<Optional<SavedUser>> getUserData(@NotNull UUID uuid) {
        return plugin.supplyAsync(() -> plugin.getDatabase().getUserData(uuid));
    }

    /**
     * Returns saved {@link SavedUser} for the given player's username (case-insensitive), if they exist.
     *
     * @param username The username of the user to get data for.
     * @return The {@link SavedUser} of the user.
     * @since 3.0
     */
    public final CompletableFuture<Optional<SavedUser>> getUserData(@NotNull String username) {
        return plugin.supplyAsync(() -> plugin.getDatabase().getUserDataByName(username));
    }

    /**
     * Get the timestamp of when a user's cooldown for performing an {@link TransactionResolver.Action action} expires.
     *
     * @param user   The {@link User} to get data for.
     * @param action The {@link TransactionResolver.Action} to get the cooldown for
     * @return An {@link Optional} containing the {@link Instant} the cooldown expires, if there is one
     * @since 4.4
     */
    public final CompletableFuture<Optional<Instant>> getCooldown(@NotNull User user,
                                                                  @NotNull TransactionResolver.Action action) {
        return plugin.supplyAsync(() -> plugin.getDatabase().getCooldown(action, user));
    }

    /**
     * Set a cooldown for a user performing an {@link TransactionResolver.Action action}.
     *
     * @param user   The {@link User} to set data for.
     * @param action The {@link TransactionResolver.Action} to set the cooldown for
     * @param expiry The {@link Instant} the cooldown expires
     * @throws IllegalArgumentException if the expiry is in the past
     * @since 4.4
     */
    public final void setCooldown(@NotNull User user, @NotNull TransactionResolver.Action action,
                                  @NotNull Instant expiry) throws IllegalArgumentException {
        if (Instant.now().isAfter(expiry)) {
            throw new IllegalArgumentException("Cooldown expiry time must be in the future");
        }
        plugin.runAsync(() -> plugin.getDatabase().setCooldown(action, user, expiry));
    }

    /**
     * Set a cooldown for a user performing an {@link TransactionResolver.Action action}.
     *
     * @param user     The {@link User} to set data for.
     * @param action   The {@link TransactionResolver.Action} to set the cooldown for
     * @param duration The {@link Duration} the cooldown lasts for
     * @throws IllegalArgumentException if the duration is negative
     * @since 4.4
     */
    public final void setCooldown(@NotNull User user, @NotNull TransactionResolver.Action action,
                                  @NotNull Duration duration) throws IllegalArgumentException {
        this.setCooldown(user, action, Instant.now().plus(duration));
    }

    /**
     * Remove a cooldown for a user performing an {@link TransactionResolver.Action action}.
     *
     * @param user   The {@link User} to remove the cooldown for
     * @param action The {@link TransactionResolver.Action} to remove the cooldown for
     * @since 4.4
     */
    public final void removeCooldown(@NotNull User user, @NotNull TransactionResolver.Action action) {
        plugin.runAsync(() -> plugin.getDatabase().removeCooldown(action, user));
    }

    /**
     * Returns the last position, as used in the {@code /back} command, for this user.
     *
     * @param user The {@link User} to get the last position for
     * @return The user's last {@link Position}, if there is one
     * @since 3.0
     */
    public CompletableFuture<Optional<Position>> getUserLastPosition(@NotNull User user) {
        return plugin.supplyAsync(() -> plugin.getDatabase().getLastPosition(user));
    }

    /**
     * Set the last {@link Position}, as used in the {@code /back} command, for this user.
     *
     * @param user     The {@link User} to set the last position for
     * @param position The {@link Position} to set as the user's last position
     * @since 4.2.1
     */
    public void setUserLastPosition(@NotNull User user, @NotNull Position position) {
        plugin.runAsync(() -> plugin.getDatabase().setLastPosition(user, position));
    }

    /**
     * Returns where the user last disconnected from a server.
     *
     * @param user The {@link User} to get the last disconnect position for
     * @return The user's offline {@link Position}, if there is one
     * @since 3.0
     */
    public CompletableFuture<Optional<Position>> getUserOfflinePosition(@NotNull User user) {
        return plugin.supplyAsync(() -> plugin.getDatabase().getOfflinePosition(user));
    }

    /**
     * Returns where the user last set their spawn point by right-clicking a bed or respawn anchor.
     *
     * <p>The optional returned by this method will be empty unless HuskHomes is running cross-server mode
     * and global respawning is enabled
     *
     * @param user The {@link User} to get the respawn position of
     * @return The user's respawn {@link Position} if they have one, otherwise an empty {@link Optional}
     * @since 3.0
     */
    public CompletableFuture<Optional<Position>> getUserRespawnPosition(@NotNull User user) {
        final Settings.CrossServerSettings settings = plugin.getSettings().getCrossServer();
        if (!settings.isEnabled() || settings.isGlobalRespawning()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return plugin.supplyAsync(() -> plugin.getDatabase().getRespawnPosition(user));
    }

    /**
     * Returns if a user is currently warming up for a timed teleport; that is, they are in the state where they
     * must stand still and not take damage for the set number of seconds before a teleport can be executed.
     *
     * @param user The {@link User} to check
     * @return {@code true} if the user is currently warming up, otherwise {@code false}
     * @since 3.0.2
     */
    public boolean isUserWarmingUp(@NotNull User user) {
        return plugin.isWarmingUp(user.getUuid());
    }

    /**
     * Save {@link SavedUser} to the database, updating it if data for the user already exists,
     * or adding new user data if it doesn't.
     *
     * @param savedUser The {@link SavedUser} to save
     * @since 3.0
     */
    public final void saveUserData(@NotNull SavedUser savedUser) {
        plugin.runAsync(() -> plugin.getDatabase().updateUserData(savedUser));
    }

    /**
     * Edit {@link SavedUser} data for a given player by username, if they exist.
     *
     * @param username The username of the user to edit data for.
     * @param editor   A {@link Consumer} that will be passed the {@link SavedUser} to edit
     * @since 4.0
     */
    public final void editUserData(@NotNull String username, @NotNull Consumer<SavedUser> editor) {
        plugin.getSavedUsers().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst()
                .ifPresent(savedUser -> {
                    editor.accept(savedUser);
                    saveUserData(savedUser);
                });
    }

    /**
     * Edit {@link SavedUser} data for the given player's account {@link UUID}, if they exist.
     *
     * @param uuid   The {@link UUID} of the user to edit data for.
     * @param editor A {@link Consumer} that will be passed the {@link SavedUser} to edit
     * @since 4.0
     */
    public final void editUserData(@NotNull UUID uuid, @NotNull Consumer<SavedUser> editor) {
        plugin.getSavedUsers().stream()
                .filter(u -> u.getUserUuid().equals(uuid)).findFirst()
                .ifPresent(savedUser -> {
                    if (savedUser.getUser().getUuid().equals(uuid)) {
                        editor.accept(savedUser);
                        saveUserData(savedUser);
                    }
                });
    }

    /**
     * Get a list of {@link Home}s set by the given user.
     *
     * @param user The {@link User} to get the homes of
     * @return A {@link CompletableFuture} that will complete with a list of {@link Home}s set by the user
     */
    public final CompletableFuture<List<Home>> getUserHomes(@NotNull User user) {
        return plugin.supplyAsync(() -> plugin.getDatabase().getHomes(user));
    }

    /**
     * Get a list of {@link Home}s set by the given user that they have made public.
     *
     * @return A {@link CompletableFuture} with the user's public homes (where {@link Home#isPublic()})
     * @since 3.0
     */
    public final CompletableFuture<List<Home>> getUserPublicHomes(@NotNull User user) {
        return plugin.supplyAsync(() -> plugin.getDatabase().getHomes(user).stream()
                .filter(Home::isPublic)
                .collect(Collectors.toList()));
    }

    /**
     * Get a list of public homes local to this server.
     *
     * @param user The {@link User} to get the local public homes of
     * @return A {@link CompletableFuture} that will complete with a list of public {@link Home}s local to the server
     * @since 4.0
     */
    public final CompletableFuture<List<Home>> getLocalPublicHomes(@NotNull User user) {
        return plugin.supplyAsync(() -> plugin.getDatabase().getLocalPublicHomes(plugin));
    }

    /**
     * Get a list of homes that have been made public ("public homes").
     *
     * @return A {@link CompletableFuture} that will complete with a list of public {@link Home}s
     * @since 3.0
     */
    public final CompletableFuture<List<Home>> getPublicHomes() {
        return plugin.supplyAsync(() -> plugin.getDatabase().getPublicHomes());
    }

    /**
     * Get a {@link Home} from the database owned by a given {@link User} with the specified name.
     *
     * @param user     The {@link User} to get the home of
     * @param homeName The name of the home to get
     * @return A {@link CompletableFuture} with the {@link Home} if it exists, otherwise an empty {@link Optional}
     * @since 3.0
     */
    public final CompletableFuture<Optional<Home>> getHome(@NotNull User user, @NotNull String homeName) {
        return plugin.supplyAsync(() -> plugin.getDatabase().getHome(user, homeName));
    }


    /**
     * Get a {@link Home} from the database by its {@link UUID unique ID}.
     *
     * @param homeUuid The {@link UUID} of the home to get
     * @return A {@link CompletableFuture} with the {@link Home} if it exists, otherwise an empty {@link Optional}
     */
    public final CompletableFuture<Optional<Home>> getHome(@NotNull UUID homeUuid) {
        return plugin.supplyAsync(() -> plugin.getDatabase().getHome(homeUuid));
    }

    /**
     * Create a home for a given user with the specified name and position.
     * The returned future may complete exceptionally with a {@link net.william278.huskhomes.util.ValidationException}
     * if the home could not be created.
     *
     * @param owner    The {@link User} to create the home for
     * @param name     The name of the home
     * @param position The {@link Position} of the home
     * @return a {@link CompletableFuture} that will complete with the created {@link Home}.
     * @since 4.5
     */
    public CompletableFuture<Home> createHome(@NotNull User owner, @NotNull String name, @NotNull Position position) {
        return createHome(owner, name, position, false, false, false);
    }

    /**
     * Create a home for a given user with the specified name and position.
     * The returned future may complete exceptionally with a {@link net.william278.huskhomes.util.ValidationException}
     * if the home could not be created.
     *
     * @param owner              The {@link User} to create the home for
     * @param name               The name of the home
     * @param position           The {@link Position} of the home
     * @param overwrite          Whether to overwrite an existing home with the same name
     * @param buyAdditionalSlots Whether to buy additional home slots if the user has reached their maximum
     * @param ignoreMaxHomes     Whether to ignore the maximum number of homes a user can have
     * @return a {@link CompletableFuture} that will complete with the created {@link Home}.
     * @since 4.5.1
     */
    public CompletableFuture<Home> createHome(@NotNull User owner, @NotNull String name, @NotNull Position position,
                                              boolean overwrite, boolean buyAdditionalSlots, boolean ignoreMaxHomes) {
        return plugin.supplyAsync(() -> plugin.getManager().homes().createHome(
                owner, name, position, overwrite, buyAdditionalSlots, ignoreMaxHomes
        ));
    }

    /**
     * Delete a home for a given user with the specified name.
     *
     * @param owner The {@link User} to delete the home for
     * @param name  The name of the home
     * @since 4.0
     */
    public final void deleteHome(@NotNull User owner, @NotNull String name) {
        plugin.runAsync(() -> plugin.getManager().homes().deleteHome(owner, name));
    }

    /**
     * Delete a home.
     *
     * @param home The {@link Home} to delete
     * @since 4.0
     */
    public final void deleteHome(@NotNull Home home) {
        plugin.runAsync(() -> plugin.getManager().homes().deleteHome(home));
    }

    /**
     * Rename a home for a given user.
     *
     * @param owner   The {@link User} to rename the home for
     * @param oldName The name of the home to rename
     * @param newName The new name of the home
     * @since 4.0
     */
    public final void renameHome(@NotNull User owner, @NotNull String oldName, @NotNull String newName) {
        plugin.runAsync(() -> plugin.getManager().homes().setHomeName(owner, oldName, newName));
    }

    /**
     * Rename a home.
     *
     * @param home    The {@link Home} to rename
     * @param newName The new name of the home
     * @since 4.0
     */
    public final void renameHome(@NotNull Home home, @NotNull String newName) {
        plugin.runAsync(() -> plugin.getManager().homes().setHomeName(home, newName));
    }

    /**
     * Set the privacy of a home.
     *
     * @param owner    The {@link User} to set the privacy of the home for
     * @param name     The name of the home
     * @param isPublic Whether the home should be public or not
     * @since 4.0
     */
    public final void setHomePrivacy(@NotNull User owner, @NotNull String name, boolean isPublic) {
        plugin.runAsync(() -> plugin.getManager().homes().setHomePrivacy(owner, name, isPublic));
    }

    /**
     * Set the privacy of a home.
     *
     * @param home     The {@link Home} to set the privacy of
     * @param isPublic Whether the home should be public or not
     * @since 4.0
     */
    public final void setHomePrivacy(@NotNull Home home, boolean isPublic) {
        plugin.runAsync(() -> plugin.getManager().homes().setHomePrivacy(home, isPublic));
    }

    /**
     * Set the description of a home.
     *
     * @param owner       The {@link User} to set the description of the home for
     * @param name        The name of the home
     * @param description The description of the home
     * @since 4.0
     */
    public final void setHomeDescription(@NotNull User owner, @NotNull String name, @NotNull String description) {
        plugin.runAsync(() -> plugin.getManager().homes().setHomeDescription(owner, name, description));
    }

    /**
     * Set the description of a home.
     *
     * @param home        The {@link Home} to set the description of
     * @param description The description of the home
     * @since 4.0
     */
    public final void setHomeDescription(@NotNull Home home, @NotNull String description) {
        plugin.runAsync(() -> plugin.getManager().homes().setHomeDescription(home, description));
    }

    /**
     * Relocate a home for a given user.
     *
     * @param owner    The {@link User} to relocate the home for
     * @param name     The name of the home
     * @param position The new {@link Position} of the home
     * @since 4.0
     */
    public final void relocateHome(@NotNull User owner, @NotNull String name, @NotNull Position position) {
        plugin.runAsync(() -> plugin.getManager().homes().setHomePosition(owner, name, position));
    }

    /**
     * Relocate a home.
     *
     * @param home     The {@link Home} to relocate
     * @param position The new {@link Position} of the home
     * @since 4.0
     */
    public final void relocateHome(@NotNull Home home, @NotNull Position position) {
        plugin.runAsync(() -> plugin.getManager().homes().setHomePosition(home, position));
    }


    /**
     * Set the meta tags of a home.
     *
     * @param owner The {@link User} to set the meta tags of the home for
     * @param name  The name of the home
     * @param tags  The meta tags to set
     * @since 4.0
     */
    public final void setHomeMetaTags(@NotNull User owner, @NotNull String name, @NotNull Map<String, String> tags) {
        plugin.runAsync(() -> plugin.getManager().homes().setHomeMetaTags(owner, name, tags));
    }

    /**
     * Set the meta tags of a home.
     *
     * @param home The {@link Home} to set the meta tags of
     * @param tags The meta tags to set
     * @since 4.0
     */
    public final void setHomeMetaTags(@NotNull Home home, @NotNull Map<String, String> tags) {
        plugin.runAsync(() -> plugin.getManager().homes().setHomeMetaTags(home, tags));
    }

    /**
     * Edit the meta tags of a home.
     *
     * @param owner     The {@link User} to edit the meta tags of the home for
     * @param name      The name of the home
     * @param tagEditor The {@link Consumer} to edit the meta tags with
     * @since 4.0
     */
    public final void editHomeMetaTags(@NotNull User owner, @NotNull String name,
                                       @NotNull Consumer<Map<String, String>> tagEditor) {
        plugin.runAsync(() -> plugin.getDatabase().getHome(owner, name).ifPresent(home -> {
            final Map<String, String> tags = home.getMeta().getTags();
            tagEditor.accept(tags);
            setHomeMetaTags(home, tags);
        }));
    }

    /**
     * Get a list of {@link Warp}s local to this server.
     *
     * @return A {@link CompletableFuture} that will complete with a list of {@link Warp}s local to the server
     * @since 4.0
     */
    public final CompletableFuture<List<Warp>> getLocalWarps() {
        return plugin.supplyAsync(() -> plugin.getDatabase().getLocalWarps(plugin));
    }

    /**
     * Get a list of {@link Warp}s.
     *
     * @return A {@link CompletableFuture} that will complete with a list of {@link Warp}s
     * @since 3.0
     */
    public final CompletableFuture<List<Warp>> getWarps() {
        return plugin.supplyAsync(() -> plugin.getDatabase().getWarps());
    }

    /**
     * Get a {@link Warp} from the database with the specified name.
     *
     * @param warpName The name of the warp to get
     * @return A {@link CompletableFuture} with the {@link Warp} if it exists, otherwise an empty {@link Optional}
     * @since 3.0
     */
    public final CompletableFuture<Optional<Warp>> getWarp(@NotNull String warpName) {
        return plugin.supplyAsync(() -> plugin.getDatabase().getWarp(warpName));
    }

    /**
     * Get a {@link Warp} from the database by its {@link UUID unique ID}.
     *
     * @param warpUuid The {@link UUID} of the warp to get
     * @return A {@link CompletableFuture} with the {@link Warp} if it exists, otherwise an empty {@link Optional}
     */
    public final CompletableFuture<Optional<Warp>> getWarp(@NotNull UUID warpUuid) {
        return plugin.supplyAsync(() -> plugin.getDatabase().getWarp(warpUuid));
    }

    /**
     * Create a new {@link Warp} with the given name and {@link Position}.
     *
     * @param name     The name of the warp
     * @param position The {@link Position} of the warp
     * @return A {@link CompletableFuture} that will complete with the created {@link Warp}
     * @since 4.5
     */
    public final CompletableFuture<Warp> createWarp(@NotNull String name, @NotNull Position position) {
        return plugin.supplyAsync(() -> plugin.getManager().warps().createWarp(name, position));
    }

    /**
     * Delete a {@link Warp} with the given name.
     *
     * @param name The name of the warp to delete
     */
    public final void deleteWarp(@NotNull String name) {
        plugin.runAsync(() -> plugin.getManager().warps().deleteWarp(name));
    }

    /**
     * Delete a {@link Warp}.
     *
     * @param warp The {@link Warp} to delete
     */
    public final void deleteWarp(@NotNull Warp warp) {
        plugin.runAsync(() -> plugin.getManager().warps().deleteWarp(warp));
    }

    /**
     * Rename a {@link Warp} by name.
     *
     * @param oldName The name of the warp to rename
     * @param newName The new name of the warp
     */
    public final void renameWarp(@NotNull String oldName, @NotNull String newName) {
        plugin.runAsync(() -> plugin.getManager().warps().setWarpName(oldName, newName));
    }

    /**
     * Rename a {@link Warp}.
     *
     * @param warp    The {@link Warp} to rename
     * @param newName The new name of the warp
     */
    public final void renameWarp(@NotNull Warp warp, @NotNull String newName) {
        plugin.runAsync(() -> plugin.getManager().warps().setWarpName(warp, newName));
    }

    /**
     * Set the description of a {@link Warp} by name.
     *
     * @param name        The name of the warp to set the description of
     * @param description The new description of the warp
     */
    public final void setWarpDescription(@NotNull String name, @NotNull String description) {
        plugin.runAsync(() -> plugin.getManager().warps().setWarpDescription(name, description));
    }

    /**
     * Set the description of a {@link Warp}.
     *
     * @param warp        The {@link Warp} to set the description of
     * @param description The new description of the warp
     */
    public final void setWarpDescription(@NotNull Warp warp, @NotNull String description) {
        plugin.runAsync(() -> plugin.getManager().warps().setWarpDescription(warp, description));
    }

    /**
     * Set the {@link Position} of a {@link Warp} by name.
     *
     * @param name     The name of the warp to set the {@link Position} of
     * @param position The new {@link Position} of the warp
     */
    public final void relocateWarp(@NotNull String name, @NotNull Position position) {
        plugin.runAsync(() -> plugin.getManager().warps().setWarpPosition(name, position));
    }

    /**
     * Set the {@link Position} of a {@link Warp}.
     *
     * @param warp     The {@link Warp} to set the {@link Position} of
     * @param position The new {@link Position} of the warp
     */
    public final void relocateWarp(@NotNull Warp warp, @NotNull Position position) {
        plugin.runAsync(() -> plugin.getManager().warps().setWarpPosition(warp, position));
    }

    /**
     * Set the meta tags of a {@link Warp} by name.
     *
     * @param name The name of the warp to set the meta tags of
     * @param tags The new meta tags of the warp
     * @since 4.0
     */
    public final void setWarpMetaTags(@NotNull String name, @NotNull Map<String, String> tags) {
        plugin.runAsync(() -> plugin.getManager().warps().setWarpMetaTags(name, tags));
    }

    /**
     * Set the meta tags of a {@link Warp}.
     *
     * @param warp The {@link Warp} to set the meta tags of
     * @param tags The new meta tags of the warp
     * @since 4.0
     */
    public final void setWarpMetaTags(@NotNull Warp warp, @NotNull Map<String, String> tags) {
        plugin.runAsync(() -> plugin.getManager().warps().setWarpMetaTags(warp, tags));
    }

    /**
     * Edit the meta tags of a {@link Warp} by name.
     *
     * @param name      The name of the warp to edit the meta tags of
     * @param tagEditor A {@link Consumer} that will be passed the current meta tags of the warp
     *                  and should edit them in-place
     * @since 4.0
     */
    public final void editWarpMetaTag(@NotNull String name, @NotNull Consumer<Map<String, String>> tagEditor) {
        plugin.runAsync(() -> plugin.getDatabase().getWarp(name).ifPresent(warp -> {
            final Map<String, String> tags = warp.getMeta().getTags();
            tagEditor.accept(tags);
            setWarpMetaTags(warp, tags);
        }));
    }


    /**
     * Get the canonical {@link Position} of the spawn point.
     *
     * <p>Note that if cross-server and global spawn are enabled in the config,
     * this may not return a position on this server.
     *
     * @return A {@link CompletableFuture} that will complete with the {@link Position} of the spawn point
     */
    public final CompletableFuture<Optional<? extends Position>> getSpawn() {
        return plugin.supplyAsync(plugin::getSpawn);
    }

    /**
     * Get the maximum number of set homes a given {@link OnlineUser} can make.
     *
     * @param user The {@link OnlineUser} to get the maximum number of homes for
     * @return The maximum number of homes the user can set
     * @since 3.0
     */
    public final int getMaxHomeSlots(@NotNull OnlineUser user) {
        return plugin.getManager().homes().getMaxHomes(user);
    }

    /**
     * Get the number of homes an {@link OnlineUser} can set for free.
     *
     * <p>This is irrelevant unless the server is using economy features with HuskHomes.
     *
     * @param user The {@link OnlineUser} to get the number of free home slots for
     * @return The number of homes the user can set for free
     * @since 3.0
     */
    public final int getFreeHomeSlots(@NotNull OnlineUser user) {
        return plugin.getManager().homes().getFreeHomes(user);
    }

    /**
     * Get the number of homes an {@link OnlineUser} can make public.
     *
     * @param user The {@link OnlineUser} to get the number of public home slots for
     * @return The number of homes the user can make public
     * @since 3.0
     */
    public final int getMaxPublicHomeSlots(@NotNull OnlineUser user) {
        return plugin.getManager().homes().getMaxPublicHomes(user);
    }

    /**
     * Get a {@link TeleportBuilder} to construct and executeTeleport a (timed) teleport.
     *
     * @param teleporter The {@link OnlineUser} to teleport
     * @return A {@link TeleportBuilder} to construct and executeTeleport a (timed) teleport
     * @since 4.0
     */
    @NotNull
    public final TeleportBuilder teleportBuilder(@NotNull OnlineUser teleporter) {
        return teleportBuilder().teleporter(teleporter);
    }

    /**
     * Get a {@link TeleportBuilder} to construct and executeTeleport a (timed) teleport.
     *
     * @return A {@link TeleportBuilder} to construct and executeTeleport a (timed) teleport
     * @since 4.0
     */
    @NotNull
    public final TeleportBuilder teleportBuilder() {
        return Teleport.builder(plugin);
    }


    /**
     * Attempt to teleport an {@link OnlineUser} to a randomly generated {@link Position}. The {@link Position} will be
     * generated by the current {@link RandomTeleportEngine}.
     *
     * @param user          The {@link OnlineUser} to teleport
     * @param timedTeleport Whether the teleport should be timed or not (requiring a warmup where they must stand still
     *                      for a period of time)
     * @param rtpArgs       Arguments that will be passed to the implementing {@link RandomTeleportEngine}
     * @since 3.0
     */
    public final void randomlyTeleportPlayer(@NotNull OnlineUser user, boolean timedTeleport,
                                             @NotNull String... rtpArgs) {
        plugin.getRandomTeleportEngine()
                .getRandomPosition(user.getPosition().getWorld(), rtpArgs)
                .thenAccept(position -> {
                    if (position.isEmpty()) {
                        throw new IllegalStateException("Random teleport engine returned an empty position");
                    }

                    Teleport.builder(plugin)
                            .teleporter(user)
                            .target(position.get())
                            .buildAndComplete(timedTeleport);
                }).exceptionally(e -> {
                    throw new IllegalStateException("Random teleport engine threw an exception", e);
                });
    }

    /**
     * Attempt to teleport an {@link OnlineUser} to a randomly generated {@link Position}. The {@link Position} will be
     * generated by the current {@link RandomTeleportEngine}.
     *
     * @param user The {@link OnlineUser} to teleport
     * @since 3.0
     */
    public final void randomlyTeleportPlayer(@NotNull OnlineUser user) {
        this.randomlyTeleportPlayer(user, false);
    }

    /**
     * Set the {@link RandomTeleportEngine} to use for processing random teleports.
     *
     * <p>The engine will be used to process all random {@link Teleport teleports},
     * including both {@code /rtp} command executions and {@link #randomlyTeleportPlayer(OnlineUser) API calls}.
     *
     * @param randomTeleportEngine the {@link RandomTeleportEngine} to use to process random teleports
     * @see RandomTeleportEngine
     * @since 3.0
     */
    public final void setRandomTeleportEngine(@NotNull RandomTeleportEngine randomTeleportEngine) {
        plugin.setRandomTeleportEngine(randomTeleportEngine);
    }

    /**
     * Get a {@link MineDown}-formatted locale by key from the plugin {@link Locales} file.
     *
     * @param localeKey    The key of the locale to get
     * @param replacements Replacement strings to apply to the locale
     * @return The {@link MineDown}-formatted locale
     * @apiNote Since v3.0.4, this returns MineDown-<a href="https://docs.adventure.kyori.net/">Adventure</a> components
     * @since 3.0
     */
    public final Optional<MineDown> getLocale(@NotNull String localeKey, @NotNull String... replacements) {
        return plugin.getLocales().getLocale(localeKey, replacements);
    }

    /**
     * Get a raw locale string by key from the plugin {@link Locales} file.
     *
     * @param localeKey    The key of the locale to get
     * @param replacements Replacement strings to apply to the locale
     * @return The raw locale string
     * @since 3.0
     */
    public final Optional<String> getRawLocale(@NotNull String localeKey, @NotNull String... replacements) {
        return plugin.getLocales().getRawLocale(localeKey, replacements);
    }

    /**
     * Get an instance of the HuskHomes API.
     *
     * @return instance of the HuskHomes API
     * @throws NotRegisteredException if the API has not yet been registered.
     * @since 1.0
     */
    @NotNull
    public static BaseHuskHomesAPI getInstance() throws NotRegisteredException {
        if (instance == null) {
            throw new NotRegisteredException();
        }
        return instance;
    }

    /**
     * <b>(Internal use only)</b> - Unregister the API instance.
     *
     * @since 1.0
     */
    @ApiStatus.Internal
    public static void unregister() {
        instance = null;
    }

    /**
     * An exception indicating the plugin has been accessed before it has been registered.
     */
    public static final class NotRegisteredException extends IllegalStateException {

        private static final String MESSAGE = """
                Could not access the HuskHomes API as it has not yet been registered. This could be because:
                1) HuskHomes has failed to enable successfully
                2) Your plugin isn't set to load after HuskHomes has
                   (Check if it set as a (soft)depend in plugin.yml or to load: BEFORE in paper-plugin.yml?)
                3) You are attempting to access HuskHomes on plugin construction/before your plugin has enabled.""";

        NotRegisteredException() {
            super(MESSAGE);
        }

    }


}
