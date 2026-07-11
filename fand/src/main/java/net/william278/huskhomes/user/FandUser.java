/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 */

package net.william278.huskhomes.user;

import io.fand.api.entity.Player;
import io.fand.api.player.RespawnLocation;
import io.fand.api.world.Vector3;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.william278.huskhomes.FandAdapter;
import net.william278.huskhomes.FandHuskHomes;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.TeleportationException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public final class FandUser extends OnlineUser {

    private static final Key BUNGEE_CHANNEL = Key.key("bungeecord", "main");

    private final Player player;
    private final String invulnerableTag;

    private FandUser(@NotNull Player player, @NotNull FandHuskHomes plugin) {
        super(player.uniqueId(), player.name(), plugin);
        this.player = player;
        this.invulnerableTag = plugin.getKey("invulnerable").asString();
    }

    @ApiStatus.Internal
    @NotNull
    public static FandUser adapt(@NotNull Player player, @NotNull FandHuskHomes plugin) {
        return new FandUser(player, plugin);
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @Override
    public Position getPosition() {
        return FandAdapter.adapt(player, plugin.getServerName());
    }

    @Override
    public Optional<Position> getBedSpawnPosition() {
        return player.respawnLocation()
                .map(RespawnLocation::location)
                .map(location -> FandAdapter.adapt(location, plugin.getServerName()));
    }

    @Override
    public double getHealth() {
        return player.health();
    }

    @Override
    public boolean isPermissionSet(@NotNull String permission) {
        return player.permissionValue(permission).isPresent();
    }

    @Override
    public boolean hasPermission(@NotNull String node) {
        return player.can(node);
    }

    @Override
    @NotNull
    public Map<String, Boolean> getPermissions() {
        final Map<String, Boolean> values = new LinkedHashMap<>();
        ((FandHuskHomes) plugin).getPermissions().keySet()
                .forEach(permission -> values.put(permission, player.can(permission)));
        return values;
    }

    @Override
    @NotNull
    protected List<Integer> getNumericalPermissions(@NotNull String nodePrefix) {
        final List<Integer> permissions = new ArrayList<>();
        for (int value = 0; value < 100; value++) {
            if (hasPermission(nodePrefix + value)) {
                permissions.add(value);
            }
        }
        permissions.sort(Collections.reverseOrder());
        return List.copyOf(permissions);
    }

    @Override
    @NotNull
    public Audience getAudience() {
        return player;
    }

    @Override
    public CompletableFuture<Void> dismount() {
        return player.dismount().thenRun(player::ejectPassengers);
    }

    @Override
    public void teleportLocally(@NotNull Location location, boolean async) throws TeleportationException {
        final io.fand.api.world.Location destination = FandAdapter.adapt(location, ((FandHuskHomes) plugin).server())
                .orElseThrow(() -> new TeleportationException(TeleportationException.Type.WORLD_NOT_FOUND, plugin));
        player.teleport(destination).whenComplete((success, failure) -> {
            if (failure != null) {
                plugin.log(Level.WARNING, "Failed to teleport player " + getName(), failure);
            }
        });
    }

    @Override
    public void sendPluginMessage(byte[] message) {
        ((FandHuskHomes) plugin).getContext().pluginMessaging().send(player, BUNGEE_CHANNEL, message);
    }

    @Override
    public boolean isMoving() {
        final Vector3 velocity = player.velocity();
        return Math.abs(velocity.x()) > 0.001D
                || Math.abs(velocity.y()) > 0.001D
                || Math.abs(velocity.z()) > 0.001D;
    }

    @Override
    public boolean isVanished() {
        return false;
    }

    @Override
    public boolean hasInvulnerability() {
        return markedAsInvulnerable || player.scoreboardTags().contains(invulnerableTag);
    }

    @Override
    public void handleInvulnerability() {
        final long invulnerableTicks = 20L * plugin.getSettings().getGeneral().getTeleportInvulnerabilityTime();
        if (invulnerableTicks <= 0 || player.invulnerable()) {
            return;
        }
        markedAsInvulnerable = true;
        player.addScoreboardTag(invulnerableTag);
        player.setInvulnerable(true);
        plugin.runSyncDelayed(this::removeInvulnerabilityIfPermitted, this, invulnerableTicks);
    }

    @Override
    public void removeInvulnerabilityIfPermitted() {
        if (!hasInvulnerability()) {
            return;
        }
        player.setInvulnerable(false);
        player.removeScoreboardTag(invulnerableTag);
        markedAsInvulnerable = false;
    }

    @Override
    public boolean isValid() {
        return player.online();
    }
}
