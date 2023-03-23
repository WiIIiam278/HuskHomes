package net.william278.huskhomes.command;

import de.themoep.minedown.adventure.MineDown;
import net.william278.desertwell.AboutMenu;
import net.william278.desertwell.UpdateChecker;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.user.CommandUser;
import net.william278.paginedown.PaginatedList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class HuskHomesCommand extends Command implements TabProvider {

    private static final List<String> SUB_COMMANDS = List.of("about", "help", "reload", "update");

    private final UpdateChecker updateChecker;
    private final AboutMenu aboutMenu;

    protected HuskHomesCommand(@NotNull HuskHomes plugin) {
        super("huskhomes", List.of(), "[" + String.join("|", SUB_COMMANDS) + "]", plugin);
        setOperatorCommand(true);

        this.updateChecker = plugin.getUpdateChecker();
        this.aboutMenu = AboutMenu.create("HuskHomes")
                .withDescription("A powerful, intuitive and flexible teleportation suite")
                .withVersion(plugin.getVersion())
                .addAttribution("Author",
                        AboutMenu.Credit.of("William278").withDescription("Click to visit website").withUrl("https://william278.net"))
                .addAttribution("Contributors",
                        AboutMenu.Credit.of("imDaniX").withDescription("Code, refactoring"),
                        AboutMenu.Credit.of("Log1x").withDescription("Code"))
                .addAttribution("Translators",
                        AboutMenu.Credit.of("SnivyJ").withDescription("Simplified Chinese (zh-cn)"),
                        AboutMenu.Credit.of("ApliNi").withDescription("Simplified Chinese (zh-cn)"),
                        AboutMenu.Credit.of("TonyPak").withDescription("Traditional Chinese (zh-tw)"),
                        AboutMenu.Credit.of("davgo0103").withDescription("Traditional Chinese (zh-tw)"),
                        AboutMenu.Credit.of("Villag3r_").withDescription("Italian (it-it)"),
                        AboutMenu.Credit.of("ReferTV").withDescription("Polish (pl)"),
                        AboutMenu.Credit.of("anchelthe").withDescription("Spanish (es-es)"),
                        AboutMenu.Credit.of("Chiquis2005").withDescription("Spanish (es-es)"),
                        AboutMenu.Credit.of("Ceddix").withDescription("German, (de-de)"),
                        AboutMenu.Credit.of("Pukejoy_1").withDescription("Bulgarian (bg-bg)"))
                .addButtons(
                        AboutMenu.Link.of("https://william278.net/docs/huskhomes").withText("Documentation").withIcon("⛏"),
                        AboutMenu.Link.of("https://github.com/WiIIiam278/HuskHomes2/issues").withText("Issues").withIcon("❌").withColor("#ff9f0f"),
                        AboutMenu.Link.of("https://discord.gg/tVYhJfyDWG").withText("Discord").withIcon("⭐").withColor("#6773f5"));
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        final String action = parseStringArg(args, 0).orElse("about");
        if (SUB_COMMANDS.contains(action) && !executor.hasPermission(getPermission(action))) {
            plugin.getLocales().getLocale("error_no_permission")
                    .ifPresent(executor::sendMessage);
            return;
        }

        switch (action.toLowerCase()) {
            case "about" -> executor.sendMessage(aboutMenu.toMineDown());
            case "help" -> executor.sendMessage(getCommandList(executor)
                    .getNearestValidPage(parseIntArg(args, 0).orElse(1)));
            case "reload" -> {
                if (!plugin.loadConfigs()) {
                    executor.sendMessage(new MineDown("[Error:](#ff3300) [Failed to reload the plugin. Check console for errors.](#ff7e5e)"));
                    return;
                }
                executor.sendMessage(new MineDown("[HuskHomes](#00fb9a bold) &#00fb9a&| Reloaded config & message files."));
            }
            case "update" -> updateChecker.isUpToDate().thenAccept(upToDate -> {
                if (upToDate) {
                    plugin.getLocales().getLocale("up_to_date", plugin.getVersion().toString())
                            .ifPresent(executor::sendMessage);
                    return;
                }
                updateChecker.getLatestVersion().thenAccept(latest -> plugin.getLocales()
                        .getLocale("update_available", latest.toString(), plugin.getVersion().toString())
                        .ifPresent(executor::sendMessage));
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
                                        Locales.escapeText(command.getDescription().length() > 50
                                                ? command.getDescription().substring(0, 49).trim() + "…"
                                                : command.getDescription()),
                                        Locales.escapeText(plugin.getLocales().wrapText(command.getDescription(), 40)))
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
        return args.length < 2 ? SUB_COMMANDS : List.of();
    }

}
