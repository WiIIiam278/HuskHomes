package net.william278.huskhomes.listener;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.BukkitPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class BukkitEventListener extends EventListener implements Listener {

    public BukkitEventListener(@NotNull HuskHomes implementor) {
        super(implementor);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        super.onPlayerJoin(BukkitPlayer.adapt(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        super.onPlayerLeave(BukkitPlayer.adapt(event.getPlayer()));
    }

}
