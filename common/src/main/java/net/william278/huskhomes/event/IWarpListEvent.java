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

import java.util.List;

/**
 * Represents an event that fires when a player requests to view a list of warps.
 */
public interface IWarpListEvent extends Cancellable {

    /**
     * Get the list of warps to be displayed.
     *
     * @return the list of warps
     */
    @NotNull
    List<Warp> getWarps();

    /**
     * Set the list of warps to be displayed.
     *
     * @param warps the list of warps
     */
    void setWarps(@NotNull List<Warp> warps);

    /**
     * Get the user viewing the warp list.
     *
     * @return the user viewing the warp list
     */
    @NotNull
    CommandUser getListViewer();

}
