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

import com.pokeskies.fabricpluginmessaging.PluginMessagePacket;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.util.TriState;
import net.kyori.adventure.audience.Audience;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.Entity;
import net.william278.huskhomes.FabricHuskHomes;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.teleport.TeleportationException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FabricUser extends OnlineUser {

    private final String invulnerableTag = plugin.getKey("invulnerable").asString();
    private final ServerPlayer player;

    private FabricUser(@NotNull ServerPlayer player, @NotNull FabricHuskHomes plugin) {
        super(player.getUUID(), player.getGameProfile().name(), plugin);
        this.player = player;
    }

    @NotNull
    @ApiStatus.Internal
    public static FabricUser adapt(@NotNull ServerPlayer player, @NotNull FabricHuskHomes plugin) {
        return new FabricUser(player, plugin);
    }

    @Override
    public Position getPosition() {
        return FabricHuskHomes.Adapter.adapt(
                player.position(),
                getServerWorld(player),
                player.getYRot(),
                player.getXRot(),
                plugin.getServerName()
        );
    }

    @NotNull
    private ServerLevel getServerWorld(@NotNull ServerPlayer player) {
        return player.level();
    }

    @Override
    public Optional<Position> getBedSpawnPosition() {
        final var respawnConfig = player.getRespawnConfig();
        if (respawnConfig == null) {
            return Optional.empty();
        }

        final BlockPos spawn = respawnConfig.respawnData().pos();
        final float angle = respawnConfig.respawnData().yaw();
        final ResourceKey<net.minecraft.world.level.Level> world = respawnConfig.respawnData().dimension();
        return Optional.of(Position.at(
                spawn.getX(),
                spawn.getY(),
                spawn.getZ(),
                angle,
                0,
                World.from(
                        world.identifier().asString(),
                        UUID.nameUUIDFromBytes(world.identifier().asString().getBytes())
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
        final boolean op = Boolean.TRUE.equals(((FabricHuskHomes) plugin).getPermissions().getOrDefault(node, true));
        final boolean hasCommandLevel = player.permissions()
                .hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(3)));
        return Permissions.check(player, node, !op || hasCommandLevel);
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
            player.getPassengers().forEach(Entity::stopRiding);
            future.complete(null);
        }, this);
        return future;
    }

    @Override
    public void teleportLocally(@NotNull Location location, boolean async) throws TeleportationException {
        final MinecraftServer server = player.level().getServer();
        if (server == null) {
            throw new TeleportationException(TeleportationException.Type.ILLEGAL_TARGET_COORDINATES, plugin);
        }

        final ServerLevel world = FabricHuskHomes.Adapter.adapt(location.getWorld(), server);
        if (world == null) {
            throw new TeleportationException(TeleportationException.Type.WORLD_NOT_FOUND, plugin);
        }

        plugin.runSync(() -> {
            player.stopRiding();
            player.getPassengers().forEach(Entity::stopRiding);
            player.fallDistance = 0f;
            player.teleportTo(
                    world,
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    Set.of(),
                    location.getYaw(),
                    location.getPitch(),
                    true
            );
        }, this);
    }

    @Override
    public void sendPluginMessage(byte[] message) {
        ServerPlayNetworking.send(player, new PluginMessagePacket(message));
    }

    @Override
    public boolean isMoving() {
        return player.isInWater() || player.isFallFlying() || player.isSprinting() || player.isShiftKeyDown();
    }

    @Override
    public boolean isVanished() {
        return false;
    }

    @Override
    public boolean hasInvulnerability() {
        return markedAsInvulnerable || player.entityTags().contains(invulnerableTag);
    }

    @Override
    public void handleInvulnerability() {
        final long invulnerableTicks = 20L * plugin.getSettings().getGeneral().getTeleportInvulnerabilityTime();
        if (invulnerableTicks <= 0) {
            return;
        }
        player.setInvulnerable(true);
        player.addTag(invulnerableTag);
        markedAsInvulnerable = true;
        plugin.runSyncDelayed(this::removeInvulnerabilityIfPermitted, this, invulnerableTicks);
    }

    @Override
    public void removeInvulnerabilityIfPermitted() {
        if (hasInvulnerability()) {
            player.setInvulnerable(false);
        }
        player.removeTag(invulnerableTag);
        markedAsInvulnerable = false;
    }

    @NotNull
    public ServerPlayer getPlayer() {
        return player;
    }

    @Override
    public boolean isValid() {
        return player.isAlive();
    }

}
