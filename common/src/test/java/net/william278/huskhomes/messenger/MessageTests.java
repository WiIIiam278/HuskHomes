package net.william278.huskhomes.messenger;

import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.Server;
import net.william278.huskhomes.position.World;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class MessageTests {

    @Test
    public void testMessageSerializationWithEmptyPayload() {
        final Message message = new Message(
                Message.MessageType.TP_REQUEST,
                "TestSender",
                "TestTarget",
                MessagePayload.empty(),
                Message.RelayType.MESSAGE,
                "TestClusterId");
        Assertions.assertNotNull(message.toJson());

        final Message readMessage = Message.fromJson(message.toJson());
        Assertions.assertEquals(message.uuid, readMessage.uuid);
        Assertions.assertEquals(message.sender, readMessage.sender);
        Assertions.assertEquals(message.targetPlayer, readMessage.targetPlayer);
        Assertions.assertEquals(message.type, readMessage.type);
    }

    @Test
    public void testMessageSerializationWithPositionPayload() {
        final Message message = new Message(
                Message.MessageType.POSITION_REQUEST,
                "TestSender",
                "TestTarget",
                MessagePayload.withPosition(new Position(63.25, 127.43, -32, 180f, -94.3f,
                        new World("TestWorld", UUID.randomUUID()), new Server("TestServer"))),
                Message.RelayType.REPLY,
                "TestClusterId");
        Assertions.assertNotNull(message.toJson());

        final Message readMessage = Message.fromJson(message.toJson());
        Assertions.assertEquals(message.uuid, readMessage.uuid);
        Assertions.assertNotNull(readMessage.payload.position);

        assert message.payload.position != null;
        Assertions.assertEquals(message.payload.position.x, readMessage.payload.position.x);
        Assertions.assertEquals(message.payload.position.y, readMessage.payload.position.y);
        Assertions.assertEquals(message.payload.position.z, readMessage.payload.position.z);
    }

}
