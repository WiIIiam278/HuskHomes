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
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.kyori.adventure.audience.Audience;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.william278.huskhomes.FabricHuskHomes;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.teleport.TeleportationException;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FabricUser extends OnlineUser {

    private final FabricHuskHomes plugin;
    private final ServerPlayerEntity player;

    private FabricUser(@NotNull ServerPlayerEntity player, @NotNull FabricHuskHomes plugin) {
        super(player.getUuid(), player.getEntityName());
        this.player = player;
        this.plugin = plugin;
    }

    @NotNull
    public static FabricUser adapt(@NotNull ServerPlayerEntity player, @NotNull FabricHuskHomes plugin) {
        return new FabricUser(player, plugin);
    }

    @Override
    public Position getPosition() {
        return Position.at(
                player.getX(), player.getY(), player.getZ(),
                player.getYaw(), player.getPitch(),
                World.from(
                        player.getWorld().getRegistryKey().getValue().asString(),
                        UUID.nameUUIDFromBytes(player.getWorld().getRegistryKey().getValue().asString().getBytes())
                ),
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
    public boolean hasPermission(@NotNull String node) {
        final boolean requiresOp = Boolean.TRUE.equals(plugin.getPermissions().getOrDefault(node, true));
        return Permissions.check(player, node, !requiresOp || player.hasPermissionLevel(3));
    }

    @Override
    @NotNull
    public Map<String, Boolean> getPermissions() {
        return plugin.getPermissions().entrySet().stream()
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
    public void playSound(@NotNull String soundEffect) throws IllegalArgumentException {
        // Do nothing - todo: Fix sounds when adventure-platform-fabric is updated.
    }

    @Override
    public void teleportLocally(@NotNull Location location, boolean async) throws TeleportationException {
        final MinecraftServer server = player.getServer();
        if (server == null) {
            throw new TeleportationException(TeleportationException.Type.ILLEGAL_TARGET_COORDINATES, plugin);
        }

        FabricDimensions.teleport(
                player,
                server.getWorld(server.getWorldRegistryKeys().stream()
                        .filter(key -> key.getValue().equals(Identifier.tryParse(location.getWorld().getName())))
                        .findFirst().orElseThrow(
                                () -> new TeleportationException(TeleportationException.Type.WORLD_NOT_FOUND, plugin)
                        )),
                new TeleportTarget(
                        new Vec3d(location.getX(), location.getY(), location.getZ()),
                        Vec3d.ZERO,
                        location.getYaw(),
                        location.getPitch()
                )
        );
    }

    @Override
    public void sendPluginMessage(@NotNull String channel, byte[] message) {
        final PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBytes(message);
        final CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(parseIdentifier(channel), buf);
        player.networkHandler.sendPacket(packet);
    }

    @Override
    public boolean isMoving() {
        return player.isTouchingWater() || player.isFallFlying() || player.isSprinting() || player.isSneaking();
    }

    @Override
    public boolean isVanished() {
        return false;
    }

    @NotNull
    private static Identifier parseIdentifier(@NotNull String channel) {
        if (channel.equals("BungeeCord")) {
            return new Identifier("bungeecord", "main");
        }
        return Optional.ofNullable(Identifier.tryParse(channel))
                .orElseThrow(() -> new IllegalArgumentException("Invalid channel name: " + channel));
    }

}
