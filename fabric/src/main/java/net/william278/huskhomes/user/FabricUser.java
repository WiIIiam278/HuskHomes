package net.william278.huskhomes.user;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.kyori.adventure.audience.Audience;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.william278.huskhomes.FabricHuskHomes;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.teleport.TeleportationException;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FabricUser extends OnlineUser {
    private final FabricHuskHomes plugin;
    private final ServerPlayerEntity player;

    private FabricUser(@NotNull FabricHuskHomes plugin, @NotNull ServerPlayerEntity player) {
        super(player.getUuid(), player.getEntityName());
        this.plugin = plugin;
        this.player = player;
    }

    @NotNull
    public static FabricUser adapt(@NotNull FabricHuskHomes plugin, @NotNull ServerPlayerEntity player) {
        return new FabricUser(plugin, player);
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
        return true; //todo
    }

    @Override
    @NotNull
    public Map<String, Boolean> getPermissions() {
        return Map.of(); //todo
    }

    @Override
    @NotNull
    public Audience getAudience() {
        return player;
    }

    @Override
    public void teleportLocally(@NotNull Location location, boolean asynchronous) throws TeleportationException {
        final MinecraftServer server = player.getWorld().getServer();
        final Identifier worldId = Identifier.tryParse(location.getWorld().getName());
        player.teleport(
                server.getWorld(server.getWorldRegistryKeys().stream()
                        .filter(key -> key.getValue().equals(worldId)).findFirst()
                        .orElseThrow(() -> new TeleportationException(TeleportationException.Type.WORLD_NOT_FOUND))
                ),
                location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch()
        );
    }

    @Override
    public void sendPluginMessage(@NotNull String channel, byte[] message) {
        final PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBytes(message);
        final CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(Identifier.tryParse(channel), buf);
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

}
