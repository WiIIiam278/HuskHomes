package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TpHereCommand extends CommandBase implements TabCompletable {

    protected TpHereCommand(@NotNull HuskHomes implementor) {
        super("tphere", Permission.COMMAND_TPHERE, implementor, "tpohere");
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        if (args.length != 1) {
            plugin.getLocales().getLocale("error_invalid_syntax", "/tphere <player>")
                    .ifPresent(onlineUser::sendMessage);
            return;
        }
        plugin.getTeleportManager().teleportPlayerByName(args[0], onlineUser.getPosition().join(), onlineUser);
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        return args.length <= 1 ? plugin.getCache().players.stream()
                .filter(s -> s.toLowerCase().startsWith(args.length == 1 ? args[0].toLowerCase() : ""))
                .sorted().collect(Collectors.toList()) : Collections.emptyList();
    }
}
