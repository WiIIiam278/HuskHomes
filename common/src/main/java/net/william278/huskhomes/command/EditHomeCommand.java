package net.william278.huskhomes.command;

import de.themoep.minedown.adventure.MineDown;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.hook.EconomyHook;
import net.william278.huskhomes.position.Home;
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

public class EditHomeCommand extends SavedPositionCommand<Home> {

    public EditHomeCommand(@NotNull HuskHomes plugin) {
        super("edithome", List.of(), Home.class, List.of("rename", "description", "relocate", "privacy"), plugin);
        addAdditionalPermissions(arguments.stream()
                .collect(HashMap::new, (m, e) -> m.put(getPermission(e), false), HashMap::putAll));
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
                    !ownerEditing || executor.hasPermission(getOtherPermission()),
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
        }
    }

    private void setHomeName(@NotNull CommandUser executor, @NotNull Home home, boolean ownerEditing, @NotNull String[] args) {
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

    private void setHomeDescription(@NotNull CommandUser executor, @NotNull Home home, boolean ownerEditing, @NotNull String[] args) {
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
                plugin.getLocales().getLocale("edit_home_update_description", home.getName(), oldDescription, newDescription)
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
                plugin.getLocales().getLocale("edit_home_update_location_other", home.getOwner().getUsername(), home.getName())
                        .ifPresent(executor::sendMessage);
            }
        });
    }

    private void setHomePrivacy(@NotNull CommandUser executor, @NotNull Home home, boolean ownerEditing, @NotNull String[] args) {
        if (!executor.hasPermission(getPermission("privacy"))) {
            plugin.getLocales().getLocale("error_no_permission")
                    .ifPresent(executor::sendMessage);
            return;
        }

        final Optional<String> privacyArg = parseStringArg(args, 1).map(String::toLowerCase);
        if (privacyArg.isEmpty() || !(privacyArg.get().equals("public") || privacyArg.get().equals("private"))) {
            plugin.getLocales().getLocale("error_invalid_syntax",
                            "/edithome " + home.getName() + " privacy <public|private>")
                    .ifPresent(executor::sendMessage);
            return;
        }

        // Check against economy
        if (executor instanceof OnlineUser user && !plugin.validateEconomyCheck(user, EconomyHook.Action.MAKE_HOME_PUBLIC)) {
            return;
        }

        home.setPublic(privacyArg.get().equals("public"));
        plugin.fireEvent(plugin.getHomeEditEvent(home, executor), (event) -> {
            try {
                plugin.getManager().homes().setHomePrivacy(event.getHome(), home.isPublic());
            } catch (ValidationException e) {
                int maxHomes = executor instanceof OnlineUser user ?
                        user.getMaxPublicHomes(plugin.getSettings().getMaxPublicHomes(),
                                plugin.getSettings().doStackPermissionLimits()) :
                        plugin.getSettings().getMaxPublicHomes();
                e.dispatchHomeError(executor, false, plugin, Integer.toString(maxHomes));
                return;
            }

            // Perform transaction
            if (executor instanceof OnlineUser user) {
                plugin.performEconomyTransaction(user, EconomyHook.Action.MAKE_HOME_PUBLIC);
            }

            final String privacy = home.isPublic() ? "public" : "private";
            if (ownerEditing) {
                plugin.getLocales().getLocale("edit_home_privacy_" + privacy + "_success",
                                home.getMeta().getName())
                        .ifPresent(executor::sendMessage);
            } else {
                plugin.getLocales().getLocale("edit_home_privacy_" + privacy + "_success_other",
                                home.getOwner().getUsername(), home.getMeta().getName())
                        .ifPresent(executor::sendMessage);
            }
        });
    }

    /**
     * Get a formatted home editor chat window for a supplied {@link Home}
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
        return new ArrayList<>() {{
            if (!otherViewer) {
                plugin.getLocales().getLocale("edit_home_menu_title", home.getMeta().getName())
                        .ifPresent(this::add);
            } else {
                plugin.getLocales().getLocale("edit_home_menu_title_other", home.getOwner().getUsername(), home.getMeta().getName())
                        .ifPresent(this::add);
            }

            plugin.getLocales().getLocale("edit_home_menu_metadata_" + (!home.isPublic() ? "private" : "public"),
                            DateTimeFormatter.ofPattern("MMM dd yyyy, HH:mm")
                                    .format(home.getMeta().getCreationTime().atZone(ZoneId.systemDefault())),
                            home.getUuid().toString().split(Pattern.quote("-"))[0],
                            home.getUuid().toString())
                    .ifPresent(this::add);

            if (home.getMeta().getDescription().length() > 0) {
                plugin.getLocales().getLocale("edit_home_menu_description",
                                home.getMeta().getDescription().length() > 50
                                        ? home.getMeta().getDescription().substring(0, 49).trim() + "…" : home.getMeta().getDescription(),
                                plugin.getLocales().wrapText(home.getMeta().getDescription(), 40))
                        .ifPresent(this::add);
            }

            if (!plugin.getSettings().doCrossServer()) {
                plugin.getLocales().getLocale("edit_home_menu_world", home.getWorld().getName())
                        .ifPresent(this::add);
            } else {
                plugin.getLocales().getLocale("edit_home_menu_world_server", home.getWorld().getName(), home.getServer())
                        .ifPresent(this::add);
            }

            plugin.getLocales().getLocale("edit_home_menu_coordinates",
                            String.format("%.1f", home.getX()), String.format("%.1f", home.getY()), String.format("%.1f", home.getZ()),
                            String.format("%.2f", home.getYaw()), String.format("%.2f", home.getPitch()))
                    .ifPresent(this::add);

            final String formattedName = home.getOwner().getUsername() + "." + home.getMeta().getName();
            if (showTeleportButton) {
                plugin.getLocales().getLocale("edit_home_menu_use_buttons",
                                formattedName)
                        .ifPresent(this::add);
            }
            final String escapedName = Locales.escapeText(formattedName);
            plugin.getLocales().getRawLocale("edit_home_menu_manage_buttons", escapedName,
                            showPrivacyToggleButton ? plugin.getLocales()
                                    .getRawLocale("edit_home_menu_privacy_button_"
                                                  + (home.isPublic() ? "private" : "public"), escapedName)
                                    .orElse("") : "")
                    .map(MineDown::new).ifPresent(this::add);
            plugin.getLocales().getLocale("edit_home_menu_meta_edit_buttons",
                            formattedName)
                    .ifPresent(this::add);
        }};
    }

}
