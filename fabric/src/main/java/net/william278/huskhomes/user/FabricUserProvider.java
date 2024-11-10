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

import net.minecraft.server.network.ServerPlayerEntity;
import net.william278.huskhomes.FabricHuskHomes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface FabricUserProvider extends UserProvider {

    @Override
    @NotNull
    default OnlineUser getOnlineUser(@NotNull UUID uuid) {
        return getOnlineUser(getPlugin().getMinecraftServer().getPlayerManager().getPlayer(uuid));
    }

    @NotNull
    default FabricUser getOnlineUser(@Nullable ServerPlayerEntity player) {
        if (player == null) {
            throw new IllegalArgumentException("Player is not online");
        }
        FabricUser user = (FabricUser) getOnlineUserMap().get(player.getUuid());
        if (user == null || user.getPlayer().isRemoved() || user.getPlayer().isDisconnected()) {
            user = FabricUser.adapt(player, getPlugin());
            getOnlineUserMap().put(player.getUuid(), user);
            return user;
        }
        return user;
    }

    @NotNull
    FabricHuskHomes getPlugin();

}
