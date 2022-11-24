package net.william278.huskhomes.event;

import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Warp;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WarpListEvent extends Event implements IWarpListEvent, Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    @NotNull
    private final List<Warp> warps;
    @NotNull
    private final OnlineUser onlineUser;
    private boolean cancelled;

    public WarpListEvent(@NotNull List<Warp> warps, @NotNull OnlineUser onlineUser) {
        this.warps = warps;
        this.onlineUser = onlineUser;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    @NotNull
    public List<Warp> getWarps() {
        return warps;
    }

    @Override
    @NotNull
    public OnlineUser getOnlineUser() {
        return onlineUser;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
