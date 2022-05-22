package net.william278.huskhomes.player;

import de.themoep.minedown.MineDown;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.TeleportResult;
import org.bukkit.Bukkit;
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
        return get(user.uuid());
    }

    public static Optional<BukkitPlayer> get(@NotNull UUID uuid) {
        final org.bukkit.entity.Player bukkitPlayer = Bukkit.getPlayer(uuid);
        if (bukkitPlayer != null) {
            return Optional.of(adapt(bukkitPlayer));
        }
        return Optional.empty();
    }

    public static BukkitPlayer adapt(@NotNull org.bukkit.entity.Player player) {
        return new BukkitPlayer(player);
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
    public Position getPosition() {
        return null;
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
        return null; //todo
    }

    @Override
    public CompletableFuture<Integer> getMaxHomes() {
        return null; //todo
    }

    @Override
    public CompletableFuture<Integer> getFreeHomes() {
        return null; //todo
    }

}
