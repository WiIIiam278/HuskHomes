package net.william278.huskhomes.event;

import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.Teleport;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TeleportBackEvent extends TeleportEvent implements ITeleportBackEvent, Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public TeleportBackEvent(@NotNull Teleport teleport) {
        super(teleport);
    }

    @Override
    @NotNull
    public Position getLastPosition() {
        return (Position) getTeleport().getTarget();
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


}
