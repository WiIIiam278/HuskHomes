package net.william278.huskhomes.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.william278.huskhomes.FabricHuskHomes;
import net.william278.huskhomes.position.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface FabricSafetyResolver extends SafetyResolver {

    @Override
    default CompletableFuture<Optional<Location>> findSafeGroundLocation(@NotNull Location location) {
        final MinecraftServer server = ((FabricHuskHomes) getPlugin()).getMinecraftServer();
        final Identifier worldId = Identifier.tryParse(location.getWorld().getName());
        final Optional<ServerWorld> locationWorld = server.getWorldRegistryKeys().stream()
                .filter(key -> key.getValue().equals(worldId)).findFirst()
                .map(server::getWorld);
        if (locationWorld.isEmpty()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        final ServerWorld world = locationWorld.get();

        final BlockPos.Mutable blockPos = new BlockPos.Mutable(location.getX(), location.getY(), location.getZ());
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                blockPos.set(location.getX() + x, location.getY(), location.getZ() + z);
                final int highestY = getHighestYAt(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
                final BlockState blockState = world.getBlockState(blockPos.withY(highestY));

                final Material material = blockState.getMaterial();
                final Identifier blockId = Registries.BLOCK.getId(blockState.getBlock());
                if (!material.isLiquid() && material != Material.FIRE && isBlockSafe(blockId.toString())) {
                    return CompletableFuture.completedFuture(Optional.of(Location.at(
                            blockPos.getX() + 0.5,
                            highestY + 1,
                            blockPos.getZ() + 0.5,
                            location.getWorld()
                    )));
                }
            }
        }

        return CompletableFuture.completedFuture(Optional.empty());
    }

    private int getHighestYAt(@NotNull BlockView blockView, int x, int y, int z) {
        final BlockPos.Mutable cursor = new BlockPos.Mutable(x, y, z);
        while (blockView.getBlockState(cursor).isAir() && cursor.getY() > blockView.getBottomY()) {
            cursor.move(Direction.DOWN);
        }
        return cursor.getY();
    }

}
