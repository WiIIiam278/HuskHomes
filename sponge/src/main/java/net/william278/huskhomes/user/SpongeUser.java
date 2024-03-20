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

import net.kyori.adventure.audience.Audience;
import net.william278.huskhomes.SpongeHuskHomes;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.util.SpongeAdapter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.util.RespawnLocation;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Map;
import java.util.Optional;

public class SpongeUser extends OnlineUser {

    private final ServerPlayer player;

    private SpongeUser(@NotNull ServerPlayer player, @NotNull SpongeHuskHomes plugin) {
        super(player.uniqueId(), player.name(), plugin);
        this.player = player;
    }

    /**
     * Adapt a {@link ServerPlayer} to a {@link OnlineUser}.
     *
     * @param player the online {@link ServerPlayer} to adapt
     * @return the adapted {@link OnlineUser}
     */
    @NotNull
    public static SpongeUser adapt(@NotNull ServerPlayer player, @NotNull SpongeHuskHomes plugin) {
        return new SpongeUser(player, plugin);
    }

    /**
     * Get the {@link ServerPlayer} associated with this {@link OnlineUser}.
     *
     * @return the {@link ServerPlayer}
     */
    @NotNull
    public ServerPlayer getPlayer() {
        return player;
    }

    @Override
    public Position getPosition() {
        return Position.at(
                SpongeAdapter.adaptLocation(player.serverLocation())
                        .orElseThrow(() -> new IllegalStateException("Failed to adapt player location")),
                plugin.getServerName()
        );
    }

    @Override
    public Optional<Position> getBedSpawnPosition() {
        // Resolve the player's RespawnLocation from the world-key map and adapt as location
        return player.get(Keys.RESPAWN_LOCATIONS)
                .flatMap(resourceMap -> resourceMap.values().stream().findFirst())
                .flatMap(RespawnLocation::asLocation).flatMap(SpongeAdapter::adaptLocation)
                .map(location -> Position.at(location, plugin.getServerName()));
    }

    @Override
    public double getHealth() {
        return player.health().asImmutable().get();
    }

    @Override
    public boolean hasPermission(@NotNull String node) {
        return player.hasPermission(node);
    }

    @Override
    @NotNull
    public Map<String, Boolean> getPermissions() {
        return player.transientSubjectData().permissions(SubjectData.GLOBAL_CONTEXT);
    }

    @Override
    @NotNull
    public Audience getAudience() {
        return player;
    }

    @Override
    public void teleportLocally(@NotNull Location location, boolean async) {
        plugin.runSync(() -> {
            final Optional<ServerLocation> serverLocation = SpongeAdapter.adaptLocation(location);
            if (serverLocation.isEmpty()) {
                return;
            }
            player.vehicle().ifPresent(vehicle -> vehicle.get().passengers().remove(player));
            player.setLocation(serverLocation.get());
        });
    }

    @Override
    public void sendPluginMessage(@NotNull String channel, byte[] message) {
        ((SpongeHuskHomes) plugin).getPluginMessageChannel().sendTo(player, buf -> buf.writeBytes(message));
    }

    @Override
    public boolean isMoving() {
        return player.velocity().get().lengthSquared() > 0.0075;
    }

    @Override
    public boolean isVanished() {
        return false;
    }

    /**
     * Handles player invulnerability after teleporting.
     */
    @Override
    public void handleInvulnerability() {
        if (plugin.getSettings().getGeneral().getTeleportInvulnerabilityTime() <= 0) {
            return;
        }
        long invulnerabilityTimeInTicks = 20L * plugin.getSettings().getGeneral().getTeleportInvulnerabilityTime();
        player.invulnerable().set(false);
        // Remove the invulnerability
        plugin.runSyncDelayed(() -> player.invulnerable().set(true), invulnerabilityTimeInTicks);
    }

}
