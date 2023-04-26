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
import net.william278.huskhomes.user.CommandUser;
import net.william278.paginedown.PaginatedList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HuskHomesCommand extends Command implements TabProvider {

    private static final Map<String, Boolean> SUB_COMMANDS = Map.of(
            "about", false,
            "help", false,
            "reload", true,
            "update", true
    );

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
                        AboutMenu.Link.of("https://github.com/WiIIiam278/HuskHomes2/issues").text("Issues").icon("❌").color(TextColor.color(0xff9f0f)),
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
                    executor.sendMessage(new MineDown("[Error:](#ff3300) [Failed to reload the plugin. Check console for errors.](#ff7e5e)"));
                    return;
                }
                executor.sendMessage(new MineDown("[HuskHomes](#00fb9a bold) &#00fb9a&| Reloaded config & message files."));
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

    @NotNull
    private PaginatedList getCommandList(@NotNull CommandUser user) {
        return PaginatedList.of(plugin.getCommands().stream()
                        .filter(command -> user.hasPermission(command.getPermission()))
                        .map(command -> plugin.getLocales().getRawLocale("command_list_item",
                                        Locales.escapeText(command.getName()),
                                        Locales.escapeText(plugin.getLocales()
                                                .truncateText(command.getDescription(), 50)),
                                        Locales.escapeText(plugin.getLocales()
                                                .wrapText(command.getUsage() + "\n" + command.getDescription(), 40)))
                                .orElse(command.getName()))
                        .collect(Collectors.toList()),
                plugin.getLocales().getBaseList(Math.min(plugin.getSettings().getListItemsPerPage(), 6))
                        .setHeaderFormat(plugin.getLocales().getRawLocale("command_list_title").orElse(""))
                        .setItemSeparator("\n").setCommand("/huskhomes:huskhomes help")
                        .build());
    }

    @Override
    @NotNull
    public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
        return args.length < 2 ? SUB_COMMANDS.keySet().stream().sorted().toList() : List.of();
    }

}
