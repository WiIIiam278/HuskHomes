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
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

/**
 * Represents a world on a server.
 */
@Getter
@Setter
@AllArgsConstructor(staticName = "from")
@NoArgsConstructor
public class World {

    @Expose
    private String name;
    @Expose
    private UUID uuid;
    @Expose
    @Nullable
    @Getter(AccessLevel.NONE)
    private Environment environment = null;

    @NotNull
    public static World from(@NotNull String name, @NotNull UUID uuid) {
        return new World(name, uuid, null);
    }

    @NotNull
    public static World from(@NotNull String name) {
        return from(name, UUID.randomUUID());
    }

    @NotNull
    @SuppressWarnings("unused")
    public Environment getEnvironment() {
        return environment == null ? Environment.OVERWORLD : environment;
    }

    /**
     * Identifies the environment of the world.
     */
    public enum Environment {
        OVERWORLD,
        NETHER,
        THE_END,
        CUSTOM;

        @NotNull
        public static Environment match(@NotNull String name) {
            return switch (name.toLowerCase(Locale.ENGLISH)) {
                case "overworld" -> OVERWORLD;
                case "nether", "the_nether" -> NETHER;
                case "the_end" -> THE_END;
                default -> CUSTOM;
            };
        }
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        if (obj instanceof World world) {
            return this.uuid.equals(world.uuid);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
