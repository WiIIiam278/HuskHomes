package net.william278.huskhomes.teleport;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.BukkitHuskHomes;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
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
        return CompletableFuture.supplyAsync(() -> {
            CompletableFuture<TimedTeleport> teleportCompletableFuture = new CompletableFuture<>();
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Display countdown action bar message
                    plugin.getLocales().getLocale("teleporting_action_bar_countdown", Integer.toString(teleport.timeLeft))
                            .ifPresent(message -> teleport.getPlayer().sendActionBar(message));

                    // Tick (decrement) the timed teleport timer
                    final Optional<TimedTeleport> result = tickTimedTeleport(teleport);
                    if (result.isPresent()) {
                        teleportCompletableFuture.complete(teleport);
                        this.cancel();
                    }
                }
            }.runTaskTimerAsynchronously((BukkitHuskHomes) plugin, 0L, 20L);

            return teleportCompletableFuture.join();
        });
    }

}
