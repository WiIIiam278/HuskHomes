package me.william278.huskhomes2;

import me.william278.huskhomes2.integrations.vanishChecker;
import me.william278.huskhomes2.objects.TeleportRequest;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class teleportRequestHandler {

    // Target player and teleport request to them hashmap
    public static HashMap<Player, TeleportRequest> teleportRequests = new HashMap<>();

    private static void sendTeleportRequestCrossServer(Player requester, String targetPlayerName, String teleportRequestType) {
        String pluginMessage = teleportRequestType + "_request";
        pluginMessageHandler.sendPluginMessage(requester, targetPlayerName, pluginMessage, requester.getName());
    }

    private static void replyTeleportRequestCrossServer(Player replier, String requesterName, String teleportRequestType, boolean accepted) {
        String pluginMessage = teleportRequestType + "_request_reply";
        pluginMessageHandler.sendPluginMessage(replier, requesterName, pluginMessage, replier.getName() + ":" + accepted);
    }

    public static TextComponent createButton(String buttonText, net.md_5.bungee.api.ChatColor color, ClickEvent.Action actionType, String command, String hoverMessage, net.md_5.bungee.api.ChatColor hoverMessageColor, Boolean hoverMessageItalic) {
        TextComponent button = new TextComponent(buttonText);
        button.setColor(color);

        button.setClickEvent(new ClickEvent(actionType, (command)));
        button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder(hoverMessage).color(hoverMessageColor).italic(hoverMessageItalic).create())));
        return button;
    }

    public static void sendTpAcceptDenyButtons(Player p) {
        // Send the "Accept" or "Decline" response buttons to the player who has received a request
        // Options text
        TextComponent options = new TextComponent(messageManager.getRawMessage("tpa_request_buttons_prompt"));
        options.setColor(net.md_5.bungee.api.ChatColor.GRAY);

        TextComponent separator = new TextComponent(messageManager.getRawMessage("list_item_divider"));
        separator.setColor(net.md_5.bungee.api.ChatColor.GRAY);

        // Build the components together
        ComponentBuilder teleportResponses = new ComponentBuilder();
        teleportResponses.append(options);
        teleportResponses.append(createButton(messageManager.getRawMessage("tpa_accept_button"), net.md_5.bungee.api.ChatColor.GREEN, ClickEvent.Action.RUN_COMMAND, "/tpaccept", messageManager.getRawMessage("tpa_accept_button_tooltip"), net.md_5.bungee.api.ChatColor.GRAY, false));
        teleportResponses.append(separator);
        teleportResponses.append(createButton(messageManager.getRawMessage("tpa_decline_button"), net.md_5.bungee.api.ChatColor.RED, ClickEvent.Action.RUN_COMMAND, "/tpdeny", messageManager.getRawMessage("tpa_decline_button_tooltip"), net.md_5.bungee.api.ChatColor.GRAY, false));

        // Create and send the message
        p.spigot().sendMessage(teleportResponses.create());
    }

    public static void sendTeleportToRequest(Player requester, String targetPlayerName) {
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            if (targetPlayer.getUniqueId() != requester.getUniqueId()) {
                if (!vanishChecker.isVanished(targetPlayer)) {
                    teleportRequests.put(targetPlayer, new TeleportRequest(requester.getName(), "tpa"));
                    messageManager.sendMessage(requester, "tpa_request_sent", targetPlayerName);
                    messageManager.sendMessage(targetPlayer, "tpa_request_ask", requester.getName());
                    teleportRequestHandler.sendTpAcceptDenyButtons(targetPlayer);
                } else {
                    messageManager.sendMessage(requester, "error_player_not_found", targetPlayerName);
                }
            } else {
                messageManager.sendMessage(requester, "error_tp_self");
            }
        } else {
            if (HuskHomes.settings.doBungee()) {
                sendTeleportRequestCrossServer(requester, targetPlayerName, "tpa");
                messageManager.sendMessage(requester, "tpa_request_sent", targetPlayerName);
            } else {
                messageManager.sendMessage(requester, "error_player_not_found", targetPlayerName);
            }
        }
    }

    public static void sendTeleportHereRequest(Player requester, String targetPlayerName) {
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            if (targetPlayer.getUniqueId() != requester.getUniqueId()) {
                teleportRequests.put(targetPlayer, new TeleportRequest(requester.getName(), "tpahere"));
                messageManager.sendMessage(requester, "tpahere_request_sent", targetPlayerName);
                messageManager.sendMessage(targetPlayer, "tpahere_request_ask", requester.getName());
                teleportRequestHandler.sendTpAcceptDenyButtons(targetPlayer);
            } else {
                messageManager.sendMessage(requester, "error_tp_self");
            }
        } else {
            if (HuskHomes.settings.doBungee()) {
                sendTeleportRequestCrossServer(requester, targetPlayerName, "tpahere");
                messageManager.sendMessage(requester, "tpahere_request_sent", targetPlayerName);
            } else {
                messageManager.sendMessage(requester, "error_player_not_found", targetPlayerName);
            }
        }
    }

    public static void replyTpRequest(Player p, boolean accepted) {
        if (!teleportRequests.containsKey(p)) {
            messageManager.sendMessage(p, "error_tpa_no_pending_request");
            return;
        }
        TeleportRequest teleportRequest = teleportRequests.get(p);
        String requesterName = teleportRequest.getSenderName();
        String requestType = teleportRequest.getRequestType();
        Player requester = Bukkit.getPlayer(requesterName);

        if (requester != null) {
            if (accepted) {
                messageManager.sendMessage(p, "tpa_you_accepted", requesterName);
                messageManager.sendMessage(requester, "tpa_has_accepted", p.getName());

                if (requestType.equals("tpa")) {
                    teleportManager.queueTimedTeleport(requester, p.getName());
                } else if (requestType.equals("tpahere")) {
                    teleportManager.queueTimedTeleport(p, requesterName);
                }
            } else {
                messageManager.sendMessage(p, "tpa_you_declined", requesterName);
                messageManager.sendMessage(requester, "tpa_has_declined", p.getName());
            }
        } else {
            if (HuskHomes.settings.doBungee()) {
                if (accepted) {
                    messageManager.sendMessage(p, "tpa_you_accepted", requesterName);

                    if (requestType.equals("tpa")) {
                        replyTeleportRequestCrossServer(p, requesterName, "tpa", true);
                    } else if (requestType.equals("tpahere")) {
                        replyTeleportRequestCrossServer(p, requesterName, "tpahere", true);
                        teleportManager.queueTimedTeleport(p, requesterName);
                    }
                } else {
                    replyTeleportRequestCrossServer(p, requesterName, requestType, false);
                    messageManager.sendMessage(p, "tpa_you_declined", requesterName);
                }
            } else {
                messageManager.sendMessage(p, "error_player_not_found", requesterName);
            }
        }
        teleportRequests.remove(p);
    }

}
