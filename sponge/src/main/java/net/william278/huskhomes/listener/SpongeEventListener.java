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

import net.william278.huskhomes.SpongeHuskHomes;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.user.SpongeUser;
import net.william278.huskhomes.util.SpongeAdapter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.entity.MovementType;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.world.portal.Portal;
import org.spongepowered.api.world.portal.PortalTypes;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Optional;

public class SpongeEventListener extends EventListener {

    public SpongeEventListener(@NotNull SpongeHuskHomes plugin) {
        super(plugin);
        plugin.getGame().eventManager().registerListeners(plugin.getPluginContainer(), this);
    }

    @Listener
    public void onPlayerJoin(final ServerSideConnectionEvent.Join event) {
        super.handlePlayerJoin(SpongeUser.adapt(event.player(), (SpongeHuskHomes) plugin));
    }

    @Listener
    public void onPlayerLeave(final ServerSideConnectionEvent.Disconnect event) {
        super.handlePlayerJoin(SpongeUser.adapt(event.player(), (SpongeHuskHomes) plugin));
    }

    @Listener
    public void onPlayerDeath(final DestructEntityEvent.Death event) {
        if (event.entity() instanceof ServerPlayer player) {
            super.handlePlayerJoin(SpongeUser.adapt(player, (SpongeHuskHomes) plugin));
        }
    }

    @Listener
    public void onPlayerRespawn(final RespawnPlayerEvent event) {
        final Optional<Portal> type = event.context().get(EventContextKeys.PORTAL);
        if (type.isPresent() && type.get().type().equals(PortalTypes.END.get())) {
            return;
        }
        super.handlePlayerJoin(SpongeUser.adapt(event.entity(), (SpongeHuskHomes) plugin));
    }

    @Listener
    public void onPlayerTeleport(final MoveEntityEvent event) {
        if (event.entity() instanceof ServerPlayer player) {
            final Optional<MovementType> type = event.context().get(EventContextKeys.MOVEMENT_TYPE);
            if (type.isPresent() && type.get().equals(MovementTypes.ENTITY_TELEPORT.get())) {
                SpongeAdapter.adaptLocation(ServerLocation.of(player.world(), event.originalPosition()))
                        .ifPresent(location -> super.handlePlayerTeleport(
                                SpongeUser.adapt(player, (SpongeHuskHomes) plugin),
                                Position.at(location, plugin.getServerName())
                        ));
            }
        }
    }

    @Listener
    @SuppressWarnings("unchecked")
    public void onPlayerUpdateRespawnLocation(InteractBlockEvent.Secondary event) {
        if (event.context().get(EventContextKeys.PLAYER).isPresent()) {
            final ServerPlayer player = (ServerPlayer) event.context().get(EventContextKeys.PLAYER).get();
            final BlockType type = event.block().state().type();

            if (type.isAnyOf(BlockTypes.BLACK_BED, BlockTypes.BLUE_BED, BlockTypes.BROWN_BED, BlockTypes.CYAN_BED,
                    BlockTypes.GRAY_BED, BlockTypes.GREEN_BED, BlockTypes.LIGHT_BLUE_BED, BlockTypes.LIGHT_GRAY_BED,
                    BlockTypes.LIME_BED, BlockTypes.MAGENTA_BED, BlockTypes.ORANGE_BED, BlockTypes.PINK_BED,
                    BlockTypes.PURPLE_BED, BlockTypes.RED_BED, BlockTypes.WHITE_BED, BlockTypes.YELLOW_BED,
                    BlockTypes.RESPAWN_ANCHOR)) {

                super.handlePlayerUpdateSpawnPoint(SpongeUser.adapt(player, (SpongeHuskHomes) plugin),
                        Position.at(
                                SpongeAdapter.adaptLocation(player.serverLocation()).orElseThrow(),
                                plugin.getServerName()
                        ));
            }
        }
    }

}
