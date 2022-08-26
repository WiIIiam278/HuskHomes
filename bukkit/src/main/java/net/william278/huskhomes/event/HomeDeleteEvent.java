package net.william278.huskhomes.event;

import net.william278.huskhomes.position.Home;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class HomeDeleteEvent extends Event implements IHomeDeleteEvent, Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    @NotNull
    private final Home home;

    private boolean cancelled;

    public HomeDeleteEvent(@NotNull Home home) {
        this.home = home;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

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
    public @NotNull Home getHome() {
        return home;
    }
}
