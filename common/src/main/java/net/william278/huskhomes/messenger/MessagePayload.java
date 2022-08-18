package net.william278.huskhomes.messenger;

import net.william278.huskhomes.position.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a payload sent in a cross-server {@link Message}
 */
public class MessagePayload {

    /**
     * A position field
     */
    @Nullable
    public Position position;

    /**
     * Returns an empty cross-server message payload
     *
     * @return an empty payload
     */
    @NotNull
    public static MessagePayload empty() {
        return new MessagePayload();
    }

    /**
     * Returns a payload containing a {@link Position}
     *
     * @param position the position to send
     * @return a payload containing the position
     */
    @NotNull
    public static MessagePayload withPosition(@NotNull Position position) {
        final MessagePayload payload = new MessagePayload();
        payload.position = position;
        return payload;
    }

    private MessagePayload() {
    }

}
