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

import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a home set by a {@link User}.
 */
public class Home extends SavedPosition {

    // The delimiter used to separate the owner's name from the home name in the home identifier.
    private static String delimiter = ".";

    private final User owner;
    private boolean isPublic;

    private Home(double x, double y, double z, float yaw, float pitch, @NotNull World world, @NotNull String server,
                 @NotNull PositionMeta positionMeta, @NotNull UUID uuid, @NotNull User owner, boolean isPublic) {
        super(x, y, z, yaw, pitch, world, server, positionMeta, uuid);
        this.owner = owner;
        this.setPublic(isPublic);
    }

    private Home(@NotNull Position position, @NotNull PositionMeta meta, @NotNull User owner) {
        super(position, meta);
        this.owner = owner;
        this.setPublic(false);
    }

    @ApiStatus.Internal
    public static void setDelimiter(@NotNull String delimiter) {
        Home.delimiter = delimiter;
    }

    @NotNull
    @ApiStatus.Internal
    public static String getDelimiter() {
        return delimiter;
    }

    /**
     * Creates a new {@link Home} from a series of coordinates, a viewing angle, a {@link World}, a server name, a
     * {@link PositionMeta}, a {@link UUID}, a {@link User} and a boolean representing whether the home is public.
     *
     * @param x            The x coordinate of the home
     * @param y            The y coordinate of the home
     * @param z            The z coordinate of the home
     * @param yaw          The yaw of the home
     * @param pitch        The pitch of the home
     * @param world        The {@link World} the home is in
     * @param server       The name of the server the home is in
     * @param positionMeta The {@link PositionMeta} of the home
     * @param uuid         The {@link UUID} of the home
     * @param owner        The {@link User} who owns the home
     * @param isPublic     Whether the home is public
     * @return the new {@link Home}
     */
    @NotNull
    public static Home from(double x, double y, double z, float yaw, float pitch, @NotNull World world,
                            @NotNull String server, @NotNull PositionMeta positionMeta, @NotNull UUID uuid,
                            @NotNull User owner, boolean isPublic) {
        return new Home(x, y, z, yaw, pitch, world, server, positionMeta, uuid, owner, isPublic);
    }

    /**
     * Creates a new {@link Home} from a {@link Position} and {@link PositionMeta}.
     *
     * @param position The {@link Position} to create the home from
     * @param meta     The {@link PositionMeta} to create the home from
     * @param owner    The {@link User} who owns the home
     * @return the new {@link Home}
     */
    @NotNull
    public static Home from(@NotNull Position position, @NotNull PositionMeta meta, @NotNull User owner) {
        return new Home(position, meta, owner);
    }

    @NotNull
    public Home copy() {
        return Home.from(
                getX(), getY(), getZ(), getYaw(), getPitch(),
                getWorld(), getServer(), getMeta().copy(),
                getUuid(), getOwner(), isPublic()
        );
    }

    /**
     * Get the {@link User} who owns this home.
     *
     * @return the {@link User} who owns this home
     */
    @NotNull
    public User getOwner() {
        return owner;
    }

    /**
     * Returns whether this home is public.
     *
     * @return {@code true} if this home is public.
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * Sets whether this home is public.
     *
     * @param isPublic {@code true} if this home is public
     */
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * Get a version of the canonical home identifier string that is safe for use in formatted locales.
     * Safe identifiers will only contain alphanumeric ASCII characters (a-z, A-Z, 0-9), underscores and dashes.
     *
     * <p>This typically consists of the owner's username, a delimiter, and the home {@link #getUuid() UUID}.
     *
     * @return the locale-safe canonical home identifier string
     */
    @NotNull
    @Override
    public String getSafeIdentifier() {
        return getOwner().getName() + getDelimiter() + super.getSafeIdentifier();
    }

    /**
     * Get the canonical home identifier string.
     *
     * <p>This typically consists of the owner's username, a delimiter, and the home {@link #getName() name}.
     *
     * @return the canonical home identifier string
     * @see #getSafeIdentifier() #getSafeIdentifier() to get a locale-safe version of this string
     */
    @NotNull
    @Override
    public String getIdentifier() {
        return getOwner().getName() + getDelimiter() + super.getIdentifier();
    }

}
