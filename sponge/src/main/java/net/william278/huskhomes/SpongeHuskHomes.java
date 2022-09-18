package net.william278.huskhomes;

import net.william278.desertwell.Version;
import net.william278.huskhomes.command.CommandBase;
import net.william278.huskhomes.config.CachedSpawn;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.event.EventDispatcher;
import net.william278.huskhomes.hook.PluginHook;
import net.william278.huskhomes.messenger.NetworkMessenger;
import net.william278.huskhomes.migrator.Migrator;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.SavedPositionManager;
import net.william278.huskhomes.position.Server;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.random.RandomTeleportEngine;
import net.william278.huskhomes.request.RequestManager;
import net.william278.huskhomes.teleport.TeleportManager;
import net.william278.huskhomes.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Plugin("huskhomes")
public class SpongeHuskHomes implements HuskHomes {

    @Override
    public @NotNull Logger getLoggingAdapter() {
        return null;
    }

    @Override
    public @NotNull List<OnlineUser> getOnlinePlayers() {
        return null;
    }

    @Override
    public @NotNull Settings getSettings() {
        return null;
    }

    @Override
    public @NotNull Locales getLocales() {
        return null;
    }

    @Override
    public @NotNull Database getDatabase() {
        return null;
    }

    @Override
    public @NotNull Cache getCache() {
        return null;
    }

    @Override
    public @NotNull TeleportManager getTeleportManager() {
        return null;
    }

    @Override
    public @NotNull RequestManager getRequestManager() {
        return null;
    }

    @Override
    public @NotNull SavedPositionManager getSavedPositionManager() {
        return null;
    }

    @Override
    public @NotNull NetworkMessenger getNetworkMessenger() throws HuskHomesException {
        return null;
    }

    @Override
    public @NotNull RandomTeleportEngine getRandomTeleportEngine() {
        return null;
    }

    @Override
    public void setRandomTeleportEngine(@NotNull RandomTeleportEngine randomTeleportEngine) {

    }

    @Override
    public @NotNull EventDispatcher getEventDispatcher() {
        return null;
    }

    @Override
    public List<Migrator> getMigrators() {
        return null;
    }

    @Override
    public Optional<CachedSpawn> getLocalCachedSpawn() {
        return Optional.empty();
    }

    @Override
    public void setServerSpawn(@NotNull Location location) {

    }

    @Override
    public @NotNull Set<PluginHook> getPluginHooks() {
        return null;
    }

    @Override
    public CompletableFuture<Optional<Location>> getSafeGroundLocation(@NotNull Location location) {
        return null;
    }

    @Override
    public @NotNull Server getPluginServer() throws HuskHomesException {
        return null;
    }

    @Override
    public CompletableFuture<Void> fetchServer(@NotNull OnlineUser requester) {
        return null;
    }

    @Override
    public @Nullable InputStream getResource(@NotNull String name) {
        return null;
    }

    @Override
    public @NotNull List<World> getWorlds() {
        return null;
    }

    @Override
    public @NotNull Version getPluginVersion() {
        return null;
    }

    @Override
    public @NotNull List<CommandBase> getCommands() {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> reload() {
        return null;
    }

    @Override
    public void registerMetrics(int metricsId) {
        // todo
    }

}
