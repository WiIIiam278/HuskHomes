package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.user.User;
import net.william278.paginedown.PaginatedList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PrivateHomeListCommand extends ListCommand {

    protected PrivateHomeListCommand(@NotNull HuskHomes plugin) {
        super("homelist", List.of("homes"), "[player] [page]", plugin);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        final Optional<String> homeOwner = args.length == 2 ? parseStringArg(args, 0)
                : executor instanceof OnlineUser user ? Optional.of(user.getUsername()) : Optional.empty();
        final int pageNumber = parseIntArg(args, args.length > 0 ? args.length - 1 : 0).orElse(1);
        if (homeOwner.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
            return;
        }

        this.showHomeList(executor, homeOwner.get(), pageNumber);
    }

    protected void showHomeList(@NotNull CommandUser executor, @NotNull String homeOwner, int pageNumber) {
        final Optional<User> targetUser = plugin.getDatabase().getUserDataByName(homeOwner).map(SavedUser::getUser);
        if (targetUser.isEmpty()) {
            plugin.getLocales().getLocale("error_player_not_found", homeOwner)
                    .ifPresent(executor::sendMessage);
            return;
        }

        final User user = targetUser.get();
        if (executor instanceof OnlineUser onlineUser && !user.getUuid().equals(onlineUser.getUuid())
                && !executor.hasPermission(getPermission("other"))) {
            plugin.getLocales().getLocale("error_no_permission")
                    .ifPresent(executor::sendMessage);
            return;
        }

        if (cachedLists.containsKey(user.getUuid())) {
            executor.sendMessage(cachedLists.get(user.getUuid()).getNearestValidPage(pageNumber));
            return;
        }

        final List<Home> homes = plugin.getDatabase().getHomes(user);
        plugin.fireEvent(plugin.getViewHomeListEvent(homes, executor, false),
                (event) -> this.generateList(executor, user, event.getHomes()).ifPresent(homeList -> {
                    cachedLists.put(user.getUuid(), homeList);
                    executor.sendMessage(homeList.getNearestValidPage(pageNumber));
                }));
    }

    private Optional<PaginatedList> generateList(@NotNull CommandUser executor, @NotNull User user, @NotNull List<Home> homes) {
        if (homes.isEmpty()) {
            if (!executor.equals(user)) {
                plugin.getLocales().getLocale("error_no_homes_set_other", user.getUsername())
                        .ifPresent(executor::sendMessage);
            } else {
                plugin.getLocales().getLocale("error_no_homes_set").ifPresent(executor::sendMessage);
            }
            return Optional.empty();
        }

        final String homeListArguments = !executor.equals(user) ? " " + user.getUsername() : "";
        final PaginatedList homeList = PaginatedList.of(homes.stream().map(home ->
                        plugin.getLocales()
                                .getRawLocale("home_list_item",
                                        Locales.escapeText(home.getMeta().getName()),
                                        home.getOwner().getUsername() + "." + Locales.escapeText(home.getMeta().getName()),
                                        Locales.escapeText(plugin.getLocales().wrapText(home.getMeta().getDescription(), 40)))
                                .orElse(home.getMeta().getName())).sorted().collect(Collectors.toList()),
                plugin.getLocales()
                        .getBaseList(plugin.getSettings().getListItemsPerPage())
                        .setHeaderFormat(plugin.getLocales().getRawLocale("home_list_page_title",
                                        user.getUsername(), "%first_item_on_page_index%",
                                        "%last_item_on_page_index%", "%total_items%")
                                .orElse(""))
                        .setCommand("/huskhomes:homelist" + homeListArguments).build());
        return Optional.of(homeList);
    }

}
