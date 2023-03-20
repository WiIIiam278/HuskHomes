package net.william278.huskhomes.event;

import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.CommandUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WarpListEvent extends Event implements IWarpListEvent, Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private List<Warp> warps;
    private final CommandUser listViewer;
    private boolean cancelled;

    public WarpListEvent(@NotNull List<Warp> warps, @NotNull CommandUser listViewer) {
        this.warps = warps;
        this.listViewer = listViewer;
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
    @NotNull
    public List<Warp> getWarps() {
        return warps;
    }

    @Override
    public void setWarps(@NotNull List<Warp> warps) {
        this.warps = warps;
    }

    @Override
    @NotNull
    public CommandUser getListViewer() {
        return listViewer;
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
