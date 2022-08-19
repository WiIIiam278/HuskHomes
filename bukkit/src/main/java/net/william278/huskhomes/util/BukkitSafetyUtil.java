package net.william278.huskhomes.util;

import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.World;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BukkitSafetyUtil {

    /**
     * Finds a safe location to teleport to about a point.
     *
     * @param world          The world to search in
     * @param bukkitLocation The location to search around
     * @param chunkSnapshot  The chunk snapshot to search in
     * @return The safe location, if found
     */
    public static Optional<Location> findSafeLocation(@NotNull World world, @NotNull org.bukkit.Location bukkitLocation,
                                                @NotNull ChunkSnapshot chunkSnapshot) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int chunkX = Math.abs(bukkitLocation.getBlockX() % 16) + i;
                int chunkZ = Math.abs(bukkitLocation.getBlockZ() % 16) + j;
                if (chunkX < 0 || chunkX > 15 || chunkZ < 0 || chunkZ > 15) {
                    continue;
                }
                int chunkY = chunkSnapshot.getHighestBlockYAt(chunkX, chunkZ);
                if (isSafePosition(chunkSnapshot, chunkX, chunkY, chunkZ)) {
                    return Optional.of(new Location(bukkitLocation.getBlockX() + i,
                            chunkY + 1.5d, bukkitLocation.getBlockZ() + j,
                            90, 0, world));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Searches a chunk and returns if a block is safe to teleport to.
     *
     * @param chunkSnapshot The chunk snapshot to search in
     * @param chunkX        The chunk x coordinate to search at
     * @param chunkY        The chunk y coordinate to search at
     * @param chunkZ        The chunk z coordinate to search at
     * @return True if the block is safe, false otherwise
     */
    public static boolean isSafePosition(@NotNull ChunkSnapshot chunkSnapshot, final int chunkX, final int chunkY, final int chunkZ) {
        final Material blockType = chunkSnapshot.getBlockType(chunkX, chunkY, chunkZ);
        return switch (blockType) {
            // Special case handling for safe, un-solid blocks
            case TALL_GRASS, TALL_SEAGRASS, FERN, LARGE_FERN, DANDELION, POPPY, BLUE_ORCHID, ALLIUM, AZURE_BLUET,
                    RED_TULIP, ORANGE_TULIP, WHITE_TULIP, PINK_TULIP, OXEYE_DAISY, CORNFLOWER, LILY_OF_THE_VALLEY,
                    ROSE_BUSH, WHEAT_SEEDS, BEETROOT_SEEDS, MELON_SEEDS, PUMPKIN_SEEDS, GRASS_PATH, FARMLAND,
                    OAK_LEAVES, BIRCH_LEAVES, DEAD_BUSH, SUGAR_CANE, SNOW, ICE, BLUE_ICE, FROSTED_ICE -> true;
            default -> blockType.isSolid() || blockType.isOccluding();
        };
    }

}
