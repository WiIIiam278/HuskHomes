package net.william278.huskhomes.player;

import io.papermc.lib.PaperLib;
import net.kyori.adventure.audience.Audience;
import net.william278.huskhomes.BukkitHuskHomes;
import net.william278.huskhomes.HuskHomesException;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.TeleportResult;
import net.william278.huskhomes.util.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Bukkit implementation of an {@link OnlineUser}
 */
public class BukkitPlayer extends OnlineUser {

    /**
     * Number of ticks to wait before {@link #sendPluginMessage(String, byte[]) sending a plugin message}.
     * </p>
     * This is needed because it is not possible to have players dispatch plugin messages in certain circumstance,
     * such as on the tick that they join the server.
     */
    private static final long PLUGIN_MESSAGE_DISPATCH_DELAY = 3L;

    // Instance of the implementing plugin
    private final BukkitHuskHomes plugin;

    // The Bukkit player
    private final Player player;

    private BukkitPlayer(@NotNull Player player) {
        super(player.getUniqueId(), player.getName());
        this.plugin = BukkitHuskHomes.getInstance();
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
    public Position getPosition() {
        return new Position(BukkitAdapter.adaptLocation(player.getLocation())
                .orElseThrow(() -> new HuskHomesException("Failed to get the position of a BukkitPlayer (null)")),
                plugin.getServerName());

    }

    @Override
    public Optional<Position> getBedSpawnPosition() {
        return Optional.ofNullable(player.getBedSpawnLocation()).flatMap(BukkitAdapter::adaptLocation)
                .map(location -> new Position(location, plugin.getServerName()));
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
    protected @NotNull Audience getAudience() {
        return plugin.getAudiences().player(player);
    }

    @Override
    public CompletableFuture<TeleportResult> teleportLocally(@NotNull Location location, boolean asynchronous) {
        final Optional<org.bukkit.Location> bukkitLocation = BukkitAdapter.adaptLocation(location);
        if (bukkitLocation.isEmpty()) {
            return CompletableFuture.completedFuture(TeleportResult.FAILED_INVALID_WORLD);
        }
        assert bukkitLocation.get().getWorld() != null;
        if (!bukkitLocation.get().getWorld().getWorldBorder().isInside(bukkitLocation.get())) {
            return CompletableFuture.completedFuture(TeleportResult.FAILED_ILLEGAL_COORDINATES);
        }
        final CompletableFuture<TeleportResult> resultCompletableFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (asynchronous) {
                PaperLib.teleportAsync(player, bukkitLocation.get(), PlayerTeleportEvent.TeleportCause.PLUGIN)
                        .thenAccept(result -> resultCompletableFuture.complete(
                                result ? TeleportResult.COMPLETED_LOCALLY : TeleportResult.FAILED_INVALID_WORLD));
            } else {
                player.teleport(bukkitLocation.get(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                resultCompletableFuture.complete(TeleportResult.COMPLETED_LOCALLY);
            }
        });
        return resultCompletableFuture;
    }

    @Override
    public boolean isMoving() {
        // Get the player momentum and return if they are moving
        return player.getVelocity().length() >= 0.1;
    }

    @Override
    public boolean isVanished() {
        // Return the value of the player's "vanished" metadata tag if they have it
        return player.getMetadata("vanished")
                .stream()
                .map(MetadataValue::asBoolean)
                .findFirst()
                .orElse(false);
    }

    /**
     * Send a Bukkit plugin message
     *
     * @implNote This is dispatched after {@link #PLUGIN_MESSAGE_DISPATCH_DELAY a delay} to ensure that the player
     * is ready to send receive messages
     */
    public void sendPluginMessage(@NotNull String channel, final byte[] message) {
        Bukkit.getScheduler().runTaskLater(plugin,
                () -> player.sendPluginMessage(plugin, channel, message), PLUGIN_MESSAGE_DISPATCH_DELAY);
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
