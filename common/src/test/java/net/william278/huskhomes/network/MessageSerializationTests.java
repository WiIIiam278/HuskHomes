package net.william278.huskhomes.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@DisplayName("Message Serialization Tests")
public class MessageSerializationTests {

    private final static List<Message> TEST_MESSAGES = List.of(
            Message.builder()
                    .type(Message.Type.REQUEST_PLAYER_LIST)
                    .target("TestTarget")
                    .payload(Payload.empty())
                    .build(),
            Message.builder()
                    .type(Message.Type.TELEPORT_REQUEST)
                    .target("TestTarget")
                    .payload(Payload.withPosition(
                            new Position(63.25, 127.43, -32, 180f, -94.3f,
                                    new World("TestWorld", UUID.randomUUID()), "TestServer")))
                    .build(),
            Message.builder()
                    .type(Message.Type.TELEPORT_TO_NETWORKED_USER)
                    .target("TestTarget")
                    .payload(Payload.withString("TestString"))
                    .build(),
            Message.builder()
                    .type(Message.Type.PLAYER_LIST)
                    .target("TestTarget")
                    .payload(Payload.withStringList(List.of("TestString1", "TestString2", "TestString3")))
                    .build(),
            Message.builder()
                    .type(Message.Type.TELEPORT_TO_POSITION)
                    .target("TestTarget")
                    .payload(Payload.withPosition(
                            new Position(63.25, 127.43, -32, 180f, -94.3f,
                                    new World("TestWorld", UUID.randomUUID()), "TestServer")))
                    .build()
    );

    // Parameterized test for Message serialization
    @DisplayName("Test Message Serialization/Deserialization")
    @ParameterizedTest(name = "{1} Message")
    @MethodSource("provideMessages")
    public void testMessageSerialization(@NotNull Message message, @SuppressWarnings("unused") String ignored) {
        final Gson gson = createGson();
        final String serializedMessage = gson.toJson(message);
        Assertions.assertNotNull(serializedMessage);
        final Message deserializedMessage = gson.fromJson(serializedMessage, Message.class);
        Assertions.assertNotNull(deserializedMessage);
        Assertions.assertEquals(message.getType(), deserializedMessage.getType());
        Assertions.assertEquals(message.getScope(), deserializedMessage.getScope());
        Assertions.assertEquals(message.getTarget(), deserializedMessage.getTarget());
        Assertions.assertEquals(message.getPayload().getPosition().isPresent(), deserializedMessage.getPayload().getPosition().isPresent());
        Assertions.assertEquals(message.getPayload().getString().isPresent(), deserializedMessage.getPayload().getString().isPresent());
        Assertions.assertEquals(message.getPayload().getStringList().isPresent(), deserializedMessage.getPayload().getStringList().isPresent());
        Assertions.assertEquals(message.getPayload().getStringList().isPresent(), deserializedMessage.getPayload().getStringList().isPresent());
    }

    @NotNull
    private static Gson createGson() {
        return new GsonBuilder().create();
    }

    private static Stream<Arguments> provideMessages() {
        return TEST_MESSAGES.stream().map(message -> Arguments.of(message, message.getType().name()));
    }

}