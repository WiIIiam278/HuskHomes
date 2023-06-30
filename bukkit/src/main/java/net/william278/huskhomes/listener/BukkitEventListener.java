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
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.user.BukkitUser;
import net.william278.huskhomes.util.BukkitAdapter;
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

    protected boolean checkForBed = true;

    public BukkitEventListener(@NotNull BukkitHuskHomes plugin) {
        super(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        super.handlePlayerJoin(BukkitUser.adapt(event.getPlayer(), (BukkitHuskHomes) plugin));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLeave(PlayerQuitEvent event) {
        super.handlePlayerLeave(BukkitUser.adapt(event.getPlayer(), (BukkitHuskHomes) plugin));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        super.handlePlayerDeath(BukkitUser.adapt(event.getEntity(), (BukkitHuskHomes) plugin));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (event.getRespawnReason() != PlayerRespawnEvent.RespawnReason.DEATH) {
            return;
        }
        super.handlePlayerRespawn(BukkitUser.adapt(event.getPlayer(), (BukkitHuskHomes) plugin));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        final Player player = event.getPlayer();

        // Return if the disconnecting entity is a Citizens NPC, or if the teleport was naturally caused
        if (player.hasMetadata("NPC")) {
            return;
        }
        if (!(event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND
                || event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN)) {
            return;
        }

        final BukkitUser bukkitUser = BukkitUser.adapt(player, (BukkitHuskHomes) plugin);
        BukkitAdapter.adaptLocation(event.getFrom()).ifPresent(sourceLocation ->
                handlePlayerTeleport(bukkitUser, Position.at(sourceLocation, plugin.getServerName())));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerUpdateRespawnLocation(PlayerInteractEvent event) {
        if (!checkForBed || !(plugin.getSettings().doCrossServer() && plugin.getSettings().isGlobalRespawning())) {
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
        BukkitAdapter.adaptLocation(location).ifPresent(adaptedLocation -> super.handlePlayerUpdateSpawnPoint(
                BukkitUser.adapt(event.getPlayer(), (BukkitHuskHomes) plugin),
                Position.at(adaptedLocation, plugin.getServerName()))
        );
    }


}
