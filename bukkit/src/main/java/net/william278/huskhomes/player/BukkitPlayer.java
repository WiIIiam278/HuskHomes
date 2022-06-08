package net.william278.huskhomes.player;

import de.themoep.minedown.MineDown;
import io.papermc.lib.PaperLib;
import net.william278.huskhomes.HuskHomesBukkit;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.TeleportResult;
import net.william278.huskhomes.util.EconomyUnsupportedException;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BukkitPlayer implements Player {

    private final org.bukkit.entity.Player bukkitPlayer;

    private BukkitPlayer(org.bukkit.entity.Player bukkitPlayer) {
        this.bukkitPlayer = bukkitPlayer;
    }

    public static Optional<BukkitPlayer> get(@NotNull User user) {
        return get(user.uuid);
    }

    public static Optional<BukkitPlayer> get(@NotNull UUID uuid) {
        final org.bukkit.entity.Player bukkitPlayer = Bukkit.getPlayer(uuid);
        if (bukkitPlayer != null) {
            return Optional.of(adapt(bukkitPlayer));
        }
        return Optional.empty();
    }

    public static Optional<BukkitPlayer> get(@NotNull String username) {
        final org.bukkit.entity.Player bukkitPlayer = Bukkit.getPlayerExact(username);
        if (bukkitPlayer != null) {
            return Optional.of(adapt(bukkitPlayer));
        }
        return Optional.empty();
    }

    public static BukkitPlayer adapt(@NotNull org.bukkit.entity.Player player) {
        return new BukkitPlayer(player);
    }

    public org.bukkit.entity.Player toBukkitPlayer() {
        return bukkitPlayer;
    }

    @Override
    public String getName() {
        return bukkitPlayer.getName();
    }

    @Override
    public UUID getUuid() {
        return bukkitPlayer.getUniqueId();
    }

    @Override
    public CompletableFuture<Position> getPosition() {
        final Location location = getLocation();
        return HuskHomesBukkit.getInstance().getServer(this).thenApplyAsync(server -> new Position(
                location.x, location.y, location.z, location.yaw, location.pitch, location.world, server));

    }

    @Override
    public Location getLocation() {
        return HuskHomesBukkit.BukkitAdapter.adaptLocation(bukkitPlayer.getLocation());
    }

    @Override
    public double getHealth() {
        return bukkitPlayer.getHealth();
    }

    @Override
    public boolean hasPermission(@NotNull String node) {
        return bukkitPlayer.hasPermission(node);
    }

    @Override
    public void sendMessage(@NotNull MineDown mineDown) {
        bukkitPlayer.spigot().sendMessage(mineDown.toComponent());
    }

    @Override
    public CompletableFuture<TeleportResult> teleport(Location location) {
        return CompletableFuture.supplyAsync(() -> {
            final Optional<org.bukkit.Location> bukkitLocation = HuskHomesBukkit.BukkitAdapter.adaptLocation(location);
            if (bukkitLocation.isEmpty()) {
                return TeleportResult.FAILED_INVALID_WORLD;
            }
            assert bukkitLocation.get().getWorld() != null;
            if (!bukkitLocation.get().getWorld().getWorldBorder().isInside(bukkitLocation.get())) {
                return TeleportResult.FAILED_ILLEGAL_COORDINATES;
            }
            System.out.println("teleport 5");
            final CompletableFuture<TeleportResult> resultCompletableFuture = new CompletableFuture<>();
            Bukkit.getScheduler().runTask(HuskHomesBukkit.getInstance(), () ->
                    PaperLib.teleportAsync(bukkitPlayer, bukkitLocation.get(), PlayerTeleportEvent.TeleportCause.COMMAND)
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
        return 0;
    }

    @Override
    public void deductEconomyBalance() throws EconomyUnsupportedException {

    }

    /**
     * Send a Bukkit plugin message
     */
    public void sendPluginMessage(@NotNull Plugin source, @NotNull String channel, @NotNull byte[] message) {
        bukkitPlayer.sendPluginMessage(source, channel, message);
    }
}
