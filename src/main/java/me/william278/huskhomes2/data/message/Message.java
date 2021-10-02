package me.william278.huskhomes2.data.message;

import me.william278.huskhomes2.HuskHomes;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.StringJoiner;

public abstract class Message {

    private static final String MESSAGE_DATA_SEPARATOR = "$";

    private final int clusterId;
    private final Message.MessageType messageType;
    private final String targetPlayerName;
    private final String messageData;

    public Message(String targetPlayerName, MessageType pluginMessageType, String... messageData) {
        this.clusterId = HuskHomes.getSettings().getClusterId();
        this.messageType = pluginMessageType;
        StringJoiner newMessageData = new StringJoiner(MESSAGE_DATA_SEPARATOR);
        for (String dataItem : messageData) {
            newMessageData.add(dataItem);
        }
        this.messageData = newMessageData.toString();
        this.targetPlayerName = targetPlayerName;
    }

    public Message(MessageType pluginMessageType, String... messageData) {
        this.clusterId = HuskHomes.getSettings().getClusterId();
        this.messageType = pluginMessageType;
        StringJoiner newMessageData = new StringJoiner(MESSAGE_DATA_SEPARATOR);
        for (String dataItem : messageData) {
            newMessageData.add(dataItem);
        }
        this.messageData = newMessageData.toString();
        this.targetPlayerName = null;
    }

    public Message(int clusterId, String targetPlayerName, String pluginMessageType, String... messageData) {
        this.clusterId = clusterId;
        this.messageType = MessageType.valueOf(pluginMessageType.toUpperCase(Locale.ENGLISH));
        StringJoiner newMessageData = new StringJoiner(MESSAGE_DATA_SEPARATOR);
        for (String dataItem : messageData) {
            newMessageData.add(dataItem);
        }
        this.messageData = newMessageData.toString();
        this.targetPlayerName = targetPlayerName;
    }

    // Get the string version of the plugin message type
    protected String getPluginMessageString(Message.MessageType type) {
        return type.name().toLowerCase(Locale.ENGLISH);
    }

    public int getClusterId() {
        return clusterId;
    }

    public Message.MessageType getMessageType() {
        return messageType;
    }

    public String getTargetPlayerName() {
        return targetPlayerName;
    }

    public String getMessageData() {
        return messageData;
    }

    public String[] getMessageDataItems() {
        return messageData.split("\\" + MESSAGE_DATA_SEPARATOR);
    }

    public abstract void send(Player sender);

    public abstract void sendToAllServers(Player sender);

    public abstract void sendToServer(Player sender, String server);

    public enum MessageType {
        SET_TP_DESTINATION,
        CONFIRM_DESTINATION_SET,
        TPA_REQUEST,
        TPA_HERE_REQUEST,
        TPA_REQUEST_REPLY,
        TPA_HERE_REQUEST_REPLY,
        TELEPORT_TO_ME,
        GET_PLAYER_LIST,
        RETURN_PLAYER_LIST
    }
}
