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

package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.paginedown.PaginatedList;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class ListCommand extends Command {

    protected final Map<UUID, PaginatedList> cachedLists;

    protected ListCommand(@NotNull String name, @NotNull List<String> aliases, @NotNull String usage,
                          @NotNull HuskHomes plugin) {
        super(name, aliases, usage, plugin);
        this.cachedLists = new HashMap<>();
    }

    public void invalidateCaches() {
        cachedLists.clear();
    }

}
