package net.william278.huskhomes.config;

import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import net.william278.huskhomes.position.Server;
import org.jetbrains.annotations.NotNull;

@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃ Server ID cache. Must match  ┃
        ┃ server name in proxy config. ┃
        ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛""")
public class CachedServer {

    /**
     * Name of the server (must match proxy config ID)
     */
    @YamlKey("server_name")
    public String serverName;

    public CachedServer(@NotNull String serverName) {
        this.serverName = serverName;
    }

    @SuppressWarnings("unused")
    public CachedServer() {
    }

    @NotNull
    public Server getServer() {
        return new Server(serverName);
    }

}