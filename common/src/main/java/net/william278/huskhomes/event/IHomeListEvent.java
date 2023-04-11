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

import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents an event that fires when a player requests to view a list of homes
 */
public interface IHomeListEvent extends Cancellable {

    /**
     * Get the list of homes to be displayed
     *
     * @return the list of homes
     */
    @NotNull
    List<Home> getHomes();

    void setHomes(@NotNull List<Home> homes);

    /**
     * Get the player viewing the home list
     *
     * @return the player viewing the home list
     */
    @NotNull
    CommandUser getListViewer();

    /**
     * Indicates if the player has requested to view a list of public homes
     *
     * @return true if the player has requested to view a list of public homes
     */
    boolean getIsPublicHomeList();

}
