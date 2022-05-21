package net.william278.huskhomes.data;

import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.PositionMeta;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.teleport.Teleport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

//todo write (hikari for this; direct driver for SQLite probably)
public class MySqlDatabase extends Database {

    public MySqlDatabase(@NotNull Settings settings) {
        super(settings);
    }

    @Override
    public CompletableFuture<Void> initialize() {
        return null;
    }

    @Override
    protected CompletableFuture<Integer> setPosition(@NotNull Position position) {
        return null;
    }

    @Override
    protected CompletableFuture<Integer> setPositionMeta(@NotNull PositionMeta meta) {
        return null;
    }

    @Override
    public CompletableFuture<Void> ensurePlayer(@NotNull Player player) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<Player>> getPlayerByName(@NotNull String name) {
        return null;
    }

    @Override
    public CompletableFuture<List<Home>> getHomes(@NotNull Player player) {
        return null;
    }

    @Override
    public CompletableFuture<List<Warp>> getWarps() {
        return null;
    }

    @Override
    public CompletableFuture<List<Home>> getPublicHomes() {
        return null;
    }

    @Override
    public CompletableFuture<Optional<Home>> getHome(@NotNull Player player, @NotNull String homeName) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<Home>> getHome(@NotNull UUID uuid) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<Warp>> getWarp(@NotNull String warpName) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<Warp>> getWarp(@NotNull UUID uuid) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<Teleport>> getCurrentTeleport(@NotNull Player player) {
        return null;
    }

    @Override
    public CompletableFuture<Void> setCurrentTeleport(@NotNull Player player, @Nullable Teleport teleport) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<Position>> getLastPosition(@NotNull Player player) {
        return null;
    }

    @Override
    public CompletableFuture<Void> setLastPosition(@NotNull Player player, @NotNull Position position) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<Position>> getOfflinePosition(@NotNull Player player) {
        return null;
    }

    @Override
    public CompletableFuture<Void> setOfflinePosition(@NotNull Player player, @NotNull Position position) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<Position>> getRespawnPosition(@NotNull Player player) {
        return null;
    }

    @Override
    public CompletableFuture<Void> setRespawnPosition(@NotNull Player player, @Nullable Position position) {
        return null;
    }

    @Override
    public CompletableFuture<Void> setHome(@NotNull Home home) {
        return null;
    }

    @Override
    public CompletableFuture<Void> setWarp(@NotNull Warp warp) {
        return null;
    }

    @Override
    public CompletableFuture<Void> deleteHome(@NotNull UUID uuid) {
        return null;
    }

    @Override
    public CompletableFuture<Void> deleteWarp(@NotNull Warp uuid) {
        return null;
    }
}
