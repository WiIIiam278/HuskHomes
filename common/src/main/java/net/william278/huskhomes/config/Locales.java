package net.william278.huskhomes.config;

import de.themoep.minedown.MineDown;
import net.william278.annotaml.RootedMap;
import net.william278.annotaml.YamlFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Loaded locales used by the plugin to display various locales
 */
@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃      HuskHomes Locales       ┃
        ┃    Developed by William278   ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ See plugin about menu for international locale credits
        ┣╸ Formatted in MineDown: https://github.com/Phoenix616/MineDown
        ┗╸ Translate HuskHomes: https://william278.net/docs/huskhomes/Translating-HuskHomes""")
public class Locales {

    @RootedMap
    public Map<String, String> rawLocales = new HashMap<>();

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

    @SuppressWarnings("unused")
    public Locales() {
    }

}
