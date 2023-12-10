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
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.util.TransactionResolver;
import net.william278.huskhomes.util.ValidationException;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class EditHomeCommand extends SavedPositionCommand<Home> {

    public EditHomeCommand(@NotNull HuskHomes plugin) {
        super("edithome", List.of(), Home.class, List.of("rename", "description", "relocate", "privacy"), plugin);
        addAdditionalPermissions(arguments.stream().collect(HashMap::new, (m, e) -> m.put(e, false), HashMap::putAll));
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull Home home, @NotNull String[] args) {
        final boolean ownerEditing = home.getOwner().equals(executor);
        if (!ownerEditing && !executor.hasPermission(getOtherPermission())) {
            plugin.getLocales().getLocale("error_no_permission")
                    .ifPresent(executor::sendMessage);
            return;
        }

        final Optional<String> operation = parseStringArg(args, 0);
        if (operation.isEmpty()) {
            getHomeEditorWindow(home, !ownerEditing,
                    ownerEditing || executor.hasPermission(getOtherPermission()),
                    executor.hasPermission(getPermission("privacy")))
                    .forEach(executor::sendMessage);
            return;
        }

        if (!arguments.contains(operation.get().toLowerCase())) {
            plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
            return;
        }

        switch (operation.get().toLowerCase()) {
            case "rename" -> setHomeName(executor, home, ownerEditing, args);
            case "description" -> setHomeDescription(executor, home, ownerEditing, args);
            case "relocate" -> setHomePosition(executor, home, ownerEditing);
            case "privacy" -> setHomePrivacy(executor, home, ownerEditing, args);
            default -> throw new IllegalStateException("Unexpected value: " + operation.get().toLowerCase());
        }
    }

    private void setHomeName(@NotNull CommandUser executor, @NotNull Home home, boolean ownerEditing,
                             @NotNull String[] args) {
        final String oldName = home.getName();
        final Optional<String> optionalName = parseStringArg(args, 1);
        if (optionalName.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_syntax",
                            "/edithome " + home.getName() + " rename <name>")
                    .ifPresent(executor::sendMessage);
            return;
        }

        home.getMeta().setName(optionalName.get());
        plugin.fireEvent(plugin.getHomeEditEvent(home, executor), (event) -> {
            final String newName = event.getHome().getName();
            try {
                plugin.getManager().homes().setHomeName(home, newName);
            } catch (ValidationException e) {
                e.dispatchHomeError(executor, false, plugin, newName);
                return;
            }

            if (ownerEditing) {
                plugin.getLocales().getLocale("edit_home_update_name", oldName, newName)
                        .ifPresent(executor::sendMessage);
            } else {
                plugin.getLocales().getLocale("edit_home_update_name_other", home.getOwner().getUsername(),
                                oldName, newName)
                        .ifPresent(executor::sendMessage);
            }
        });
    }

    private void setHomeDescription(@NotNull CommandUser executor, @NotNull Home home, boolean ownerEditing,
                                    @NotNull String[] args) {
        final String oldDescription = home.getMeta().getDescription();
        final Optional<String> optionalDescription = parseGreedyArguments(args);
        if (optionalDescription.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_syntax",
                            "/edithome " + home.getName() + " description <text>")
                    .ifPresent(executor::sendMessage);
            return;
        }

        home.getMeta().setDescription(optionalDescription.get());
        plugin.fireEvent(plugin.getHomeEditEvent(home, executor), (event) -> {
            final String newDescription = event.getHome().getMeta().getDescription();
            try {
                plugin.getManager().homes().setHomeDescription(home, newDescription);
            } catch (ValidationException e) {
                e.dispatchHomeError(executor, false, plugin, newDescription);
                return;
            }

            if (ownerEditing) {
                plugin.getLocales().getLocale("edit_home_update_description",
                                home.getName(), oldDescription, newDescription)
                        .ifPresent(executor::sendMessage);
            } else {
                plugin.getLocales().getLocale("edit_home_update_description_other", home.getOwner().getUsername(),
                                home.getName(), oldDescription, newDescription)
                        .ifPresent(executor::sendMessage);
            }
        });
    }

    private void setHomePosition(@NotNull CommandUser executor, @NotNull Home home, boolean ownerEditing) {
        if (!(executor instanceof OnlineUser user)) {
            plugin.getLocales().getLocale("error_in_game_only")
                    .ifPresent(executor::sendMessage);
            return;
        }

        home.update(user.getPosition());
        plugin.fireEvent(plugin.getHomeEditEvent(home, executor), (event) -> {
            try {
                plugin.getManager().homes().setHomePosition(home, home);
            } catch (ValidationException e) {
                e.dispatchHomeError(executor, false, plugin, home.getName());
                return;
            }

            if (ownerEditing) {
                plugin.getLocales().getLocale("edit_home_update_location", home.getName())
                        .ifPresent(executor::sendMessage);
            } else {
                plugin.getLocales().getLocale("edit_home_update_location_other",
                                home.getOwner().getUsername(), home.getName())
                        .ifPresent(executor::sendMessage);
            }
        });
    }

    private void setHomePrivacy(@NotNull CommandUser executor, @NotNull Home home, boolean ownerEditing,
                                @NotNull String[] args) {
        if (!executor.hasPermission(getPermission("privacy"))) {
            plugin.getLocales().getLocale("error_no_permission")
                    .ifPresent(executor::sendMessage);
            return;
        }

        // Check against the economy hook
        if (executor instanceof OnlineUser user
                && !plugin.validateTransaction(user, TransactionResolver.Action.MAKE_HOME_PUBLIC)) {
            return;
        }

        // Set the home privacy
        home.setPublic(parseStringArg(args, 1)
                .map("public"::equalsIgnoreCase)
                .orElse(!home.isPublic()));

        // Fire the event
        plugin.fireEvent(plugin.getHomeEditEvent(home, executor), (event) -> {
            try {
                plugin.getManager().homes().setHomePrivacy(
                        home.getOwner().equals(executor) ? (OnlineUser) executor : home.getOwner(),
                        home,
                        home.isPublic()
                );
            } catch (ValidationException e) {
                int maxHomes = plugin.getManager().homes().getMaxPublicHomes(
                        executor instanceof OnlineUser user ? user : null
                );
                e.dispatchHomeError(executor, false, plugin, Integer.toString(maxHomes));
                return;
            }

            // Perform transaction
            if (executor instanceof OnlineUser user) {
                plugin.performTransaction(user, TransactionResolver.Action.MAKE_HOME_PUBLIC);
            }

            final String privacy = home.isPublic() ? "public" : "private";
            if (ownerEditing) {
                plugin.getLocales().getLocale("edit_home_privacy_" + privacy + "_success",
                                home.getName())
                        .ifPresent(executor::sendMessage);
            } else {
                plugin.getLocales().getLocale("edit_home_privacy_" + privacy + "_success_other",
                                home.getOwner().getUsername(), home.getName())
                        .ifPresent(executor::sendMessage);
            }
        });
    }

    /**
     * Get a formatted home editor chat window for a supplied {@link Home}.
     *
     * @param home                    The home to display
     * @param otherViewer             If the viewer of the editor is not the homeowner
     * @param showTeleportButton      Whether to show the teleport "use" button
     * @param showPrivacyToggleButton Whether to show the home privacy toggle button
     * @return List of {@link MineDown} messages to send to the editor that form the menu
     */
    @NotNull
    private List<MineDown> getHomeEditorWindow(@NotNull Home home, boolean otherViewer,
                                               boolean showTeleportButton, boolean showPrivacyToggleButton) {
        final List<MineDown> messages = new ArrayList<>();
        if (!otherViewer) {
            plugin.getLocales().getLocale("edit_home_menu_title", home.getName())
                    .ifPresent(messages::add);
        } else {
            plugin.getLocales().getLocale("edit_home_menu_title_other",
                            home.getOwner().getUsername(), home.getName())
                    .ifPresent(messages::add);
        }

        plugin.getLocales().getLocale("edit_home_menu_metadata_" + (!home.isPublic() ? "private" : "public"),
                        DateTimeFormatter.ofPattern("MMM dd yyyy, HH:mm")
                                .format(home.getMeta().getCreationTime().atZone(ZoneId.systemDefault())),
                        home.getUuid().toString().split(Pattern.quote("-"))[0],
                        home.getUuid().toString())
                .ifPresent(messages::add);

        if (home.getMeta().getDescription().length() > 0) {
            plugin.getLocales().getLocale("edit_home_menu_description",
                            plugin.getLocales().truncateText(home.getMeta().getDescription(), 50),
                            plugin.getLocales().wrapText(home.getMeta().getDescription(), 40))
                    .ifPresent(messages::add);
        }

        if (!plugin.getSettings().doCrossServer()) {
            plugin.getLocales().getLocale("edit_home_menu_world", home.getWorld().getName())
                    .ifPresent(messages::add);
        } else {
            plugin.getLocales().getLocale("edit_home_menu_world_server",
                            home.getWorld().getName(), home.getServer())
                    .ifPresent(messages::add);
        }

        plugin.getLocales().getLocale("edit_home_menu_coordinates",
                        String.format("%.1f", home.getX()),
                        String.format("%.1f", home.getY()),
                        String.format("%.1f", home.getZ()),
                        String.format("%.2f", home.getYaw()),
                        String.format("%.2f", home.getPitch()))
                .ifPresent(messages::add);

        if (showTeleportButton) {
            plugin.getLocales().getLocale("edit_home_menu_use_buttons", home.getSafeIdentifier())
                    .ifPresent(messages::add);
        }
        plugin.getLocales().getRawLocale("edit_home_menu_manage_buttons",
                        home.getSafeIdentifier(),
                        showPrivacyToggleButton ? plugin.getLocales()
                                .getRawLocale("edit_home_menu_privacy_button_"
                                        + (home.isPublic() ? "private" : "public"), home.getSafeIdentifier())
                                .orElse("") : "")
                .map(MineDown::new).ifPresent(messages::add);
        plugin.getLocales().getLocale("edit_home_menu_meta_edit_buttons", home.getSafeIdentifier())
                .ifPresent(messages::add);
        return messages;
    }

}
