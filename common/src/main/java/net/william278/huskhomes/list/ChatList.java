package net.william278.huskhomes.list;

import de.themoep.minedown.MineDown;
import net.william278.huskhomes.config.Locales;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * A list used to display saved positions
 */
public abstract class ChatList {

    /**
     * Instance of the plugin {@link Locales}
     */
    private final Locales locales;

    /**
     * List of {@link ListItem}s to display
     */
    protected final List<ListItem> positions;
    /**
     * The number of items to be displayed on each page
     */
    protected final int itemsPerPage;
    /**
     * The command used to switch players
     */
    protected final String command;


    public ChatList(@NotNull List<ListItem> positions, int itemsPerPage, @NotNull String command, @NotNull Locales locales) {
        this.positions = positions;
        this.itemsPerPage = itemsPerPage;
        this.command = command;
        this.locales = locales;
        Collections.sort(this.positions);
    }

    /**
     * Gets a list of formatted MineDown sequences to form a page
     */
    public List<MineDown> getDisplay(int pageNumber) {
        final List<MineDown> display = new ArrayList<>();
        final int pageCount = (int) Math.ceil((double) positions.size() / (double) itemsPerPage);

        if (pageNumber <= 0 || pageNumber > pageCount) {
            return Collections.singletonList(locales.getLocale("error_invalid_page_number").orElse(new MineDown("")));
        }

        // Calculate display bounds
        final int lastItemIndex = positions.size() - 1;
        final int itemIndexStart = Math.min(itemsPerPage * (pageNumber - 1), lastItemIndex);
        final int itemIndexEnd = Math.min((itemsPerPage * pageNumber) - 1, lastItemIndex);

        // Add the header with page information
        display.add(getHeader(itemIndexStart + 1, itemIndexEnd + 1, lastItemIndex + 1));

        // Add the items
        if (itemIndexEnd - itemIndexStart < 0) {
            display.add(locales.getLocale("page_no_items").orElse(new MineDown("")));
        } else {
            final StringJoiner itemJoiner = new StringJoiner(getItemSeparator());
            for (int i = itemIndexStart; i <= itemIndexEnd; i++) {
                itemJoiner.add(getItemDisplayLocale(positions.get(i)));
            }
            display.add(new MineDown(itemJoiner.toString()));
        }

        // Add the footer with navigation buttons
        display.add(getFooter(determineFooterLayout(pageNumber, pageCount), pageNumber, pageCount));
        return display;
    }


    /**
     * Determines the {@link FooterLayout} to use for a given page
     *
     * @param pageNumber number of page to get layout for
     * @param pageCount  total number of pages
     * @return the {@link FooterLayout} for the page
     */
    private FooterLayout determineFooterLayout(int pageNumber, int pageCount) {
        final boolean isNextPage = pageNumber + 1 <= pageCount;
        final boolean isPreviousPage = pageNumber - 1 > 0;
        if (isNextPage && isPreviousPage) {
            return FooterLayout.BOTH_BUTTONS;
        }
        if (isNextPage) {
            return FooterLayout.NEXT_BUTTON;
        }
        if (isPreviousPage) {
            return FooterLayout.PREVIOUS_BUTTON;
        }
        return FooterLayout.NO_BUTTONS;
    }

    protected abstract String getItemDisplayLocale(@NotNull ListItem item);

    protected abstract String getItemSeparator();

    protected abstract MineDown getHeader(int pageItemStart, int pageItemEnd, int totalItemCount);

    private MineDown getFooter(@NotNull FooterLayout layout, int pageNumber, int maxPages) {
        return switch (layout) {
            case NEXT_BUTTON -> locales.getLocale("page_options_min",
                            Integer.toString(pageNumber), Integer.toString(maxPages),
                            "/" + command + " " + (pageNumber + 1))
                    .orElse(new MineDown(""));
            case PREVIOUS_BUTTON -> locales.getLocale("page_options_max",
                            "/" + command + " " + (pageNumber - 1), Integer.toString(pageNumber),
                            Integer.toString(maxPages))
                    .orElse(new MineDown(""));
            case NO_BUTTONS -> locales.getLocale("page_options_min_max",
                            Integer.toString(pageNumber), Integer.toString(maxPages))
                    .orElse(new MineDown(""));
            case BOTH_BUTTONS -> locales.getLocale("page_options",
                            "/" + command + " " + (pageNumber - 1), Integer.toString(pageNumber),
                            Integer.toString(maxPages), "/" + command + " " + (pageNumber + 1))
                    .orElse(new MineDown(""));
        };
    }

    /**
     * Used for identifying the layout of a list footer
     */
    protected enum FooterLayout {
        /**
         * List footer with a previous page button
         */
        PREVIOUS_BUTTON,

        /**
         * List footer with a next page button
         */
        NEXT_BUTTON,

        /**
         * List footer with both a previous page and a next page button.
         */
        BOTH_BUTTONS,

        /**
         * List footer with no buttons
         */
        NO_BUTTONS
    }
}
