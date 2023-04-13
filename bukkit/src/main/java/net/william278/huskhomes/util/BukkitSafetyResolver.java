package net.william278.huskhomes.util;

import io.papermc.lib.PaperLib;
import net.william278.huskhomes.position.Location;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface BukkitSafetyResolver extends SafetyResolver {

    @Override
    default CompletableFuture<Optional<Location>> findSafeGroundLocation(@NotNull Location location) {
        final org.bukkit.Location bukkitLocation = BukkitAdapter.adaptLocation(location).orElse(null);
        if (bukkitLocation == null || bukkitLocation.getWorld() == null) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        return PaperLib.getChunkAtAsync(bukkitLocation).thenApply(Chunk::getChunkSnapshot).thenApply(snapshot -> {
            final int chunkX = bukkitLocation.getBlockX() & 0xF;
            final int chunkZ = bukkitLocation.getBlockZ() & 0xF;

            for (int dX = -1; dX <= 2; dX++) {
                for (int dZ = -1; dZ <= 2; dZ++) {
                    final int x = chunkX + dX;
                    final int z = chunkZ + dZ;
                    if (x < 0 || x >= 16 || z < 0 || z >= 16) {
                        continue;
                    }
                    final int y = snapshot.getHighestBlockYAt(x, z);
                    final Material blockType = snapshot.getBlockType(chunkX, y, chunkZ);
                    if (isBlockSafe(blockType.getKey().toString())) {
                        return Optional.of(Location.at(
                                (location.getX() + dX) + 0.5d,
                                y + 1.25d,
                                (location.getZ() + dZ) + 0.5d,
                                location.getWorld()
                        ));
                    }
                }
            }
            return Optional.empty();
        });
    }

}
