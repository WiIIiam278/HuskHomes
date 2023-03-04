package net.william278.huskhomes.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class MessageTests {

    private final static Gson GSON = new GsonBuilder().create();
    private final static Message DUMMY_NO_PAYLOAD = Message.builder()
            .type(Message.Type.TELEPORT_REQUEST)
            .target("TestTarget")
            .payload(Payload.empty())
            .build();

    private final static Message DUMMY_PAYLOAD = Message.builder()
            .type(Message.Type.TELEPORT_REQUEST)
            .target("TestTarget")
            .payload(Payload.withPosition(
                    new Position(63.25, 127.43, -32, 180f, -94.3f,
                            new World("TestWorld", UUID.randomUUID()), "TestServer")))
            .build();

    @Test
    @DisplayName("Test message serialization with empty payload")
    public void testMessageSerializationWithEmptyPayload() {
        Assertions.assertNotNull(GSON.toJson(DUMMY_NO_PAYLOAD));

        final Message readMessage = GSON.fromJson(GSON.toJson(DUMMY_NO_PAYLOAD), Message.class);
        Assertions.assertEquals(DUMMY_NO_PAYLOAD.getUuid(), readMessage.getUuid());
        Assertions.assertEquals(DUMMY_NO_PAYLOAD.getTarget(), readMessage.getTarget());
        Assertions.assertEquals(DUMMY_NO_PAYLOAD.getType(), readMessage.getType());
    }

    @Test
    @DisplayName("Test message serialization with position payload")
    public void testMessageSerializationWithPositionPayload() {
        Assertions.assertNotNull(GSON.toJson(DUMMY_PAYLOAD));

        final Message readMessage = GSON.fromJson(GSON.toJson(DUMMY_PAYLOAD), Message.class);
        Assertions.assertEquals(DUMMY_PAYLOAD.getUuid(), readMessage.getUuid());
        Assertions.assertNotNull(readMessage.getPayload().getPosition());

        assert readMessage.getPayload().getPosition().isPresent() && DUMMY_PAYLOAD.getPayload().getPosition().isPresent();
        Assertions.assertEquals(DUMMY_PAYLOAD.getPayload().getPosition().get().getX(),
                readMessage.getPayload().getPosition().get().getX());
        Assertions.assertEquals(DUMMY_PAYLOAD.getPayload().getPosition().get().getY(),
                readMessage.getPayload().getPosition().get().getY());
        Assertions.assertEquals(DUMMY_PAYLOAD.getPayload().getPosition().get().getZ(),
                readMessage.getPayload().getPosition().get().getZ());
    }
}
