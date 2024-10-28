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

package net.william278.huskhomes.teleport;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Represents the username of a player who may or may not be online, either locally as an {@link OnlineUser},
 * or on a different server.
 *
 * @param name The name of the player
 */
public record Username(@NotNull String name) implements Teleportable, Target {

    /**
     * Search for a local {@link OnlineUser} by their name.
     *
     * <p>If a user by the name provided is on the {@link HuskHomes#getUserList() player list}, then this
     * method will search for the user by exact name.
     *
     * <p>Otherwise, the lookup will first attempt to find the user by exact name, and if that fails, it will search for
     * the closest name match.
     *
     * @param plugin The instance of {@link HuskHomes}
     * @return An {@link Optional} containing the {@link OnlineUser} if found
     * @throws TeleportationException If the user is not found
     */
    @NotNull
    public Optional<OnlineUser> findLocally(@NotNull HuskHomes plugin) {
        return plugin.getUserList().stream()
                .anyMatch((user) -> user.getName().equalsIgnoreCase(name))
                ? plugin.getOnlineUserExact(name) : plugin.getOnlineUser(name);
    }

    /**
     * Get the username {@link String} being represented by this object.
     *
     * @return the username
     */
    @NotNull
    @Override
    public String getUsername() {
        return name;
    }

}
