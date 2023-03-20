package net.william278.huskhomes.event;

import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

public interface IWarpCreateEvent extends Cancellable {

    @NotNull
    String getName();

    void setName(@NotNull String name);

    @NotNull
    Position getPosition();

    void setPosition(@NotNull Position position);

    @NotNull
    CommandUser getCreator();

}
