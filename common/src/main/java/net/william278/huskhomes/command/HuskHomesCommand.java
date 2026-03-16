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
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.william278.desertwell.about.AboutMenu;
import net.william278.desertwell.util.UpdateChecker;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.database.Database;
import net.william278.huskhomes.database.DatabaseImporter;
import net.william278.huskhomes.database.MySqlDatabase;
import net.william278.huskhomes.database.PostgreSqlDatabase;
import net.william278.huskhomes.database.SqLiteDatabase;
import net.william278.huskhomes.database.H2Database;
import net.william278.huskhomes.hook.PluginHook;
import net.william278.huskhomes.importer.Importer;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.SavedUser;
import net.william278.huskhomes.user.User;
import net.william278.huskhomes.util.StatusLine;
import net.william278.paginedown.PaginatedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HuskHomesCommand extends Command implements UserListTabCompletable {

    private static final Map<String, Boolean> SUB_COMMANDS = Map.of(
            "about", false,
            "help", false,
            "reload", true,
            "status", true,
            "dump", true,
            "homeslots", true,
            "import", true,
            "importdb", true,
            "delete", true,
            "update", true
    );

    private final UpdateChecker updateChecker;
    private final AboutMenu aboutMenu;

    protected HuskHomesCommand(@NotNull HuskHomes plugin) {
        super(
                List.of("huskhomes"),
                "[" + String.join("|", SUB_COMMANDS.keySet()) + "]",
                plugin
        );
        addAdditionalPermissions(SUB_COMMANDS);

        this.updateChecker = plugin.getUpdateChecker();
        this.aboutMenu = AboutMenu.builder()
                .title(Component.text("HuskHomes"))
                .description(Component.text("The powerful & intuitive homes, warps, and teleportation suite"))
                .version(plugin.getPluginVersion())
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
                        AboutMenu.Credit.of("Funasitien").description("French, (fr-fr)"),
                        AboutMenu.Credit.of("Ceddix").description("German, (de-de)"),
                        AboutMenu.Credit.of("Pukejoy_1").description("Bulgarian (bg-bg)"),
                        AboutMenu.Credit.of("WinTone01").description("Turkish, (tr-tr)"),
                        AboutMenu.Credit.of("EmanuelFNC").description("Brazilian Portuguese, (pt-br)"),
                        AboutMenu.Credit.of("xMattNice_").description("Brazilian Portuguese, (pt-br)"),
                        AboutMenu.Credit.of("Iamsad_VN").description("Vietnamese, (vi-vn)"))
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
            case "help" -> executor.sendMessage(
                    getCommandList(executor).getNearestValidPage(parseIntArg(args, 1).orElse(1))
            );
            case "status" -> {
                getPlugin().getLocales().getLocale("system_status_header").ifPresent(executor::sendMessage);
                executor.sendMessage(Component.join(
                        JoinConfiguration.newlines(),
                        Arrays.stream(StatusLine.values()).map(s -> s.get(plugin)).toList()
                ));
            }
            case "dump" -> {
                if (!parseStringArg(args, 1).map(s -> s.equals("confirm")).orElse(false)) {
                    getPlugin().getLocales().getLocale("system_dump_confirm").ifPresent(executor::sendMessage);
                    return;
                }

                getPlugin().getLocales().getLocale("system_dump_started").ifPresent(executor::sendMessage);
                plugin.runAsync(() -> {
                    final String url = plugin.createDump(executor);
                    getPlugin().getLocales().getLocale("system_dump_ready").ifPresent(executor::sendMessage);
                    executor.sendMessage(Component.text(url).clickEvent(ClickEvent.openUrl(url))
                            .decorate(TextDecoration.UNDERLINED).color(NamedTextColor.GRAY));
                });
            }
            case "homeslots" -> {
                if (!plugin.isUsingEconomy() || args.length <= 1) {
                    plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                            .ifPresent(executor::sendMessage);
                    return;
                }
                this.manageHomeSlots(executor, removeFirstArg(args));
            }
            case "import" -> {
                if (plugin.getImporters().isEmpty()) {
                    plugin.getLocales().getLocale("error_no_importers_available")
                            .ifPresent(executor::sendMessage);
                    return;
                }
                this.importData(executor, removeFirstArg(args));
            }
            case "importdb" -> this.importDatabase(executor, removeFirstArg(args));
            case "reload" -> plugin.runSync(() -> {
                try {
                    plugin.unloadHooks(PluginHook.Register.ON_ENABLE, PluginHook.Register.AFTER_LOAD);
                    plugin.loadConfigs();
                    plugin.loadHooks(PluginHook.Register.ON_ENABLE, PluginHook.Register.AFTER_LOAD);
                    plugin.registerHooks(PluginHook.Register.ON_ENABLE, PluginHook.Register.AFTER_LOAD);
                    plugin.getLocales().getLocale("reload_complete").ifPresent(executor::sendMessage);
                } catch (Throwable e) {
                    executor.sendMessage(new MineDown(
                            "[Error:](#ff3300) [Failed to reload the plugin. Check console for errors.](#ff7e5e)"
                    ));
                    plugin.log(Level.SEVERE, "Failed to reload the plugin", e);
                }
            });
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
                    plugin.getLocales().getLocale("up_to_date", plugin.getPluginVersion().toString())
                            .ifPresent(executor::sendMessage);
                    return;
                }
                plugin.getLocales().getLocale("update_available", checked.getLatestVersion().toString(),
                        plugin.getPluginVersion().toString()).ifPresent(executor::sendMessage);
            });
            default -> plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
        }
    }

    // Manage a user's home slots
    private void manageHomeSlots(@NotNull CommandUser executor, @NotNull String[] args) {
        // Parse args
        final Optional<SavedUser> savedUser = parseStringArg(args, 0)
                .flatMap(u -> plugin.getDatabase().getUser(u));
        final String actionArg = parseStringArg(args, 1).orElse("view").toLowerCase(Locale.ENGLISH);
        final Optional<Integer> valueArg = parseIntArg(args, 2);

        // Resolve user
        if (savedUser.isEmpty()) {
            plugin.getLocales().getLocale("error_player_not_found", args[0])
                    .ifPresent(executor::sendMessage);
            return;
        }

        // Perform actions
        final SavedUser user = savedUser.get();
        switch (actionArg) {
            case "view" -> {
                final List<Home> homes = plugin.getDatabase().getHomes(user.getUser());
                plugin.getLocales().getLocale("user_home_slots_status", user.getUsername(),
                                Integer.toString(user.getHomeSlots()), Integer.toString(homes.size()))
                        .ifPresent(executor::sendMessage);
            }
            case "set", "add", "remove" -> {
                if (valueArg.isEmpty()) {
                    plugin.getLocales().getLocale("error_invalid_syntax",
                                    "/%s homeslots <set|add|remove> <value>".formatted(getName()))
                            .ifPresent(executor::sendMessage);
                    return;
                }

                // Calculate new slots and apply
                int value = valueArg.orElseThrow();
                if (actionArg.equals("add")) {
                    value = user.getHomeSlots() + value;
                } else if (actionArg.equals("remove")) {
                    value = user.getHomeSlots() - value;
                }
                if (value < 0) {
                    plugin.getLocales().getLocale("error_invalid_syntax",
                                    "/%s homeslots [view|set|add|remove]".formatted(getName()))
                            .ifPresent(executor::sendMessage);
                    return;
                }

                user.setHomeSlots(value);
                plugin.getDatabase().updateUserData(user);
                plugin.getLocales().getLocale("user_home_slots_updated",
                                user.getUsername(), Integer.toString(user.getHomeSlots()))
                        .ifPresent(executor::sendMessage);
            }
            default -> plugin.getLocales().getLocale("error_invalid_syntax",
                            "/%s homeslots [view|set|add|remove]".formatted(getName()))
                    .ifPresent(executor::sendMessage);
        }
    }

    // Import data from another plugin
    private void importData(@NotNull CommandUser executor, @NotNull String[] args) {
        switch (parseStringArg(args, 0).orElse("list")) {
            case "start" -> parseStringArg(args, 1).ifPresentOrElse(
                    (name) -> {
                        final Optional<Importer> importer = plugin.getImporterByName(name);
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

    // Import data from one database type to another
    private void importDatabase(@NotNull CommandUser executor, @NotNull String[] args) {
        if (args.length < 2) {
            plugin.getLocales().getLocale("error_invalid_syntax",
                    "/" + getName() + " importdb <source_type> <target_type> [confirm]")
                    .ifPresent(executor::sendMessage);
            plugin.getLocales().getLocale("database_import_invalid_type")
                    .ifPresent(executor::sendMessage);
            return;
        }

        final String sourceTypeStr = args[0].toUpperCase();
        final String targetTypeStr = args[1].toUpperCase();
        final boolean confirm = parseStringArg(args, 2).map(s -> s.equalsIgnoreCase("confirm")).orElse(false);

        // Validate database types
        Database.Type sourceType, targetType;
        try {
            sourceType = Database.Type.valueOf(sourceTypeStr);
            targetType = Database.Type.valueOf(targetTypeStr);
        } catch (IllegalArgumentException e) {
            plugin.getLocales().getLocale("database_import_invalid_type")
                    .ifPresent(executor::sendMessage);
            return;
        }

        if (sourceType == targetType) {
            plugin.getLocales().getLocale("database_import_same_type")
                    .ifPresent(executor::sendMessage);
            return;
        }

        if (!confirm) {
            plugin.getLocales().getLocale("database_import_warning",
                    sourceType.getDisplayName(), targetType.getDisplayName(),
                    sourceTypeStr, targetTypeStr)
                    .ifPresent(executor::sendMessage);
            return;
        }

        plugin.getLocales().getLocale("database_import_started",
                sourceType.getDisplayName(), targetType.getDisplayName())
                .ifPresent(executor::sendMessage);

        plugin.runAsync(() -> {
            try {
                // Send progress message
                plugin.getLocales().getLocale("database_import_connecting")
                        .ifPresent(executor::sendMessage);

                // Create temporary database instances for import
                Database sourceDb = createDatabaseInstance(sourceType);
                Database targetDb = createDatabaseInstance(targetType);

                // Initialize databases
                sourceDb.initialize();
                targetDb.initialize();

                if (!sourceDb.isLoaded()) {
                    plugin.getLocales().getLocale("database_import_source_failed", sourceType.getDisplayName())
                            .ifPresent(executor::sendMessage);
                    return;
                }

                if (!targetDb.isLoaded()) {
                    plugin.getLocales().getLocale("database_import_target_failed", targetType.getDisplayName())
                            .ifPresent(executor::sendMessage);
                    return;
                }

                // Send progress message
                plugin.getLocales().getLocale("database_import_progress")
                        .ifPresent(executor::sendMessage);

                // Perform the import with progress updates
                DatabaseImporter importer = new DatabaseImporter(plugin, sourceDb, targetDb, executor);
                DatabaseImporter.ImportResult result = importer.importAllData().get();

                // Close temporary database connections
                sourceDb.close();
                targetDb.close();

                // Send result to executor
                if (result.success) {
                    plugin.getLocales().getLocale("database_import_success")
                            .ifPresent(executor::sendMessage);
                    plugin.getLocales().getLocale("database_import_stats",
                            Integer.toString(result.usersImported),
                            Integer.toString(result.homesImported),
                            Integer.toString(result.warpsImported),
                            Integer.toString(result.positionsImported),
                            Integer.toString(result.cooldownsImported))
                            .ifPresent(executor::sendMessage);
                } else {
                    plugin.getLocales().getLocale("database_import_failed", result.errorMessage)
                            .ifPresent(executor::sendMessage);
                }

            } catch (Exception e) {
                plugin.log(Level.SEVERE, "Database import failed", e);
                plugin.getLocales().getLocale("database_import_failed", e.getMessage())
                        .ifPresent(executor::sendMessage);
            }
        });
    }

    private Database createDatabaseInstance(@NotNull Database.Type type) {
        switch (type) {
            case MYSQL, MARIADB -> {
                return new MySqlDatabase(plugin);
            }
            case POSTGRESQL -> {
                return new PostgreSqlDatabase(plugin);
            }
            case SQLITE -> {
                return new SqLiteDatabase(plugin);
            }
            case H2 -> {
                return new H2Database(plugin);
            }
            default -> throw new IllegalStateException("Unexpected database type: " + type);
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
                savedUser = plugin.getDatabase().getUser(UUID.fromString(nameOrUuid.get()));
            } catch (IllegalArgumentException e) {
                savedUser = plugin.getDatabase().getUser(nameOrUuid.get());
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
                        savedUser.get().getUser().getName()).ifPresent(executor::sendMessage);
                return;
            }

            final int homesDeleted = plugin.getManager().homes().deleteAllHomes(user);
            plugin.getDatabase().deleteUser(user.getUuid());
            plugin.getLocales().getLocale("delete_player_success",
                            savedUser.get().getUser().getName(), Integer.toString(homesDeleted))
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
                                                command.getUsage(),
                                                command.getDescription())
                                        ))
                                .orElse(command.getName()))
                        .collect(Collectors.toList()),
                plugin.getLocales().getBaseList(Math.min(plugin.getSettings().getGeneral().getListItemsPerPage(), 6))
                        .setHeaderFormat(plugin.getLocales().getRawLocale("command_list_title").orElse(""))
                        .setItemSeparator("\n").setCommand("/huskhomes:huskhomes help")
                        .build());
    }

    @NotNull
    private PaginatedList getImporterList() {
        return PaginatedList.of(plugin.getImporters().stream()
                        .map(importer -> plugin.getLocales().getRawLocale("importer_list_item",
                                        Locales.escapeText(importer.getName()),
                                        Locales.escapeText(importer.getSupportedImportData().stream()
                                                .map(Importer.ImportData::getName)
                                                .collect(Collectors.joining(", "))))
                                .orElse(importer.getName()))
                        .collect(Collectors.toList()),
                plugin.getLocales().getBaseList(Math.min(plugin.getSettings().getGeneral().getListItemsPerPage(), 6))
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
                case "dump" -> List.of("confirm");
                case "homeslots" -> UserListTabCompletable.super.getUsernameList();
                case "import" -> List.of("start", "list");
                case "importdb" -> List.of("SQLITE", "H2", "MYSQL", "MARIADB", "POSTGRESQL");
                case "delete" -> List.of("player", "homes", "warps");
                default -> null;
            };
            case 3 -> switch (args[0].toLowerCase()) {
                case "homeslots" -> List.of("view", "add", "remove", "set");
                case "import" -> {
                    if (!args[1].equalsIgnoreCase("start")) {
                        yield null;
                    }
                    yield plugin.getImporters().stream().map(Importer::getName).toList();
                }
                case "importdb" -> List.of("SQLITE", "H2", "MYSQL", "MARIADB", "POSTGRESQL");
                default -> null;
            };
            case 4 -> switch (args[0].toLowerCase()) {
                case "importdb" -> List.of("confirm");
                default -> null;
            };
            default -> null;
        };
    }

}
