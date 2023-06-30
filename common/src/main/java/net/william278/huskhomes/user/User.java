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

package net.william278.huskhomes.user;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a user who has data saved in the database.
 */
public class User {

    @NotNull
    private final UUID uuid;

    @NotNull
    private final String username;

    /**
     * Get a user from a {@link UUID} and username.
     *
     * @param uuid     Minecraft account {@link UUID} of the player
     * @param username Username of the player
     */
    protected User(@NotNull UUID uuid, @NotNull String username) {
        this.uuid = uuid;
        this.username = username;
    }

    @NotNull
    public static User of(@NotNull UUID uuid, @NotNull String username) {
        return new User(uuid, username);
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        if (obj instanceof User user) {
            return user.getUuid().equals(getUuid());
        }
        return super.equals(obj);
    }

    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    @NotNull
    public String getUsername() {
        return username;
    }
}
