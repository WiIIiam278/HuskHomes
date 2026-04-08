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

import io.papermc.lib.PaperLib;
import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.config.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitEventListener extends EventListener implements Listener {

    protected boolean usePaperEvents = false;
    protected final Set<UUID> pendingRespawnTeleport = ConcurrentHashMap.newKeySet();

    public BukkitEventListener(@NotNull BukkitHuskHomes plugin) {
        super(plugin);
    }

    @Override
    public void register() {
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        pendingRespawnTeleport.remove(event.getPlayer().getUniqueId());
        getPlugin().getOnlineUserMap().remove(event.getPlayer().getUniqueId());
        super.handlePlayerJoin(getPlugin().getOnlineUser(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLeave(PlayerQuitEvent event) {
        pendingRespawnTeleport.remove(event.getPlayer().getUniqueId());
        super.handlePlayerLeave(getPlugin().getOnlineUser(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();
        pendingRespawnTeleport.add(player.getUniqueId());
        startRespawnPolling(player);
        super.handlePlayerDeath(getPlugin().getOnlineUser(player));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        pendingRespawnTeleport.remove(event.getPlayer().getUniqueId());
        if (usePaperEvents) {
            return;
        }
        getPlugin().getOnlineUserMap().remove(event.getPlayer().getUniqueId());
        this.handleLocalServerRespawn(event);
        super.handlePlayerRespawn(getPlugin().getOnlineUser(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        final Player player = event.getPlayer();

        // Return if the disconnecting entity is a Citizens NPC, or if the teleport was naturally caused
        if (player.hasMetadata("NPC")) {
            return;
        }
        if (!(event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND ||
              event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN)) {
            return;
        }

        this.handlePlayerTeleport(
                getPlugin().getOnlineUser(player),
                BukkitHuskHomes.Adapter.adapt(event.getFrom(), getPlugin().getServerName())
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerTakeDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        // Cancel warmup on any "hurt" event during warmup, even if damage is cancelled/blocked/absorbed
        if (!getPlugin().isWarmingUp(player.getUniqueId()) || event.getDamage() <= 0) {
            return;
        }
        getPlugin().getWarmupDamagedUsers().add(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerUpdateRespawnLocation(PlayerInteractEvent event) {
        final Settings.CrossServerSettings crossServer = getPlugin().getSettings().getCrossServer();
        if (usePaperEvents || !(crossServer.isEnabled()) && crossServer.isGlobalRespawning()) {
            return;
        }
        if (event.getClickedBlock() == null || !(event.getClickedBlock().getBlockData() instanceof Bed
                                                 || event.getClickedBlock().getBlockData() instanceof RespawnAnchor)) {
            return;
        }

        final Location location = event.getPlayer().getBedSpawnLocation();
        if (location == null) {
            return;
        }

        // Update the player's respawn location
        this.handlePlayerUpdateSpawnPoint(
                getPlugin().getOnlineUser(event.getPlayer()),
                BukkitHuskHomes.Adapter.adapt(location, getPlugin().getServerName())
        );
    }

    /**
     * Polls every tick after death. Once the player is no longer dead, teleport to spawn immediately.
     */
    private void startRespawnPolling(@NotNull Player player) {
        final UUID uuid = player.getUniqueId();
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            final int maxAttempts = 200; // 10 seconds timeout
            for (int i = 0; i < maxAttempts; i++) {
                if (!pendingRespawnTeleport.contains(uuid)) {
                    return;
                }
                final Player onlinePlayer = Bukkit.getPlayer(uuid);
                if (onlinePlayer == null) {
                    pendingRespawnTeleport.remove(uuid);
                    return;
                }
                if (!onlinePlayer.isDead()) {
                    pendingRespawnTeleport.remove(uuid);
                    teleportToSpawnIfNeeded(onlinePlayer);
                    return;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            pendingRespawnTeleport.remove(uuid);
        });
    }

    private void teleportToSpawnIfNeeded(@NotNull Player player) {
        final Settings.CrossServerSettings crossServer = getPlugin().getSettings().getCrossServer();
        if (crossServer.isEnabled() && crossServer.isGlobalRespawning()) {
            return;
        }

        final boolean alwaysAtSpawn = getPlugin().getSettings().getGeneral().isAlwaysRespawnAtSpawn();
        if (!alwaysAtSpawn && player.getBedSpawnLocation() != null) {
            return;
        }

        final var spawnOpt = getPlugin().getSpawn();
        if (spawnOpt.isEmpty()) {
            return;
        }

        final var spawn = spawnOpt.get();
        final Location loc = BukkitHuskHomes.Adapter.adapt(spawn);
        if (loc == null || loc.getWorld() == null) {
            return;
        }

        player.setFallDistance(0f);
        PaperLib.teleportAsync(player, loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    protected final void handleLocalServerRespawn(@NotNull PlayerRespawnEvent event) {
        final Settings.CrossServerSettings crossServer = getPlugin().getSettings().getCrossServer();
        if (crossServer.isEnabled() && crossServer.isGlobalRespawning()) {
            return;
        }

        if (!getPlugin().getSettings().getGeneral().isAlwaysRespawnAtSpawn()
                && (event.isBedSpawn() || event.isAnchorSpawn())) {
            return;
        }

        final var spawnOpt = getPlugin().getSpawn();
        if (spawnOpt.isEmpty()) {
            return;
        }

        final Location loc = BukkitHuskHomes.Adapter.adapt(spawnOpt.get());
        if (loc == null || loc.getWorld() == null) {
            return;
        }

        event.setRespawnLocation(loc);
    }

    @Override
    @NotNull
    protected BukkitHuskHomes getPlugin() {
        return (BukkitHuskHomes) super.getPlugin();
    }


}
