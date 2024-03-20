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

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents a world on a server.
 */
@NoArgsConstructor
public class World {

    private String name;
    private UUID uuid;
    @Nullable
    private Environment environment;

    private World(@NotNull String name, @NotNull UUID uuid, @Nullable Environment environment) {
        this.setName(name);
        this.setUuid(uuid);
        this.setEnvironment(environment);
    }

    @NotNull
    public static World from(@NotNull String name, @NotNull UUID uuid, @NotNull Environment environment) {
        return new World(name, uuid, environment);
    }

    @NotNull
    public static World from(@NotNull String name, @NotNull UUID uuid) {
        return new World(name, uuid, null);
    }

    /**
     * The name of this world, as defined by the world directory name.
     */
    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    /**
     * UUID of this world, as defined by the {@code uid.dat} file in the world directory.
     */
    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Environment of the world ({@link Environment#OVERWORLD}, {@link Environment#NETHER}, {@link Environment#THE_END},
     * or {@link Environment#CUSTOM}).
     *
     * <p>Will return {@link Environment#OVERWORLD} if the environment is null
     */
    @NotNull
    public Environment getEnvironment() {
        return Optional.ofNullable(environment).orElse(Environment.OVERWORLD);
    }

    public void setEnvironment(@Nullable Environment environment) {
        this.environment = environment;
    }

    /**
     * Identifies the environment of the world.
     */
    public enum Environment {
        OVERWORLD,
        NETHER,
        THE_END,
        CUSTOM
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        if (obj instanceof World world) {
            return world.getUuid().equals(getUuid());
        }
        return super.equals(obj);
    }
}
