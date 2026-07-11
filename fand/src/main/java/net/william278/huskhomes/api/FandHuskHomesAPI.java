/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 */

package net.william278.huskhomes.api;

import io.fand.api.entity.Player;
import net.william278.huskhomes.FandAdapter;
import net.william278.huskhomes.FandHuskHomes;
import net.william278.huskhomes.position.Location;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.user.FandUser;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * HuskHomes API adapter for the Fand platform.
 */
public final class FandHuskHomesAPI extends BaseHuskHomesAPI {

    private FandHuskHomesAPI(@NotNull FandHuskHomes plugin) {
        super(plugin);
    }

    @NotNull
    public static FandHuskHomesAPI getInstance() throws NotRegisteredException {
        return (FandHuskHomesAPI) BaseHuskHomesAPI.getInstance();
    }

    @ApiStatus.Internal
    public static void register(@NotNull FandHuskHomes plugin) {
        instance = new FandHuskHomesAPI(plugin);
    }

    @NotNull
    public OnlineUser adaptUser(@NotNull Player player) {
        return ((FandHuskHomes) plugin).getOnlineUser(player);
    }

    @NotNull
    public Player getPlayer(@NotNull OnlineUser user) {
        if (!(user instanceof FandUser fandUser)) {
            throw new IllegalArgumentException("User is not a Fand user");
        }
        return fandUser.getPlayer();
    }

    @NotNull
    public Location adaptLocation(@NotNull io.fand.api.world.Location location) {
        return FandAdapter.adapt(location);
    }

    @NotNull
    public Position adaptPosition(@NotNull io.fand.api.world.Location location) {
        return FandAdapter.adapt(location, getServer());
    }

    @NotNull
    public Optional<? extends io.fand.api.world.World> getWorld(@NotNull World world) {
        return FandAdapter.adapt(world, ((FandHuskHomes) plugin).server());
    }

    @NotNull
    public Optional<io.fand.api.world.Location> adaptLocation(@NotNull Location location) {
        return FandAdapter.adapt(location, ((FandHuskHomes) plugin).server());
    }
}
