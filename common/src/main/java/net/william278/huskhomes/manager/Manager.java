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

package net.william278.huskhomes.manager;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.network.Message;
import org.jetbrains.annotations.NotNull;

public class Manager {

    private final HuskHomes plugin;
    private final HomesManager homes;
    private final WarpsManager warps;
    private final RequestsManager requests;

    public Manager(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
        this.homes = new HomesManager(plugin);
        this.warps = new WarpsManager(plugin);
        this.requests = new RequestsManager(plugin);
    }

    @NotNull
    public HomesManager homes() {
        return homes;
    }

    @NotNull
    public WarpsManager warps() {
        return warps;
    }

    @NotNull
    public RequestsManager requests() {
        return requests;
    }

    // Update caches on all servers
    protected void propagateCacheUpdate() {
        if (plugin.getSettings().getCrossServer().isEnabled()) {
            plugin.getOnlineUsers().stream().findAny().ifPresent(user -> Message.builder()
                    .type(Message.Type.UPDATE_CACHES)
                    .scope(Message.Scope.SERVER)
                    .target(Message.TARGET_ALL)
                    .build().send(plugin.getMessenger(), user));
        }
    }
}
