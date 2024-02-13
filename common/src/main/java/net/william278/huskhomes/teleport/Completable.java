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
 * Represents a Teleport that is completable
 */
public interface Completable {

    void execute() throws TeleportationException;

    @NotNull
    OnlineUser getExecutor();

    /**
     * Complete the teleport and handle exceptions
     *
     * @param args Optional args to pass to the teleport exception handler
     */
    default void complete(@NotNull String... args) {
        try {
            execute();
        } catch (TeleportationException e) {
            e.displayMessage(getExecutor(), args);
        }
    }

}
