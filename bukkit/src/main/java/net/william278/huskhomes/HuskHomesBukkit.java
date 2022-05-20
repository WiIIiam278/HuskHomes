package net.william278.huskhomes;

import net.william278.huskhomes.config.Messages;
import net.william278.huskhomes.config.Settings;
import net.william278.huskhomes.player.Player;
import net.william278.huskhomes.position.Server;
import net.william278.huskhomes.util.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class HuskHomesBukkit extends JavaPlugin implements HuskHomes {

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public Logger getLoggingAdapter() {
        return null;
    }

    @Override
    public Set<Player> getOnlinePlayers() {
        return null;
    }

    @Override
    public Settings getSettings() {
        return null;
    }

    @Override
    public Messages getMessages() {
        return null;
    }

    @Override
    public Server getServerData() {
        return null;
    }
}
