package net.william278.huskhomes.config;

import net.william278.annotaml.Annotaml;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Objects;
import java.util.Set;

public class LocalesTests {

    @Test
    public void testAllLocalesPresent() {
        // Load locales/en-gb.yml as an InputStream
        try (InputStream localeStream = LocalesTests.class.getClassLoader().getResourceAsStream("locales/en-gb.yml")) {
            Assertions.assertNotNull(localeStream, "en-gb.yml is missing from the locales folder");
            final Locales englishLocales = Annotaml.create(Locales.class, localeStream).get();
            final Set<String> keys = englishLocales.rawLocales.keySet();

            // Iterate through every locale file in the locales folder
            URL url = LocalesTests.class.getClassLoader().getResource("locales");
            Assertions.assertNotNull(url, "locales folder is missing");

            for (File file : Objects.requireNonNull(new File(url.getPath()).listFiles(file -> file.getName().endsWith("yml")
                                                                                              && !file.getName().equals("en-gb.yml")))) {
                final Set<String> fileKeys = Annotaml.create(file, Locales.class).get().rawLocales.keySet();
                keys.forEach(key -> Assertions.assertTrue(fileKeys.contains(key),
                        "Locale key " + key + " is missing from " + file.getName()));
            }
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
