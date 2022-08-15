package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.list.PrivateHomeList;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.util.Permission;
import net.william278.huskhomes.util.RegexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class HomeCommand extends CommandBase implements TabCompletable, ConsoleExecutable {

    public HomeCommand(@NotNull HuskHomes implementor) {
        super("home", Permission.COMMAND_HOME, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        switch (args.length) {
            case 0 -> plugin.getDatabase().getHomes(onlineUser).thenAccept(homes -> {
                // Send the home list if they have homes set. If they have just one home set, teleport the player
                try {
                    switch (homes.size()) {
                        case 0 ->
                                plugin.getLocales().getLocale("error_no_homes_set").ifPresent(onlineUser::sendMessage);
                        case 1 -> plugin.getTeleportManager().teleport(onlineUser, homes.get(0)).thenAccept(result ->
                                plugin.getTeleportManager().finishTeleport(onlineUser, result)).join();
                        default -> {
                            final PrivateHomeList homeList = new PrivateHomeList(homes, onlineUser, plugin);
                            plugin.getCache().positionLists.put(onlineUser.uuid, homeList);
                            homeList.getDisplay(1).forEach(onlineUser::sendMessage);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            case 1 -> {
                // Parse the home name input and teleport the player to the home
                final String homeName = args[0];
                RegexUtil.matchDisambiguatedHomeIdentifier(homeName).ifPresentOrElse(
                        homeIdentifier -> plugin.getDatabase().getUserDataByName(homeIdentifier.ownerName())
                                .thenAccept(optionalUserData -> optionalUserData.ifPresentOrElse(
                                        userData -> plugin.getTeleportManager().teleportToHome(onlineUser, userData.user(), homeIdentifier.homeName()),
                                        () -> plugin.getLocales().getLocale("error_home_invalid_other", homeIdentifier.ownerName(), homeIdentifier.homeName())
                                                .ifPresent(onlineUser::sendMessage))),
                        () -> plugin.getTeleportManager().teleportToHome(onlineUser, onlineUser, homeName));
            }
            default -> plugin.getLocales().getLocale("error_invalid_syntax", "/home [name]")
                    .ifPresent(onlineUser::sendMessage);
        }
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        //todo
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        return plugin.getCache().homes.get(onlineUser.uuid).stream()
                .filter(s -> s.toLowerCase().startsWith(args.length >= 1 ? args[0].toLowerCase() : ""))
                .sorted().collect(Collectors.toList());
    }
}
