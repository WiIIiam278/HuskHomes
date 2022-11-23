package net.william278.huskhomes.event;

import net.william278.huskhomes.teleport.TimedTeleport;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TeleportWarmupEvent extends Event implements ITeleportWarmupEvent, Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    @NotNull
    private final TimedTeleport warp;
    private final int duration;
    private boolean cancelled;

    public TeleportWarmupEvent(@NotNull TimedTeleport warp, int duration) {
        this.warp = warp;
        this.duration = duration;
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
    public int getWarmupDuration() {
        return duration;
    }

    @NotNull
    @Override
    public TimedTeleport getTimedTeleport() {
        return warp;
    }
}
