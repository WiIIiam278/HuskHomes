package me.william278.huskhomes2.data.message.redis;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.data.message.CrossServerMessageHandler;
import me.william278.huskhomes2.data.message.pluginmessage.PluginMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.Collections;

public class RedisReceiver {

    public static final String REDIS_CHANNEL = "HuskHomes";

    public static Player getRandomReceiver() {
        ArrayList<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.isEmpty()) {
            return null;
        }
        Collections.shuffle(players);
        return players.get(0);
    }

    public static void listen() {
        Jedis jedis = new Jedis(HuskHomes.getSettings().getRedisHost(), HuskHomes.getSettings().getRedisPort());
        final String jedisPassword = HuskHomes.getSettings().getRedisPassword();
        if (!jedisPassword.equals("")) {
            jedis.auth(jedisPassword);
        }
        jedis.connect();
        new Thread(() -> jedis.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                if (!channel.equals(REDIS_CHANNEL)) {
                    return;
                }

                // Redis messages are formatted as such:
                // HuskHomes:<cluster_id>:<message_type>:<target_player>£ followed by the message arguments and data.
                final String[] splitMessage = message.split("£");
                final String messageType = splitMessage[0];
                int clusterID;

                // Return if the message was not sent by HuskHomes
                if (!messageType.contains("HuskHomes:")) {
                    return;
                }

                // Ensure the cluster ID matches
                try {
                    clusterID = Integer.parseInt(messageType.split(":")[1]);
                } catch (Exception e) {
                    // In case the message is malformed or the cluster ID is invalid
                    HuskHomes.getInstance().getLogger().warning("Received a HuskHomes redis message with an invalid server Cluster ID! \n" +
                            "Please ensure that the cluster ID is set to a valid integer on all servers.");
                    return;
                }
                if (HuskHomes.getSettings().getClusterId() != clusterID) {
                    return;
                }

                // Get the player targeted by the message and make sure they are online
                final String target = messageType.split(":")[3];
                Player receiver = Bukkit.getPlayerExact(target);

                // If the redis message was targeting this server
                if (target.equalsIgnoreCase("-all-")) {
                    receiver = getRandomReceiver();
                } else if (target.contains("server-")) { //todo In the future, allow redis messages to execute without a player online. For now it's duplicating the behaviour of plugin messages for consistency.
                    if (target.split("-")[1].equalsIgnoreCase(HuskHomes.getSettings().getServerID())) {
                        receiver = getRandomReceiver();
                    } else {
                        return; // The message was targeting another server
                    }
                }

                // Return if the target is not available
                if (receiver == null) {
                    return;
                }
                if (!receiver.isOnline()) {
                    return;
                }

                final String messageData = splitMessage[1];
                CrossServerMessageHandler.handlePluginMessage(new PluginMessage(clusterID, receiver.getName(), messageType.split(":")[2], messageData), receiver);
            }
        }, REDIS_CHANNEL), "Redis Subscriber").start();
    }
}
