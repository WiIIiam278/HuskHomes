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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.william278.desertwell.about.AboutMenu;
import net.william278.desertwell.util.UpdateChecker;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.importer.Importer;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.user.User;
import net.william278.paginedown.PaginatedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HuskHomesCommand extends Command implements TabProvider {

    private static final Map<String, Boolean> SUB_COMMANDS = Map.of(
            "about", false,
            "help", false,
            "reload", true,
            "import", true,
            "delete", true,
            "update", true
    );

    private boolean importersLoaded = false;
    private final UpdateChecker updateChecker;
    private final AboutMenu aboutMenu;

    protected HuskHomesCommand(@NotNull HuskHomes plugin) {
        super("huskhomes", List.of(), "[" + String.join("|", SUB_COMMANDS.keySet()) + "]", plugin);
        addAdditionalPermissions(SUB_COMMANDS);

        this.updateChecker = plugin.getUpdateChecker();
        this.aboutMenu = AboutMenu.builder()
                .title(Component.text("HuskHomes"))
                .description(Component.text("A powerful, intuitive and flexible teleportation suite"))
                .version(plugin.getVersion())
                .credits("Author",
                        AboutMenu.Credit.of("William278").description("Click to visit website").url("https://william278.net"))
                .credits("Contributors",
                        AboutMenu.Credit.of("imDaniX").description("Code, refactoring"),
                        AboutMenu.Credit.of("Log1x").description("Code"))
                .credits("Translators",
                        AboutMenu.Credit.of("SnivyJ").description("Simplified Chinese (zh-cn)"),
                        AboutMenu.Credit.of("ApliNi").description("Simplified Chinese (zh-cn)"),
                        AboutMenu.Credit.of("Wtq_").description("Simplified Chinese (zh-cn)"),
                        AboutMenu.Credit.of("TonyPak").description("Traditional Chinese (zh-tw)"),
                        AboutMenu.Credit.of("davgo0103").description("Traditional Chinese (zh-tw)"),
                        AboutMenu.Credit.of("Villag3r_").description("Italian (it-it)"),
                        AboutMenu.Credit.of("ReferTV").description("Polish (pl)"),
                        AboutMenu.Credit.of("anchelthe").description("Spanish (es-es)"),
                        AboutMenu.Credit.of("Chiquis2005").description("Spanish (es-es)"),
                        AboutMenu.Credit.of("Ceddix").description("German, (de-de)"),
                        AboutMenu.Credit.of("Pukejoy_1").description("Bulgarian (bg-bg)"))
                .buttons(
                        AboutMenu.Link.of("https://william278.net/docs/huskhomes").text("Documentation").icon("⛏"),
                        AboutMenu.Link.of("https://github.com/WiIIiam278/HuskHomes/issues").text("Issues").icon("❌").color(TextColor.color(0xff9f0f)),
                        AboutMenu.Link.of("https://discord.gg/tVYhJfyDWG").text("Discord").icon("⭐").color(TextColor.color(0x6773f5)))
                .build();
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        final String action = parseStringArg(args, 0).orElse("about");
        if (SUB_COMMANDS.containsKey(action) && !executor.hasPermission(getPermission(action))) {
            plugin.getLocales().getLocale("error_no_permission")
                    .ifPresent(executor::sendMessage);
            return;
        }

        switch (action.toLowerCase()) {
            case "about" -> executor.sendMessage(aboutMenu.toComponent());
            case "help" -> executor.sendMessage(getCommandList(executor)
                    .getNearestValidPage(parseIntArg(args, 1).orElse(1)));
            case "reload" -> {
                if (!plugin.loadConfigs()) {
                    executor.sendMessage(new MineDown(
                            "[Error:](#ff3300) [Failed to reload the plugin. Check console for errors.](#ff7e5e)"
                    ));
                    return;
                }
                executor.sendMessage(new MineDown(
                        "[HuskHomes](#00fb9a bold) [| Reloaded config & message files.](#00fb9a)\n"
                                + "[ℹ If you have modified the database or cross-server message broker settings,"
                                + " you need to restart your server for these changes to take effect.](gray)"
                ));
            }
            case "import" -> {
                if (!importersLoaded) {
                    importersLoaded = true;
                    plugin.registerImporters();
                }
                if (plugin.getImporters().isEmpty()) {
                    plugin.getLocales().getLocale("error_no_importers_available")
                            .ifPresent(executor::sendMessage);
                    return;
                }
                this.importData(executor, removeFirstArg(args));
            }
            case "delete" -> {
                if (args.length < 2) {
                    plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                            .ifPresent(executor::sendMessage);
                    return;
                }
                final String[] deletionArgs = removeFirstArg(args);
                switch (deletionArgs[0].toLowerCase(Locale.ENGLISH)) {
                    case "player" -> this.deletePlayerData(executor, removeFirstArg(deletionArgs));
                    case "homes" -> this.deleteHomes(executor, removeFirstArg(deletionArgs));
                    case "warps" -> this.deleteWarps(executor, removeFirstArg(deletionArgs));
                    default -> plugin.getLocales().getLocale("error_invalid_syntax",
                                    "/" + getName() + " delete <player|homes|warps> [args]")
                            .ifPresent(executor::sendMessage);
                }
            }
            case "update" -> updateChecker.check().thenAccept(checked -> {
                if (checked.isUpToDate()) {
                    plugin.getLocales().getLocale("up_to_date", plugin.getVersion().toString())
                            .ifPresent(executor::sendMessage);
                    return;
                }
                plugin.getLocales().getLocale("update_available", checked.getLatestVersion().toString(),
                        plugin.getVersion().toString()).ifPresent(executor::sendMessage);
            });
            default -> plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
        }
    }

    // Import data from another plugin
    private void importData(@NotNull CommandUser executor, @NotNull String[] args) {
        switch (parseStringArg(args, 0).orElse("list")) {
            case "start" -> parseStringArg(args, 1).ifPresentOrElse(
                    name -> {
                        final Optional<Importer> importer = plugin.getImporters().stream()
                                .filter(available -> available.getImporterName().equalsIgnoreCase(name)).findFirst();
                        if (importer.isEmpty()) {
                            plugin.getLocales().getLocale("error_invalid_importer")
                                    .ifPresent(executor::sendMessage);
                            return;
                        }
                        importer.get().start(executor);
                    },
                    () -> plugin.getLocales().getLocale("error_invalid_syntax",
                                    "/" + getName() + " import start <importer>")
                            .ifPresent(executor::sendMessage)
            );
            case "list" -> executor.sendMessage(getImporterList()
                    .getNearestValidPage(parseIntArg(args, 1).orElse(1)));
            default -> plugin.getLocales().getLocale("error_invalid_syntax",
                            "/" + getName() + " import <start|list>")
                    .ifPresent(executor::sendMessage);
        }
    }

    // Delete the data of a player
    private void deletePlayerData(@NotNull CommandUser executor, @NotNull String[] args) {
        final Optional<String> nameOrUuid = parseStringArg(args, 0);
        if (nameOrUuid.isEmpty()) {
            plugin.getLocales().getLocale("error_invalid_syntax",
                            "/" + getName() + " delete player <player> [confirm]")
                    .ifPresent(executor::sendMessage);
            return;
        }

        plugin.runAsync(() -> {
            Optional<SavedUser> savedUser;
            try {
                savedUser = plugin.getDatabase().getUserData(UUID.fromString(nameOrUuid.get()));
            } catch (IllegalArgumentException e) {
                savedUser = plugin.getDatabase().getUserDataByName(nameOrUuid.get());
            }

            if (savedUser.isEmpty()) {
                plugin.getLocales().getLocale("error_player_not_found", nameOrUuid.get())
                        .ifPresent(executor::sendMessage);
                return;
            }

            final User user = savedUser.get().getUser();
            if (!parseStringArg(args, 1)
                    .map(a -> a.equalsIgnoreCase("confirm")).orElse(false)) {
                plugin.getLocales().getLocale("delete_player_confirm",
                        savedUser.get().getUser().getUsername()).ifPresent(executor::sendMessage);
                return;
            }

            final int homesDeleted = plugin.getManager().homes().deleteAllHomes(user);
            plugin.getDatabase().deleteUserData(user.getUuid());
            plugin.getLocales().getLocale("delete_player_success",
                            savedUser.get().getUser().getUsername(), Integer.toString(homesDeleted))
                    .ifPresent(executor::sendMessage);
        });
    }

    // Delete homes in a certain world and/or server
    private void deleteHomes(@NotNull CommandUser executor, @NotNull String[] args) {
        final Map<String, String> filters;
        try {
            filters = getBulkDeleteFilters(args);
        } catch (IllegalArgumentException e) {
            plugin.getLocales().getLocale("error_invalid_syntax",
                            "/" + getName() + " delete homes <world> <server> [confirm]")
                    .ifPresent(executor::sendMessage);
            return;
        }

        if (!parseStringArg(args, 2).map(a -> a.equalsIgnoreCase("confirm")).orElse(false)) {
            plugin.getLocales().getLocale("bulk_delete_homes_confirm",
                    filters.get("world"), filters.get("server")).ifPresent(executor::sendMessage);
            return;
        }
        plugin.runAsync(() -> {
            final int homesDeleted = plugin.getManager().homes().deleteAllHomes(
                    filters.get("world"), filters.get("server")
            );
            plugin.getLocales().getLocale("bulk_delete_homes_success",
                            Integer.toString(homesDeleted), filters.get("world"), filters.get("server"))
                    .ifPresent(executor::sendMessage);
        });
    }

    // Delete warps in a certain world and/or server
    private void deleteWarps(@NotNull CommandUser executor, @NotNull String[] args) {
        final Map<String, String> filters;
        try {
            filters = getBulkDeleteFilters(args);
        } catch (IllegalArgumentException e) {
            plugin.getLocales().getLocale("error_invalid_syntax",
                            "/" + getName() + " delete warps <world> <server> [confirm]")
                    .ifPresent(executor::sendMessage);
            return;
        }

        if (!parseStringArg(args, 2).map(a -> a.equalsIgnoreCase("confirm")).orElse(false)) {
            plugin.getLocales().getLocale("bulk_delete_warps_confirm",
                    filters.get("world"), filters.get("server")).ifPresent(executor::sendMessage);
            return;
        }
        plugin.runAsync(() -> {
            final int homesDeleted = plugin.getManager().warps().deleteAllWarps(
                    filters.get("world"), filters.get("server")
            );
            plugin.getLocales().getLocale("bulk_delete_warps_success",
                            Integer.toString(homesDeleted), filters.get("world"), filters.get("server"))
                    .ifPresent(executor::sendMessage);
        });
    }

    @NotNull
    private Map<String, String> getBulkDeleteFilters(@NotNull String[] args) throws IllegalArgumentException {
        final Map<String, String> filters = new LinkedHashMap<>();
        filters.put("world", parseStringArg(args, 0)
                .orElseThrow(() -> new IllegalArgumentException("World not specified")));
        filters.put("server", parseStringArg(args, 1).orElse(plugin.getServerName()));
        return filters;
    }

    @NotNull
    private PaginatedList getCommandList(@NotNull CommandUser user) {
        return PaginatedList.of(plugin.getCommands().stream()
                        .filter(command -> user.hasPermission(command.getPermission()))
                        .map(command -> plugin.getLocales().getRawLocale("command_list_item",
                                        Locales.escapeText(command.getName()),
                                        Locales.escapeText(
                                                plugin.getLocales().truncateText(command.getDescription(), 50)
                                        ),
                                        Locales.escapeText(String.format("%s\n\n%s",
                                                plugin.getLocales().wrapText(command.getUsage(), 50),
                                                plugin.getLocales().wrapText(command.getDescription(), 50)
                                        )))
                                .orElse(command.getName()))
                        .collect(Collectors.toList()),
                plugin.getLocales().getBaseList(Math.min(plugin.getSettings().getListItemsPerPage(), 6))
                        .setHeaderFormat(plugin.getLocales().getRawLocale("command_list_title").orElse(""))
                        .setItemSeparator("\n").setCommand("/huskhomes:huskhomes help")
                        .build());
    }

    @NotNull
    private PaginatedList getImporterList() {
        return PaginatedList.of(plugin.getImporters().stream()
                        .map(importer -> plugin.getLocales().getRawLocale("importer_list_item",
                                        Locales.escapeText(importer.getImporterName()),
                                        Locales.escapeText(importer.getSupportedImportData().stream()
                                                .map(Importer.ImportData::getName)
                                                .collect(Collectors.joining(", "))))
                                .orElse(importer.getName()))
                        .collect(Collectors.toList()),
                plugin.getLocales().getBaseList(Math.min(plugin.getSettings().getListItemsPerPage(), 6))
                        .setHeaderFormat(plugin.getLocales().getRawLocale("importer_list_title").orElse(""))
                        .setItemSeparator("\n").setCommand("/huskhomes:huskhomes import list")
                        .build());
    }

    @Override
    @Nullable
    public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
        return switch (args.length) {
            case 0, 1 -> SUB_COMMANDS.keySet().stream().sorted().toList();
            case 2 -> switch (args[0].toLowerCase()) {
                case "help" -> IntStream.rangeClosed(1, getCommandList(user).getTotalPages())
                        .mapToObj(Integer::toString).toList();
                case "import" -> List.of("start", "list");
                case "delete" -> List.of("player", "homes", "warps");
                default -> null;
            };
            case 3 -> {
                if (!args[0].equalsIgnoreCase("import") && !args[1].equalsIgnoreCase("start")) {
                    yield null;
                }
                yield plugin.getImporters().stream().map(Importer::getImporterName).toList();
            }
            default -> null;
        };
    }

}
