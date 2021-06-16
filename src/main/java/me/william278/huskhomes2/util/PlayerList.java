package me.william278.huskhomes2.util;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.data.pluginmessage.PluginMessage;
import me.william278.huskhomes2.data.pluginmessage.PluginMessageType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;

public class PlayerList {

    private static final HuskHomes plugin = HuskHomes.getInstance();
    private static final long playerListUpdateTime = HuskHomes.getSettings().getCrossServerTabUpdateDelay() * 20L;

    private static HashSet<String> players;

    public PlayerList() {
        players = new HashSet<>();
    }

    public void initialize() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                updateList(p);
                break;
            }
        }, 0, playerListUpdateTime));
    }

    public void updateList(Player updateRequester) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            players.clear();
            if (HuskHomes.getSettings().doBungee() && HuskHomes.getSettings().doCrossServerTabCompletion()) {
                Bukkit.getScheduler().runTask(plugin, () -> new PluginMessage(PluginMessageType.GET_PLAYER_LIST, HuskHomes.getSettings().getServerID()).sendToServer(updateRequester));
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
        });
    }

    public HashSet<String> getPlayers() {
        return players;
    }

    public void addPlayer(String player) {
        players.add(player);
    }

    public void addPlayers(String[] playerList) {
        players.addAll(Arrays.asList(playerList));
    }

}
