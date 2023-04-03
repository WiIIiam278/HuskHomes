package net.william278.huskhomes.event;

import net.william278.huskhomes.teleport.TeleportRequest;
import net.william278.huskhomes.user.OnlineUser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ReplyTeleportRequestEvent extends Event implements IReplyTeleportRequestEvent, Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final TeleportRequest request;
    private final OnlineUser recipient;
    private boolean cancelled;

    public ReplyTeleportRequestEvent(@NotNull OnlineUser recipient, @NotNull TeleportRequest teleport) {
        this.request = teleport;
        this.recipient = recipient;
    }

    @Override
    @NotNull
    public TeleportRequest getRequest() {
        return request;
    }

    @Override
    @NotNull
    public OnlineUser getRecipient() {
        return recipient;
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
