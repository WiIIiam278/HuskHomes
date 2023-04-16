package net.william278.huskhomes.position;

import net.william278.huskhomes.user.User;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@DisplayName("Saved Position Tests")
public class SavedPositionTests {

    // Map of positions - true if the position contains unsafe characters, false if not
    private static final Map<String, Boolean> POSITION_UNSAFE_NAMES = Map.of(
            "TestPosition", false,
            "ExamplePos", false,
            "SafeName", false,
            "[Unsafe]", true,
            "Unsafe]", true,
            "[Name](bad)", true,
            "Unsafe[Name]", true,
            "不安全(不安全)", true
    );

    @DisplayName("Test Home Identifiers")
    @ParameterizedTest(name = "Home: \"{1}\" (Unsafe: {2})")
    @MethodSource("provideHomeData")
    public void testHomeIdentifiers(@NotNull Home home, @NotNull @SuppressWarnings("unused") String name, boolean isUnsafeName) {
        Assertions.assertEquals(
                home.getIdentifier(),
                home.getOwner().getUsername() + Home.IDENTIFIER_DELIMITER + home.getName()
        );
        Assertions.assertEquals(
                home.getSafeIdentifier(),
                isUnsafeName
                        ? home.getOwner().getUsername() + Home.IDENTIFIER_DELIMITER + home.getUuid()
                        : home.getIdentifier()
        );
    }

    @DisplayName("Test Warp Identifiers")
    @ParameterizedTest(name = "Warp: \"{1}\" (Unsafe: {2})")
    @MethodSource("provideWarpData")
    public void testWarpIdentifiers(@NotNull Warp warp, @NotNull @SuppressWarnings("unused") String name, boolean isUnsafeName) {
        Assertions.assertEquals(
                warp.getIdentifier(),
                warp.getName()
        );
        Assertions.assertEquals(
                warp.getSafeIdentifier(),
                isUnsafeName ? warp.getUuid().toString() : warp.getIdentifier()
        );
    }

    @NotNull
    private static Stream<Arguments> provideWarpData() {
        final Position position = Position.at(63.25, 127.43, -32, 180f, -94.3f,
                World.from("TestWorld", UUID.randomUUID()), "TestServer");
        return POSITION_UNSAFE_NAMES.entrySet().stream()
                .map(entry -> Arguments.of(
                        Warp.from(position, PositionMeta.create(entry.getKey(), "")),
                        entry.getKey(),
                        entry.getValue()
                ));
    }

    @NotNull
    private static Stream<Arguments> provideHomeData() {
        final Position position = Position.at(63.25, 127.43, -32, 180f, -94.3f,
                World.from("TestWorld", UUID.randomUUID()), "TestServer");
        return POSITION_UNSAFE_NAMES.entrySet().stream()
                .map(entry -> Arguments.of(
                        Home.from(
                                position,
                                PositionMeta.create(entry.getKey(), ""),
                                User.of(UUID.randomUUID(), "TestUser")
                        ),
                        entry.getKey(),
                        entry.getValue()));
    }


}
