package net.william278.huskhomes.player;

import de.themoep.minedown.MineDown;
import io.papermc.lib.PaperLib;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.TeleportResult;
import net.william278.huskhomes.util.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
     *
     * @param player the online {@link Player} to adapt
     * @return the adapted {@link OnlineUser}
     */
    @NotNull
    public static BukkitPlayer adapt(@NotNull Player player) {
        return new BukkitPlayer(player);
    }

    /**
     * Get an online {@link BukkitPlayer} by their UUID
     *
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
     *
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
        return BukkitHuskHomes.getInstance().getServer(this).thenApply(server -> new Position(
                location.x, location.y, location.z, location.yaw, location.pitch, location.world, server));

    }

    @Override
    public CompletableFuture<Optional<Position>> getBedSpawnPosition() {
        return BukkitHuskHomes.getInstance().getServer(this).thenApply(server ->
                Optional.ofNullable(player.getBedSpawnLocation()).flatMap(BukkitAdapter::adaptLocation)
                        .map(location -> new Position(location.x, location.y, location.z,
                                location.yaw, location.pitch, location.world, server)));
    }

    @Override
    public Location getLocation() {
        return BukkitAdapter.adaptLocation(player.getLocation()).orElse(null);
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
    public @NotNull Map<String, Boolean> getPermissions() {
        return player.getEffectivePermissions().stream().collect(
                Collectors.toMap(PermissionAttachmentInfo::getPermission,
                        PermissionAttachmentInfo::getValue, (a, b) -> b));
    }

    @Override
    public void sendActionBar(@NotNull MineDown mineDown) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, mineDown.replace().toComponent());
    }

    @Override
    public void sendMessage(@NotNull MineDown mineDown) {
        final BaseComponent[] messageComponents = mineDown.replace().toComponent();
        if (messageComponents.length == 0) {
            return;
        }
        player.spigot().sendMessage(messageComponents);
    }

    @Override
    public CompletableFuture<TeleportResult> teleport(Location location) {
        return CompletableFuture.supplyAsync(() -> {
            final Optional<org.bukkit.Location> bukkitLocation = BukkitAdapter.adaptLocation(location);
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
                                    resultCompletableFuture.complete(TeleportResult.COMPLETED_LOCALLY);
                                } else {
                                    resultCompletableFuture.complete(TeleportResult.FAILED_INVALID_WORLD);
                                }
                            }));
            return resultCompletableFuture.join();
        });
    }

    /**
     * Send a Bukkit plugin message
     */
    public void sendPluginMessage(@NotNull Plugin source, @NotNull String channel, final byte[] message) {
        player.sendPluginMessage(source, channel, message);
    }

    /**
     * Return the {@link Player} wrapped by this {@link BukkitPlayer}
     *
     * @return the {@link Player} wrapped by this {@link BukkitPlayer}
     */
    public Player getPlayer() {
        return player;
    }
}
