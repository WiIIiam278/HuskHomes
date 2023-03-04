package net.william278.huskhomes.teleport;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record Username(@NotNull String name) implements Teleportable, Target {

    @NotNull
    public Optional<OnlineUser> findLocally(@NotNull HuskHomes plugin) throws TeleportationException {
        return plugin.findOnlinePlayer(name);
    }

}
