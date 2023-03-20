package net.william278.huskhomes.event;

import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;

public interface IHomeCreateEvent extends Cancellable {

    @NotNull
    User getOwner();

    @NotNull
    String getName();

    void setName(@NotNull String name);

    @NotNull
    Position getPosition();

    void setPosition(@NotNull Position position);

    @NotNull
    CommandUser getCreator();

}
