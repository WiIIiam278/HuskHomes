package me.william278.huskhomes2;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;

public class CrossServerListHandler {

    // A HashMap mapping all the HuskHomes servers on the Bungee Network
    // (with the same Cluster ID) with their list of players
    private static final HashMap<String,HashSet<String>> playerList = new HashMap<>();

    // Returns a HashSet of all players on the Network
    public static HashSet<String> getGlobalPlayerList() {
        HashSet<String> players = getOtherServerPlayerList();
        for (Player p : Bukkit.getOnlinePlayers()) {
            players.add(p.getName());
        }
        return players;
    }

    public static HashSet<String> getOtherServerPlayerList() {
        HashSet<String> players = new HashSet<>();
        for (String server : playerList.keySet()) {
            players.addAll(playerList.get(server));
        }
        return players;
    }

    public static void updateHashset(String server, HashSet<String> players) {
        playerList.put(server, players);
    }

    public static void requestHashsetUpdate(Player player) {
        playerList.clear();
        PluginMessageHandler.requestPlayerLists(player);
    }
}
