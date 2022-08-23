package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TpAllCommand extends CommandBase {

    protected TpAllCommand(@NotNull HuskHomes implementor) {
        super("tpall", Permission.COMMAND_TPALL, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        if (args.length != 0) {
            plugin.getLocales().getLocale("error_invalid_syntax", "/tpaall")
                    .ifPresent(onlineUser::sendMessage);
            return;
        }

        // Update cached players
        plugin.getCache().updateOnlinePlayerList(plugin, onlineUser);

        // Determine players to teleport
        final List<String> players = plugin.getCache().players.stream()
                .filter(userName -> !userName.equalsIgnoreCase(onlineUser.username)).toList();
        if (players.isEmpty()) {
            plugin.getLocales().getLocale("error_no_players_online").ifPresent(onlineUser::sendMessage);
            return;
        }

        // Teleport every player
        players.forEach(playerName -> plugin.getTeleportManager()
                .teleportPlayerByName(playerName, onlineUser.getPosition(), onlineUser));

    }

}
