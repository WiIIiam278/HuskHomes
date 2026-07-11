/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 */

package net.william278.huskhomes.util;

import io.fand.api.block.Block;
import io.fand.api.world.World;
import net.william278.huskhomes.FandAdapter;
import net.william278.huskhomes.FandHuskHomes;
import net.william278.huskhomes.position.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface FandSavePositionProvider extends SavePositionProvider {

    @Override
    default CompletableFuture<Optional<Location>> findSafeGroundLocation(@NotNull Location location) {
        final CompletableFuture<Optional<Location>> result = new CompletableFuture<>();
        final FandHuskHomes plugin = getPlugin();
        plugin.getContext().scheduler().runMain(() -> {
            try {
                result.complete(FandAdapter.adapt(location.getWorld(), plugin.server())
                        .filter(world -> world.worldBorder().contains(location.getX(), location.getZ()))
                        .flatMap(world -> findSafeLocationNear(location, world)));
            } catch (Throwable failure) {
                result.completeExceptionally(failure);
            }
        });
        return result;
    }

    private Optional<Location> findSafeLocationNear(@NotNull Location location, @NotNull World world) {
        final int originX = (int) Math.floor(location.getX());
        final int originZ = (int) Math.floor(location.getZ());
        final int minY = configuredHeight(
                getPlugin().getSettings().getRtp().getMinHeight(),
                location.getWorld().getName(),
                location.getWorld().getEnvironment() == net.william278.huskhomes.position.World.Environment.NETHER
                        ? 0 : -64
        );
        final int maxY = configuredHeight(
                getPlugin().getSettings().getRtp().getMaxHeight(),
                location.getWorld().getName(),
                location.getWorld().getEnvironment() == net.william278.huskhomes.position.World.Environment.NETHER
                        ? 256 : 320
        );

        for (int xOffset = -SEARCH_RADIUS; xOffset <= SEARCH_RADIUS; xOffset++) {
            for (int zOffset = -SEARCH_RADIUS; zOffset <= SEARCH_RADIUS; zOffset++) {
                final int x = originX + xOffset;
                final int z = originZ + zOffset;
                final Optional<Integer> y = findSafeY(world, x, z, minY, maxY,
                        location.getWorld().getEnvironment());
                if (y.isPresent()) {
                    return Optional.of(Location.at(x + 0.5D, y.get(), z + 0.5D, location.getWorld()));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<Integer> findSafeY(@NotNull World world, int x, int z, int minY, int maxY,
                                        @NotNull net.william278.huskhomes.position.World.Environment environment) {
        if (environment == net.william278.huskhomes.position.World.Environment.NETHER) {
            for (int y = minY + 1; y < maxY - 1; y++) {
                if (isSafeLocation(world, x, y, z)) {
                    return Optional.of(y);
                }
            }
            return Optional.empty();
        }

        final int y = Math.max(minY + 1, Math.min(world.highestBlockYAt(x, z) + 1, maxY - 1));
        return isSafeLocation(world, x, y, z) ? Optional.of(y) : Optional.empty();
    }

    private boolean isSafeLocation(@NotNull World world, int x, int y, int z) {
        final Block ground = world.blockAt(x, y - 1, z);
        final Block body = world.blockAt(x, y, z);
        final Block head = world.blockAt(x, y + 1, z);
        return ground.solid() && !ground.fluidBlock() && !ground.water() && !ground.lava()
                && isBlockSafeForStanding(ground.type().key().asString())
                && occupiable(body) && occupiable(head);
    }

    private boolean occupiable(@NotNull Block block) {
        return (block.air() || block.replaceable())
                && !block.water() && !block.lava()
                && isBlockSafeForOccupation(block.type().key().asString());
    }

    private static int configuredHeight(@NotNull List<String> entries, @NotNull String worldName, int fallback) {
        for (String entry : entries) {
            final int separator = entry.lastIndexOf(':');
            if (separator <= 0 || !entry.substring(0, separator).equals(worldName)) {
                continue;
            }
            try {
                return Integer.parseInt(entry.substring(separator + 1));
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    @Override
    @NotNull
    FandHuskHomes getPlugin();
}
