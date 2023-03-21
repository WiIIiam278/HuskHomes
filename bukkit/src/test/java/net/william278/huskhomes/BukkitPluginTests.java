package net.william278.huskhomes;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import de.themoep.minedown.adventure.MineDown;
import net.william278.huskhomes.command.BukkitCommand;
import net.william278.huskhomes.command.Command;
import net.william278.huskhomes.command.RtpCommand;
import net.william278.huskhomes.user.BukkitUser;
import net.william278.huskhomes.user.ConsoleUser;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Uses MockBukkit to test the plugin on a mock Spigot server implementing the Bukkit 1.16 API.
 */
@DisplayName("Bukkit Plugin Tests")
public class BukkitPluginTests {

    private static ServerMock server;
    private static BukkitHuskHomes plugin;

    @BeforeAll
    @DisplayName("Test Plugin Setup")
    public static void setUpPlugin() {
        server = MockBukkit.mock();
        server.addSimpleWorld("world");
        plugin = MockBukkit.load(BukkitHuskHomes.class);
    }

    @AfterAll
    @DisplayName("Tear down Plugin")
    public static void tearDownPlugin() {
        MockBukkit.unmock();
    }

    @Nested
    @DisplayName("Plugin Initialization Tests")
    public class ValidationTests {

        @Test
        @DisplayName("Test Plugin Enables")
        public void testPluginEnables() {
            Assertions.assertTrue(plugin.isEnabled());
        }

        @Test
        @DisplayName("Test Command Registration")
        public void testCommandRegistration() {
            for (BukkitCommand.Type command : BukkitCommand.Type.values()) {
                Assertions.assertNotNull(plugin.getCommand(command.getCommand().getName()));
            }
            Assertions.assertEquals(BukkitCommand.Type.values().length, plugin.getCommands().size());
        }

        @Test
        @DisplayName("Test Player Adapter")
        public void testPlayerAdaption() {
            PlayerMock player = server.addPlayer();
            Assertions.assertNotNull(BukkitUser.adapt(player));
        }

    }

    @Nested
    @DisplayName("Locale Formatting Tests")
    public class LocaleFormattingTests {

        @Test
        @DisplayName("Test Message Formatting")
        public void testMessageFormatting() {
            PlayerMock player = server.addPlayer();

            final MineDown simpleLocale = plugin.getLocales()
                    .getLocale("error_in_game_only")
                    .orElseThrow(() -> new IllegalStateException("Failed to load locale"));
            final String simpleLocaleText = plugin.getLocales().getRawLocale("error_in_game_only")
                    .orElseThrow(() -> new IllegalStateException("Failed to load raw locale"));
            BukkitUser.adapt(player).sendMessage(simpleLocale);
            player.assertSaid(simpleLocaleText);
        }

        @Test
        @DisplayName("Test Message Dispatching")
        public void testMessageDispatching() {
            PlayerMock player = server.addPlayer();

            final MineDown locale = plugin.getLocales()
                    .getLocale("teleporting_action_bar_warmup", Integer.toString(3))
                    .orElseThrow(() -> new IllegalStateException("Failed to load locale"));
            BukkitUser.adapt(player).sendActionBar(locale);
            BukkitUser.adapt(player).sendMessage(locale);
            BukkitUser.adapt(player).sendTitle(locale, false);
            BukkitUser.adapt(player).sendTitle(locale, true);
        }

        @Test
        @DisplayName("Test Locale Loading")
        public void testLocalesLoading() {
            final Map<String, String> rawLocales = plugin.getLocales().rawLocales;
            Assertions.assertTrue(rawLocales.size() > 0);
            rawLocales.forEach((key, value) -> Assertions.assertNotNull(value));
        }

        @Test
        @DisplayName("Test Locale Parsing")
        public void testLocaleParsing() {
            final Map<String, String> rawLocales = plugin.getLocales().rawLocales;
            BukkitUser bukkitUser = BukkitUser.adapt(server.addPlayer());
            rawLocales.forEach((key, value) -> {
                Optional<MineDown> locale = plugin.getLocales().getLocale(key);
                Assertions.assertTrue(locale.isPresent());
                bukkitUser.sendMessage(locale.get());
            });
        }

    }

    @Nested
    @DisplayName("Command Execution Tests")
    public class CommandExecutionTests {

        @DisplayName("Test User Execution")
        @ParameterizedTest(name = "/{2} Command")
        @MethodSource("provideUserExecutedCommands")
        public void testUserExecution(@NotNull Command command, @NotNull OnlineUser user,
                                      @NotNull @SuppressWarnings("unused") String commandName) {
            Assertions.assertNotNull(command);
            Assertions.assertNotNull(user);
            command.execute(user, new String[0]);
        }

        @DisplayName("Test Console Execution")
        @ParameterizedTest(name = "/{2} Command")
        @MethodSource("provideConsoleExecutedCommands")
        public void testUserExecution(@NotNull Command command, @NotNull ConsoleUser console,
                                      @NotNull @SuppressWarnings("unused") String commandName) {
            Assertions.assertNotNull(command);
            Assertions.assertNotNull(console);
            command.execute(console, new String[0]);
        }

        public static Stream<Arguments> provideUserExecutedCommands() {
            final List<Command> commands = plugin.getCommands();
            final PlayerMock player = server.addPlayer();
            player.setOp(true);

            final BukkitUser playerUser = BukkitUser.adapt(player);
            return commands.stream()
                    .filter(command -> !(command instanceof RtpCommand))
                    .flatMap(command -> Stream.of(Arguments.of(command, playerUser, command.getName())));
        }

        public static Stream<Arguments> provideConsoleExecutedCommands() {
            final List<Command> commands = plugin.getCommands();
            final ConsoleUser console = plugin.getConsole();
            return commands.stream()
                    .filter(command -> !(command instanceof RtpCommand))
                    .flatMap(command -> Stream.of(Arguments.of(command, console, command.getName())));
        }

    }

}
