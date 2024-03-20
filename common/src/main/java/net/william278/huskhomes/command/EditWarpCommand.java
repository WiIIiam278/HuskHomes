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

import de.themoep.minedown.adventure.MineDown;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.util.ValidationException;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class EditWarpCommand extends SavedPositionCommand<Warp> {

    public EditWarpCommand(@NotNull HuskHomes plugin) {
        super("editwarp", List.of(), Warp.class, List.of("rename", "description", "relocate"), plugin);
        setOperatorCommand(true);
        addAdditionalPermissions(arguments.stream().collect(HashMap::new, (m, e) -> m.put(e, false), HashMap::putAll));
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull Warp warp, @NotNull String[] args) {
        final Optional<String> operation = parseStringArg(args, 0);
        if (operation.isEmpty()) {
            getWarpEditorWindow(warp).forEach(executor::sendMessage);
            return;
        }

        if (!arguments.contains(operation.get().toLowerCase())) {
            plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
            return;
        }

        switch (operation.get().toLowerCase()) {
            case "rename" -> setWarpName(executor, warp, args);
            case "description" -> setWarpDescription(executor, warp, args);
            case "relocate" -> setWarpPosition(executor, warp);
            default -> throw new IllegalStateException("Unexpected value: " + operation.get().toLowerCase());
        }
    }

    private void setWarpName(@NotNull CommandUser executor, @NotNull Warp warp, @NotNull String[] args) {
        final Warp original = warp.copy();
        final Optional<String> optionalName = parseStringArg(args, 1);
        if (optionalName.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_syntax",
                            "/editwarp " + warp.getName() + " rename <name>")
                    .ifPresent(executor::sendMessage);
            return;
        }
        warp.getMeta().setName(optionalName.get());
        plugin.fireEvent(plugin.getWarpEditEvent(warp, original, executor), (event) -> {
            try {
                plugin.getManager().warps().setWarpName(warp, warp.getName());
            } catch (ValidationException e) {
                e.dispatchWarpError(executor, plugin, warp.getName());
                return;
            }

            plugin.getLocales().getLocale("edit_warp_update_name", original.getName(), optionalName.get())
                    .ifPresent(executor::sendMessage);
        });
    }

    private void setWarpDescription(@NotNull CommandUser executor, @NotNull Warp warp, @NotNull String[] args) {
        final Warp original = warp.copy();
        final Optional<String> optionalDescription = parseGreedyArguments(args);
        if (optionalDescription.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_syntax",
                            "/editwarp " + warp.getName() + " description <text>")
                    .ifPresent(executor::sendMessage);
            return;
        }

        warp.getMeta().setDescription(optionalDescription.get());
        plugin.fireEvent(plugin.getWarpEditEvent(warp, original, executor), (event) -> {
            try {
                plugin.getManager().warps().setWarpDescription(warp, warp.getMeta().getDescription());
            } catch (ValidationException e) {
                e.dispatchWarpError(executor, plugin, warp.getMeta().getDescription());
                return;
            }

            plugin.getLocales().getLocale("edit_warp_update_description", warp.getName(),
                            original.getMeta().getDescription(), warp.getMeta().getDescription())
                    .ifPresent(executor::sendMessage);
        });
    }

    private void setWarpPosition(@NotNull CommandUser executor, @NotNull Warp warp) {
        if (!(executor instanceof OnlineUser user)) {
            plugin.getLocales().getLocale("error_in_game_only")
                    .ifPresent(executor::sendMessage);
            return;
        }

        final Warp original = warp.copy();
        warp.update(user.getPosition());
        plugin.fireEvent(plugin.getWarpEditEvent(warp, original, executor), (event) -> {
            try {
                plugin.getManager().warps().setWarpPosition(warp, warp);
            } catch (ValidationException e) {
                e.dispatchWarpError(executor, plugin, warp.getName());
                return;
            }

            plugin.getLocales().getLocale("edit_warp_update_location", warp.getName())
                    .ifPresent(executor::sendMessage);
        });
    }

    /**
     * Get a formatted warp editor chat window for a supplied {@link Warp}.
     *
     * @param warp The warp to display
     * @return List of {@link MineDown} messages to send to the editor that form the menu
     */
    @NotNull
    private List<MineDown> getWarpEditorWindow(@NotNull Warp warp) {
        final List<MineDown> messages = new ArrayList<>();
        plugin.getLocales().getLocale("edit_warp_menu_title", warp.getName())
                .ifPresent(messages::add);

        plugin.getLocales().getLocale("edit_warp_menu_metadata",
                        DateTimeFormatter.ofPattern("MMM dd yyyy, HH:mm")
                                .format(warp.getMeta().getCreationTime().atZone(ZoneId.systemDefault())),
                        warp.getUuid().toString().split(Pattern.quote("-"))[0],
                        warp.getUuid().toString())
                .ifPresent(messages::add);

        if (!warp.getMeta().getDescription().isEmpty()) {
            plugin.getLocales().getLocale("edit_warp_menu_description",
                            plugin.getLocales().truncateText(warp.getMeta().getDescription(), 50),
                            warp.getMeta().getDescription())
                    .ifPresent(messages::add);
        }

        if (!plugin.getSettings().getCrossServer().isEnabled()) {
            plugin.getLocales().getLocale("edit_warp_menu_world",
                    warp.getWorld().getName()).ifPresent(messages::add);
        } else {
            plugin.getLocales().getLocale("edit_warp_menu_world_server",
                    warp.getWorld().getName(), warp.getServer()).ifPresent(messages::add);
        }

        plugin.getLocales().getLocale("edit_warp_menu_coordinates",
                        String.format("%.1f", warp.getX()),
                        String.format("%.1f", warp.getY()),
                        String.format("%.1f", warp.getZ()),
                        String.format("%.2f", warp.getYaw()),
                        String.format("%.2f", warp.getPitch()))
                .ifPresent(messages::add);

        plugin.getLocales().getLocale("edit_warp_menu_use_buttons", warp.getSafeIdentifier())
                .ifPresent(messages::add);
        plugin.getLocales().getLocale("edit_warp_menu_manage_buttons", warp.getSafeIdentifier())
                .ifPresent(messages::add);
        plugin.getLocales().getLocale("edit_warp_menu_meta_edit_buttons", warp.getSafeIdentifier())
                .ifPresent(messages::add);
        return messages;
    }

}
