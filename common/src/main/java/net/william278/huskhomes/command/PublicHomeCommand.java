package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.util.Permission;
import net.william278.huskhomes.util.RegexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PublicHomeCommand extends CommandBase implements TabCompletable, ConsoleExecutable {

    protected PublicHomeCommand(@NotNull HuskHomes implementor) {
        super("publichome", Permission.COMMAND_PUBLIC_HOME, implementor, "phome");
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        switch (args.length) {
            case 0 -> plugin.getDatabase().getPublicHomes().thenAccept(publicHomes -> {
                // Display the public home list if there are any public home set
                if (publicHomes.size() == 0) {
                    plugin.getLocales().getLocale("error_no_public_homes_set").ifPresent(onlineUser::sendMessage);
                    return;
                }

                onlineUser.sendMessage(plugin.getCache().getPublicHomeList(onlineUser, plugin.getLocales(), publicHomes,
                        plugin.getSettings().listItemsPerPage,1));
            });
            case 1 -> {
                final String homeName = args[0];
                // Match the input to a home identifier and teleport
                RegexUtil.matchDisambiguatedHomeIdentifier(homeName).ifPresentOrElse(
                        homeIdentifier -> plugin.getDatabase().getUserDataByName(homeIdentifier.ownerName())
                                .thenAccept(optionalUserData -> optionalUserData.ifPresentOrElse(
                                        userData -> plugin.getTeleportManager().teleportToHomeByName(onlineUser, userData.user(), homeIdentifier.homeName()),
                                        () -> plugin.getLocales().getLocale("error_home_invalid_other", homeIdentifier.ownerName(), homeIdentifier.homeName())
                                                .ifPresent(onlineUser::sendMessage))),
                        () -> plugin.getDatabase().getPublicHomes().thenAccept(publicHomes -> {
                            // If the identifier format was not used, attempt to teleport the player to the closest match
                            final List<Home> homeMatches = publicHomes.stream()
                                    .filter(home -> home.meta.name.equalsIgnoreCase(homeName)).toList();
                            if ((long) homeMatches.size() == 1) {
                                plugin.getTeleportManager().teleportToHome(onlineUser, homeMatches.get(0));
                            } else {
                                plugin.getLocales().getLocale("error_invalid_syntax", "/publichome [<owner_name>.<home_name>]")
                                        .ifPresent(onlineUser::sendMessage);
                            }
                        }));
            }
            default -> plugin.getLocales().getLocale("error_invalid_syntax", "/publichome [<owner_name>.<home_name>]")
                    .ifPresent(onlineUser::sendMessage);
        }
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        //todo
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull String[] args, @Nullable OnlineUser user) {
        // Return every public home name as username.home_name from the cache
        final List<String> publicHomes = new ArrayList<>();
        plugin.getCache().publicHomes.forEach((ownerName, homeNames) ->
                homeNames.forEach(homeName -> publicHomes.add(ownerName + "." + homeName)));
        return args.length > 1 ? Collections.emptyList() : publicHomes.stream().filter(publicHomeIdentifier ->
                        publicHomeIdentifier.split(Pattern.quote("."))[1].toLowerCase()
                                .startsWith(args.length == 1 ? args[0].toLowerCase() : ""))
                .sorted().collect(Collectors.toList());
    }
}
