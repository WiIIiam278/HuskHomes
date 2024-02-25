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
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.TeleportationException;
import net.william278.huskhomes.util.BukkitAdapter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;
import space.arim.morepaperlib.scheduling.GracefulScheduling;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
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

    /**
     * Adapt a {@link Player} to a {@link OnlineUser}.
     *
     * @param player the online {@link Player} to adapt
     * @return the adapted {@link OnlineUser}
     */
    @NotNull
    public static BukkitUser adapt(@NotNull Player player, @NotNull BukkitHuskHomes plugin) {
        return new BukkitUser(player, plugin);
    }

    /**
     * Return the {@link Player} wrapped by this {@link BukkitUser}.
     *
     * @return the {@link Player} wrapped by this {@link BukkitUser}
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    @Override
    public Position getPosition() {
        return Position.at(BukkitAdapter.adaptLocation(player.getLocation()).orElseThrow(
                        () -> new IllegalStateException("Failed to get the position of a BukkitPlayer (null)")
                ),
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
    public void teleportLocally(@NotNull Location location, boolean async) throws TeleportationException {
        // Ensure the world exists
        final Optional<org.bukkit.Location> resolvedLocation = BukkitAdapter.adaptLocation(location);
        if (resolvedLocation.isEmpty() || resolvedLocation.get().getWorld() == null) {
            throw new TeleportationException(TeleportationException.Type.WORLD_NOT_FOUND, plugin);
        }

        // Ensure the coordinates are within the world limits
        final org.bukkit.Location bukkitLocation = resolvedLocation.get();
        if (!bukkitLocation.getWorld().getWorldBorder().isInside(resolvedLocation.get())) {
            throw new TeleportationException(TeleportationException.Type.ILLEGAL_TARGET_COORDINATES, plugin);
        }

        // Run on the appropriate thread scheduler for this platform
        final GracefulScheduling scheduler = ((BukkitHuskHomes) plugin).getScheduler();
        scheduler.entitySpecificScheduler(player).run(
                () -> {
                    player.leaveVehicle();
                    player.eject();
                    if (async || scheduler.isUsingFolia()) {
                        PaperLib.teleportAsync(player, bukkitLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
                        return;
                    }
                    player.teleport(bukkitLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
                },
                () -> plugin.log(Level.WARNING, "User offline when teleporting: " + player.getName())
        );
    }

    /**
     * Get the player momentum and return if they are moving.
     *
     * @return {@code true} if the player is moving, {@code false} otherwise
     **/
    @Override
    public boolean isMoving() {
        return player.getVelocity().length() >= 0.1;
    }

    /**
     * Return the value of the player's "vanished" metadata tag if they have it.
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
     * Handles player invulnerability after teleporting.
     */
    @Override
    public void handleInvulnerability() {
        if (plugin.getSettings().getGeneral().getTeleportInvulnerabilityTime() <= 0) {
            return;
        }
        long invulnerabilityTimeInTicks = 20L * plugin.getSettings().getGeneral().getTeleportInvulnerabilityTime();
        player.setInvulnerable(true);
        // Remove the invulnerability
        plugin.runSyncDelayed(() -> player.setInvulnerable(false), invulnerabilityTimeInTicks);
    }

    /**
     * Send a Bukkit plugin message to the player.
     */
    public void sendPluginMessage(@NotNull String channel, final byte[] message) {
        player.sendPluginMessage((BukkitHuskHomes) plugin, channel, message);
    }

}
