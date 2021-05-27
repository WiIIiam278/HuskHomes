package me.william278.huskhomes2.teleport;

import de.themoep.minedown.MineDown;
import me.william278.huskhomes2.HuskHomes;
import me.william278.huskhomes2.MessageManager;
import me.william278.huskhomes2.api.events.PlayerSetHomeEvent;
import me.william278.huskhomes2.commands.HomeCommand;
import me.william278.huskhomes2.data.DataManager;
import me.william278.huskhomes2.data.pluginmessage.PluginMessage;
import me.william278.huskhomes2.data.pluginmessage.PluginMessageType;
import me.william278.huskhomes2.integrations.VanishChecker;
import me.william278.huskhomes2.integrations.VaultIntegration;
import me.william278.huskhomes2.teleport.points.Home;
import me.william278.huskhomes2.teleport.points.RandomPoint;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class TeleportRequestHandler {

    private static final HuskHomes plugin = HuskHomes.getInstance();

    // Target player and teleport request to them hashmap
    public static Map<Player, TeleportRequest> teleportRequests = new HashMap<>();

    // Timestamp identifying when the global player list should be updated next;
    private static long nextPlayerListUpdateTime = Instant.now().getEpochSecond() + HuskHomes.getSettings().getCrossServerTabUpdateDelay();

    private static void sendTeleportRequestCrossServer(Player requester, String targetPlayerName, TeleportRequest.RequestType requestType) {
        if (requestType == TeleportRequest.RequestType.TPA) {
            new PluginMessage(targetPlayerName, PluginMessageType.TPA_REQUEST, requester.getName()).send(requester);
        } else if (requestType == TeleportRequest.RequestType.TPAHERE) {
            new PluginMessage(targetPlayerName, PluginMessageType.TPAHERE_REQUEST, requester.getName()).send(requester);
        }
    }

    private static void replyTeleportRequestCrossServer(Player replier, String requesterName, TeleportRequest.RequestType requestType, boolean accepted) {
        if (requestType == TeleportRequest.RequestType.TPA) {
            new PluginMessage(requesterName,  PluginMessageType.TPA_REQUEST_REPLY, replier.getName(), Boolean.toString(accepted));
        } else if (requestType == TeleportRequest.RequestType.TPAHERE) {
            new PluginMessage(requesterName,  PluginMessageType.TPAHERE_REQUEST_REPLY, replier.getName(), Boolean.toString(accepted));
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

                            if (requestType == TeleportRequest.RequestType.TPA) {
                                replyTeleportRequestCrossServer(p, requesterName, TeleportRequest.RequestType.TPA, true);
                            } else if (requestType == TeleportRequest.RequestType.TPAHERE) {
                                replyTeleportRequestCrossServer(p, requesterName, TeleportRequest.RequestType.TPAHERE, true);
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

            /*// Update the global player list
            if (HuskHomes.getSettings().doBungee() & HuskHomes.getSettings().doCrossServerTabCompletion()) {
                final long currentTimestmap = Instant.now().getEpochSecond();
                if (currentTimestmap >= nextPlayerListUpdateTime & Bukkit.getOnlinePlayers().size() > 0) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        CrossServerListHandler.updatePlayerList(p);
                        break;
                    }
                    nextPlayerListUpdateTime = currentTimestmap + HuskHomes.getSettings().getCrossServerTabUpdateDelay();
                }
            }*/
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
