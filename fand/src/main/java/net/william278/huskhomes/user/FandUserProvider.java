/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 */

package net.william278.huskhomes.user;

import io.fand.api.entity.Player;
import net.william278.huskhomes.FandHuskHomes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface FandUserProvider extends UserProvider {

    @Override
    @NotNull
    default OnlineUser getOnlineUser(@NotNull UUID uuid) {
        return getOnlineUser(getPlugin().server().player(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Player is not online")));
    }

    @NotNull
    default FandUser getOnlineUser(@NotNull Player player) {
        final OnlineUser cached = getOnlineUserMap().get(player.uniqueId());
        if (cached instanceof FandUser user && user.getPlayer().online()) {
            return user;
        }
        final FandUser user = FandUser.adapt(player, getPlugin());
        getOnlineUserMap().put(player.uniqueId(), user);
        return user;
    }

    @Override
    @NotNull
    FandHuskHomes getPlugin();
}
