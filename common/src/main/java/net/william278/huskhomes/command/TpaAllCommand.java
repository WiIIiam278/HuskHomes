package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.network.Message;
import net.william278.huskhomes.network.Payload;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportBuilder;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.teleport.TeleportRequest;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class TpaAllCommand extends InGameCommand {

    protected TpaAllCommand(@NotNull HuskHomes plugin) {
        super("tpaall", List.of(), "", plugin);
    }

    @Override
    public void execute(@NotNull OnlineUser executor, @NotNull String[] args) {
        if (plugin.getManager().requests().isIgnoringRequests(executor)) {
            plugin.getLocales().getLocale("error_ignoring_teleport_requests")
                    .ifPresent(executor::sendMessage);
            return;
        }

        plugin.getManager().requests().sendTeleportAllRequest(executor);
        plugin.getLocales().getLocale("teleporting_all_players")
                .ifPresent(executor::sendMessage);
    }

}
