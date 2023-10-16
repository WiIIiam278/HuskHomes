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
import net.william278.huskhomes.teleport.Target;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a position - a {@link Location} somewhere on the proxy network or server.
 */
public class Position extends Location implements Target {

    @Expose
    private String server;

    protected Position(double x, double y, double z, float yaw, float pitch,
                       @NotNull World world, @NotNull String server) {
        super(x, y, z, yaw, pitch, world);
        this.setServer(server);
    }

    protected Position(@NotNull Location location, @NotNull String server) {
        super(location.getX(), location.getY(), location.getZ(), location.getYaw(),
                location.getPitch(), location.getWorld());
        this.setServer(server);
    }

    @SuppressWarnings("unused")
    private Position() {
    }

    @NotNull
    public static Position at(double x, double y, double z, float yaw, float pitch,
                              @NotNull World world, @NotNull String server) {
        return new Position(x, y, z, yaw, pitch, world, server);
    }

    @NotNull
    public static Position at(double x, double y, double z,
                              @NotNull World world, @NotNull String server) {
        return Position.at(x, y, z, 0, 0, world, server);
    }

    @NotNull
    public static Position at(@NotNull Location location, @NotNull String server) {
        return new Position(location, server);
    }

    @Override
    public void update(@NotNull Position newPosition) {
        super.update(newPosition);
        this.setServer(newPosition.getServer());
    }

    /**
     * The name of the server the position is on.
     */
    @NotNull
    public String getServer() {
        return server;
    }

    public void setServer(@NotNull String server) {
        this.server = server;
    }

    @Override
    public String toString() {
        return "x: " + (int) getX() + ", "
                + "y: " + (int) getY() + ", "
                + "z: " + (int) getZ() + " "
                + "(" + getWorld().getName() + " / " + getServer() + ")";
    }

}
