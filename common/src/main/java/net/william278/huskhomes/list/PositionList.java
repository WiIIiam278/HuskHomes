package net.william278.huskhomes.list;

import de.themoep.minedown.MineDown;
import net.william278.huskhomes.position.SavedPosition;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * A list used to display saved positions
 */
public abstract class PositionList {
    protected final List<SavedPosition> positions;
    protected final int itemsPerPage;

    public PositionList(@NotNull List<SavedPosition> positions, int itemsPerPage) {
        this.positions = positions;
        this.itemsPerPage = itemsPerPage;
        Collections.sort(this.positions);
    }

    /**
     * Gets a list of formatted MineDown sequences to form a page
     */
    public List<MineDown> getDisplay(int pageNumber) {
        final List<MineDown> display = new ArrayList<>();
        final int pageCount = (int) Math.ceil((double) positions.size() / (double) itemsPerPage);

        if (pageNumber <= 0 || pageNumber > pageCount) {
            return Collections.singletonList(getInvalidPageNumberMessage());
        }

        // Calculate display bounds
        final int lastItemIndex = positions.size() - 1;
        final int itemIndexStart = Math.min(itemsPerPage * (pageNumber - 1), lastItemIndex);
        final int itemIndexEnd = Math.min((itemsPerPage * pageNumber) - 1, lastItemIndex);

        // Add the header with page information
        display.add(getHeader(itemIndexStart + 1, itemIndexEnd + 1, lastItemIndex + 1));

        // Add the items
        if (itemIndexEnd - itemIndexStart < 0) {
            display.add(getNoItemsMessage());
        } else {
            final StringJoiner itemJoiner = new StringJoiner(getItemSeparator());
            for (int i = itemIndexStart; i <= itemIndexEnd; i++) {
                itemJoiner.add(getFormattedItem(positions.get(i)));
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

    protected abstract String getItemSeparator();

    protected abstract MineDown getHeader(int pageItemStart, int pageItemEnd, int totalItemCount);

    protected abstract MineDown getFooter(@NotNull FooterLayout layout, int pageNumber, int maxPages);

    protected abstract String getFormattedItem(SavedPosition position);

    protected abstract MineDown getNoItemsMessage();

    protected abstract MineDown getInvalidPageNumberMessage();

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
