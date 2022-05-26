package net.william278.huskhomes.teleport;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.HuskHomesBukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Bukkit implementation of a {@link TeleportManager}, handling timed teleports through a BukkitRunnable
 */
public class BukkitTeleportManager extends TeleportManager {

    public BukkitTeleportManager(@NotNull HuskHomes implementor) {
        super(implementor);
    }

    @Override
    public CompletableFuture<TimedTeleport> processTimedTeleport(@NotNull final TimedTeleport teleport) {
        final CompletableFuture<TimedTeleport> timedTeleportCompletableFuture = new CompletableFuture<>();
        new BukkitRunnable() {
            @Override
            public void run() {
                // Tick (decrement) the timed teleport timer
                if (tickTimedTeleport(teleport, timedTeleportCompletableFuture)) {
                    this.cancel();
                }
            }
        }.runTaskTimerAsynchronously((HuskHomesBukkit) plugin, 0L, 20L);
        return timedTeleportCompletableFuture;
    }

}
