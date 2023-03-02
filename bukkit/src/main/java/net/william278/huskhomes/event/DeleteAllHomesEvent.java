package net.william278.huskhomes.event;

import net.william278.huskhomes.user.User;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DeleteAllHomesEvent extends Event implements IDeleteAllHomesEvent, Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean cancelled;

    @NotNull
    private final User homeOwner;

    public DeleteAllHomesEvent(@NotNull User homeOwner) {
        this.homeOwner = homeOwner;
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
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public @NotNull User getHomeOwner() {
        return homeOwner;
    }
}
