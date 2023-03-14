package net.william278.huskhomes.command;

import de.themoep.minedown.adventure.MineDown;
import net.william278.desertwell.AboutMenu;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HuskHomesCommand extends Command {

    private static final List<String> SUB_COMMANDS = List.of("about", "help", "reload", "update");
    private final AboutMenu aboutMenu;

    protected HuskHomesCommand(@NotNull HuskHomes implementor) {
        super("huskhomes", List.of(), "[" + String.join("|", SUB_COMMANDS) + "]", implementor);
        this.aboutMenu = AboutMenu.create("HuskHomes")
                .withDescription("A powerful, intuitive and flexible teleportation suite")
                .withVersion(implementor.getVersion())
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
            case "help" -> executor.sendMessage(plugin.getCache().getCommandList(executor, plugin.getLocales(),
                    plugin.getCommands(), plugin.getSettings().getListItemsPerPage(),
                    parseIntArg(args, 1).orElse(1)));
            case "reload" -> {
                if (!plugin.reload()) {
                    executor.sendMessage(new MineDown("[Error:](#ff3300) [Failed to reload the plugin. Check console for errors.](#ff7e5e)"));
                    return;
                }
                executor.sendMessage(new MineDown("[HuskHomes](#00fb9a bold) &#00fb9a&| Reloaded config & message files."));
            }
            case "update" -> plugin.getLatestVersionIfOutdated()
                    .thenAccept(newestVersion -> newestVersion.ifPresentOrElse(
                            newVersion -> executor.sendMessage(
                                    new MineDown("[HuskHomes](#00fb9a bold) [| A new version of HuskHomes is available!"
                                            + " (v" + newVersion + " (Running: v" + plugin.getVersion() + ")](#00fb9a)")),
                            () -> executor.sendMessage(
                                    new MineDown("[HuskHomes](#00fb9a bold) [| HuskHomes is up-to-date."
                                            + " (Running: v" + plugin.getVersion() + ")](#00fb9a)"))));
            default -> plugin.getLocales().getLocale("error_invalid_syntax", getUsage())
                    .ifPresent(executor::sendMessage);
        }
    }

    @Override
    @NotNull
    public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
        if (args.length < 2) {
            return filter(SUB_COMMANDS, args);
        }
        return List.of();
    }

}
