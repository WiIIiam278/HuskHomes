package me.william278.huskhomes2.api.events;

import me.william278.huskhomes2.teleport.points.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * An event, fired when a player sets a new home
 */
public class PlayerSetHomeEvent extends PlayerEvent implements Cancellable {

    private final Home home;
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean isCancelled;

    /**
     * An event, fired when a player sets a {@link Home}
     *
     * @param player the {@link Player} setting the Home
     * @param home   the {@link Home} being set
     */
    public PlayerSetHomeEvent(Player player, Home home) {
        super(player);
        this.home = home;
        this.isCancelled = false;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    /**
     * Get the Home being set
     *
     * @return the {@link Home}
     */
    public Home getHome() {
        return home;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }
}
