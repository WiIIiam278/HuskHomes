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

package net.william278.huskhomes.position;

import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a server warp.
 */
public class Warp extends SavedPosition {

    private static final String USE_PERMISSION_FORMAT = "huskhomes.warp.%s";
    private static final String USE_PERMISSION_WILDCARD = "huskhomes.warp.*";

    private Warp(double x, double y, double z, float yaw, float pitch, @NotNull World world, @NotNull String server,
                 @NotNull PositionMeta positionMeta, @NotNull UUID uuid) {
        super(x, y, z, yaw, pitch, world, server, positionMeta, uuid);
    }

    /**
     * Create a new {@link Warp} from a {@link Position} and {@link PositionMeta}.
     *
     * @param position The {@link Position} to save as a warp
     * @param meta     {@link PositionMeta} information about this position
     */
    private Warp(@NotNull Position position, @NotNull PositionMeta meta) {
        super(position, meta);
    }

    @NotNull
    public static Warp from(double x, double y, double z, float yaw, float pitch, @NotNull World world,
                            @NotNull String server, @NotNull PositionMeta positionMeta, @NotNull UUID uuid) {
        return new Warp(x, y, z, yaw, pitch, world, server, positionMeta, uuid);
    }

    @NotNull
    public static Warp from(@NotNull Position position, @NotNull PositionMeta meta) {
        return new Warp(position, meta);
    }

    @NotNull
    public Warp copy() {
        return new Warp(
                getX(), getY(), getZ(), getYaw(), getPitch(),
                getWorld(), getServer(), getMeta().copy(),
                getUuid()
        );
    }

    @NotNull
    public static String getPermission(@NotNull String warpName) {
        return USE_PERMISSION_FORMAT.formatted(warpName);
    }

    @NotNull
    public static String getWildcardPermission() {
        return USE_PERMISSION_WILDCARD;
    }

    @NotNull
    public String getPermission() {
        return Warp.getPermission(getName());
    }

    public boolean hasPermission(@NotNull CommandUser user) {
        return user.hasPermission(Warp.getWildcardPermission()) || user.hasPermission(getPermission());
    }

}
