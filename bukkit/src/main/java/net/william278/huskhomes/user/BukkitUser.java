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

import io.papermc.lib.PaperLib;
import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.network.PluginMessageBroker;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.TeleportationException;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Bukkit's implementation of an {@link OnlineUser}.
 */
public class BukkitUser extends OnlineUser {

    private final Player player;

    private BukkitUser(@NotNull Player player, @NotNull BukkitHuskHomes plugin) {
        super(player.getUniqueId(), player.getName(), plugin);
        this.player = player;
    }

    @NotNull
    @ApiStatus.Internal
    public static BukkitUser adapt(@NotNull Player player, @NotNull BukkitHuskHomes plugin) {
        return new BukkitUser(player, plugin);
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @Override
    public Position getPosition() {
        return BukkitHuskHomes.Adapter.adapt(player.getLocation(), plugin.getServerName());
    }

    @Override
    public Optional<Position> getBedSpawnPosition() {
        return Optional.ofNullable(player.getBedSpawnLocation())
                .map(loc -> BukkitHuskHomes.Adapter.adapt(loc, plugin.getServerName()));
    }

    @Override
    public double getHealth() {
        return player.getHealth();
    }

    @Override
    public boolean hasPermission(@NotNull String node) {
        return player.hasPermission(node);
    }


    @Override
    @NotNull
    public Map<String, Boolean> getPermissions() {
        return player.getEffectivePermissions().stream()
                .collect(Collectors.toMap(
                        PermissionAttachmentInfo::getPermission,
                        PermissionAttachmentInfo::getValue, (a, b) -> b
                ));
    }

    @Override
    public CompletableFuture<Void> dismount() {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        plugin.runSync(() -> {
            player.leaveVehicle();
            player.eject();
            future.complete(null);
        }, this);
        return future;
    }

    @Override
    public void teleportLocally(@NotNull Location target, boolean async) throws TeleportationException {
        // Ensure the location is valid (world exists, coordinates are within the world border)
        final org.bukkit.Location location = BukkitHuskHomes.Adapter.adapt(target);
        if (location.getWorld() == null) {
            throw new TeleportationException(TeleportationException.Type.WORLD_NOT_FOUND, plugin);
        }
        if (!location.getWorld().getWorldBorder().isInside(location)) {
            throw new TeleportationException(TeleportationException.Type.ILLEGAL_TARGET_COORDINATES, plugin);
        }

        // Run on the appropriate thread scheduler for this platform
        plugin.runSync(() -> {
            player.leaveVehicle();
            player.eject();
            if (async || ((BukkitHuskHomes) plugin).getScheduler().isUsingFolia()) {
                PaperLib.teleportAsync(player, location, PlayerTeleportEvent.TeleportCause.PLUGIN);
                return;
            }
            player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }, this);
    }

    @Override
    public boolean isMoving() {
        return player.getVelocity().length() >= 0.1;
    }

    @Override
    public void sendPluginMessage(byte[] message) {
        player.sendPluginMessage((BukkitHuskHomes) plugin, PluginMessageBroker.BUNGEE_CHANNEL_ID, message);
    }

    @Override
    public boolean isVanished() {
        return player.getMetadata("vanished")
                .stream()
                .map(MetadataValue::asBoolean)
                .findFirst()
                .orElse(false);
    }

    @Override
    public void handleInvulnerability() {
        if (plugin.getSettings().getGeneral().getTeleportInvulnerabilityTime() <= 0) {
            return;
        }
        long invulnerabilityTimeInTicks = 20L * plugin.getSettings().getGeneral().getTeleportInvulnerabilityTime();
        player.setInvulnerable(true);
        // Remove the invulnerability
        plugin.runSyncDelayed(() -> player.setInvulnerable(false), this, invulnerabilityTimeInTicks);
    }

    @Override
    public void removeInvulnerabilityIfPermitted() {
        if (plugin.isInvulnerable(this.getUuid())) {
            player.setInvulnerable(false);
        }
    }

}
