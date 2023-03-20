package net.william278.huskhomes.event;

import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.CommandUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class WarpDeleteEvent extends Event implements IWarpDeleteEvent, Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Warp warp;
    private final CommandUser deleter;
    private boolean cancelled;

    public WarpDeleteEvent(@NotNull Warp warp, @NotNull CommandUser deleter) {
        this.warp = warp;
        this.deleter = deleter;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    @NotNull
    public Warp getWarp() {
        return warp;
    }

    @Override
    @NotNull
    public CommandUser getDeleter() {
        return deleter;
    }
}
