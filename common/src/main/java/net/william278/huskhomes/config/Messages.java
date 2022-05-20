package net.william278.huskhomes.config;

import de.themoep.minedown.MineDown;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Optional;

/**
 * Loaded locales used by the plugin to display various messages
 */
public class Messages {

    @NotNull
    private final HashMap<String, String> rawMessages;

    private Messages(YamlDocument messagesConfig) {
        this.rawMessages = new HashMap<>();
        for (String messageId : messagesConfig.getRoutesAsStrings(false)) {
            rawMessages.put(messageId, messagesConfig.getString(messageId));
        }
    }

    /**
     * Returns an unformatted message loaded from the messages file
     *
     * @param messageId String identifier of the message, corresponding to a key in the file
     * @return An {@link Optional} containing the message corresponding to the id, if it exists
     */
    public Optional<String> getRawMessage(String messageId) {
        if (rawMessages.containsKey(messageId)) {
            return Optional.of(rawMessages.get(messageId));
        }
        return Optional.empty();
    }

    /**
     * Returns an unformatted message loaded from the messages file, with replacements applied
     *
     * @param messageId String identifier of the message, corresponding to a key in the file
     * @return An {@link Optional} containing the replacement-applied message corresponding to the id, if it exists
     */
    public Optional<String> getRawMessage(String messageId, String... replacements) {
        return getRawMessage(messageId).map(message -> applyReplacements(message, replacements));
    }

    /**
     * Returns a MineDown-formatted message from the messages file
     *
     * @param messageId String identifier of the message, corresponding to a key in the file
     * @return An {@link Optional} containing the formatted message corresponding to the id, if it exists
     */
    public Optional<MineDown> getMessage(String messageId) {
        return getRawMessage(messageId).map(MineDown::new);
    }

    /**
     * Returns a MineDown-formatted message from the messages file, with replacements applied
     *
     * @param messageId String identifier of the message, corresponding to a key in the file
     * @return An {@link Optional} containing the replacement-applied, formatted message corresponding to the id, if it exists
     */
    public Optional<MineDown> getMessage(String messageId, String... replacements) {
        return getRawMessage(messageId, replacements).map(MineDown::new);
    }

    /**
     * Apply placeholder replacements to a raw message
     *
     * @param rawMessage   The raw, unparsed message
     * @param replacements Ordered list of placeholder replacement Strings
     * @return the raw message, with inserted placeholders
     */
    private String applyReplacements(String rawMessage, String... replacements) {
        int replacementIndexer = 1;
        for (String replacement : replacements) {
            String replacementString = "%" + replacementIndexer + "%";
            rawMessage = rawMessage.replace(replacementString, replacement);
            replacementIndexer = replacementIndexer + 1;
        }
        return rawMessage;
    }

    /**
     * Load the messages from a BoostedYaml {@link YamlDocument} messages file
     *
     * @param messagesConfig The loaded {@link YamlDocument} messages.yml file
     * @return the loaded {@link Messages}
     */
    public static Messages load(YamlDocument messagesConfig) {
        return new Messages(messagesConfig);
    }

}
