package net.william278.huskhomes.listener;

import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.user.BukkitUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.util.BukkitAdapter;
import org.bukkit.Bukkit;
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

    public BukkitEventListener(@NotNull BukkitHuskHomes huskHomes) {
        super(huskHomes);
        Bukkit.getServer().getPluginManager().registerEvents(this, huskHomes);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        super.handlePlayerJoin(BukkitUser.adapt(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLeave(PlayerQuitEvent event) {
        super.handlePlayerLeave(BukkitUser.adapt(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        super.handlePlayerDeath(BukkitUser.adapt(event.getEntity()));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        super.handlePlayerRespawn(BukkitUser.adapt(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        final Player player = event.getPlayer();

        // Return if the disconnecting entity is a Citizens NPC, or if the teleport was naturally caused
        if (player.hasMetadata("NPC")) return;
        if (!(event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND
              || event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN)) return;

        final BukkitUser bukkitUser = BukkitUser.adapt(player);
        BukkitAdapter.adaptLocation(event.getFrom()).ifPresent(sourceLocation ->
                handlePlayerTeleport(bukkitUser, Position.at(sourceLocation, plugin.getServerName())));
    }

    //todo When defining paper-plugin.yml files gets merged, use the PlayerSetSpawnEvent in the paper module
    // (https://jd.papermc.io/paper/1.19/com/destroystokyo/paper/event/player/PlayerSetSpawnEvent.html)
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerUpdateRespawnLocation(PlayerInteractEvent event) {
        if (!(plugin.getSettings().isCrossServer() && plugin.getSettings().isGlobalRespawning())) return;
        if (event.getClickedBlock() == null) return;
        if (!(event.getClickedBlock().getBlockData() instanceof Bed
              || event.getClickedBlock().getBlockData() instanceof RespawnAnchor)) return;

        final Location location = event.getPlayer().getBedSpawnLocation();
        if (location == null) return;

        // Update the player's respawn location
        BukkitAdapter.adaptLocation(location).ifPresent(adaptedLocation -> {
            final OnlineUser onlineUser = BukkitUser.adapt(event.getPlayer());
            super.handlePlayerUpdateSpawnPoint(onlineUser, Position.at(
                    adaptedLocation.getX(), adaptedLocation.getY(), adaptedLocation.getZ(),
                    adaptedLocation.getYaw(), adaptedLocation.getPitch(),
                    adaptedLocation.getWorld(), plugin.getServerName()));
        });
    }

}
