package net.william278.huskhomes.teleport;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.position.Position;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Cross-platform teleportation manager
 */
public abstract class TeleportManager {

    /**
     * Instance of the implementing plugin
     */
    @NotNull
    private final HuskHomes plugin;

    /**
     * {@link Set} of currently processing teleports
     */
    @NotNull
    private final Set<CompletableFuture<Teleport>> processingTeleports;

    public TeleportManager(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
        this.processingTeleports = new HashSet<>();
    }

    /**
     * Teleport the player to a specified {@link Position}
     *
     * @param position the target {@link Position} to teleport to
     * @return A {@link CompletableFuture} callback returning the {@link TeleportResult} of completing the teleport
     */
    public CompletableFuture<TeleportResult> teleport(Player player, Position position) {
        if (position.server.equals(plugin.getServerData())) {
            return teleportLocally(new Teleport(player, position));
        } else {
            return teleportCrossServer(new Teleport(player, position));
        }
    }

    public abstract CompletableFuture<TeleportResult> teleportLocally(Teleport teleport);

    private CompletableFuture<TeleportResult> teleportCrossServer(Teleport teleport) {
        return null; //todo dispatch cross-server message here!
    }

}
