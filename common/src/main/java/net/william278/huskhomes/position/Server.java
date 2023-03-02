package net.william278.huskhomes.position;

import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Represents a server on a proxied network
 */
@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃ Server ID cache. Must match  ┃
        ┃ server name in proxy config. ┃
        ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛""")
public class Server {

    /**
     * Default server identifier
     */
    @NotNull
    public static Server getDefault() {
        final Server server = new Server();
        try {
            final Path serverDirectory = Path.of(System.getProperty("user.dir"));
            server.setName(serverDirectory.getFileName().toString().trim());
        } catch (Exception e) {
            server.setName("server");
        }
        return server;
    }

    @YamlKey("server_name")
    private String name = getDefault().getName();

    public Server(@NotNull String name) {
        this.setName(name);
    }

    private Server() {
    }

    @Override
    public boolean equals(@NotNull Object other) {
        // If the name of this server matches another, the servers are the same.
        if (other instanceof Server server) {
            return server.getName().equalsIgnoreCase(this.getName());
        }
        return super.equals(other);
    }

    /**
     * Proxy-defined name of this server
     */
    @NotNull
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
