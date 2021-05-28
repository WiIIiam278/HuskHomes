package me.william278.huskhomes2;

import java.util.HashMap;
import java.util.HashSet;

public class CrossServerListHandler {

    // A HashMap mapping all the HuskHomes servers on the Bungee Network
    // (with the same Cluster ID) with their list of players
    /*private static final HashMap<String,HashSet<String>> playerList = new HashMap<>();

    public static HashSet<String> getOtherServerPlayerList() {
        HashSet<String> players = new HashSet<>();
        for (String server : playerList.keySet()) {
            players.addAll(playerList.get(server));
        }
        return players;
    }*/

    /*public static void updateHashset(String server, HashSet<String> players) {
        playerList.put(server, players);
    }
    */
    /*public static void updatePlayerList(Player p) {
        playerList.clear();
        if (p == null) {
            for (Player x : Bukkit.getOnlinePlayers()) {
                p = x;
            }
        }
        if (p != null) {
            PluginMessageHandler.requestPlayerLists(p);
        }
    }*/
}
