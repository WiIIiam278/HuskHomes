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
//#if MC>=260000
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.Entity;
//#elseif MC>=12108
//$$ import net.minecraft.command.permission.Permission;
//$$ import net.minecraft.command.permission.PermissionLevel;
//$$ import net.minecraft.entity.Entity;
//$$ import net.minecraft.registry.RegistryKey;
//$$ import net.minecraft.server.network.ServerPlayerEntity;
//$$ import net.minecraft.server.world.ServerWorld;
//$$ import net.minecraft.util.math.BlockPos;
//#else
//$$ import net.minecraft.entity.Entity;
//$$ import net.minecraft.registry.RegistryKey;
//$$ import net.minecraft.server.network.ServerPlayerEntity;
//$$ import net.minecraft.server.world.ServerWorld;
//$$ import net.minecraft.util.math.BlockPos;
//#endif
import net.minecraft.server.MinecraftServer;
import net.william278.huskhomes.FabricHuskHomes;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.teleport.TeleportationException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class FabricUser extends OnlineUser {

    private final String invulnerableTag = plugin.getKey("invulnerable").asString();
    private final
            //#if MC>=260000
            ServerPlayer
            //#else
            //$$ ServerPlayerEntity
            //#endif
            player;

    private FabricUser(@NotNull
            //#if MC>=260000
            ServerPlayer
            //#else
            //$$ ServerPlayerEntity
            //#endif
            player, @NotNull FabricHuskHomes plugin) {
        //#if MC>=260000
        super(player.getUUID(), player.getGameProfile().name(), plugin);
        //#elseif MC>=12108
        //$$ super(player.getUuid(), player.getGameProfile().name(), plugin);
        //#else
        //$$ super(player.getUuid(), player.getGameProfile().getName(), plugin);
        //#endif
        this.player = player;
    }

    @NotNull
    @ApiStatus.Internal
    public static FabricUser adapt(@NotNull
            //#if MC>=260000
            ServerPlayer
            //#else
            //$$ ServerPlayerEntity
            //#endif
            player, @NotNull FabricHuskHomes plugin) {
        return new FabricUser(player, plugin);
    }

    @Override
    public Position getPosition() {
        return FabricHuskHomes.Adapter.adapt(
                //#if MC>=260000
                player.position(),
                //#elseif MC>=12108
                //$$ player.getEntityPos(),
                //#else
                //$$ player.getPos(),
                //#endif
                getServerWorld(player),
                //#if MC>=260000
                player.getYRot(), player.getXRot(),
                //#else
                //$$ player.getYaw(), player.getPitch(),
                //#endif
                plugin.getServerName()
        );
    }

    @NotNull
    private
            //#if MC>=260000
            ServerLevel
            //#else
            //$$ ServerWorld
            //#endif
            getServerWorld(@NotNull
                    //#if MC>=260000
                    ServerPlayer
                    //#else
                    //$$ ServerPlayerEntity
                    //#endif
                    player) {
        //#if MC>=260000
        return player.level();
        //#elseif MC <=12105
        //$$ return player.getServerWorld();
        //#elseif MC >=12108
        //$$ return player.getEntityWorld();
        //#else
        //$$ return player.getWorld();
        //#endif
    }

    @Override
    public Optional<Position> getBedSpawnPosition() {
        //#if MC>=260000
        final var respawnConfig = player.getRespawnConfig();
        if (respawnConfig == null) {
            return Optional.empty();
        }

        final BlockPos spawn = respawnConfig.respawnData().pos();
        final float angle = respawnConfig.respawnData().yaw();
        final ResourceKey<net.minecraft.world.level.Level> world = respawnConfig.respawnData().dimension();
        //#elseif MC<=12104
        //$$ final BlockPos spawn = player.getSpawnPointPosition();
        //$$ final float angle = player.getSpawnAngle();
        //$$ final RegistryKey<net.minecraft.world.World> world = player.getSpawnPointDimension();
        //#elseif MC>=12108
        //$$ final BlockPos spawn = player.getRespawn() == null ? null : player.getRespawn().respawnData().getPos();
        //$$ final float angle = player.getRespawn() == null ? 0 : player.getRespawn().respawnData().yaw();
        //$$ final RegistryKey<net.minecraft.world.World> world = player.getRespawn() == null ? null : player.getRespawn().respawnData().getDimension();
        //#else
        //$$ final BlockPos spawn = player.getRespawn() == null ? null : player.getRespawn().pos();
        //$$ final float angle = player.getRespawn() == null ? 0 : player.getRespawn().angle();
        //$$ final RegistryKey<net.minecraft.world.World> world = player.getRespawn() == null ? null : player.getRespawn().dimension();
        //#endif
        if (spawn == null || world == null) {
            return Optional.empty();
        }

        return Optional.of(Position.at(
                spawn.getX(), spawn.getY(), spawn.getZ(),
                angle, 0,
                World.from(
                        //#if MC>=260000
                        world.identifier().asString(),
                        UUID.nameUUIDFromBytes(world.identifier().asString().getBytes())
                        //#else
                        //$$ world.getValue().asString(),
                        //$$ UUID.nameUUIDFromBytes(world.getValue().asString().getBytes())
                        //#endif
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
        //#if MC>=260000
        final boolean hasPermission = player.permissions()
                .hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(3)));
        //#elseif MC>=12108
        //$$ boolean hasPermission = player.getPermissions().hasPermission(new Permission.Level(PermissionLevel.fromLevel(3)));
        //#else
        //$$ boolean hasPermission = player.hasPermissionLevel(3);
        //#endif
        return Permissions.check(player, node, !op || hasPermission);
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
            //#if MC>=260000
            player.getPassengers().forEach(Entity::stopRiding);
            //#else
            //$$ player.getPassengerList().forEach(Entity::stopRiding);
            //#endif
            future.complete(null);
        }, this);
        return future;
    }

    @Override
    public void teleportLocally(@NotNull Location location, boolean async) throws TeleportationException {
        //#if MC>=260000
        final MinecraftServer server = player.level().getServer();
        //#elseif MC>=12108
        //$$ final MinecraftServer server = player.getEntityWorld().getServer();
        //#else
        //$$ final MinecraftServer server = player.getServer();
        //#endif

        if (server == null) {
            throw new TeleportationException(TeleportationException.Type.ILLEGAL_TARGET_COORDINATES, plugin);
        }
        final
                //#if MC>=260000
                ServerLevel
                //#else
                //$$ ServerWorld
                //#endif
                world = FabricHuskHomes.Adapter.adapt(location.getWorld(), server);
        if (world == null) {
            throw new TeleportationException(TeleportationException.Type.WORLD_NOT_FOUND, plugin);
        }

        // Synchronously teleport
        plugin.runSync(() -> {
            player.stopRiding();
            //#if MC>=260000
            player.getPassengers().forEach(Entity::stopRiding);
            //#else
            //$$ player.getPassengerList().forEach(Entity::stopRiding);
            //#endif
            player.fallDistance = 0f;
            //#if MC>=260000
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
            //#elseif MC>=12104
            //$$ player.teleport(
            //$$         world, location.getX(), location.getY(), location.getZ(),
            //$$         Set.of(),
            //$$         location.getYaw(), location.getPitch(),
            //$$         true
            //$$ );
            //#else
            //$$ player.teleport(
            //$$         world, location.getX(), location.getY(), location.getZ(),
            //$$         location.getYaw(), location.getPitch()
            //$$ );
            //#endif
        }, this);
    }

    @Override
    public void sendPluginMessage(byte[] message) {
        ServerPlayNetworking.send(player, new PluginMessagePacket(message));
    }

    @Override
    public boolean isMoving() {
        //#if MC>=260000
        return player.isInWater() || player.isFallFlying() || player.isSprinting() || player.isShiftKeyDown();
        //#elseif MC>=12103
        //$$ return player.isTouchingWater() || player.isGliding() || player.isSprinting() || player.isSneaking();
        //#else
        //$$ return player.isTouchingWater() || player.isSprinting() || player.isSneaking();
        //#endif
    }

    @Override
    public boolean isVanished() {
        return false;
    }

    @Override
    public boolean hasInvulnerability() {
        //#if MC>=260000
        return markedAsInvulnerable || player.entityTags().contains(invulnerableTag);
        //#else
        //$$ return markedAsInvulnerable || player.getCommandTags().contains(invulnerableTag);
        //#endif
    }

    @Override
    public void handleInvulnerability() {
        final long invulnerableTicks = 20L * plugin.getSettings().getGeneral().getTeleportInvulnerabilityTime();
        if (invulnerableTicks <= 0) {
            return;
        }
        player.setInvulnerable(true);
        //#if MC>=260000
        player.addTag(invulnerableTag);
        //#else
        //$$ player.getCommandTags().add(invulnerableTag);
        //#endif
        markedAsInvulnerable = true;
        plugin.runSyncDelayed(this::removeInvulnerabilityIfPermitted, this, invulnerableTicks);
    }

    @Override
    public void removeInvulnerabilityIfPermitted() {
        if (this.hasInvulnerability()) {
            player.setInvulnerable(false);
        }
        //#if MC>=260000
        player.removeTag(invulnerableTag);
        //#else
        //$$ player.removeCommandTag(invulnerableTag);
        //#endif
        markedAsInvulnerable = false;
    }

    @NotNull
    public
            //#if MC>=260000
            ServerPlayer
            //#else
            //$$ ServerPlayerEntity
            //#endif
            getPlayer() {
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
