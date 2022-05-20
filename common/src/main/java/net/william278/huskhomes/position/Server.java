package net.william278.huskhomes.position;

/**
 * Represents a server on a proxied network
 */
public class Server {

    /**
     * Proxy-defined identifier of this server
     */
    public String id;

    public Server(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Server server) {
            // If the id of this server matches another, the servers are the same.
            return server.id.equalsIgnoreCase(this.id);
        }
        return super.equals(other);
    }
}
