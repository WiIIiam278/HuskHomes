package net.william278.huskhomes.event;

import net.william278.huskhomes.teleport.Teleport;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TeleportEvent extends Event implements ITeleportEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Teleport teleport;
    private boolean cancelled;

    public TeleportEvent(@NotNull Teleport teleport) {
        this.teleport = teleport;
    }

    @Override
    @NotNull
    public Teleport getTeleport() {
        return teleport;
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
}
