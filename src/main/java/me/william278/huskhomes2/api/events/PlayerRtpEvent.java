package me.william278.huskhomes2.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerRtpEvent extends Event implements Cancellable {

    private Player p;
    private String[] args;
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean isCancelled;

    public PlayerRtpEvent(Player p, String[] args) {
        this.isCancelled = false;
        this.p=p;
        this.args=args;
    }
    public Player getPlayer(){
        return p;
    }
    public String[] getArgs(){
        return args;
    }
    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
