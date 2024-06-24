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

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.william278.huskhomes.FabricHuskHomes;
import net.william278.huskhomes.config.Server;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.user.FabricUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * The HuskHomes API implementation for the Fabric platform, providing methods to access ServerPlayerEntity data,
 * homes, warps and process teleports
 *
 * <p>Retrieve an instance of the API class via {@link #getInstance()}.
 *
 * @since 4.7
 */
@SuppressWarnings("unused")
public class FabricHuskHomesAPI extends BaseHuskHomesAPI {

    /**
     * <b>(Internal use only)</b> - Constructor, instantiating the API.
     */
    @ApiStatus.Internal
    private FabricHuskHomesAPI(@NotNull FabricHuskHomes plugin) {
        super(plugin);
    }

    /**
     * Get an instance of the HuskHomes API.
     *
     * @return instance of the HuskHomes API
     * @throws NotRegisteredException if the API has not yet been registered.
     */
    @NotNull
    public static FabricHuskHomesAPI getInstance() throws NotRegisteredException {
        return (FabricHuskHomesAPI) BaseHuskHomesAPI.getInstance();
    }

    /**
     * <b>(Internal use only)</b> - Register the API for this platform.
     *
     * @param plugin the plugin instance
     */
    @ApiStatus.Internal
    public static void register(@NotNull FabricHuskHomes plugin) {
        instance = new FabricHuskHomesAPI(plugin);
    }

    /**
     * Returns an {@link OnlineUser} instance for the given Fabric {@link ServerPlayerEntity}.
     *
     * @param player the Fabric ServerPlayerEntity to get the {@link User} instance for
     * @return the {@link OnlineUser} instance for the given Fabric {@link ServerPlayerEntity}
     * @since 4.7
     */
    @NotNull
    public OnlineUser adaptUser(@NotNull ServerPlayerEntity player) {
        return FabricUser.adapt(player, (FabricHuskHomes) plugin);
    }

    /**
     * Returns the Fabric {@link ServerPlayerEntity} being represented by the given {@link OnlineUser}.
     *
     * @param user {@link OnlineUser} to get the Fabric ServerPlayerEntity from
     * @return the Fabric {@link ServerPlayerEntity} being represented by the given {@link OnlineUser}
     * @since 4.7
     */
    @NotNull
    public ServerPlayerEntity getServerPlayerEntity(@NotNull OnlineUser user) {
        return ((FabricUser) user).getPlayer();
    }

    /**
     * Returns the Fabric {@link Location} being represented by the given {@link Position}.
     *
     * @param position the {@link Position} to get the Fabric location from
     * @return the fabric {@link ServerWorld} being represented by the given {@link Position}
     * @since 4.7
     */
    @Nullable
    public ServerWorld getServerWorld(@NotNull World position) {
        return FabricHuskHomes.Adapter.adapt(position, ((FabricHuskHomes) plugin).getMinecraftServer());
    }

    /**
     * Returns the Fabric {@link TeleportTarget} being represented by the given {@link Location}.
     *
     * @param location      the {@link Location} to get the {@link TeleportTarget} from
     * @param afterTeleport the {@link Consumer} to run after the teleport completes
     * @return the {@link TeleportTarget} being represented by the given {@link Location}
     * @since 4.7
     */
    @Nullable
    public TeleportTarget getTeleportTarget(@NotNull Location location, @NotNull Consumer<Entity> afterTeleport) {
        return FabricHuskHomes.Adapter.adapt(location, ((FabricHuskHomes) plugin).getMinecraftServer(), afterTeleport);
    }

    /**
     * Returns the Fabric {@link TeleportTarget} being represented by the given {@link Location}.
     *
     * @param location the {@link Location} to get the {@link TeleportTarget} from
     * @return the {@link TeleportTarget} being represented by the given {@link Location}
     * @since 4.7
     */
    @Nullable
    public TeleportTarget getTeleportTarget(@NotNull Location location) {
        return getTeleportTarget(location, (entity) -> {
        });
    }

    /**
     * Returns a {@link Location} instance for the given Fabric {@link Location} on the server.
     *
     * @return the {@link Location} instance for the given Fabric {@link Location}
     * @since 4.7
     */
    @Nullable
    public Location adaptLocation(@NotNull Vec3d pos, @NotNull net.minecraft.world.World world,
                                  float yaw, float pitch) {
        return FabricHuskHomes.Adapter.adapt(pos, world, yaw, pitch);
    }

    /**
     * Returns a {@link Position} instance for the given Fabric {@link Location} on the server.
     *
     * @param pos    the {@link Vec3d} position to get the {@link Position} instance for
     * @param world  the world the position is on
     * @param yaw    the yaw of the position
     * @param pitch  the pitch of the position
     * @param server the {@link Server} the position is on
     * @return the {@link Position} instance for the given Fabric {@link Location} on the given {@link Server}
     * @see Position#getServer() to get the server the position is on
     * @since 4.7
     */
    @NotNull
    public Position adaptPosition(@NotNull Vec3d pos, @NotNull net.minecraft.world.World world,
                                  float yaw, float pitch, @NotNull String server) {
        return FabricHuskHomes.Adapter.adapt(pos, world, yaw, pitch, server);
    }

    /**
     * Returns a {@link Position} instance for the given Fabric {@link Location} on the server.
     *
     * @param pos   the {@link Vec3d} position to get the {@link Position} instance for
     * @param world the world the position is on
     * @param yaw   the yaw of the position
     * @param pitch the pitch of the position
     * @return the {@link Position} instance for the given Fabric {@link Location} on the given {@link Server}
     * @see Position#getServer() to get the server the position is on
     * @since 4.7
     */
    @NotNull
    public Position adaptPosition(@NotNull Vec3d pos, @NotNull net.minecraft.world.World world,
                                  float yaw, float pitch) {
        return FabricHuskHomes.Adapter.adapt(pos, world, yaw, pitch, getServer());
    }

}
