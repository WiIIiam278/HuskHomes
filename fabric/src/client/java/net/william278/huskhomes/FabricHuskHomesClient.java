package net.william278.huskhomes;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.william278.huskhomes.gui.HomeListScreen;
import net.william278.huskhomes.gui.HomeProvider;
import net.william278.huskhomes.gui.IntegratedServerHomeProvider;
import org.lwjgl.glfw.GLFW;

public class FabricHuskHomesClient extends FabricHuskHomes implements ClientModInitializer {

    private HomeProvider provider;

    @Override
    public void onInitializeClient() {
        this.onInitialize();
        this.registerProvider();
        this.registerHotkey();
    }

    private void registerHotkey() {
        final KeyBinding binding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.huskhomes.open_home_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.huskhomes.key_bindings"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!binding.wasPressed()) {
                return;
            }
            if (client.currentScreen == null) {
                client.setScreen(new HomeListScreen(provider));
            } else if (client.currentScreen instanceof HomeListScreen screen) {
                screen.close();
            }
        });
    }

    private void registerProvider() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (server.isDedicated()) {
                //todo plugin message provider
                return;
            }
            this.provider = new IntegratedServerHomeProvider(this);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((a, b) -> this.provider = null);
    }

}
