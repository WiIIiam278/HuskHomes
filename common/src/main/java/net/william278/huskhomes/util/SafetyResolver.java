package net.william278.huskhomes.util;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SafetyResolver {

    /**
     * Returns a safe ground location for the specified {@link Location} if possible
     *
     * @param location the {@link Location} to find a safe ground location for
     * @return a {@link CompletableFuture} that will complete with an optional of the safe ground position, if it is
     * possible to find one
     */
    CompletableFuture<Optional<Location>> findSafeGroundLocation(@NotNull Location location);

    /**
     * Returns if the block, by provided identifier, is unsafe
     *
     * @param blockId The block identifier (e.g. {@code minecraft:stone})
     * @return {@code true} if the block is on the unsafe blocks list, {@code false} otherwise
     */
    default boolean isBlockSafe(@NotNull String blockId) {
        return !getPlugin().getUnsafeBlocks().isUnsafe(blockId);
    }

    @NotNull
    HuskHomes getPlugin();

}
