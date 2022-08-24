package net.william278.huskhomes.command;

import de.themoep.minedown.MineDown;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.position.PositionMeta;
import net.william278.huskhomes.util.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EditWarpCommand extends CommandBase implements TabCompletable {

    private final String[] EDIT_WARP_COMPLETIONS = {"rename", "description", "relocate"};

    protected EditWarpCommand(@NotNull HuskHomes implementor) {
        super("editwarp", Permission.COMMAND_EDIT_WARP, implementor);
    }

    @Override
    public void onExecute(@NotNull OnlineUser onlineUser, @NotNull String[] args) {
        if (args.length >= 1) {
            final String warpName = args[0];
            final String editOperation = args.length >= 2 ? args[1] : null;
            final String editArgs = getEditArguments(args);

            plugin.getDatabase().getWarp(warpName).thenAccept(optionalWarp -> {
                if (optionalWarp.isEmpty()) {
                    plugin.getLocales().getLocale("error_warp_invalid", warpName)
                            .ifPresent(onlineUser::sendMessage);
                    return;
                }
                editWarp(optionalWarp.get(), onlineUser, editOperation, editArgs);
            });
        } else {
            plugin.getLocales().getLocale("error_invalid_syntax",
                            "/editwarp <name> [" + String.join("|", EDIT_WARP_COMPLETIONS) + "] [args]")
                    .ifPresent(onlineUser::sendMessage);
        }
    }

    /**
     * Perform the specified EditOperation on the specified warp
     *
     * @param warp          The warp to edit
     * @param editor        The player who is editing the warp
     * @param editOperation The edit operation to perform
     * @param editArgs      Arguments for the edit operation
     */
    private void editWarp(@NotNull Warp warp, @NotNull OnlineUser editor,
                          @Nullable String editOperation, @Nullable String editArgs) {
        final AtomicBoolean showMenuFlag = new AtomicBoolean(false);

        if (editOperation == null) {
            getWarpEditorWindow(warp, true, editor.hasPermission(Permission.COMMAND_WARP.node))
                    .forEach(editor::sendMessage);
            return;
        }
        if (editArgs != null) {
            String argToCheck = editArgs;
            if (editArgs.contains(Pattern.quote(" "))) {
                argToCheck = editArgs.split(Pattern.quote(" "))[0];
            }
            if (argToCheck.equals("-m")) {
                showMenuFlag.set(true);
                editArgs = editArgs.replaceFirst("-m", "");
            }
        }

        switch (editOperation.toLowerCase()) {
            case "rename" -> {
                if (editArgs == null || editArgs.contains(Pattern.quote(" "))) {
                    plugin.getLocales().getLocale("error_invalid_syntax",
                                    "/editwarp <name> rename <new name>")
                            .ifPresent(editor::sendMessage);
                    return;
                }

                final String oldWarpName = warp.meta.name;
                final String newWarpName = editArgs;
                plugin.getSavedPositionManager().updateWarpMeta(warp, new PositionMeta(newWarpName, warp.meta.description))
                        .thenAccept(renameResult -> (switch (renameResult.resultType()) {
                            case SUCCESS ->
                                    plugin.getLocales().getLocale("edit_warp_update_name", oldWarpName, newWarpName);
                            case FAILED_DUPLICATE -> plugin.getLocales().getLocale("error_warp_name_taken");
                            case FAILED_NAME_LENGTH -> plugin.getLocales().getLocale("error_warp_name_length");
                            case FAILED_NAME_CHARACTERS -> plugin.getLocales().getLocale("error_warp_name_characters");
                            default -> plugin.getLocales().getLocale("error_warp_description_characters");
                        }).ifPresent(editor::sendMessage));
            }
            case "description" -> {
                final String oldWarpDescription = warp.meta.description;
                final String newDescription = editArgs != null ? editArgs : "";

                plugin.getSavedPositionManager().updateWarpMeta(warp, new PositionMeta(warp.meta.name, newDescription))
                        .thenAccept(descriptionUpdateResult -> (switch (descriptionUpdateResult.resultType()) {
                            case SUCCESS -> plugin.getLocales().getLocale("edit_warp_update_description",
                                    warp.meta.name,
                                    oldWarpDescription.isBlank() ? plugin.getLocales()
                                            .getRawLocale("item_no_description").orElse("N/A") : oldWarpDescription,
                                    newDescription.isBlank() ? plugin.getLocales()
                                            .getRawLocale("item_no_description").orElse("N/A") : newDescription);
                            case FAILED_DESCRIPTION_LENGTH ->
                                    plugin.getLocales().getLocale("error_warp_description_length");
                            case FAILED_DESCRIPTION_CHARACTERS ->
                                    plugin.getLocales().getLocale("error_warp_description_characters");
                            default -> plugin.getLocales().getLocale("error_warp_name_characters");
                        }).ifPresent(editor::sendMessage));
            }
            case "relocate" ->
                    plugin.getSavedPositionManager().updateWarpPosition(warp, editor.getPosition()).thenRun(() -> {
                        editor.sendMessage(plugin.getLocales().getLocale("edit_warp_update_location",
                                warp.meta.name).orElse(new MineDown("")));

                        // Show the menu if the menu flag is set
                        if (showMenuFlag.get()) {
                            getWarpEditorWindow(warp, false, editor.hasPermission(Permission.COMMAND_WARP.node))
                                    .forEach(editor::sendMessage);
                        }
                    });
            default -> plugin.getLocales().getLocale("error_invalid_syntax",
                            "/editwarp <name> [" + String.join("|", EDIT_WARP_COMPLETIONS) + "] [args]")
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

    /**
     * Get a formatted warp editor chat window for a supplied {@link Warp}
     *
     * @param warp               The warp to display
     * @param showTitle          Whether to show the menu title
     * @param showTeleportButton Whether to show the teleport "use" button
     * @return List of {@link MineDown} messages to send to the editor that form the menu
     */
    @NotNull
    private List<MineDown> getWarpEditorWindow(@NotNull Warp warp, final boolean showTitle,
                                               final boolean showTeleportButton) {
        return new ArrayList<>() {{
            if (showTitle) {
                plugin.getLocales().getLocale("edit_warp_menu_title", warp.meta.name)
                        .ifPresent(this::add);
            }

            plugin.getLocales().getLocale("edit_warp_menu_metadata",
                            DateTimeFormatter.ofPattern("MMM dd yyyy, HH:mm")
                                    .format(warp.meta.creationTime.atZone(ZoneId.systemDefault())),
                            warp.uuid.toString().split(Pattern.quote("-"))[0],
                            warp.uuid.toString())
                    .ifPresent(this::add);

            if (warp.meta.description.length() > 0) {
                plugin.getLocales().getLocale("edit_warp_menu_description",
                                warp.meta.description.length() > 50
                                        ? warp.meta.description.substring(0, 49).trim() + "…" : warp.meta.description,
                                plugin.getLocales().formatDescription(warp.meta.description))
                        .ifPresent(this::add);
            }

            if (!plugin.getSettings().crossServer) {
                plugin.getLocales().getLocale("edit_warp_menu_world", warp.world.name).ifPresent(this::add);
            } else {
                plugin.getLocales().getLocale("edit_warp_menu_world_server", warp.world.name, warp.server.name).ifPresent(this::add);
            }

            plugin.getLocales().getLocale("edit_warp_menu_coordinates",
                            String.format("%.1f", warp.x), String.format("%.1f", warp.y), String.format("%.1f", warp.z),
                            String.format("%.2f", warp.yaw), String.format("%.2f", warp.pitch))
                    .ifPresent(this::add);

            if (showTeleportButton) {
                plugin.getLocales().getLocale("edit_warp_menu_use_buttons", warp.meta.name)
                        .ifPresent(this::add);
            }
            plugin.getLocales().getLocale("edit_warp_menu_manage_buttons", warp.meta.name)
                    .ifPresent(this::add);
            plugin.getLocales().getLocale("edit_warp_menu_meta_edit_buttons", warp.meta.name)
                    .ifPresent(this::add);
        }};
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull String[] args, @Nullable OnlineUser user) {
        return switch (args.length) {
            case 0, 1 -> plugin.getCache().warps.stream()
                    .filter(s -> s.toLowerCase().startsWith(args.length == 1 ? args[0].toLowerCase() : ""))
                    .sorted().collect(Collectors.toList());
            case 2 -> Arrays.stream(EDIT_WARP_COMPLETIONS)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .sorted().collect(Collectors.toList());
            default -> Collections.emptyList();
        };
    }
}
