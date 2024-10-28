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

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.PaperHuskHomes;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.user.BukkitUser;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;

public class PaperEventListener extends BukkitEventListener implements Listener {

    public PaperEventListener(@NotNull PaperHuskHomes plugin) {
        super(plugin);
        this.usePaperEvents = true;
    }

    @Override
    public void register() {
        ((PaperHuskHomes) plugin).getServer().getPluginManager().registerEvents(this, (PaperHuskHomes) plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerUpdateRespawnLocation(PlayerSetSpawnEvent event) {
        final Settings.CrossServerSettings crossServer = plugin.getSettings().getCrossServer();
        if (!(crossServer.isEnabled() && crossServer.isGlobalRespawning())) {
            return;
        }

        // Ensure the updated location is correct
        final Location location = event.getLocation();
        if (location == null) {
            return;
        }

        // Update the player's respawn location
        this.handlePlayerUpdateSpawnPoint(
                BukkitUser.adapt(event.getPlayer(), (BukkitHuskHomes) plugin),
                BukkitHuskHomes.Adapter.adapt(location, plugin.getServerName())
        );
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    @Override
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (event.getRespawnReason() != PlayerRespawnEvent.RespawnReason.DEATH) {
            return;
        }
        super.handlePlayerRespawn(BukkitUser.adapt(event.getPlayer(), (BukkitHuskHomes) plugin));
    }

}
