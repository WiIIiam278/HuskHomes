/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 */

package net.william278.huskhomes;

import io.fand.api.entity.Player;
import net.kyori.adventure.key.Key;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

public final class FandAdapter {

    private FandAdapter() {
    }

    @NotNull
    public static Location adapt(@NotNull io.fand.api.world.Location location) {
        return Location.at(
                location.x(), location.y(), location.z(), location.yaw(), location.pitch(), adapt(location.world())
        );
    }

    @NotNull
    public static Position adapt(@NotNull io.fand.api.world.Location location, @NotNull String server) {
        return Position.at(adapt(location), server);
    }

    @NotNull
    public static World adapt(@NotNull io.fand.api.world.World world) {
        final String name = world.key().asMinimalString();
        return World.from(
                name,
                UUID.nameUUIDFromBytes(world.key().asString().getBytes(StandardCharsets.UTF_8)),
                World.Environment.match(name)
        );
    }

    @NotNull
    public static Optional<? extends io.fand.api.world.World> adapt(@NotNull World world,
                                                                    @NotNull io.fand.api.Server server) {
        return server.world(Key.key(world.getName()));
    }

    @NotNull
    public static Optional<io.fand.api.world.Location> adapt(@NotNull Location location,
                                                             @NotNull io.fand.api.Server server) {
        return adapt(location.getWorld(), server).map(world -> world.at(
                location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch()
        ));
    }

    @NotNull
    public static Position adapt(@NotNull Player player, @NotNull String server) {
        return adapt(player.location(), server);
    }
}
