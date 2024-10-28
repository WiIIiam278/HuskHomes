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

import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;
import java.util.stream.Collectors;

public interface MapHookProvider {

    @NotNull
    @Unmodifiable
    default Set<MapHook> getMapHooks() {
        return getHooks().stream()
                .filter(hook -> hook instanceof MapHook)
                .map(hook -> (MapHook) hook)
                .collect(Collectors.toSet());
    }

    default void addMappedHome(@NotNull Home home) {
        getMapHooks().forEach(h -> h.addHome(home));
    }

    default void removeMappedHome(@NotNull Home home) {
        getMapHooks().forEach(h -> h.removeHome(home));
    }

    default void removeAllMappedHomes(@NotNull String worldName) {
        getMapHooks().forEach(h -> h.clearHomes(worldName));
    }

    default void removeAllMappedHomes(@NotNull User user) {
        getMapHooks().forEach(h -> h.clearHomes(user));
    }

    default void addMappedWarp(@NotNull Warp warp) {
        getMapHooks().forEach(h -> h.addWarp(warp));
    }

    default void removeMappedWarp(@NotNull Warp warp) {
        getMapHooks().forEach(h -> h.removeWarp(warp));
    }

    default void removeAllMappedWarps(@NotNull String worldName) {
        getMapHooks().forEach(h -> h.clearWarps(worldName));
    }

    default void removeAllMappedWarps() {
        getMapHooks().forEach(MapHook::clearWarps);
    }

    @NotNull
    Set<Hook> getHooks();

}
