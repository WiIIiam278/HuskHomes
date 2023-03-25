package net.william278.huskhomes.command;

import net.william278.huskhomes.HuskHomes;
import net.william278.paginedown.PaginatedList;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class ListCommand extends Command {

    protected final Map<UUID, PaginatedList> cachedLists;

    protected ListCommand(@NotNull String name, @NotNull List<String> aliases, @NotNull String usage, @NotNull HuskHomes plugin) {
        super(name, aliases, usage, plugin);
        this.cachedLists = new HashMap<>();
    }

    public void invalidateCaches() {
        cachedLists.clear();
    }

}
