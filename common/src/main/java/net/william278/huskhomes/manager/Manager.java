package net.william278.huskhomes.manager;

import net.william278.huskhomes.HuskHomes;
import org.jetbrains.annotations.NotNull;

public class Manager {

    private final HomesManager homes;
    private final WarpsManager warps;
    private final RequestsManager requests;

    public Manager(@NotNull HuskHomes plugin) {
        this.homes = new HomesManager(plugin);
        this.warps = new WarpsManager(plugin);
        this.requests = new RequestsManager(plugin);
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
    public RequestsManager requests() {
        return requests;
    }
}
