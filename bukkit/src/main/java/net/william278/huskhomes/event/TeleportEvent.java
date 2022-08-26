package net.william278.huskhomes.event;

import net.william278.huskhomes.teleport.Teleport;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TeleportEvent extends Event implements ITeleportEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    @NotNull
    private final Teleport teleport;

    public TeleportEvent(@NotNull Teleport teleport) {
        this.teleport = teleport;
    }

    @Override
    public @NotNull Teleport getTeleport() {
        return teleport;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

}
