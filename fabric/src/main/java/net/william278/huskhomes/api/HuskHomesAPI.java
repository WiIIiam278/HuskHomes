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

import net.minecraft.server.network.ServerPlayerEntity;
import net.william278.huskhomes.FabricHuskHomes;
import net.william278.huskhomes.user.FabricUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * The HuskHomes API implementation for the Bukkit platform, providing methods to access player data, homes, warps
 * and process teleports
 *
 * <p>Retrieve an instance of the API class via {@link #getInstance()}.
 */
@SuppressWarnings("unused")
public class HuskHomesAPI extends BaseHuskHomesAPI {

    // Instance of the plugin
    private static HuskHomesAPI instance;
    private FabricHuskHomes plugin;


    /**
     * <b>(Internal use only)</b> - Constructor, instantiating the API.
     */
    @ApiStatus.Internal
    private HuskHomesAPI(@NotNull FabricHuskHomes plugin) {
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
        if (instance == null) {
            throw new NotRegisteredException();
        }
        return instance;
    }

    public FabricHuskHomes getPlugin() {
        return plugin;
    }

    /**
     * <b>(Internal use only)</b> - Register the API for this platform.
     *
     * @param plugin the plugin instance
     */
    @ApiStatus.Internal
    public static void register(@NotNull FabricHuskHomes plugin) {
        instance = new HuskHomesAPI(plugin);
    }

    /**
     * <b>(Internal use only)</b> - Unregister the API for this platform.
     */
    @ApiStatus.Internal
    public static void unregister() {
        instance = null;
    }

    /**
     * Returns an {@link OnlineUser} instance for the given bukkit {@link ServerPlayerEntity}.
     *
     * @param player the bukkit player to get the {@link User} instance for
     * @return the {@link OnlineUser} instance for the given bukkit {@link ServerPlayerEntity}
     * @since 3.0
     */
    @NotNull
    public OnlineUser adaptUser(@NotNull ServerPlayerEntity player) {
        return FabricUser.adapt(player, plugin);
    }

    /**
     * Returns the bukkit {@link ServerPlayerEntity} being represented by the given {@link OnlineUser}.
     *
     * @param user {@link OnlineUser} to get the bukkit player from
     * @return the bukkit {@link ServerPlayerEntity} being represented by the given {@link OnlineUser}
     * @since 3.0
     */
    @NotNull
    public ServerPlayerEntity getPlayer(@NotNull OnlineUser user) {
        return ((FabricUser) user).getPlayer();
    }

    /**
     * Get the name of this server.
     *
     * @return the server name
     * @since 4.0
     */
    @NotNull
    public String getServer() {
        return plugin.getServerName();
    }

}
