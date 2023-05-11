package net.william278.huskhomes.client;

import com.google.gson.Gson;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class HuskHomesClient implements ClientModInitializer {

    private static final Identifier CHANNEL_IDENTIFIER = Objects.requireNonNull(
            Identifier.tryParse(ClientQueryHandler.CLIENT_MESSAGE_CHANNEL)
    );

    private final Gson gson = new Gson();
    private final ConcurrentHashMap<UUID, CompletableFuture<ClientQuery>> queries = new ConcurrentHashMap<>();
    private final KeyBinding menuKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.huskhomes.open_menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.huskhomes.category"
    ));

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKeyBinding.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new PositionScreen(this));
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> initializeChannelReceiver());
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ClientPlayNetworking.unregisterReceiver(CHANNEL_IDENTIFIER);
            queries.clear();
        });
    }

    private void initializeChannelReceiver() {
        ClientPlayNetworking.registerReceiver(CHANNEL_IDENTIFIER, (client, handler, buf, responseSender) -> {
            try {
                final String json = buf.readString();
                final ClientQuery query = gson.fromJson(json, ClientQuery.class);
                queries.computeIfPresent(query.getUuid(), (uuid, future) -> {
                    future.complete(query);
                    return null;
                });
                queries.remove(query.getUuid());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    public void sendQuery(@NotNull ClientQuery query, @NotNull Consumer<ClientQuery> callback, @NotNull Runnable exception) {
        final CompletableFuture<ClientQuery> future = new CompletableFuture<>();
        queries.put(query.getUuid(), future);

        // Send the query through a plugin message
        PacketByteBuf packetBuf = PacketByteBufs.create();
        packetBuf.writeBytes(gson.toJson(query).getBytes(StandardCharsets.UTF_8));

        // Write the plugin message to the channel
        ClientPlayNetworking.send(CHANNEL_IDENTIFIER, packetBuf);
        future.orTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        exception.run();
                    } else {
                        callback.accept(result);
                    }
                });
    }

}
