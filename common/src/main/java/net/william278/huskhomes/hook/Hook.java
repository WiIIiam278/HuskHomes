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

package net.william278.huskhomes.hook;

import net.william278.huskhomes.HuskHomes;
import org.jetbrains.annotations.NotNull;

/**
 * A plugin hook, where HuskHomes hooks into another plugin
 */
public abstract class Hook {

    /**
     * The plugin that this hook is for
     */
    protected final HuskHomes plugin;

    /**
     * The name of the hook
     */
    protected final String name;

    /**
     * Construct a new {@link Hook}
     *
     * @param plugin the {@link HuskHomes} instance
     */
    protected Hook(@NotNull HuskHomes plugin, @NotNull String name) {
        this.plugin = plugin;
        this.name = name;
    }

    /**
     * Initialize the hook and return {@code true} if it could be enabled
     */
    public abstract void initialize() ;

    @NotNull
    public String getName() {
        return name;
    }


}
