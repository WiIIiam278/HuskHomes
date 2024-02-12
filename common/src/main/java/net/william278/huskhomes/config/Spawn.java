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

package net.william278.huskhomes.config;

import de.exlll.configlib.Configuration;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Used to store the server spawn location.
 */
@Getter
@Configuration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Spawn {

    static final String CONFIG_HEADER = """
            ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
            ┃    Server /spawn location    ┃
            ┃ Edit in-game using /setspawn ┃
            ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛""";

    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private String worldName;
    private String worldUuid;

    /**
     * Returns the {@link Position} of the spawn.
     *
     * @param server The server the spawn is on
     * @return The {@link Position} of the spawn
     */
    @NotNull
    public Position getPosition(@NotNull String server) {
        return Position.at(x, y, z, yaw, pitch, World.from(worldName, UUID.fromString(worldUuid)), server);
    }

    /**
     * Set the {@link Location} of the spawn.
     *
     * @param location The {@link Location} of the spawn
     */
    public Spawn(@NotNull Location location) {
        this(
                location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(),
                location.getWorld().getName(), location.getWorld().getUuid().toString()
        );
    }
}
