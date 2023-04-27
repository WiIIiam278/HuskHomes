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
import net.kyori.adventure.audience.Audience;
import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.TeleportationException;
import net.william278.huskhomes.util.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Bukkit implementation of an {@link OnlineUser}
 */
public class BukkitUser extends OnlineUser {

    private final BukkitHuskHomes plugin;
    private final Player player;

    private BukkitUser(@NotNull Player player) {
        super(player.getUniqueId(), player.getName());
        this.plugin = BukkitHuskHomes.getInstance();
        this.player = player;
    }

    /**
     * Adapt a {@link Player} to a {@link OnlineUser}
     *
     * @param player the online {@link Player} to adapt
     * @return the adapted {@link OnlineUser}
     */
    @NotNull
    public static BukkitUser adapt(@NotNull Player player) {
        return new BukkitUser(player);
    }

    @Override
    public Position getPosition() {
        return Position.at(BukkitAdapter.adaptLocation(player.getLocation())
                        .orElseThrow(() -> new IllegalStateException("Failed to get the position of a BukkitPlayer (null)")),
                plugin.getServerName());

    }

    @Override
    public Optional<Position> getBedSpawnPosition() {
        return Optional.ofNullable(player.getBedSpawnLocation()).flatMap(BukkitAdapter::adaptLocation)
                .map(location -> Position.at(location, plugin.getServerName()));
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
    @NotNull
    public Audience getAudience() {
        return plugin.getAudiences().player(player);
    }

    @Override
    public void teleportLocally(@NotNull Location location, boolean asynchronous) throws TeleportationException {
        final Optional<org.bukkit.Location> resolvedLocation = BukkitAdapter.adaptLocation(location);
        if (resolvedLocation.isEmpty() || resolvedLocation.get().getWorld() == null) {
            throw new TeleportationException(TeleportationException.Type.WORLD_NOT_FOUND);
        }

        final org.bukkit.Location bukkitLocation = resolvedLocation.get();
        if (!bukkitLocation.getWorld().getWorldBorder().isInside(resolvedLocation.get())) {
            throw new TeleportationException(TeleportationException.Type.ILLEGAL_TARGET_COORDINATES);
        }
        plugin.runAsync(() -> {
            if (asynchronous) {
                PaperLib.teleportAsync(player, bukkitLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
            } else {
                player.teleport(bukkitLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
        }, location);
    }

    /**
     * Get the player momentum and return if they are moving
     *
     * @return {@code true} if the player is moving, {@code false} otherwise
     **/
    @Override
    public boolean isMoving() {
        return player.getVelocity().length() >= 0.1;
    }

    /**
     * Return the value of the player's "vanished" metadata tag if they have it
     *
     * @return {@code true} if the player is vanished, {@code false} otherwise
     */
    @Override
    public boolean isVanished() {
        return player.getMetadata("vanished")
                .stream()
                .map(MetadataValue::asBoolean)
                .findFirst()
                .orElse(false);
    }

    /**
     * Send a Bukkit plugin message to the player
     */
    public void sendPluginMessage(@NotNull String channel, final byte[] message) {
        player.sendPluginMessage(plugin, channel, message);
    }

    /**
     * Return the {@link Player} wrapped by this {@link BukkitUser}
     *
     * @return the {@link Player} wrapped by this {@link BukkitUser}
     */
    public Player getPlayer() {
        return player;
    }
}
