package me.william278.huskhomes2.utils;

import java.util.regex.Pattern;

public final class RegexUtil {

    public static final Pattern DESCRIPTION_PATTERN = Pattern.compile("[a-zA-Z0-9\\d\\-_\\s]+");
    public static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z0-9_\\-]+");
    public static final Pattern OWNER_NAME_PATTERN = Pattern.compile("[A-Za-z0-9_]\\.[A-Za-z0-9_\\-]");

    private RegexUtil() {
        // No object for you
    }
}
