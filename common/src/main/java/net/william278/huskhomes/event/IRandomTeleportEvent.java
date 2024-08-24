package net.william278.huskhomes.event;

import net.william278.huskhomes.position.Position;
import org.jetbrains.annotations.NotNull;

public interface IRandomTeleportEvent extends ITeleportEvent {

    @NotNull
    Position getPosition();

}