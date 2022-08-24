package net.william278.huskhomes.util;

import java.util.Optional;
import java.util.regex.Pattern;

import net.william278.huskhomes.position.PositionMeta;
import org.jetbrains.annotations.NotNull;

/**
 * Provides a number of utility regular expressions for checking home name patterns
 */
public final class RegexUtil {

    /**
     * Pattern for checking home and warp {@link PositionMeta} description fields
     */
    public static final Pattern DESCRIPTION_PATTERN = Pattern.compile("[a-zA-Z\\d\\-_\\s]*");

    /**
     * Pattern for checking home and warp {@link PositionMeta} name fields
     */
    public static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z\\d_\\-]+");

    /**
     * Pattern for checking home input fields disambiguated by the owner's name
     * <p>
     * e.g. {@code ownerName.homeName}
     */
    private static final Pattern OWNER_DISAMBIGUATED_HOME_IDENTIFIER_PATTERN = Pattern.compile("\\w+\\.[^.]{1,16}$");

    /**
     * Match pattern for checking home input fields disambiguated by the owner's name
     *
     * @param input input string to match
     * @return An optional containing the {@link DisambiguatedHomeIdentifier} if matched, otherwise empty if not
     */
    public static Optional<DisambiguatedHomeIdentifier> matchDisambiguatedHomeIdentifier(@NotNull String input) {
        if (OWNER_DISAMBIGUATED_HOME_IDENTIFIER_PATTERN.matcher(input).matches()) {
            final String[] separatedInput = input.split(Pattern.quote("."));
            return Optional.of(new DisambiguatedHomeIdentifier(separatedInput[0], separatedInput[1]));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Represents an identifier for a home or warp {@link PositionMeta} that is disambiguated by the owner's name
     *
     * @param ownerName the username of a home's owner
     * @param homeName  the name of a home
     */
    public record DisambiguatedHomeIdentifier(String ownerName, String homeName) {


        /**
         * Get the period-separated formatted disambiguated home identifier
         *
         * @return the formatted disambiguated home identifier (e.g. {@code ownerName.homeName})
         */
        public String toString() {
            return ownerName + "." + homeName;
        }
    }

}
