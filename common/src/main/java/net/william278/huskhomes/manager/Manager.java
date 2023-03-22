package net.william278.huskhomes.manager;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.network.Message;
import org.jetbrains.annotations.NotNull;

public class Manager {

    private final HuskHomes plugin;
    private final HomesManager homes;
    private final WarpsManager warps;
    private final RequestsManager requests;

    public Manager(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
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

    // Update caches on all servers
    protected void propagateCacheUpdate() {
        if (plugin.getSettings().isCrossServer()) {
            plugin.getOnlineUsers().stream().findAny().ifPresent(user -> Message.builder()
                    .type(Message.Type.UPDATE_CACHES)
                    .scope(Message.Scope.SERVER)
                    .target(Message.TARGET_ALL)
                    .build().send(plugin.getMessenger(), user));
        }
    }
}
