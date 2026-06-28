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

//#if MC>=260000
import net.minecraft.server.level.ServerPlayer;
//#else
//$$ import net.minecraft.server.network.ServerPlayerEntity;
//#endif
import net.william278.huskhomes.FabricHuskHomes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface FabricUserProvider extends UserProvider {

    @Override
    @NotNull
    default OnlineUser getOnlineUser(@NotNull UUID uuid) {
        //#if MC>=260000
        return getOnlineUser(getPlugin().getMinecraftServer().getPlayerList().getPlayer(uuid));
        //#else
        //$$ return getOnlineUser(getPlugin().getMinecraftServer().getPlayerManager().getPlayer(uuid));
        //#endif
    }

    @NotNull
    default FabricUser getOnlineUser(
            //#if MC>=260000
            @Nullable ServerPlayer player
            //#else
            //$$ @Nullable ServerPlayerEntity player
            //#endif
    ) {
        if (player == null) {
            throw new IllegalArgumentException("Player is not online");
        }
        FabricUser user = (FabricUser) getOnlineUserMap().get(
                //#if MC>=260000
                player.getUUID()
                //#else
                //$$ player.getUuid()
                //#endif
        );
        if (user == null || user.getPlayer().isRemoved() ||
                //#if MC>=260000
                user.getPlayer().hasDisconnected()
                //#else
                //$$ user.getPlayer().isDisconnected()
                //#endif
        ) {
            user = FabricUser.adapt(player, getPlugin());
            getOnlineUserMap().put(
                    //#if MC>=260000
                    player.getUUID(),
                    //#else
                    //$$ player.getUuid(),
                    //#endif
                    user
            );
            return user;
        }
        return user;
    }

    @NotNull
    FabricHuskHomes getPlugin();

}
