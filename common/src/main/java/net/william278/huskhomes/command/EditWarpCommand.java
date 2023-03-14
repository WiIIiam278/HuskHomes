package net.william278.huskhomes.command;

import de.themoep.minedown.adventure.MineDown;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.util.Permission;
import net.william278.huskhomes.util.ValidationException;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class EditWarpCommand extends SavedPositionCommand<Warp> {

    public EditWarpCommand(@NotNull HuskHomes plugin) {
        super("editwarp", List.of(), Warp.class, List.of("rename", "description", "relocate"), plugin);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull Warp warp, @NotNull String[] args) {
        final Optional<String> operation = parseStringArg(args, 0);
        if (operation.isEmpty()) {
            getWarpEditorWindow(warp, true, executor.hasPermission(Permission.COMMAND_WARP.node))
                    .forEach(executor::sendMessage);
            return;
        }

        if (!arguments.contains(operation.get().toLowerCase())) {
            plugin.getLocales().getLocale("error_invalid_syntax",
                            "/editwarp " + warp.getName() + " <" + String.join("|", arguments) + ">")
                    .ifPresent(executor::sendMessage);
            return;
        }

        switch (operation.get().toLowerCase()) {
            case "rename" -> setWarpName(executor, warp, args);
            case "description" -> setWarpDescription(executor, warp, args);
            case "relocate" -> setWarpPosition(executor, warp);
        }
    }

    private void setWarpName(@NotNull CommandUser executor, @NotNull Warp warp, @NotNull String[] args) {
        final Optional<String> newName = parseStringArg(args, 1);
        if (newName.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_syntax",
                            "/editwarp " + warp.getName() + " rename <name>")
                    .ifPresent(executor::sendMessage);
            return;
        }

        try {
            plugin.getManager().warps().renameWarp(warp, newName.get());
        } catch (ValidationException e) {
            e.dispatchWarpError(executor, plugin, newName.get());
            return;
        }

        plugin.getLocales().getLocale("edit_warp_update_name", warp.getName(), newName.get())
                .ifPresent(executor::sendMessage);
    }

    private void setWarpDescription(@NotNull CommandUser executor, @NotNull Warp warp, @NotNull String[] args) {
        final Optional<String> newDescription = parseGreedyString(args, 1);
        if (newDescription.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_syntax", 
                            "/editwarp " + warp.getName() + " description <text>")
                    .ifPresent(executor::sendMessage);
            return;
        }

        try {
            plugin.getManager().warps().setWarpDescription(warp, newDescription.get());
        } catch (ValidationException e) {
            e.dispatchWarpError(executor, plugin, newDescription.get());
            return;
        }
        
        plugin.getLocales().getLocale("edit_warp_update_description", warp.getName(), newDescription.get())
                .ifPresent(executor::sendMessage);
    }

    private void setWarpPosition(@NotNull CommandUser executor, @NotNull Warp warp) {
        if (!(executor instanceof OnlineUser user)) {
            plugin.getLocales().getLocale("error_in_game_only")
                    .ifPresent(executor::sendMessage);
            return;
        }

        try {
            plugin.getManager().warps().relocateWarp(warp, user.getPosition());
        } catch (ValidationException e) {
            e.dispatchWarpError(executor, plugin, warp.getName());
            return;
        }

        plugin.getLocales().getLocale("edit_warp_update_location", warp.getName())
                .ifPresent(executor::sendMessage);
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
    private List<MineDown> getWarpEditorWindow(@NotNull Warp warp, boolean showTitle, boolean showTeleportButton) {
        return new ArrayList<>() {{
            if (showTitle) {
                plugin.getLocales().getLocale("edit_warp_menu_title", warp.getMeta().getName())
                        .ifPresent(this::add);
            }

            plugin.getLocales().getLocale("edit_warp_menu_metadata",
                            DateTimeFormatter.ofPattern("MMM dd yyyy, HH:mm")
                                    .format(warp.getMeta().getCreationTime().atZone(ZoneId.systemDefault())),
                            warp.getUuid().toString().split(Pattern.quote("-"))[0],
                            warp.getUuid().toString())
                    .ifPresent(this::add);

            if (warp.getMeta().getDescription().length() > 0) {
                plugin.getLocales().getLocale("edit_warp_menu_description",
                                warp.getMeta().getDescription().length() > 50
                                        ? warp.getMeta().getDescription().substring(0, 49).trim() + "…" : warp.getMeta().getDescription(),
                                plugin.getLocales().wrapText(warp.getMeta().getDescription(), 40))
                        .ifPresent(this::add);
            }

            if (!plugin.getSettings().isCrossServer()) {
                plugin.getLocales().getLocale("edit_warp_menu_world", warp.getWorld().getName()).ifPresent(this::add);
            } else {
                plugin.getLocales().getLocale("edit_warp_menu_world_server", warp.getWorld().getName(), warp.getServer()).ifPresent(this::add);
            }

            plugin.getLocales().getLocale("edit_warp_menu_coordinates",
                            String.format("%.1f", warp.getX()), String.format("%.1f", warp.getY()), String.format("%.1f", warp.getZ()),
                            String.format("%.2f", warp.getYaw()), String.format("%.2f", warp.getPitch()))
                    .ifPresent(this::add);

            if (showTeleportButton) {
                plugin.getLocales().getLocale("edit_warp_menu_use_buttons", warp.getMeta().getName())
                        .ifPresent(this::add);
            }
            plugin.getLocales().getLocale("edit_warp_menu_manage_buttons", warp.getMeta().getName())
                    .ifPresent(this::add);
            plugin.getLocales().getLocale("edit_warp_menu_meta_edit_buttons", warp.getMeta().getName())
                    .ifPresent(this::add);
        }};
    }

}
