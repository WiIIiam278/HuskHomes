package net.william278.huskhomes.list;

import de.themoep.minedown.MineDown;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.SavedPosition;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class PrivateHomeList extends PositionList {

    private final HuskHomes plugin;
    private final User homeOwner;

    public PrivateHomeList(@NotNull List<Home> homes, @NotNull User homeOwner, @NotNull HuskHomes implementor) {
        super(homes.stream().map(position -> (SavedPosition) position).collect(Collectors.toList()),
                10); //todo config settable
        this.plugin = implementor;
        this.homeOwner = homeOwner;
    }

    @Override
    protected String getItemSeparator() {
        return plugin.getLocales().getRawLocale("list_item_divider").orElse(" ");
    }

    @Override
    protected MineDown getHeader(int pageItemStart, int pageItemEnd, int totalItemCount) {
        return plugin.getLocales().getLocale("private_home_list_page_top", homeOwner.username,
                        Integer.toString(pageItemStart), Integer.toString(pageItemEnd), Integer.toString(totalItemCount))
                .orElseGet(() -> new MineDown(""));
    }

    @Override
    protected MineDown getFooter(@NotNull FooterLayout layout, int pageNumber, int maxPages) {
        return switch (layout) {
            case NEXT_BUTTON -> plugin.getLocales().getLocale("page_options_min",
                            Integer.toString(pageNumber), Integer.toString(maxPages),
                            "/homelist " + (pageNumber + 1))
                    .orElse(new MineDown(""));
            case PREVIOUS_BUTTON -> plugin.getLocales().getLocale("page_options_max",
                            "/homelist " + (pageNumber - 1), Integer.toString(pageNumber),
                            Integer.toString(maxPages))
                    .orElse(new MineDown(""));
            case NO_BUTTONS -> plugin.getLocales().getLocale("page_options_min_max",
                            Integer.toString(pageNumber), Integer.toString(maxPages))
                    .orElse(new MineDown(""));
            case BOTH_BUTTONS -> plugin.getLocales().getLocale("page_options",
                            "/homelist " + (pageNumber - 1), Integer.toString(pageNumber),
                            Integer.toString(maxPages), "/homelist " + (pageNumber + 1))
                    .orElse(new MineDown(""));
        };
    }

    @Override
    protected String getFormattedItem(@NotNull SavedPosition position) {
        return plugin.getLocales().getRawLocale("home_list_item",
                position.meta.name, homeOwner.username).orElse(position.meta.name);
    }

    @Override
    protected MineDown getNoItemsMessage() {
        return plugin.getLocales().getLocale("page_no_items").orElse(new MineDown(""));
    }

    @Override
    protected MineDown getInvalidPageNumberMessage() {
        return plugin.getLocales().getLocale("error_invalid_page_number").orElse(new MineDown(""));
    }
}
