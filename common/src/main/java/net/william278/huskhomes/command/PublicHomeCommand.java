package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.user.CommandUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PublicHomeCommand extends HomeCommand {

    protected PublicHomeCommand(@NotNull HuskHomes plugin) {
        super("phome", List.of("publichome"), plugin);
    }

    @Override
    public void execute(@NotNull CommandUser executor, @NotNull String[] args) {
        // Display the public home list if no arguments are provided
        if (args.length == 0) {
            plugin.getCommand(PublicHomeListCommand.class)
                    .ifPresent(command -> command.showPublicHomeList(executor, 1));
            return;
        }
        super.execute(executor, args);
    }

    @Override
    @NotNull
    public List<String> suggest(@NotNull CommandUser executor, @NotNull String[] args) {
        if (args.length <= 2) {
            return filter(reduceHomeList(plugin.getManager().homes().getPublicHomes()), args);
        }
        return List.of();
    }

}