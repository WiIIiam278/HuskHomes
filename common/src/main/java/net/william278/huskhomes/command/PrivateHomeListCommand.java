/*
 * This file is part of HuskHomes, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PrivateHomeListCommand extends ListCommand {

    protected PrivateHomeListCommand(@NotNull HuskHomes plugin) {
        super("homelist", List.of("homes"), "[player] [page]", plugin);
        addAdditionalPermissions(Map.of("other", true));
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        final Optional<String> homeOwner = args.length > 0 ? parseStringArg(args, 0)
                : executor instanceof OnlineUser user ? Optional.of(user.getUsername()) : Optional.empty();
        final int pageNumber = parseIntArg(args, args.length > 1 ? 1 : 0).orElse(1);
        if (homeOwner.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
            return;
        }

        this.showHomeList(executor, homeOwner.get(), pageNumber);
    }

    protected void showHomeList(@NotNull CommandUser executor, @NotNull String homeOwner, int pageNumber) {
        final Optional<User> targetUser = plugin.getDatabase().getUserDataByName(homeOwner).map(SavedUser::getUser);
        final User user;
        final int page;
        if (targetUser.isEmpty()) {
            final Optional<Integer> pageNumberArg = parseIntArg(new String[]{homeOwner}, 0);
            if (pageNumberArg.isEmpty() || !(executor instanceof OnlineUser onlineUser)) {
                plugin.getLocales().getLocale("error_player_not_found", homeOwner)
                        .ifPresent(executor::sendMessage);
                return;
            }
            page = pageNumberArg.get();
            user = onlineUser;
        } else {
            user = targetUser.get();
            page = pageNumber;
        }

        if (executor instanceof OnlineUser onlineUser && !user.getUuid().equals(onlineUser.getUuid())
            && !executor.hasPermission(getPermission("other"))) {
            plugin.getLocales().getLocale("error_no_permission")
                    .ifPresent(executor::sendMessage);
            return;
        }

        if (cachedLists.containsKey(user.getUuid())) {
            executor.sendMessage(cachedLists.get(user.getUuid()).getNearestValidPage(page));
            return;
        }

        final List<Home> homes = plugin.getDatabase().getHomes(user);
        plugin.fireEvent(plugin.getViewHomeListEvent(homes, executor, false),
                (event) -> this.generateList(executor, user, event.getHomes()).ifPresent(homeList -> {
                    cachedLists.put(user.getUuid(), homeList);
                    executor.sendMessage(homeList.getNearestValidPage(page));
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
                                        Locales.escapeText(home.getName()), home.getSafeIdentifier(),
                                        Locales.escapeText(plugin.getLocales().wrapText(home.getMeta().getDescription(), 40)))
                                .orElse(home.getName())).sorted().collect(Collectors.toList()),
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
