package net.william278.huskhomes.list;

import de.themoep.minedown.MineDown;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.position.Home;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class PublicHomeList extends ChatList {
    private final HuskHomes plugin;

    public PublicHomeList(@NotNull List<Home> homes, @NotNull User homeOwner, @NotNull HuskHomes implementor) {
        super(homes.stream().map(position -> new ListItem(position.meta.name, position.owner.username,
                        position.meta.description)).collect(Collectors.toList()),
                10, "phomelist", implementor.getLocales()); //todo config settable items per page
        this.plugin = implementor;
    }

    @Override
    protected String getItemDisplayLocale(@NotNull ListItem item) {
        return item.getFormattedItem("public_home_list_item", plugin.getLocales());
    }

    @Override
    protected String getItemSeparator() {
        return plugin.getLocales().getRawLocale("list_item_divider").orElse(" ");
    }

    @Override
    protected MineDown getHeader(int pageItemStart, int pageItemEnd, int totalItemCount) {
        return plugin.getLocales().getLocale("public_home_list_page_top", Integer.toString(pageItemStart),
                Integer.toString(pageItemEnd), Integer.toString(totalItemCount)).orElseGet(() -> new MineDown(""));
    }

}
