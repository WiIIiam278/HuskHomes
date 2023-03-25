package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.network.Message;
import net.william278.huskhomes.network.Payload;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportationException;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TpAllCommand extends InGameCommand {

    protected TpAllCommand(@NotNull HuskHomes plugin) {
        super("tpall", List.of(), "", plugin);
        setOperatorCommand(true);
    }

    @Override
    public void execute(@NotNull OnlineUser executor, @NotNull String[] args) {
        if (plugin.getGlobalPlayerList().size() <= 1) {
            plugin.getLocales().getLocale("error_no_players_online")
                    .ifPresent(executor::sendMessage);
            return;
        }

        final Position targetPosition = executor.getPosition();
        try {
            for (OnlineUser user : plugin.getOnlineUsers()) {
                Teleport.builder(plugin)
                        .teleporter(user)
                        .target(targetPosition)
                        .toTeleport().execute();
            }
        } catch (TeleportationException e) {
            e.displayMessage(executor, plugin, args);
            return;
        }

        if (plugin.getSettings().doCrossServer()) {
            Message.builder()
                    .target(Message.TARGET_ALL)
                    .type(Message.Type.TELEPORT_TO_POSITION)
                    .payload(Payload.withPosition(targetPosition))
                    .build().send(plugin.getMessenger(), executor);
        }

        plugin.getLocales().getLocale("teleporting_all_players")
                .ifPresent(executor::sendMessage);
    }

}
