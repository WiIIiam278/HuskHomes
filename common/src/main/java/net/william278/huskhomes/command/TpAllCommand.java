package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.network.Message;
import net.william278.huskhomes.network.Payload;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.teleport.Teleport;
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
        final Position targetPosition = executor.getPosition();
        for (OnlineUser user : plugin.getOnlineUsers()) {
            Teleport.builder(plugin)
                    .teleporter(user)
                    .target(targetPosition)
                    .toTeleport().execute();
        }

        if (plugin.getSettings().isCrossServer()) {
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
