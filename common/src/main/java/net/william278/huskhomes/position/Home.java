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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a home set by a {@link User}
 */
public class Home extends SavedPosition {

    public static final String IDENTIFIER_DELIMITER = ".";
    @Expose
    private User owner;
    @Expose
    @SerializedName("public")
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

    @NotNull
    public static Home from(double x, double y, double z, float yaw, float pitch, @NotNull World world, @NotNull String server,
                            @NotNull PositionMeta positionMeta, @NotNull UUID uuid, @NotNull User owner, boolean isPublic) {
        return new Home(x, y, z, yaw, pitch, world, server, positionMeta, uuid, owner, isPublic);
    }

    @NotNull
    public static Home from(@NotNull Position position, @NotNull PositionMeta meta, @NotNull User owner) {
        return new Home(position, meta, owner);
    }

    /**
     * The {@link User} who owns this home
     */
    @NotNull
    public User getOwner() {
        return owner;
    }

    /**
     * {@code true} if this home is public
     */
    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    @NotNull
    @Override
    public String getSafeIdentifier() {
        return getOwner().getUsername() + IDENTIFIER_DELIMITER + super.getSafeIdentifier();
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return getOwner().getUsername() + IDENTIFIER_DELIMITER + super.getIdentifier();
    }

}
