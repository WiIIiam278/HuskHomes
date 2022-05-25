package net.william278.huskhomes.player;

import de.themoep.minedown.MineDown;
import io.papermc.lib.PaperLib;
import net.william278.huskhomes.HuskHomesBukkit;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.TeleportResult;
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

    public org.bukkit.entity.Player getBukkitPlayer() {
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
        final org.bukkit.Location location = bukkitPlayer.getLocation();
        return HuskHomesBukkit.getInstance().getServer(this).thenApplyAsync(server ->
                new Position(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(),
                        new net.william278.huskhomes.position.World(bukkitPlayer.getWorld().getName(),
                                bukkitPlayer.getWorld().getUID()), server));

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
        final Optional<org.bukkit.Location> bukkitLocation = HuskHomesBukkit.BukkitAdapter.adaptLocation(location);
        if (bukkitLocation.isEmpty()) {
            return CompletableFuture.supplyAsync(() -> TeleportResult.FAILED_INVALID_WORLD);
        }
        assert bukkitLocation.get().getWorld() != null;
        if (!bukkitLocation.get().getWorld().getWorldBorder().isInside(bukkitLocation.get())) {
            return CompletableFuture.supplyAsync(() -> TeleportResult.FAILED_ILLEGAL_COORDINATES);
        }
        return PaperLib.teleportAsync(bukkitPlayer, bukkitLocation.get(),
                        PlayerTeleportEvent.TeleportCause.COMMAND)
                .thenApplyAsync(result -> TeleportResult.COMPLETED_LOCALLY);
    }

    @Override
    public CompletableFuture<Integer> getMaxHomes() {
        return null; //todo
    }

    @Override
    public CompletableFuture<Integer> getFreeHomes() {
        return null; //todo
    }

    /**
     * Send a Bukkit plugin message
     */
    public void sendPluginMessage(@NotNull Plugin source, @NotNull String channel, byte @NotNull [] message) {
        bukkitPlayer.sendPluginMessage(source, channel, message);
    }
}
