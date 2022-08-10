package net.william278.huskhomes.config;

import de.themoep.minedown.MineDown;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Optional;

/**
 * Loaded locales used by the plugin to display various locales
 */
public class Locales {

    @NotNull
    private final HashMap<String, String> rawLocales;

    private Locales(@NotNull YamlDocument localesConfig) {
        this.rawLocales = new HashMap<>();
        for (String localeId : localesConfig.getRoutesAsStrings(false)) {
            rawLocales.put(localeId, StringEscapeUtils.unescapeJava(localesConfig.getString(localeId)));
        }
    }

    /**
     * Returns an unformatted locale loaded from the locales file
     *
     * @param localeId String identifier of the locale, corresponding to a key in the file
     * @return An {@link Optional} containing the locale corresponding to the id, if it exists
     */
    public Optional<String> getRawLocale(String localeId) {
        if (rawLocales.containsKey(localeId)) {
            return Optional.of(rawLocales.get(localeId));
        }
        return Optional.empty();
    }

    /**
     * Returns an unformatted locale loaded from the locales file, with replacements applied
     *
     * @param localeId    String identifier of the locale, corresponding to a key in the file
     * @param replacements Ordered array of replacement strings to fill in placeholders with
     * @return An {@link Optional} containing the replacement-applied locale corresponding to the id, if it exists
     */
    public Optional<String> getRawLocale(String localeId, String... replacements) {
        return getRawLocale(localeId).map(locale -> applyReplacements(locale, replacements));
    }

    /**
     * Returns a MineDown-formatted locale from the locales file
     *
     * @param localeId String identifier of the locale, corresponding to a key in the file
     * @return An {@link Optional} containing the formatted locale corresponding to the id, if it exists
     */
    public Optional<MineDown> getLocale(String localeId) {
        return getRawLocale(localeId).map(MineDown::new);
    }

    /**
     * Returns a MineDown-formatted locale from the locales file, with replacements applied
     *
     * @param localeId    String identifier of the locale, corresponding to a key in the file
     * @param replacements Ordered array of replacement strings to fill in placeholders with
     * @return An {@link Optional} containing the replacement-applied, formatted locale corresponding to the id, if it exists
     */
    public Optional<MineDown> getLocale(String localeId, String... replacements) {
        return getRawLocale(localeId, replacements).map(MineDown::new);
    }

    /**
     * Apply placeholder replacements to a raw locale
     *
     * @param rawLocale   The raw, unparsed locale
     * @param replacements Ordered array of replacement strings to fill in placeholders with
     * @return the raw locale, with inserted placeholders
     */
    private String applyReplacements(String rawLocale, String... replacements) {
        int replacementIndexer = 1;
        for (String replacement : replacements) {
            String replacementString = "%" + replacementIndexer + "%";
            rawLocale = rawLocale.replace(replacementString, replacement);
            replacementIndexer = replacementIndexer + 1;
        }
        return rawLocale;
    }

    /**
     * Load the locales from a BoostedYaml {@link YamlDocument} locales file
     *
     * @param localesConfig The loaded {@link YamlDocument} locales.yml file
     * @return the loaded {@link Locales}
     */
    public static Locales load(YamlDocument localesConfig) {
        return new Locales(localesConfig);
    }

}
