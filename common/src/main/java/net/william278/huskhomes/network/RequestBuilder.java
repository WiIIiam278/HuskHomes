package net.william278.huskhomes.network;

import org.jetbrains.annotations.NotNull;

public class RequestBuilder {

    private Request.MessageType type;

    private String targetPlayer;

    @NotNull
    private Payload payload = Payload.empty();

    protected RequestBuilder() {
    }

    @NotNull
    public RequestBuilder withType(Request.MessageType type) {
        this.type = type;
        return this;
    }

    @NotNull
    public RequestBuilder withTargetPlayer(String targetPlayer) {
        this.targetPlayer = targetPlayer;
        return this;
    }

    @NotNull
    public RequestBuilder withPayload(Payload payload) {
        this.payload = payload;
        return this;
    }

    @NotNull
    public Request build() {
        return new Request(type, targetPlayer, payload, Request.RelayType.MESSAGE);
    }

}
