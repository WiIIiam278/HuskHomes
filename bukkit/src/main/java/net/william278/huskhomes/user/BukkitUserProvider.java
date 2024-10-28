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

import net.william278.huskhomes.BukkitHuskHomes;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface BukkitUserProvider extends UserProvider {

    @Override
    @NotNull
    default OnlineUser getOnlineUser(@NotNull UUID uuid) {
        return getOnlineUser(getPlugin().getServer().getPlayer(uuid));
    }

    @NotNull
    default BukkitUser getOnlineUser(@Nullable Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player is not online");
        }
        BukkitUser user = (BukkitUser) getOnlineUserMap().get(player.getUniqueId());
        if (user == null) {
            user = BukkitUser.adapt(player, getPlugin());
            getOnlineUserMap().put(player.getUniqueId(), user);
            return user;
        }
        return user;
    }

    @NotNull
    BukkitHuskHomes getPlugin();

}
