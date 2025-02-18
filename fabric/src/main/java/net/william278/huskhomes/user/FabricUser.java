/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.huskhomes.user;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.util.TriState;
import net.kyori.adventure.audience.Audience;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.william278.huskhomes.FabricHuskHomes;
import net.william278.huskhomes.network.FabricPluginMessage;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.teleport.TeleportationException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class FabricUser extends OnlineUser {

    private final String INVULNERABLE_TAG = plugin.getKey("invulnerable").asString();
    private final ServerPlayerEntity player;

    private FabricUser(@NotNull ServerPlayerEntity player, @NotNull FabricHuskHomes plugin) {
        super(player.getUuid(), player.getGameProfile().getName(), plugin);
        this.player = player;
    }

    @NotNull
    @ApiStatus.Internal
    public static FabricUser adapt(@NotNull ServerPlayerEntity player, @NotNull FabricHuskHomes plugin) {
        return new FabricUser(player, plugin);
    }

    @Override
    public Position getPosition() {
        return FabricHuskHomes.Adapter.adapt(
                player.getPos(),
                player.getServerWorld(),
                player.getYaw(), player.getPitch(),
                plugin.getServerName()
        );
    }

    @Override
    public Optional<Position> getBedSpawnPosition() {
        final BlockPos spawn = player.getSpawnPointPosition();
        if (spawn == null) {
            return Optional.empty();
        }

        return Optional.of(Position.at(
                spawn.getX(), spawn.getY(), spawn.getZ(),
                player.getSpawnAngle(), 0,
                World.from(
                        player.getSpawnPointDimension().getValue().asString(),
                        UUID.nameUUIDFromBytes(player.getSpawnPointDimension().getValue().asString().getBytes())
                ),
                plugin.getServerName()
        ));
    }

    @Override
    public double getHealth() {
        return player.getHealth();
    }

    @Override
    public boolean isPermissionSet(@NotNull String permission) {
        return Permissions.getPermissionValue(player, permission) != TriState.DEFAULT;
    }

    @Override
    public boolean hasPermission(@NotNull String node) {
        boolean op = Boolean.TRUE.equals(((FabricHuskHomes) plugin).getPermissions().getOrDefault(node, true));
        return Permissions.check(player, node, !op || player.hasPermissionLevel(3));
    }

    @Override
    @NotNull
    public Map<String, Boolean> getPermissions() {
        return ((FabricHuskHomes) plugin).getPermissions().entrySet().stream()
                .filter(entry -> Permissions.check(player, entry.getKey(), entry.getValue()))
                .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
    }

    @Override
    @NotNull
    protected List<Integer> getNumericalPermissions(@NotNull String nodePrefix) {
        final List<Integer> permissions = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            if (hasPermission(nodePrefix + i)) {
                permissions.add(i);
            }
        }
        return permissions.stream().sorted(Collections.reverseOrder()).toList();
    }

    @Override
    @NotNull
    public Audience getAudience() {
        return player;
    }

    @Override
    public CompletableFuture<Void> dismount() {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        plugin.runSync(() -> {
            player.stopRiding();
            player.getPassengerList().forEach(Entity::stopRiding);
            future.complete(null);
        }, this);
        return future;
    }

    @Override
    public void teleportLocally(@NotNull Location location, boolean async) throws TeleportationException {
        final MinecraftServer server = player.getServer();
        if (server == null) {
            throw new TeleportationException(TeleportationException.Type.ILLEGAL_TARGET_COORDINATES, plugin);
        }
        final ServerWorld world = FabricHuskHomes.Adapter.adapt(location.getWorld(), server);
        if (world == null) {
            throw new TeleportationException(TeleportationException.Type.WORLD_NOT_FOUND, plugin);
        }

        // Synchronously teleport
        plugin.runSync(() -> {
            player.stopRiding();
            player.getPassengerList().forEach(Entity::stopRiding);
            player.teleportTo(FabricHuskHomes.Adapter.adapt(location, server, entity -> handleInvulnerability()));
        }, this);
    }

    @Override
    public void sendPluginMessage(byte[] message) {
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(new FabricPluginMessage(message)));
    }

    @Override
    public boolean isMoving() {
        return player.isTouchingWater() || player.isGliding() || player.isSprinting() || player.isSneaking();
    }

    @Override
    public boolean isVanished() {
        return false;
    }

    @Override
    public boolean hasInvulnerability() {
        return markedAsInvulnerable || player.getCommandTags().contains(INVULNERABLE_TAG);
    }

    @Override
    public void handleInvulnerability() {
        final long invulnerableTicks = 20L * plugin.getSettings().getGeneral().getTeleportInvulnerabilityTime();
        if (invulnerableTicks <= 0) {
            return;
        }
        player.setInvulnerable(true);
        player.getCommandTags().add(INVULNERABLE_TAG);
        markedAsInvulnerable = true;
        plugin.runSyncDelayed(this::removeInvulnerabilityIfPermitted, this, invulnerableTicks);
    }

    @Override
    public void removeInvulnerabilityIfPermitted() {
        if (this.hasInvulnerability()) {
            player.setInvulnerable(false);
        }
        player.removeCommandTag(INVULNERABLE_TAG);
        markedAsInvulnerable = false;
    }

    @NotNull
    public ServerPlayerEntity getPlayer() {
        return player;
    }

    /**
     * Check if the teleporter can teleport.
     *
     * @return true if the teleport may complete.
     */
    @Override
    public boolean isValid() {
        return player.isAlive();
    }
}
