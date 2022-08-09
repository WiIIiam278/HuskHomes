package net.william278.huskhomes.player;

import de.themoep.minedown.MineDown;
import io.papermc.lib.PaperLib;
import net.md_5.bungee.api.ChatMessageType;
import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.TeleportResult;
import net.william278.huskhomes.util.EconomyUnsupportedException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Bukkit implementation of an {@link OnlineUser}
 */
public class BukkitPlayer extends OnlineUser {

    private final Player player;

    private BukkitPlayer(@NotNull Player player) {
        super(player.getUniqueId(), player.getName());
        this.player = player;
    }

    /**
     * Adapt a {@link Player} to a {@link OnlineUser}
     * @param player the online {@link Player} to adapt
     * @return the adapted {@link OnlineUser}
     */
    @NotNull
    public static BukkitPlayer adapt(@NotNull Player player) {
        return new BukkitPlayer(player);
    }

    /**
     * Get an online {@link BukkitPlayer} by their UUID
     * @param uuid the UUID of the player to find
     * @return an {@link Optional} containing the {@link BukkitPlayer} if found; {@link Optional#empty()} otherwise
     */
    public static Optional<BukkitPlayer> get(@NotNull UUID uuid) {
        final Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            return Optional.of(adapt(player));
        }
        return Optional.empty();
    }

    /**
     * Get an online {@link BukkitPlayer} by their exact username
     * @param username the UUID of the player to find
     * @return an {@link Optional} containing the {@link BukkitPlayer} if found; {@link Optional#empty()} otherwise
     */
    public static Optional<BukkitPlayer> get(@NotNull String username) {
        final Player player = Bukkit.getPlayerExact(username);
        if (player != null) {
            return Optional.of(adapt(player));
        }
        return Optional.empty();
    }

    @Override
    public CompletableFuture<Position> getPosition() {
        final Location location = getLocation();
        return BukkitHuskHomes.getInstance().getServer(this).thenApplyAsync(server -> new Position(
                location.x, location.y, location.z, location.yaw, location.pitch, location.world, server));

    }

    @Override
    public Location getLocation() {
        return BukkitHuskHomes.BukkitAdapter.adaptLocation(player.getLocation());
    }

    @Override
    public double getHealth() {
        return player.getHealth();
    }

    @Override
    public boolean hasPermission(@NotNull String node) {
        return player.hasPermission(node);
    }

    @Override
    public void sendActionBar(@NotNull MineDown mineDown) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, mineDown.replace().toComponent());
    }

    @Override
    public void sendMessage(@NotNull MineDown mineDown) {
        player.spigot().sendMessage(mineDown.replace().toComponent());
    }

    @Override
    public CompletableFuture<TeleportResult> teleport(Location location) {
        return CompletableFuture.supplyAsync(() -> {
            final Optional<org.bukkit.Location> bukkitLocation = BukkitHuskHomes.BukkitAdapter.adaptLocation(location);
            if (bukkitLocation.isEmpty()) {
                return TeleportResult.FAILED_INVALID_WORLD;
            }
            assert bukkitLocation.get().getWorld() != null;
            if (!bukkitLocation.get().getWorld().getWorldBorder().isInside(bukkitLocation.get())) {
                return TeleportResult.FAILED_ILLEGAL_COORDINATES;
            }
            final CompletableFuture<TeleportResult> resultCompletableFuture = new CompletableFuture<>();
            Bukkit.getScheduler().runTask(BukkitHuskHomes.getInstance(), () ->
                    PaperLib.teleportAsync(player, bukkitLocation.get(), PlayerTeleportEvent.TeleportCause.COMMAND)
                            .thenAccept(result -> {
                                if (result) {
                                    resultCompletableFuture.completeAsync(() -> TeleportResult.COMPLETED_LOCALLY);
                                } else {
                                    resultCompletableFuture.completeAsync(() -> TeleportResult.FAILED_INVALID_WORLD);
                                }
                            }));
            return resultCompletableFuture.join();
        });
    }

    @Override
    public CompletableFuture<Integer> getMaxHomes() {
        return null; //todo
    }

    @Override
    public CompletableFuture<Integer> getFreeHomes() {
        return null; //todo
    }

    @Override
    public double getEconomyBalance() throws EconomyUnsupportedException {
        return 0; //todo
    }

    @Override
    public void deductEconomyBalance() throws EconomyUnsupportedException {
        //todo
    }

    /**
     * Send a Bukkit plugin message
     */
    public void sendPluginMessage(@NotNull Plugin source, @NotNull String channel, @NotNull byte[] message) {
        player.sendPluginMessage(source, channel, message);
    }
}
