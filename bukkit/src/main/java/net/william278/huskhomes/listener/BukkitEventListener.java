package net.william278.huskhomes.listener;

import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.player.BukkitPlayer;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.util.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class BukkitEventListener extends EventListener implements Listener {

    public BukkitEventListener(@NotNull BukkitHuskHomes huskHomes) {
        super(huskHomes);
        Bukkit.getServer().getPluginManager().registerEvents(this, huskHomes);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        super.handlePlayerJoin(BukkitPlayer.adapt(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLeave(PlayerQuitEvent event) {
        super.handlePlayerLeave(BukkitPlayer.adapt(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        super.handlePlayerDeath(BukkitPlayer.adapt(event.getEntity()));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        super.handlePlayerRespawn(BukkitPlayer.adapt(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerUpdateRespawnLocation(PlayerInteractEvent event) {
        if (!(plugin.getSettings().crossServer && plugin.getSettings().globalRespawning)) return;
        if (event.getClickedBlock() == null) return;
        if (!(event.getClickedBlock().getBlockData() instanceof Bed
              || event.getClickedBlock().getBlockData() instanceof RespawnAnchor)) return;

        final Location location = event.getPlayer().getBedSpawnLocation();
        if (location == null) return;

        // Update the player's respawn location
        CompletableFuture.runAsync(() -> BukkitAdapter.adaptLocation(location).ifPresent(adaptedLocation -> {
            final OnlineUser user = BukkitPlayer.adapt(event.getPlayer());
            super.handlePlayerUpdateSpawnPoint(user, new Position(
                    adaptedLocation.x, adaptedLocation.y, adaptedLocation.z,
                    adaptedLocation.yaw, adaptedLocation.pitch,
                    adaptedLocation.world, plugin.getServer(user).join()));
        }));
    }

}
