package me.william278.huskhomes2.teleport;

import de.themoep.minedown.MineDown;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.data.pluginmessage.PluginMessage;
import me.william278.huskhomes2.data.pluginmessage.PluginMessageType;
import me.william278.huskhomes2.integrations.VanishChecker;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class TeleportRequestHandler {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    // Target player and teleport request to them hashmap
    public static Map<Player, TeleportRequest> teleportRequests = new HashMap<>();

    private static void sendTeleportRequestCrossServer(Player requester, String targetPlayerName, TeleportRequest.RequestType requestType) {
        if (requestType == TeleportRequest.RequestType.TPA) {
            new PluginMessage(targetPlayerName, PluginMessageType.TPA_REQUEST, requester.getName()).send(requester);
        } else if (requestType == TeleportRequest.RequestType.TPAHERE) {
            new PluginMessage(targetPlayerName, PluginMessageType.TPAHERE_REQUEST, requester.getName()).send(requester);
        }
    }

    private static void replyTeleportRequestCrossServer(Player replier, String requesterName, TeleportRequest.RequestType requestType, boolean accepted) {
        if (requestType == TeleportRequest.RequestType.TPA) {
            new PluginMessage(requesterName, PluginMessageType.TPA_REQUEST_REPLY, replier.getName(), Boolean.toString(accepted)).send(replier);
        } else if (requestType == TeleportRequest.RequestType.TPAHERE) {
            new PluginMessage(requesterName, PluginMessageType.TPAHERE_REQUEST_REPLY, replier.getName(), Boolean.toString(accepted)).send(replier);
        }
    }

    private static TextComponent runCommandButton(String buttonText, ChatColor color, String command, String hoverMessage) {
        TextComponent button = new TextComponent(buttonText);
        button.setColor(color);

        button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, (command)));
        button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder(hoverMessage).color(ChatColor.GRAY).italic(false).create())));
        return button;
    }

    public static void sendTpAcceptDenyButtons(Player p) {
        // Send the "Accept" or "Decline" response buttons to the player who has received a request
        // Options text
        BaseComponent[] options = new MineDown(MessageManager.getRawMessage("option_selection_prompt")).toComponent();
        BaseComponent[] divider = new MineDown(MessageManager.getRawMessage("list_item_divider")).urlDetection(false).toComponent();

        // Build the components together
        ComponentBuilder teleportResponses = new ComponentBuilder();
        teleportResponses.append(options, ComponentBuilder.FormatRetention.NONE);
        teleportResponses.append(runCommandButton(MessageManager.getRawMessage("tpa_accept_button"), ChatColor.GREEN, "/tpaccept", MessageManager.getRawMessage("tpa_accept_button_tooltip")), ComponentBuilder.FormatRetention.NONE);
        teleportResponses.append(divider, ComponentBuilder.FormatRetention.NONE);
        teleportResponses.append(runCommandButton(MessageManager.getRawMessage("tpa_decline_button"), ChatColor.RED, "/tpdeny", MessageManager.getRawMessage("tpa_decline_button_tooltip")), ComponentBuilder.FormatRetention.NONE);

        // Create and send the message
        p.spigot().sendMessage(teleportResponses.create());
    }

    public static void sendTeleportToRequest(Player requester, String targetPlayerName) {
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            if (targetPlayer.getUniqueId() != requester.getUniqueId()) {
                if (!VanishChecker.isVanished(targetPlayer)) {
                    teleportRequests.put(targetPlayer, new TeleportRequest(requester.getName(), TeleportRequest.RequestType.TPA));
                    MessageManager.sendMessage(requester, "tpa_request_sent", targetPlayerName);
                    MessageManager.sendMessage(targetPlayer, "tpa_request_ask", requester.getName());
                    TeleportRequestHandler.sendTpAcceptDenyButtons(targetPlayer);
                } else {
                    MessageManager.sendMessage(requester, "error_player_not_found", targetPlayerName);
                }
            } else {
                MessageManager.sendMessage(requester, "error_tp_self");
            }
        } else {
            if (HuskHomes.getSettings().doBungee()) {
                sendTeleportRequestCrossServer(requester, targetPlayerName, TeleportRequest.RequestType.TPA);
                MessageManager.sendMessage(requester, "tpa_request_sent", targetPlayerName);
            } else {
                MessageManager.sendMessage(requester, "error_player_not_found", targetPlayerName);
            }
        }
    }

    public static void sendTeleportHereRequest(Player requester, String targetPlayerName) {
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            if (targetPlayer.getUniqueId() != requester.getUniqueId()) {
                teleportRequests.put(targetPlayer, new TeleportRequest(requester.getName(), TeleportRequest.RequestType.TPAHERE));
                MessageManager.sendMessage(requester, "tpahere_request_sent", targetPlayerName);
                MessageManager.sendMessage(targetPlayer, "tpahere_request_ask", requester.getName());
                TeleportRequestHandler.sendTpAcceptDenyButtons(targetPlayer);
            } else {
                MessageManager.sendMessage(requester, "error_tp_self");
            }
        } else {
            if (HuskHomes.getSettings().doBungee()) {
                sendTeleportRequestCrossServer(requester, targetPlayerName, TeleportRequest.RequestType.TPAHERE);
                MessageManager.sendMessage(requester, "tpahere_request_sent", targetPlayerName);
            } else {
                MessageManager.sendMessage(requester, "error_player_not_found", targetPlayerName);
            }
        }
    }

    public static void replyTpRequest(Player p, boolean accepted) {
        if (!teleportRequests.containsKey(p)) {
            MessageManager.sendMessage(p, "error_tpa_no_pending_request");
            return;
        }
        TeleportRequest teleportRequest = teleportRequests.get(p);
        if (teleportRequest.isExpired()) {
            teleportRequests.remove(p);
            MessageManager.sendMessage(p, "error_tp_request_expired");
            return;
        }
        String requesterName = teleportRequest.getSenderName();
        TeleportRequest.RequestType requestType = teleportRequest.getRequestType();
        Player requester = Bukkit.getPlayer(requesterName);

        Connection connection = HuskHomes.getConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (requester != null) {
                    if (accepted) {
                        MessageManager.sendMessage(p, "tpa_you_accepted", requesterName);
                        MessageManager.sendMessage(requester, "tpa_has_accepted", p.getName());

                        if (requestType == TeleportRequest.RequestType.TPA) {
                            TeleportManager.queueTimedTeleport(requester, p.getName(), connection);
                        } else if (requestType == TeleportRequest.RequestType.TPAHERE) {
                            TeleportManager.queueTimedTeleport(p, requesterName, connection);
                        }
                    } else {
                        MessageManager.sendMessage(p, "tpa_you_declined", requesterName);
                        MessageManager.sendMessage(requester, "tpa_has_declined", p.getName());
                    }
                } else {
                    if (HuskHomes.getSettings().doBungee()) {
                        if (accepted) {
                            MessageManager.sendMessage(p, "tpa_you_accepted", requesterName);
                            replyTeleportRequestCrossServer(p, requesterName, requestType, true);
                            if (requestType == TeleportRequest.RequestType.TPAHERE) {
                                TeleportManager.queueTimedTeleport(p, requesterName, connection);
                            }
                        } else {
                            replyTeleportRequestCrossServer(p, requesterName, requestType, false);
                            MessageManager.sendMessage(p, "tpa_you_declined", requesterName);
                        }
                    } else {
                        MessageManager.sendMessage(p, "error_player_not_found", requesterName);
                    }
                }
                teleportRequests.remove(p);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "An SQL exception occurred!", e);
            }
        });
    }
}
