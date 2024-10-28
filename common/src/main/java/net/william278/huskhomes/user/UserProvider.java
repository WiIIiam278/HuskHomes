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

package net.william278.huskhomes.user;

import net.william278.huskhomes.HuskHomes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A provider for the plugin user list, tracking online users across the network.
 *
 * @since 4.8
 */
public interface UserProvider {

    @NotNull
    Map<UUID, OnlineUser> getOnlineUserMap();

    @NotNull
    Map<String, List<User>> getGlobalUserList();

    @NotNull
    Set<SavedUser> getSavedUsers();

    @NotNull
    OnlineUser getOnlineUser(@NotNull UUID uuid);

    @NotNull
    @Unmodifiable
    default Collection<OnlineUser> getOnlineUsers() {
        return getOnlineUserMap().values();
    }

    @NotNull
    default List<User> getUserList() {
        return Stream.concat(
                getGlobalUserList().values().stream().flatMap(Collection::stream),
                getOnlineUsers().stream().filter(o -> !o.isVanished())
        ).distinct().sorted().toList();
    }

    default void setUserList(@NotNull String server, @NotNull List<User> players) {
        getGlobalUserList().values().forEach(list -> {
            list.removeAll(players);
            list.removeAll(getOnlineUsers());
        });
        getGlobalUserList().put(server, players);
    }

    default boolean isUserOnline(@NotNull User user) {
        return getOnlineUserMap().containsKey(user.getUuid());
    }

    default Optional<OnlineUser> getOnlineUser(@NotNull String playerName) {
        return getOnlineUserExact(playerName)
                .or(() -> getOnlineUsers().stream()
                        .filter(user -> user.getName().toLowerCase().startsWith(playerName.toLowerCase()))
                        .findFirst());
    }

    default Optional<OnlineUser> getOnlineUserExact(@NotNull String playerName) {
        return getOnlineUsers().stream()
                .filter(user -> user.getName().equalsIgnoreCase(playerName))
                .findFirst();
    }

    default Optional<SavedUser> getSavedUser(@NotNull User user) {
        return getSavedUsers().stream()
                .filter(savedUser -> savedUser.getUser().equals(user))
                .findFirst();
    }

    default void editSavedUser(@NotNull User user, @NotNull Consumer<SavedUser> editor) {
        getPlugin().runAsync(() -> getSavedUser(user)
                .ifPresent(result -> {
                    editor.accept(result);
                    getPlugin().getDatabase().updateUserData(result);
                }));
    }

    @NotNull
    HuskHomes getPlugin();

}
