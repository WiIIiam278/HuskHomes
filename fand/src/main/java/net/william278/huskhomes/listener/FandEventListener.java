/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 */

package net.william278.huskhomes.listener;

import io.fand.api.entity.Player;
import io.fand.api.event.EventPriority;
import io.fand.api.event.entity.EntityDamageEvent;
import io.fand.api.event.player.PlayerDeathEvent;
import io.fand.api.event.player.PlayerJoinEvent;
import io.fand.api.event.player.PlayerQuitEvent;
import io.fand.api.event.player.PlayerRespawnEvent;
import io.fand.api.event.player.PlayerTeleportEvent;
import net.william278.huskhomes.FandAdapter;
import net.william278.huskhomes.FandHuskHomes;
import org.jetbrains.annotations.NotNull;

public final class FandEventListener extends EventListener {

    public FandEventListener(@NotNull FandHuskHomes plugin) {
        super(plugin);
    }

    @Override
    public void register() {
        final var events = getPlugin().getContext().events();
        events.subscribe(PlayerJoinEvent.class, event -> {
            getPlugin().getOnlineUserMap().remove(event.player().uniqueId());
            handlePlayerJoin(getPlugin().getOnlineUser(event.player()));
        });
        events.subscribe(PlayerQuitEvent.class, event -> handlePlayerLeave(getPlugin().getOnlineUser(event.player())));
        events.subscribe(PlayerDeathEvent.class, event -> handlePlayerDeath(getPlugin().getOnlineUser(event.player())));
        events.subscribe(PlayerRespawnEvent.class, event -> handlePlayerRespawn(getPlugin().getOnlineUser(event.player())));
        events.subscribe(EntityDamageEvent.class, event -> {
            if (!(event.entity() instanceof Player player)
                    || event.amount() <= 0
                    || !getPlugin().isWarmingUp(player.uniqueId())) {
                return;
            }
            getPlugin().getWarmupDamagedUsers().add(player.uniqueId());
        });
        events.subscribe(PlayerTeleportEvent.class, EventPriority.OBSERVER, event -> {
            if (!event.cancelled()) {
                handlePlayerTeleport(
                        getPlugin().getOnlineUser(event.player()),
                        FandAdapter.adapt(event.from(), getPlugin().getServerName())
                );
            }
        });
    }

    @Override
    @NotNull
    protected FandHuskHomes getPlugin() {
        return (FandHuskHomes) super.getPlugin();
    }
}
