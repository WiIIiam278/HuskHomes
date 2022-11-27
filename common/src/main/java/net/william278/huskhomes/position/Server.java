package net.william278.huskhomes.position;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a server on a proxied network
 */
public class Server {

    /**
     * Proxy-defined name of this server
     */
    public String name;

    public Server(@Nullable String name) {
        this.name = (name != null ? name : "server");
    }

    @SuppressWarnings("unused")
    public Server() {
    }

    @Override
    public boolean equals(@NotNull Object other) {
        // If the name of this server matches another, the servers are the same.
        if (other instanceof Server server) {
            return server.name.equalsIgnoreCase(this.name);
        }
        return super.equals(other);
    }

}
