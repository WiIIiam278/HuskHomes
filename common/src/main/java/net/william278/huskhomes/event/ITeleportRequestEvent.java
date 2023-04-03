package net.william278.huskhomes.event;

import net.william278.huskhomes.teleport.TeleportRequest;
import org.jetbrains.annotations.NotNull;

public interface ITeleportRequestEvent extends Event {

    @NotNull
    TeleportRequest getRequest();

}
