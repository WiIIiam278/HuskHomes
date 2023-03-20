package net.william278.huskhomes.event;

import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.user.CommandUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class WarpCreateEvent extends Event implements IWarpCreateEvent, Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private String name;
    private Position position;
    private final CommandUser creator;
    private boolean cancelled;

    public WarpCreateEvent(@NotNull String name, @NotNull Position position, @NotNull CommandUser creator) {
        this.name = name;
        this.position = position;
        this.creator = creator;
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
    @NotNull
    public String getName() {
        return name;
    }

    @Override
    public void setName(@NotNull String name) {
        this.name = name;
    }

    @Override
    @NotNull
    public Position getPosition() {
        return position;
    }

    @Override
    public void setPosition(@NotNull Position position) {
        this.position = position;
    }

    @Override
    @NotNull
    public CommandUser getCreator() {
        return creator;
    }
}
