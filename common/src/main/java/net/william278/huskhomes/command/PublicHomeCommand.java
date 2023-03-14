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
    @NotNull
    public List<String> suggest(@NotNull CommandUser executor, @NotNull String[] args) {
        if (args.length <= 2) {
            return filter(reduceHomeList(plugin.getCache().getHomes()), args);
        }
        return List.of();
    }

}