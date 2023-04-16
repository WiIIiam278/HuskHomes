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
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.paginedown.PaginatedList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WarpListCommand extends ListCommand {

    protected WarpListCommand(@NotNull HuskHomes plugin) {
        super("warplist", List.of("warps"), "[player] [page]", plugin);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        final int pageNumber = parseIntArg(args, 0).orElse(1);
        this.showWarpList(executor, pageNumber);
    }

    protected void showWarpList(@NotNull CommandUser executor, int pageNumber) {
        if (executor instanceof OnlineUser user && cachedLists.containsKey(user.getUuid())) {
            executor.sendMessage(cachedLists.get(user.getUuid()).getNearestValidPage(pageNumber));
            return;
        }

        final List<Warp> warps = getItems(executor);
        plugin.fireEvent(plugin.getViewWarpListEvent(warps, executor),
                (event) -> this.generateList(executor, event.getWarps()).ifPresent(homeList -> {
                    if (executor instanceof OnlineUser onlineUser) {
                        cachedLists.put(onlineUser.getUuid(), homeList);
                    }
                    executor.sendMessage(homeList.getNearestValidPage(pageNumber));
                }));
    }

    private Optional<PaginatedList> generateList(@NotNull CommandUser executor, @NotNull List<Warp> warps) {
        if (warps.isEmpty()) {
            plugin.getLocales().getLocale("error_no_warps_set")
                    .ifPresent(executor::sendMessage);
            return Optional.empty();
        }

        final PaginatedList warpList = PaginatedList.of(warps.stream().map(warp ->
                        plugin.getLocales()
                                .getRawLocale("warp_list_item",
                                        Locales.escapeText(warp.getName()), warp.getSafeIdentifier(),
                                        Locales.escapeText(plugin.getLocales().wrapText(warp.getMeta().getDescription(), 40)))
                                .orElse(warp.getName())).sorted().collect(Collectors.toList()),
                plugin.getLocales()
                        .getBaseList(plugin.getSettings().getListItemsPerPage())
                        .setHeaderFormat(plugin.getLocales().getRawLocale("warp_list_page_title",
                                        "%first_item_on_page_index%", "%last_item_on_page_index%", "%total_items%")
                                .orElse(""))
                        .setCommand("/huskhomes:warplist").build());
        return Optional.of(warpList);
    }

    @NotNull
    private List<Warp> getItems(@NotNull CommandUser executor) {
        List<Warp> warps = plugin.getDatabase().getWarps();
        if (plugin.getSettings().doPermissionRestrictWarps() && !executor.hasPermission(Warp.getWildcardPermission())) {
            warps = warps.stream()
                    .filter(warp -> executor.hasPermission(getPermission(warp.getPermission())))
                    .collect(Collectors.toList());
        }
        return warps;
    }
}
