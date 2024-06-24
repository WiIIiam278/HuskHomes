This will walk you through installing HuskHomes on your Spigot, Fabric or Sponge server, or proxy network of servers.

## Requirements
> **Note:** If the plugin fails to load, please check that you are not running an [incompatible version combination](Unsupported-Versions)

* A Spigot (1.17.1+), Fabric (latest Minecraft version), or Sponge (Implementing API v10) _Minecraft: Java Edition_ server running on Java 17+
* (For proxy network support) A proxy server (Velocity, BungeeCord) and MySQL (v8.0+)/MariaDB/PostgreSQL database
* (For optional [[Redis support]]) A Redis database (v5.0+)

## Download HuskHomes for your server
Download the correct jar file for your server from the [latest release page](https://github.com/WiIIiam278/HuskHomes/releases/latest):
* the `HuskHomes-Paper` jar for Spigot or Paper servers
* the `HuskHomes-Fabric` jar for Fabric servers
* or the `HuskHomes-Sponge` jar for Sponge servers

## Single-server Setup Instructions
These instructions are for simply installing HuskHomes on one Spigot, Fabric or Sponge server.

### 1. Install the jar
- Place the plugin jar file in the `/plugins/` directory of your Spigot server, or the `/mods` directory of your Fabric/Sponge server.
### 2. Restart the server and configure
- Start, then stop your server to let HuskHomes generate the config file.
- You can now edit the [config](Config-Files) and locales to your liking.
### 3. Turn on your server
- Start your server again and enjoy HuskHomes!

-----

## Multi-server Setup Instructions
These instructions are for installing HuskHomes on multiple Spigot, Fabric or Sponge servers and having them network together. A MySQL database (v8.0+) is required.

### 1. Install the jar
- Place the plugin jar file in the `/plugins/` directory of each Spigot server, or the `/mods` directory of your Fabric/Sponge server.
- You do not need to install HuskHomes as a proxy plugin.
### 2. Restart the server and configure
- Start, then stop every server to let HuskHomes generate the config file.
- Advanced users: If you'd prefer, you can just create one config.yml file and create symbolic links in each `/plugins/HuskHomes/` (`/config/huskhomes/` on Fabric/Sponge) folder to it to make updating it easier.
### 3. Configure servers to use cross-server mode
- Navigate to the HuskHomes [config](Config-Files) file on each server (`~/plugins/HuskHomes/config.yml` on Spigot, `~/config/huskhomes/config.yml` on Fabric/Sponge)
- Under `database`, set `type` to `MYSQL`, `MARIADB` or `POSTGRESQL` (depending on which type of database you wish to use)
- Under `mysql`/`credentials`, enter the credentials of your MySQL, MariaDB or PostgreSQL database server.
- Scroll down and look for the `cross_server` section. Set `enabled` to `true`.
- You can additionally configure a Redis server to use for network messaging, if you prefer (set the `messenger_type` to `REDIS` if you do this).
- Save the config file. Make sure you have updated the file on every server.
### 4. Restart servers and set server.yml values
- Restart each server again. A `server.yml` file should generate inside the plugin config directory you navigated to earlier
- Set the `name` of the server in this file to the ID of this server as defined in the config of your proxy (e.g. if this is the "hub" server you access with `/server hub`, put "hub" here)
### 5. Restart your servers one last time
- Provided your MySQL database credentials were correct, your network should now be setup to use HuskHomes!
- You can delete the `HuskHomesData.db` SQLite flat file that was generated, if you would like.

<details>
<summary>Cross-Server RTP</summary>

When using Cross-Server RTP 2 things must be true:
1. You must be using Redis as your message broker
2. The server names in `rtp.allowed-servers` must match the `server.yml` value!
</details>

## Next steps
* [Commands & Permissions](Commands)
* [[Config Files]]
* [[Database]]
* [[Redis Support]]
* [[Translations]]