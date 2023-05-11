package net.william278.huskhomes.client;

import com.google.gson.annotations.Expose;
import net.william278.huskhomes.network.Payload;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ClientQuery {

    @Expose
    private String uuid;
    @Expose
    private Type type;
    @Expose
    private Payload payload;

    private ClientQuery(@NotNull Type type, @NotNull Payload payload) {
        this.uuid = UUID.randomUUID().toString();
        this.type = type;
        this.payload = payload;
    }

    @SuppressWarnings("unused")
    private ClientQuery() {
    }

    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    @NotNull
    public UUID getUuid() {
        return UUID.fromString(uuid);
    }

    @NotNull
    public Type getType() {
        return type;
    }

    @NotNull
    public Payload getPayload() {
        return payload;
    }

    public void setPayload(@NotNull Payload payload) {
        this.payload = payload;
    }

    public static class Builder {
        private Type type;
        private Payload payload = Payload.empty();

        private Builder() {
        }

        public Builder setType(@NotNull Type type) {
            this.type = type;
            return this;
        }

        public Builder setPayload(@NotNull Payload payload) {
            this.payload = payload;
            return this;
        }

        @NotNull
        public ClientQuery build() {
            return new ClientQuery(type, payload);
        }
    }

    public enum Type {
        HANDSHAKE,
        GET_PRIVATE_HOMES,
        GET_PUBLIC_HOMES,
        GET_WARPS
    }

}
