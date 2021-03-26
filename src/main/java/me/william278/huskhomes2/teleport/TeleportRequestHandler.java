package me.william278.huskhomes2.teleport;

import de.themoep.minedown.MineDown;
import me.william278.huskhomes2.CrossServerListHandler;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.PluginMessageHandler;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.integrations.VanishChecker;
import me.william278.huskhomes2.integrations.VaultIntegration;
import me.william278.huskhomes2.teleport.points.RandomPoint;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TeleportRequestHandler {

    // Target player and teleport request to them hashmap
    public static Map<Player, TeleportRequest> teleportRequests = new HashMap<>();

    // Timestamp identifying when the global player list should be updated next;
    private static long nextPlayerListUpdateTime = Instant.now().getEpochSecond() + HuskHomes.getSettings().getCrossServerTabUpdateDelay();

    private static void sendTeleportRequestCrossServer(Player requester, String targetPlayerName, String teleportRequestType) {
        String pluginMessage = teleportRequestType + "_request";
        PluginMessageHandler.sendPluginMessage(requester, targetPlayerName, pluginMessage, requester.getName());
    }

    private static void replyTeleportRequestCrossServer(Player replier, String requesterName, String teleportRequestType, boolean accepted) {
        String pluginMessage = teleportRequestType + "_request_reply";
        PluginMessageHandler.sendPluginMessage(replier, requesterName, pluginMessage, replier.getName() + ":" + accepted);
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
        TextComponent options = new TextComponent(MessageManager.getRawMessage("tpa_request_buttons_prompt"));
        options.setColor(ChatColor.GRAY);

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
                    teleportRequests.put(targetPlayer, new TeleportRequest(requester.getName(), "tpa"));
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
                sendTeleportRequestCrossServer(requester, targetPlayerName, "tpa");
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
                teleportRequests.put(targetPlayer, new TeleportRequest(requester.getName(), "tpahere"));
                MessageManager.sendMessage(requester, "tpahere_request_sent", targetPlayerName);
                MessageManager.sendMessage(targetPlayer, "tpahere_request_ask", requester.getName());
                TeleportRequestHandler.sendTpAcceptDenyButtons(targetPlayer);
            } else {
                MessageManager.sendMessage(requester, "error_tp_self");
            }
        } else {
            if (HuskHomes.getSettings().doBungee()) {
                sendTeleportRequestCrossServer(requester, targetPlayerName, "tpahere");
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
        String requesterName = teleportRequest.getSenderName();
        String requestType = teleportRequest.getRequestType();
        Player requester = Bukkit.getPlayer(requesterName);

        if (requester != null) {
            if (accepted) {
                MessageManager.sendMessage(p, "tpa_you_accepted", requesterName);
                MessageManager.sendMessage(requester, "tpa_has_accepted", p.getName());

                if (requestType.equals("tpa")) {
                    TeleportManager.queueTimedTeleport(requester, p.getName());
                } else if (requestType.equals("tpahere")) {
                    TeleportManager.queueTimedTeleport(p, requesterName);
                }
            } else {
                MessageManager.sendMessage(p, "tpa_you_declined", requesterName);
                MessageManager.sendMessage(requester, "tpa_has_declined", p.getName());
            }
        } else {
            if (HuskHomes.getSettings().doBungee()) {
                if (accepted) {
                    MessageManager.sendMessage(p, "tpa_you_accepted", requesterName);

                    if (requestType.equals("tpa")) {
                        replyTeleportRequestCrossServer(p, requesterName, "tpa", true);
                    } else if (requestType.equals("tpahere")) {
                        replyTeleportRequestCrossServer(p, requesterName, "tpahere", true);
                        TeleportManager.queueTimedTeleport(p, requesterName);
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
    }

    public static void startExpiredChecker(Plugin plugin) {
        Set<Player> expiredTeleportRequests = new HashSet<>();
        Set<TimedTeleport> completedTeleports = new HashSet<>();

        // Run every second
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {

            // Update active timed teleports
            if (!TeleportManager.getQueuedTeleports().isEmpty()) {
                for (TimedTeleport timedTeleport : TeleportManager.getQueuedTeleports()) {
                    Player teleporter = Bukkit.getPlayer(timedTeleport.getTeleporter().getUniqueId());
                    if (teleporter != null) {
                        if (timedTeleport.getTimeRemaining() > 0) {
                            if (!timedTeleport.hasMoved(teleporter)) {
                                if (!timedTeleport.hasLostHealth(teleporter)) {
                                    teleporter.playSound(teleporter.getLocation(), HuskHomes.getSettings().getTeleportWarmupSound(), 2, 1);
                                    MessageManager.sendActionBarMessage(teleporter, "teleporting_action_bar_countdown",
                                            Integer.toString(timedTeleport.getTimeRemaining()));
                                    timedTeleport.decrementTimeRemaining();
                                } else {
                                    MessageManager.sendActionBarMessage(teleporter, "teleporting_action_bar_cancelled");
                                    MessageManager.sendMessage(teleporter, "teleporting_cancelled_damage");
                                    teleporter.playSound(teleporter.getLocation(), HuskHomes.getSettings().getTeleportCancelledSound(), 1, 1);
                                    completedTeleports.add(timedTeleport);
                                }
                            } else {
                                MessageManager.sendActionBarMessage(teleporter, "teleporting_action_bar_cancelled");
                                MessageManager.sendMessage(teleporter, "teleporting_cancelled_movement");
                                teleporter.playSound(teleporter.getLocation(), HuskHomes.getSettings().getTeleportCancelledSound(), 1, 1);
                                completedTeleports.add(timedTeleport);
                            }
                        } else {
                            // Execute the teleport
                            String targetType = timedTeleport.getTargetType();
                            switch (targetType) {
                                case "point":
                                    TeleportManager.teleportPlayer(teleporter, timedTeleport.getTargetPoint());
                                    break;
                                case "back":
                                    if (HuskHomes.getSettings().doEconomy()) {
                                        double backCost = HuskHomes.getSettings().getBackCost();
                                        if (backCost > 0) {
                                            if (!VaultIntegration.takeMoney(teleporter, backCost)) {
                                                MessageManager.sendMessage(teleporter, "error_insufficient_funds", VaultIntegration.format(backCost));
                                                break;
                                            } else {
                                                MessageManager.sendMessage(teleporter, "back_spent_money", VaultIntegration.format(backCost));
                                            }
                                        }
                                    }
                                    TeleportManager.teleportPlayer(teleporter, timedTeleport.getTargetPoint());
                                    break;
                                case "player":
                                    TeleportManager.teleportPlayer(teleporter, timedTeleport.getTargetPlayerName());
                                    break;
                                case "random":
                                    if (HuskHomes.getSettings().doEconomy()) {
                                        double rtpCost = HuskHomes.getSettings().getRtpCost();
                                        if (rtpCost > 0) {
                                            if (!VaultIntegration.takeMoney(teleporter, rtpCost)) {
                                                MessageManager.sendMessage(teleporter, "error_insufficient_funds", VaultIntegration.format(rtpCost));
                                                break;
                                            } else {
                                                MessageManager.sendMessage(teleporter, "rtp_spent_money", VaultIntegration.format(rtpCost));
                                            }
                                        }
                                    }
                                    TeleportManager.teleportPlayer(teleporter, new RandomPoint(teleporter));
                                    DataManager.updateRtpCooldown(teleporter);
                                    break;
                            }
                            completedTeleports.add(timedTeleport);
                        }
                    } else {
                        completedTeleports.add(timedTeleport);
                    }
                }
            }

            // Clear completed teleports
            if (!completedTeleports.isEmpty()) {
                for (TimedTeleport timedTeleport : completedTeleports) {
                    TeleportManager.getQueuedTeleports().remove(timedTeleport);
                }
            }
            completedTeleports.clear();

            // Clear expired teleport requests
            clearExpiredRequests(expiredTeleportRequests);

            // Update the global player list
            if (HuskHomes.getSettings().doBungee() & HuskHomes.getSettings().doCrossServerTabCompletion()) {
                final long currentTimestmap = Instant.now().getEpochSecond();
                if (currentTimestmap >= nextPlayerListUpdateTime & Bukkit.getOnlinePlayers().size() > 0) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        CrossServerListHandler.updatePlayerList(p);
                        break;
                    }
                    nextPlayerListUpdateTime = currentTimestmap + HuskHomes.getSettings().getCrossServerTabUpdateDelay();
                }
            }
        }, 0L, 20L);
    }

    // Cancel expired teleport requests
    private static void clearExpiredRequests(Set<Player> expiredTeleportRequests) {
        // Check if any requests have expired
        for (Player p : teleportRequests.keySet()) {
            if (teleportRequests.get(p).getExpired()) {
                expiredTeleportRequests.add(p);
            }
        }

        // Clear expired requests
        for (Player p : expiredTeleportRequests) {
            teleportRequests.remove(p);
        }
        expiredTeleportRequests.clear();
    }
}
