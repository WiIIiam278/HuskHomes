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
import org.bukkit.Location;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;

public class BukkitEventListener extends EventListener implements Listener {

    protected boolean usePaperEvents = false;

    public BukkitEventListener(@NotNull BukkitHuskHomes plugin) {
        super(plugin);
    }

    @Override
    public void register() {
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        super.handlePlayerJoin(getPlugin().getOnlineUser(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLeave(PlayerQuitEvent event) {
        super.handlePlayerLeave(getPlugin().getOnlineUser(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        super.handlePlayerDeath(getPlugin().getOnlineUser(event.getEntity()));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (usePaperEvents) {
            return;
        }
        getPlugin().getOnlineUserMap().remove(event.getPlayer().getUniqueId());
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

    @Override
    @NotNull
    protected BukkitHuskHomes getPlugin() {
        return (BukkitHuskHomes) super.getPlugin();
    }


}
