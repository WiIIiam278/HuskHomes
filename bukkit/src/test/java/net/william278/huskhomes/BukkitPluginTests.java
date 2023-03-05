package net.william278.huskhomes;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import de.themoep.minedown.adventure.MineDown;
import net.william278.huskhomes.command.BukkitCommand;
import net.william278.huskhomes.user.BukkitUser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

/**
 * Uses MockBukkit to test the plugin on a mock Spigot server implementing the Bukkit 1.16 API.
 */
public class BukkitPluginTests {

    public static ServerMock server;
    public static BukkitHuskHomes plugin;

    @BeforeAll
    public static void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(BukkitHuskHomes.class);
    }

    @AfterAll
    public static void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testPluginEnables() {
        Assertions.assertTrue(plugin.isEnabled());
    }

    @Test
    public void testCommandRegistration() {
        // Assert that all commands in BukkitCommandType are registered by the plugin
        for (BukkitCommand.Type command : BukkitCommand.Type.values()) {
            Assertions.assertNotNull(plugin.getCommand(command.getCommand().command));
        }
    }

    @Test
    public void testPlayerAdaption() {
        // Assert that the player adapter is working
        PlayerMock player = server.addPlayer();
        Assertions.assertNotNull(BukkitUser.adapt(player));
    }

    @Test
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
    public void testLocalesLoading() {
        final Map<String, String> rawLocales = plugin.getLocales().rawLocales;
        Assertions.assertTrue(rawLocales.size() > 0);
        rawLocales.forEach((key, value) -> Assertions.assertNotNull(value));
    }

    @Test
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
