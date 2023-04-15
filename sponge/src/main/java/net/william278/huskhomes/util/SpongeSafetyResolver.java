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

package net.william278.huskhomes.util;

import net.william278.huskhomes.position.Location;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SpongeSafetyResolver extends SafetyResolver {

    @Override
    default CompletableFuture<Optional<Location>> findSafeGroundLocation(@NotNull Location location) {
        final Optional<ServerLocation> adaptedLocation = SpongeAdapter.adaptLocation(location);
        if (adaptedLocation.isEmpty()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        final ServerLocation serverLocation = adaptedLocation.get();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                final Vector3i cursor = serverLocation.asHighestLocation().blockPosition().add(x, 0, z);
                final BlockState blockState = serverLocation.world().block(cursor);
                if (isBlockSafe(blockState.type().key(RegistryTypes.BLOCK_TYPE).asString())) {
                    return CompletableFuture.completedFuture(Optional.of(Location.at(
                            cursor.x() + 0.5,
                            cursor.y() + 1,
                            cursor.z() + 0.5,
                            location.getWorld()
                    )));
                }
            }
        }

        return CompletableFuture.completedFuture(Optional.empty());
    }

}