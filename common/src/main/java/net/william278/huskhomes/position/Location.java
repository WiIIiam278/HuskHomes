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
import lombok.*;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a local position on this server.
 */
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {

    @Expose
    private double x;

    @Expose
    private double y;

    @Expose
    private double z;

    @Expose
    private float yaw;

    @Expose
    private float pitch;

    @Expose
    private World world;

    @NotNull
    public static Location at(double x, double y, double z, float yaw, float pitch, @NotNull World world) {
        return new Location(x, y, z, yaw, pitch, world);
    }

    @NotNull
    public static Location at(double x, double y, double z, @NotNull World world) {
        return Location.at(x, y, z, 0, 0, world);
    }

    /**
     * Update the position to match that of another position.
     *
     * @param position The position to update to
     */
    public void update(@NotNull Position position) {
        this.setX(position.getX());
        this.setY(position.getY());
        this.setZ(position.getZ());
        this.setYaw(position.getYaw());
        this.setPitch(position.getPitch());
        this.setWorld(position.getWorld());
    }

}
