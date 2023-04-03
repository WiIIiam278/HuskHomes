package net.william278.huskhomes.event;

import net.william278.huskhomes.teleport.TeleportRequest;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

public interface IReplyTeleportRequestEvent extends ITeleportRequestEvent {

    @NotNull
    OnlineUser getRecipient();

    default boolean isAccepted() {
        return getRequest().getStatus() == TeleportRequest.Status.ACCEPTED;
    }

}
