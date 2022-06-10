package net.william278.huskhomes.list;

import de.themoep.minedown.MineDown;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.position.Warp;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class WarpList extends ChatList {

    private final HuskHomes plugin;

    public WarpList(@NotNull List<Warp> warps, @NotNull HuskHomes implementor) {
        super(warps.stream().map(position -> new ListItem(position.meta.name, position.meta.description))
                        .collect(Collectors.toList()), 10, "warplist", implementor.getLocales());
        //todo config settable items per page
        this.plugin = implementor;
    }

    @Override
    protected String getItemDisplayLocale(@NotNull ListItem item) {
        return item.getFormattedItem("warp_list_item", plugin.getLocales());
    }

    @Override
    protected String getItemSeparator() {
        return plugin.getLocales().getRawLocale("list_item_divider").orElse(" ");
    }

    @Override
    protected MineDown getHeader(int pageItemStart, int pageItemEnd, int totalItemCount) {
        return plugin.getLocales().getLocale("warp_list_page_top",
                        Integer.toString(pageItemStart), Integer.toString(pageItemEnd), Integer.toString(totalItemCount))
                .orElseGet(() -> new MineDown(""));
    }

}
