package net.william278.huskhomes.player;

import net.kyori.adventure.audience.Audience;
import net.william278.huskhomes.HuskHomesException;
import net.william278.huskhomes.SpongeHuskHomes;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.TeleportResult;
import net.william278.huskhomes.util.SpongeAdapter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SpongePlayer extends OnlineUser {

    // The Sponge player
    private final Player player;

    private SpongePlayer(@NotNull Player player) {
        super(player.uniqueId(), player.name());
        this.player = player;
    }

    /**
     * Adapt a {@link Player} to a {@link OnlineUser}
     *
     * @param player the online {@link Player} to adapt
     * @return the adapted {@link OnlineUser}
     */
    @NotNull
    public static SpongePlayer adapt(@NotNull Player player) {
        return new SpongePlayer(player);
    }

    /**
     * Get an online {@link SpongePlayer} by their exact username
     *
     * @param username the UUID of the player to find
     * @return an {@link Optional} containing the {@link SpongePlayer} if found; {@link Optional#empty()} otherwise
     */
    public static Optional<SpongePlayer> get(@NotNull String username) {
        return Sponge.server().player(username).map(SpongePlayer::adapt);
    }

    @Override
    public Position getPosition() {
        return new Position(SpongeAdapter.adaptLocation(player.serverLocation())
                .orElseThrow(() -> new HuskHomesException("Failed to get the position of a BukkitPlayer (null)")),
                SpongeHuskHomes.getInstance().getPluginServer());
    }

    @Override
    public Optional<Position> getBedSpawnPosition() {
        return Optional.empty();
        //todo
/*        player.get(Keys.RESPAWN_LOCATIONS).map(val -> {
            val.get();
        }).map(Position::new);
        return SpongeAdapter.adaptLocation(player.profile().properties())
                .map(position -> new Position(position, SpongeHuskHomes.getInstance().getPluginServer()));*/
    }

    @Override
    public double getHealth() {
        return player.health().asImmutable().get();
    }

    @Override
    public boolean hasPermission(@NotNull String node) {
        return true;
    } //todo

    @Override
    @NotNull
    public Map<String, Boolean> getPermissions() {
        return Collections.emptyMap(); // todo
    }

    @Override
    @NotNull
    protected Audience getAudience() {
        return player;
    }

    @Override
    public CompletableFuture<TeleportResult> teleportLocally(@NotNull Location location, boolean asynchronous) {
        final CompletableFuture<TeleportResult> future = new CompletableFuture<>();
        final Task teleportTask = Task.builder().execute(() -> {
            final Optional<ServerLocation> serverLocation = SpongeAdapter.adaptLocation(location);
            if (serverLocation.isEmpty()) {
                future.complete(TeleportResult.FAILED_INVALID_WORLD);
                return;
            }
            player.setLocation(serverLocation.get());
            future.complete(TeleportResult.COMPLETED_LOCALLY);
        }).plugin(SpongeHuskHomes.getInstance().getPluginContainer()).build();
        Sponge.server().scheduler().submit(teleportTask);
        return future;
    }

    @Override
    public boolean isMoving() {
        return player.velocity().get().lengthSquared() > 0.0075;
    }

    @Override
    public boolean isVanished() {
        return false; //todo
    }
}
