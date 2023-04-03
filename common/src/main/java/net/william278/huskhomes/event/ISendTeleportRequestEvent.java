package net.william278.huskhomes.event;

import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

public interface ISendTeleportRequestEvent extends ITeleportRequestEvent {

    @NotNull
    OnlineUser getSender();

}
