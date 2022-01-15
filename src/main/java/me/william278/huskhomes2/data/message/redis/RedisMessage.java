package me.william278.huskhomes2.data.message.redis;

import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.data.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

public class RedisMessage extends Message {

    public static final String REDIS_MESSAGE_HEADER_SEPARATOR = "£";
    private static final HuskHomes plugin = HuskHomes.getInstance();

    public RedisMessage(String targetPlayerName, MessageType pluginMessageType, String... messageData) {
        super(targetPlayerName, pluginMessageType, messageData);
    }

    public RedisMessage(MessageType pluginMessageType, String... messageData) {
        super(pluginMessageType, messageData);
    }

    public RedisMessage(int clusterId, String targetPlayerName, String pluginMessageType, String... messageData) {
        super(clusterId, targetPlayerName, pluginMessageType, messageData);
    }

    private void dispatchRedisMessage(String target) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            try (Jedis publisher = RedisReceiver.getJedis()) {
                final String jedisPassword = HuskHomes.getSettings().getRedisPassword();
                if (!jedisPassword.equals("")) {
                    publisher.auth(jedisPassword);
                }
                publisher.connect();
                publisher.publish(RedisReceiver.REDIS_CHANNEL, getClusterId() + ":" + getPluginMessageString(getMessageType()) + ":" + target + REDIS_MESSAGE_HEADER_SEPARATOR + getMessageData());
            }
        });
    }

    @Override
    public void send(Player sender) {
        dispatchRedisMessage(getTargetPlayerName());
    }

    @Override
    public void sendToAllServers(Player sender) {
        dispatchRedisMessage("-all-");
    }

    @Override
    public void sendToServer(Player sender, String server) {
        dispatchRedisMessage("server-" + server + "");
    }
}
