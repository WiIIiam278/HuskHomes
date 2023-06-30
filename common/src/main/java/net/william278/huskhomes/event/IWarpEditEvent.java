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

package net.william278.huskhomes.event;

import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

/**
 * Representation of an event that fires when a warp is created or updated.
 */
public interface IWarpEditEvent extends Cancellable {

    /**
     * Get the warp being created or updated.
     *
     * @return the {@link Warp} being saved
     */
    @NotNull
    Warp getWarp();

    /**
     * Get the user who is editing the warp.
     *
     * @return the {@link CommandUser} who is editing the warp
     */
    @NotNull
    CommandUser getEditor();

}
