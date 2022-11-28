package net.william278.huskhomes;

import net.william278.huskhomes.messenger.Message;
import net.william278.huskhomes.messenger.NetworkMessenger;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.SpongePlayer;
import net.william278.huskhomes.position.Server;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.api.network.channel.ChannelBuf;
import org.spongepowered.api.network.channel.raw.RawDataChannel;
import org.spongepowered.api.network.channel.raw.play.RawPlayDataChannel;
import org.spongepowered.api.network.channel.raw.play.RawPlayDataHandler;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SpongePluginMessenger extends NetworkMessenger implements RawPlayDataHandler<EngineConnection> {

    private static final ResourceKey CHANNEL_KEY = ResourceKey.of("bungeecord", "main");
    private RawPlayDataChannel channel;

    @Override
    public void initialize(@NotNull HuskHomes implementor) {
        super.initialize(implementor);

        this.channel = Sponge.channelManager().ofType(CHANNEL_KEY, RawDataChannel.class).play();


        // Register stuff
        channel.addHandler(this);
    }

    @Override
    public CompletableFuture<String[]> getOnlinePlayerNames(@NotNull OnlineUser requester) {
        return null;
    }

    @Override
    public CompletableFuture<String> fetchServerName(@NotNull OnlineUser requester) {
        return null;
    }

    @Override
    public CompletableFuture<String[]> fetchOnlineServerList(@NotNull OnlineUser requester) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> sendPlayer(@NotNull OnlineUser onlineUser, @NotNull Server server) {
        return null;
    }

    @Override
    protected CompletableFuture<Message> dispatchMessage(@NotNull OnlineUser sender, @NotNull Message message) {
        return null;
    }

    @Override
    protected void sendReply(@NotNull OnlineUser replier, @NotNull Message reply) {

    }

    @Override
    public void terminate() {
        channel.removeHandler(this);
    }

    @Override
    public void handlePayload(@NotNull ChannelBuf data, @NotNull EngineConnection connection) {
        final Optional<OnlineUser> player = plugin.getOnlinePlayers().stream()
                .filter(onlineUser -> ((SpongePlayer) onlineUser).getPlayer().connection().equals(connection))
                .findFirst();

        // Player is not online
        if (player.isEmpty()) {
            return;
        }

        //todo handle
    }
}
