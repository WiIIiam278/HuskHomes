package net.william278.huskhomes.network;

import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.Server;
import net.william278.huskhomes.position.World;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class RequestTests {

    private final static Request DUMMY_NO_PAYLOAD = Request.builder()
            .withType(Request.MessageType.TELEPORT_REQUEST)
            .withTargetPlayer("TestTarget")
            .withPayload(Payload.empty())
            .build();

    private final static Request DUMMY_PAYLOAD = Request.builder()
            .withType(Request.MessageType.TELEPORT_REQUEST)
            .withTargetPlayer("TestTarget")
            .withPayload(Payload.withPosition(
                    new Position(63.25, 127.43, -32, 180f, -94.3f,
                            new World("TestWorld", UUID.randomUUID()), new Server("TestServer"))))
            .build();

    @Test
    public void testMessageSerializationWithEmptyPayload() {
        Assertions.assertNotNull(DUMMY_NO_PAYLOAD.toJson());

        final Request readRequest = Request.fromJson(DUMMY_NO_PAYLOAD.toJson());
        Assertions.assertEquals(DUMMY_NO_PAYLOAD.getUuid(), readRequest.getUuid());
        Assertions.assertEquals(DUMMY_NO_PAYLOAD.getTargetPlayer(), readRequest.getTargetPlayer());
        Assertions.assertEquals(DUMMY_NO_PAYLOAD.getType(), readRequest.getType());
    }

    @Test
    public void testMessageSerializationWithPositionPayload() {
        Assertions.assertNotNull(DUMMY_PAYLOAD.toJson());

        final Request readRequest = Request.fromJson(DUMMY_PAYLOAD.toJson());
        Assertions.assertEquals(DUMMY_PAYLOAD.getUuid(), readRequest.getUuid());
        Assertions.assertNotNull(readRequest.getPayload().getPosition());

        assert readRequest.getPayload().getPosition() != null && DUMMY_PAYLOAD.getPayload().getPosition() != null;
        Assertions.assertEquals(DUMMY_PAYLOAD.getPayload().getPosition().getX(), readRequest.getPayload().getPosition().getX());
        Assertions.assertEquals(DUMMY_PAYLOAD.getPayload().getPosition().getY(), readRequest.getPayload().getPosition().getY());
        Assertions.assertEquals(DUMMY_PAYLOAD.getPayload().getPosition().getZ(), readRequest.getPayload().getPosition().getZ());
    }
}
