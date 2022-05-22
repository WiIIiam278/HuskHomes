package net.william278.huskhomes.teleport;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.position.Position;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Cross-platform teleportation manager
 */
public class TeleportManager {

    /**
     * Instance of the implementing plugin
     */
    @NotNull
    private final HuskHomes plugin;

    public TeleportManager(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
    }

    /**
     * Teleport a {@link Player} to a specified {@link Position}
     *
     * @param player   the {@link Player} to teleport
     * @param position the target {@link Position} to teleport to
     */
    public CompletableFuture<TeleportResult> teleport(Player player, Position position) {
        final Teleport teleport = new Teleport(player, position);
        if (position.server.equals(plugin.getServerData())) {
            return player.teleport(teleport.target);
        } else {
            return teleportCrossServer(teleport);
        }
    }

    private CompletableFuture<TeleportResult> teleportCrossServer(Teleport teleport) {
        return CompletableFuture.supplyAsync(() -> {
            //todo
            return TeleportResult.COMPLETED_CROSS_SERVER;
        });
    }

}
