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

package net.william278.huskhomes.listener;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.william278.huskhomes.FabricHuskHomes;
import org.jetbrains.annotations.NotNull;

// Note that the teleport event and update player respawn position events are not handled on Fabric.
// The "update last position on teleport event" and "global respawn" features are not supported on Fabric.
public class FabricEventListener extends EventListener {

    public FabricEventListener(@NotNull FabricHuskHomes plugin) {
        super(plugin);
    }

    @Override
    public void register() {
        // Join event
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            getPlugin().getOnlineUserMap().remove(handler.getPlayer().getUuid());
            super.handlePlayerJoin(getPlugin().getOnlineUser(handler.getPlayer()));
        });

        // Quit event
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> handlePlayerLeave(
                getPlugin().getOnlineUser(handler.getPlayer())
        ));

        // Death event
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayerEntity player) {
                super.handlePlayerDeath(getPlugin().getOnlineUser(player));
            }
        });

        // Damage event (to cancel teleport warmups)
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, amount, damage, blocked) -> {
            if (!(entity instanceof ServerPlayerEntity player)) {
                return;
            }
            // Cancel warmup on any "hurt" event during warmup, even if damage is blocked/absorbed
            if (!getPlugin().isWarmingUp(player.getUuid()) || amount <= 0) {
                return;
            }
            getPlugin().getWarmupDamagedUsers().add(player.getUuid());
        });

        // Respawn event
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            getPlugin().getOnlineUserMap().remove(oldPlayer.getUuid());
            super.handlePlayerRespawn(getPlugin().getOnlineUser(newPlayer));
        });
    }

    @NotNull
    @Override
    protected FabricHuskHomes getPlugin() {
        return (FabricHuskHomes) super.getPlugin();
    }

}
