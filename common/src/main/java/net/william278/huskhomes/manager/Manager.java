package net.william278.huskhomes.manager;

import net.william278.huskhomes.HuskHomes;
import org.jetbrains.annotations.NotNull;

public class Manager {

    private final HuskHomes plugin;
    private final HomesManager homes;
    private final WarpsManager warps;
    private final UsersManager users;

    public Manager(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
        this.homes = new HomesManager(plugin);
        this.warps = new WarpsManager(plugin);
        this.users = new UsersManager(plugin);
    }

    @NotNull
    public HomesManager homes() {
        return homes;
    }

    @NotNull
    public WarpsManager warps() {
        return warps;
    }

    @NotNull
    public UsersManager users() {
        return users;
    }
}
