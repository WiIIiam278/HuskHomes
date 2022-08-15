package net.william278.huskhomes;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.william278.huskhomes.command.BukkitCommandType;
import net.william278.huskhomes.player.BukkitPlayer;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.PositionMeta;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Uses MockBukkit to test the plugin on a mock Paper server implementing the Bukkit 1.19 API.
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
        for (BukkitCommandType command : BukkitCommandType.values()) {
            Assertions.assertNotNull(plugin.getCommand(command.commandBase.command));
        }
    }

    @Test
    public void testPlayerAdaption() {
        // Assert that the player adapter is working
        PlayerMock player = server.addPlayer();
        Assertions.assertNotNull(BukkitPlayer.adapt(player));
    }

}
