package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.request.TeleportRequest;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class TpaAllCommand extends CommandBase {

    protected TpaAllCommand(@NotNull HuskHomes implementor) {
        super("tpaall", Permission.COMMAND_TPA_ALL, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        if (args.length != 0) {
            plugin.getLocales().getLocale("error_invalid_syntax", "/tpaall")
                    .ifPresent(onlineUser::sendMessage);
            return;
        }

        // Determine players to teleport and teleport them
        plugin.getCache().updatePlayerListCache(plugin, onlineUser).thenAccept(playerList -> {
            final List<String> players = plugin.getCache().players.stream()
                    .filter(userName -> !userName.equalsIgnoreCase(onlineUser.username)).toList();
            if (players.isEmpty()) {
                plugin.getLocales().getLocale("error_no_players_online").ifPresent(onlineUser::sendMessage);
                return;
            }

            // Send a teleport request to every player
            final AtomicInteger counter = new AtomicInteger(0);
            final List<CompletableFuture<Void>> sentRequestsFuture = new ArrayList<>();
            players.forEach(playerName -> sentRequestsFuture.add(plugin.getRequestManager()
                    .sendTeleportRequest(onlineUser, playerName, TeleportRequest.RequestType.TPA_HERE)
                    .thenAccept(sent -> counter.addAndGet(sent.isPresent() ? 1 : 0))));

            // Send a message when all requests have been sent
            CompletableFuture.allOf(sentRequestsFuture.toArray(new CompletableFuture[0])).thenRun(() -> {
                if (counter.get() == 0) {
                    plugin.getLocales().getLocale("error_no_players_online")
                            .ifPresent(onlineUser::sendMessage);
                    return;
                }
                plugin.getLocales().getLocale("tpaall_request_sent", Integer.toString(counter.get()))
                        .ifPresent(onlineUser::sendMessage);
            });
        });
    }

}
