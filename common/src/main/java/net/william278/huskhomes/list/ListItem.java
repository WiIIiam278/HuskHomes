package net.william278.huskhomes.list;

import net.william278.huskhomes.config.Locales;
import org.jetbrains.annotations.NotNull;

/**
 * An item on a {@link ChatList}
 */
public record ListItem(@NotNull String itemDisplayName,
                       @NotNull String... itemDisplayMeta) implements Comparable<ListItem> {

    /**
     * Returns the chat list formatted {@link ListItem} string
     *
     * @param localeId id of the locale to use when formatting this item
     * @param locales  Instance of the {@link Locales} set
     * @return the pre-formatted MineDown of this ListItem
     */
    public String getFormattedItem(String localeId, Locales locales) {
        final String[] itemDisplayComponents = new String[itemDisplayMeta.length + 1];
        itemDisplayComponents[0] = itemDisplayName;
        if (itemDisplayMeta.length >= 1) {
            System.arraycopy(itemDisplayMeta, 0, itemDisplayComponents, 1, itemDisplayMeta.length);
        }
        return locales.getRawLocale(localeId, itemDisplayComponents).orElse(itemDisplayName);
    }

    /**
     * Compare two {@link ListItem}s, returns alphabetical compareTo result
     *
     * @param other the other ListItem to be compared.
     * @return the alphabetical comparison with another {@link ListItem}'s item display name
     */
    @Override
    public int compareTo(@NotNull ListItem other) {
        return itemDisplayName.compareToIgnoreCase(other.itemDisplayName);
    }

}
