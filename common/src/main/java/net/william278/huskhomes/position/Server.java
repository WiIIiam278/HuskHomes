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

    public Server() {
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Server server) {
            // If the name of this server matches another, the servers are the same.
            return server.name.equalsIgnoreCase(this.name);
        }
        return super.equals(other);
    }

}
