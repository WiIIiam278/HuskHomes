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

import net.william278.huskhomes.config.Locales;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a persistently-saved {@link Position}.
 */
public abstract class SavedPosition extends Position implements Comparable<SavedPosition> {

    private PositionMeta meta;
    private final UUID uuid;

    protected SavedPosition(double x, double y, double z, float yaw, float pitch, @NotNull World world,
                            @NotNull String server, @NotNull PositionMeta meta, @NotNull UUID uuid) {
        super(x, y, z, yaw, pitch, world, server);
        this.setMeta(meta);
        this.uuid = uuid;
    }

    /**
     * Create a new {@link SavedPosition} from a {@link Position} and {@link PositionMeta}.
     *
     * @param position The {@link Position} to save
     * @param meta     {@link PositionMeta} information about this position
     */
    protected SavedPosition(@NotNull Position position, @NotNull PositionMeta meta) {
        super(position.getX(), position.getY(), position.getZ(), position.getYaw(), position.getPitch(),
                position.getWorld(), position.getServer());
        this.setMeta(meta);
        this.uuid = UUID.randomUUID();
    }

    /**
     * Metadata about this position (name, description).
     */
    @NotNull
    public PositionMeta getMeta() {
        return meta;
    }

    public void setMeta(@NotNull PositionMeta meta) {
        this.meta = meta;
    }

    /**
     * The name of this position. Shortcut for {@link #getMeta()}.{@link PositionMeta#getName()}.
     */
    @NotNull
    public String getName() {
        return getMeta().getName();
    }

    /**
     * A unique ID representing this position.
     */
    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Get the identifier for this position. This is the name, unless the name contains
     * characters that are not allowed in a command argument, in which case it is the UUID.
     *
     * @return The identifier for this position
     */
    @NotNull
    public String getSafeIdentifier() {
        return Locales.escapeText(getName()).equals(getName()) ? getName() : getUuid().toString();
    }

    @NotNull
    public String getIdentifier() {
        return getName();
    }

    // Compare based on names for alphabetical sorting
    @Override
    public int compareTo(@NotNull SavedPosition o) {
        return this.getName().compareTo(o.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SavedPosition) {
            return this.getUuid().equals(((SavedPosition) obj).getUuid());
        }
        return false;
    }

}
