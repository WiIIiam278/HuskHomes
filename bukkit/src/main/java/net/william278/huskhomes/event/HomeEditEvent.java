package net.william278.huskhomes.event;

import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.user.CommandUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class HomeEditEvent extends Event implements IHomeEditEvent, Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Home home;
    private final CommandUser editor;
    private boolean cancelled;

    public HomeEditEvent(@NotNull Home home, @NotNull CommandUser editor) {
        this.home = home;
        this.editor = editor;
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
    public Home getHome() {
        return home;
    }

    @Override
    @NotNull
    public CommandUser getEditor() {
        return editor;
    }

}
