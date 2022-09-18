package net.william278.huskhomes.player;

import de.themoep.minedown.MineDown;
import net.william278.huskhomes.HuskHomesException;
import net.william278.huskhomes.SpongeHuskHomes;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.TeleportResult;
import net.william278.huskhomes.util.SpongeAdapter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SpongePlayer extends OnlineUser {

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
//        player.get(Keys.RESPAWN_LOCATIONS).map(val -> {
//            val.get();
//        }).map(Position::new);
//        return SpongeAdapter.adaptLocation(player.profile().properties())
//                .map(position -> new Position(position, SpongeHuskHomes.getInstance().getPluginServer()));
    }

    @Override
    public double getHealth() {
        return 0;
    }

    @Override
    public boolean hasPermission(@NotNull String node) {
        return false;
    }

    @Override
    public @NotNull Map<String, Boolean> getPermissions() {
        return null;
    }

    @Override
    public void sendTitle(@NotNull MineDown mineDown, boolean subTitle) {

    }

    @Override
    public void sendActionBar(@NotNull MineDown mineDown) {

    }

    @Override
    public void sendMessage(@NotNull MineDown mineDown) {

    }

    @Override
    public void sendMinecraftMessage(@NotNull String translationKey) {

    }

    @Override
    public void playSound(@NotNull String soundEffect) {

    }

    @Override
    public CompletableFuture<TeleportResult> teleport(@NotNull Location location, boolean asynchronous) {
        return null;
    }

    @Override
    public boolean isMoving() {
        return false;
    }

    @Override
    public boolean isVanished() {
        return false;
    }
}
