package net.william278.huskhomes.util;

import java.util.regex.Pattern;

import net.william278.huskhomes.position.PositionMeta;

/**
 * Provides a number of utility regular expressions for checking home name patterns
 */
public final class RegexUtil {

    /**
     * Pattern for checking home and warp {@link PositionMeta} description fields
     */
    public static final Pattern DESCRIPTION_PATTERN = Pattern.compile("[a-zA-Z\\d\\-_\\s]+");

    /**
     * Pattern for checking home and warp {@link PositionMeta} name fields
     */
    public static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z\\d_\\-]+");

    /**
     * Pattern for checking public home input fields
     * e.g. {@code ownerName.homeName}
     */
    public static final Pattern OWNER_NAME_PATTERN = Pattern.compile("\\w+\\.[A-Za-z\\d_\\-]+");

}
