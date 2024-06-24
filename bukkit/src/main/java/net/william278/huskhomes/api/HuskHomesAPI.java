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

import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.config.Server;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.user.BukkitUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.User;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * The HuskHomes API implementation for the Bukkit platform, providing methods to access player data, homes, warps
 * and process teleports
 *
 * <p>Retrieve an instance of the API class via {@link #getInstance()}.
 *
 * @since 3.0
 */
@SuppressWarnings("unused")
public class HuskHomesAPI extends BaseHuskHomesAPI {

    /**
     * <b>(Internal use only)</b> - Constructor, instantiating the API.
     */
    @ApiStatus.Internal
    private HuskHomesAPI(@NotNull BukkitHuskHomes plugin) {
        super(plugin);
    }

    /**
     * Get an instance of the HuskHomes API.
     *
     * @return instance of the HuskHomes API
     * @throws NotRegisteredException if the API has not yet been registered.
     */
    @NotNull
    public static HuskHomesAPI getInstance() throws NotRegisteredException {
        return (HuskHomesAPI) BaseHuskHomesAPI.getInstance();
    }

    /**
     * <b>(Internal use only)</b> - Register the API for this platform.
     *
     * @param plugin the plugin instance
     */
    @ApiStatus.Internal
    public static void register(@NotNull BukkitHuskHomes plugin) {
        instance = new HuskHomesAPI(plugin);
    }

    /**
     * Returns an {@link OnlineUser} instance for the given Bukkit {@link Player}.
     *
     * @param player the Bukkit player to get the {@link User} instance for
     * @return the {@link OnlineUser} instance for the given Bukkit {@link Player}
     * @since 3.0
     */
    @NotNull
    public OnlineUser adaptUser(@NotNull Player player) {
        return BukkitUser.adapt(player, (BukkitHuskHomes) plugin);
    }

    /**
     * Returns the Bukkit {@link Player} being represented by the given {@link OnlineUser}.
     *
     * @param user {@link OnlineUser} to get the Bukkit player from
     * @return the Bukkit {@link Player} being represented by the given {@link OnlineUser}
     * @since 3.0
     */
    @NotNull
    public Player getPlayer(@NotNull OnlineUser user) {
        return ((BukkitUser) user).getPlayer();
    }

    /**
     * Returns the Bukkit {@link Location} being represented by the given {@link Position}.
     *
     * @param position the {@link Position} to get the Bukkit location from
     * @return the Bukkit {@link Location} being represented by the given {@link Position}
     * @since 3.0
     */
    @Nullable
    public org.bukkit.Location getLocation(@NotNull Position position) {
        return BukkitHuskHomes.Adapter.adapt(position);
    }

    /**
     * Returns a {@link Location} instance for the given Bukkit {@link Location} on the server.
     *
     * @param location the Bukkit location to get the {@link Location} instance for
     * @return the {@link Location} instance for the given Bukkit {@link Location}
     * @since 3.0
     */
    @Nullable
    public Location adaptLocation(@NotNull org.bukkit.Location location) {
        return BukkitHuskHomes.Adapter.adapt(location);
    }

    /**
     * Returns a {@link Position} instance for the given Bukkit {@link Location} on the server.
     *
     * @param location the Bukkit location to get the {@link Position} instance for
     * @param server   the {@link Server} the position is on
     * @return the {@link Position} instance for the given Bukkit {@link Location} on the given {@link Server}
     * @see Position#getServer() to get the server the position is on
     * @since 4.0
     */
    @NotNull
    public Position adaptPosition(@NotNull org.bukkit.Location location, @NotNull String server) {
        return Position.at(Objects.requireNonNull(adaptLocation(location)), server);
    }

    /**
     * Returns a {@link Position} instance for the given Bukkit {@link Location} on the server.
     *
     * @param location the Bukkit location to get the {@link Position} instance for
     * @return the {@link Position} instance for the given {@link Location} on the server
     * @since 4.0
     */
    @NotNull
    public Position adaptPosition(@NotNull org.bukkit.Location location) {
        return Position.at(Objects.requireNonNull(adaptLocation(location)), getServer());
    }

}
