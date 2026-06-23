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

import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.user.OnlineUser;
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

    private static final int MAX_RESPAWN_POLL_ATTEMPTS = 200;

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
        handleLocalServerRespawn(event);
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
     * Poll on the entity scheduler until the player has respawned, then teleport to spawn if needed.
     */
    private void startRespawnPolling(@NotNull Player player) {
        pollRespawn(player.getUniqueId(), 0, getPlugin().getOnlineUser(player));
    }

    private void pollRespawn(@NotNull UUID uuid, int attempt, @NotNull OnlineUser user) {
        if (!pendingRespawnTeleport.contains(uuid)) {
            return;
        }
        if (attempt >= MAX_RESPAWN_POLL_ATTEMPTS) {
            pendingRespawnTeleport.remove(uuid);
            return;
        }

        getPlugin().runSyncDelayed(() -> {
            if (!pendingRespawnTeleport.contains(uuid)) {
                return;
            }
            final Player onlinePlayer = Bukkit.getPlayer(uuid);
            if (onlinePlayer == null || !onlinePlayer.isOnline()) {
                pendingRespawnTeleport.remove(uuid);
                return;
            }
            if (!onlinePlayer.isDead()) {
                pendingRespawnTeleport.remove(uuid);
                teleportToSpawnIfNeeded(onlinePlayer);
                return;
            }
            pollRespawn(uuid, attempt + 1, getPlugin().getOnlineUser(onlinePlayer));
        }, user, 1L);
    }

    private void teleportToSpawnIfNeeded(@NotNull Player player) {
        final Settings.CrossServerSettings crossServer = getPlugin().getSettings().getCrossServer();
        if (crossServer.isEnabled() && crossServer.isGlobalRespawning()) {
            return;
        }

        if (!getPlugin().getSettings().getGeneral().isAlwaysRespawnAtSpawn()
                && player.getBedSpawnLocation() != null) {
            return;
        }

        getPlugin().getSpawn().ifPresent(spawn -> {
            final Location location = BukkitHuskHomes.Adapter.adapt(spawn);
            if (location.getWorld() == null) {
                return;
            }
            player.setFallDistance(0f);
            getPlugin().getPlatformOperations().teleport(
                    player, location, PlayerTeleportEvent.TeleportCause.PLUGIN, true
            );
        });
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

        getPlugin().getSpawn().ifPresent(spawn -> {
            final Location location = BukkitHuskHomes.Adapter.adapt(spawn);
            if (location.getWorld() != null) {
                event.setRespawnLocation(location);
            }
        });
    }

    @Override
    @NotNull
    protected BukkitHuskHomes getPlugin() {
        return (BukkitHuskHomes) super.getPlugin();
    }

}
