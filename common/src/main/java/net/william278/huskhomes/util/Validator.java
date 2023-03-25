package net.william278.huskhomes.util;

import net.william278.huskhomes.HuskHomes;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class Validator {

    public static final int MAX_NAME_LENGTH = 16;
    public static final int MIN_NAME_LENGTH = 1;
    public static final int MAX_DESCRIPTION_LENGTH = 256;

    private final HuskHomes plugin;

    public Validator(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if a name is valid
     *
     * @param name The name to check
     * @return True if the name is valid as per the plugin settings, false otherwise
     */
    public boolean isValidName(@NotNull String name) {
        return (isAsciiOnly(name) || plugin.getSettings().doAllowUnicodeNames())
                && !containsWhitespace(name)
                && name.length() <= MAX_NAME_LENGTH && name.length() >= MIN_NAME_LENGTH;
    }

    /**
     * Validate home and warp descriptions
     *
     * @param description The meta to validate
     * @return Whether the meta is valid against the plugin settings
     */
    public boolean isValidDescription(@NotNull String description) {
        return (isAsciiOnly(description) || plugin.getSettings().doAllowUnicodeDescriptions())
                && description.length() <= MAX_DESCRIPTION_LENGTH;
    }

    // Check if a string contains only ASCII characters
    private static boolean isAsciiOnly(@NotNull String string) {
        return string.matches("\\A\\p{ASCII}*\\z") && !string.contains("\u0000");
    }

    // Check if a string contains whitespace
    private static boolean containsWhitespace(@NotNull String string) {
        return string.matches(".*\\s.*");
    }

}
