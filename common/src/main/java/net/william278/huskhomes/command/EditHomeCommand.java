package net.william278.huskhomes.command;

import de.themoep.minedown.MineDown;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.PositionMeta;
import net.william278.huskhomes.util.Permission;
import net.william278.huskhomes.util.RegexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EditHomeCommand extends CommandBase implements TabCompletable, ConsoleExecutable {

    private final String[] EDIT_HOME_COMPLETIONS = {"rename", "description", "relocate", "privacy"};

    public EditHomeCommand(@NotNull HuskHomes implementor) {
        super("edithome", Permission.COMMAND_EDIT_HOME, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        if (args.length >= 1) {
            final String homeName = args[0];
            final String editOperation = args.length >= 2 ? args[1] : null;
            final String editArgs = getEditArguments(args);

            RegexUtil.matchDisambiguatedHomeIdentifier(homeName).ifPresentOrElse(
                    homeIdentifier -> plugin.getDatabase().getUserDataByName(homeIdentifier.ownerName())
                            .thenAccept(optionalUserData -> optionalUserData.ifPresentOrElse(userData -> {
                                        if (!userData.getUserUuid().equals(onlineUser.uuid)) {
                                            if (!onlineUser.hasPermission(Permission.COMMAND_EDIT_HOME_OTHER.node)) {
                                                plugin.getLocales().getLocale("error_no_permission")
                                                        .ifPresent(onlineUser::sendMessage);
                                                return;
                                            }
                                        }
                                        plugin.getDatabase().getHome(userData.user(), homeIdentifier.homeName()).thenAccept(optionalHome -> {
                                            if (optionalHome.isEmpty()) {
                                                plugin.getLocales().getLocale("error_home_invalid_other",
                                                                homeIdentifier.ownerName(), homeIdentifier.homeName())
                                                        .ifPresent(onlineUser::sendMessage);
                                                return;
                                            }
                                            editHome(optionalHome.get(), onlineUser, editOperation, editArgs);
                                        });

                                    },
                                    () -> plugin.getLocales().getLocale("error_home_invalid_other",
                                                    homeIdentifier.ownerName(), homeIdentifier.homeName())
                                            .ifPresent(onlineUser::sendMessage))),
                    () -> plugin.getDatabase().getHome(onlineUser, homeName).thenAccept(optionalHome -> {
                        if (optionalHome.isEmpty()) {
                            plugin.getLocales().getLocale("error_home_invalid", homeName)
                                    .ifPresent(onlineUser::sendMessage);
                            return;
                        }
                        editHome(optionalHome.get(), onlineUser, editOperation, editArgs);
                    })
            );
        } else {
            plugin.getLocales().getLocale("error_invalid_syntax",
                            "/edithome <name> [" + String.join("|", EDIT_HOME_COMPLETIONS) + "] [args]")
                    .ifPresent(onlineUser::sendMessage);
        }
    }

    /**
     * Perform the specified EditOperation on the specified home
     *
     * @param home          The home to edit
     * @param editor        The player who is editing the home
     * @param editOperation The edit operation to perform
     * @param editArgs      Arguments for the edit operation
     */
    private void editHome(@NotNull Home home, @NotNull OnlineUser editor,
                          @Nullable String editOperation, @Nullable String editArgs) {
        if (editOperation == null) {
            boolean otherOwner = editor.uuid.equals(home.owner.uuid);
            home.getHomeEditorWindow(plugin.getLocales(), otherOwner, plugin.getSettings().crossServer,
                            !otherOwner || editor.hasPermission(Permission.COMMAND_HOME_OTHER.node),
                            !otherOwner || editor.hasPermission(Permission.COMMAND_EDIT_HOME_OTHER.node))
                    .forEach(editor::sendMessage);
            return;
        }
        switch (editOperation.toLowerCase()) {
            case "rename" -> {
                if (editArgs == null || editArgs.contains(Pattern.quote(" "))) {
                    plugin.getLocales().getLocale("error_invalid_syntax",
                                    "/edithome <name> rename <new name>")
                            .ifPresent(editor::sendMessage);
                    return;
                }

                final String oldHomeName = home.meta.name;
                plugin.getSavedPositionManager().updateHomeMeta(home, new PositionMeta(editArgs, home.meta.description))
                        .thenAccept(renameResult ->
                                editor.sendMessage(switch (renameResult.resultType()) {
                                    case SUCCESS -> {
                                        if (home.owner.uuid.equals(editor.uuid)) {
                                            yield plugin.getLocales().getLocale("edit_home_update_name",
                                                    oldHomeName, editArgs).orElse(new MineDown(""));
                                        } else {
                                            yield plugin.getLocales().getLocale("edit_home_update_name_other",
                                                    home.owner.username, oldHomeName, editArgs).orElse(new MineDown(""));
                                        }
                                    }
                                    case FAILED_DUPLICATE -> plugin.getLocales().getLocale("error_set_home_name_taken")
                                            .orElse(new MineDown(""));
                                    case FAILED_NAME_LENGTH ->
                                            plugin.getLocales().getLocale("error_set_home_invalid_length")
                                                    .orElse(new MineDown(""));
                                    case FAILED_NAME_CHARACTERS ->
                                            plugin.getLocales().getLocale("error_set_home_invalid_characters")
                                                    .orElse(new MineDown(""));
                                    default -> new MineDown("");
                                }));
            }
            case "description" -> { //todo not working
                if (editArgs == null) {
                    plugin.getLocales().getLocale("error_invalid_syntax",
                                    "/edithome <name> description <new description>")
                            .ifPresent(editor::sendMessage);
                    return;
                }

                final String oldHomeDescription = home.meta.description;
                plugin.getSavedPositionManager().updateHomeMeta(home, new PositionMeta(home.meta.name, editArgs))
                        .thenAccept(renameResult ->
                                editor.sendMessage(switch (renameResult.resultType()) {
                                    case SUCCESS -> {
                                        if (home.owner.uuid.equals(editor.uuid)) {
                                            yield plugin.getLocales().getLocale("edit_home_update_name",
                                                    oldHomeDescription, editArgs).orElse(new MineDown(""));
                                        } else {
                                            yield plugin.getLocales().getLocale("edit_home_update_name_other",
                                                    home.owner.username, oldHomeDescription, editArgs).orElse(new MineDown(""));
                                        }
                                    }
                                    case FAILED_DESCRIPTION_LENGTH ->
                                            plugin.getLocales().getLocale("error_set_home_invalid_length")
                                                    .orElse(new MineDown(""));
                                    case FAILED_DESCRIPTION_CHARACTERS ->
                                            plugin.getLocales().getLocale("error_set_home_invalid_characters")
                                                    .orElse(new MineDown(""));
                                    default -> new MineDown("");
                                }));
            }
            case "relocate" -> editor.getPosition().thenAccept(position ->
                    plugin.getSavedPositionManager().updateHomePosition(home, position).thenRun(() -> {
                        if (home.owner.uuid.equals(editor.uuid)) {
                            editor.sendMessage(plugin.getLocales().getLocale("edit_home_update_location",
                                    home.meta.name).orElse(new MineDown("")));
                        } else {
                            editor.sendMessage(plugin.getLocales().getLocale("edit_home_update_location_other",
                                    home.owner.username, home.meta.name).orElse(new MineDown("")));
                        }
                    }));
            case "privacy" -> {
                boolean newIsPublic = !home.isPublic;
                if (editArgs != null) {
                    if (editArgs.equalsIgnoreCase("private")) {
                        newIsPublic = false;
                    } else if (editArgs.equalsIgnoreCase("public")) {
                        newIsPublic = true;
                    } else {
                        plugin.getLocales().getLocale("error_invalid_syntax",
                                        "/edithome <name> privacy [private|public]")
                                .ifPresent(editor::sendMessage);
                        return;
                    }
                }
                final String privacyKeyedString = newIsPublic ? "public" : "private";

                if (newIsPublic == home.isPublic) {
                    plugin.getLocales().getLocale(
                                    "error_edit_home_privacy_already_" + privacyKeyedString)
                            .ifPresent(editor::sendMessage);
                    return;
                }

                //todo economy check here

                plugin.getSavedPositionManager().updateHomePrivacy(home, newIsPublic).thenRun(() -> {
                    if (home.owner.uuid.equals(editor.uuid)) {
                        editor.sendMessage(plugin.getLocales().getLocale(
                                "edit_home_privacy_" + privacyKeyedString + "_success",
                                home.meta.name).orElse(new MineDown("")));
                    } else {
                        editor.sendMessage(plugin.getLocales().getLocale(
                                "edit_home_privacy_" + privacyKeyedString + "_success_other",
                                home.owner.username, home.meta.name).orElse(new MineDown("")));
                    }
                });
            }
            default -> plugin.getLocales().getLocale("error_invalid_syntax",
                            "/edithome <name> [" + String.join("|", EDIT_HOME_COMPLETIONS) + "] [args]")
                    .ifPresent(editor::sendMessage);
        }
    }

    @Nullable
    private String getEditArguments(@NotNull String[] args) {
        if (args.length > 2) {
            final StringJoiner joiner = new StringJoiner(" ");
            for (int i = 2; i < args.length; i++) {
                joiner.add(args[i]);
            }
            return joiner.toString();
        }
        return null;
    }

    @Override
    public void onConsoleExecute(@NotNull String[] args) {
        //todo
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        return switch (args.length) {
            case 0, 1 -> plugin.getCache().homes.get(onlineUser.uuid).stream()
                    .filter(s -> s.toLowerCase().startsWith(args.length == 1 ? args[0].toLowerCase() : ""))
                    .sorted().collect(Collectors.toList());
            case 2 -> Arrays.stream(EDIT_HOME_COMPLETIONS)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .sorted().collect(Collectors.toList());
            default -> Collections.emptyList();
        };
    }
}
