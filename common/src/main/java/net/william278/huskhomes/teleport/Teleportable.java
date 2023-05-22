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

import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a teleporter; the person performing the teleport.
 * <p>
 * Can be represented as an {@link OnlineUser} locally or as a {@link Username username} reference
 * that needs to be resolved first.
 */
public interface Teleportable {

    /**
     * Create a {@link Teleportable} from a player name
     *
     * @param teleporter the player name
     * @return the teleportable
     */
    @NotNull
    static Teleportable username(@NotNull String teleporter) {
        return new Username(teleporter);
    }

    /**
     * Get the username of the teleporter
     *
     * @return the username string
     */
    @NotNull
    String getUsername();

}
