For networks of connected servers, HuskHomes supports Redis (v5.0+) as an alternative to the default Plugin messaging protocol, for servers that would like to use it. Redis is required if you would like to use cross-server RTP, otherwise is completely optional.

## What is Redis?
[Redis](http://redis.io/) (**RE**mote **DI**ctionary **S**erver) is an open-source, in-memory data store server that can be used as a cache, message broker, streaming engine, or database.

HuskHomes supports Redis and uses it for pub/sub messaging to facilitate cross-server teleports and teleport requests, as well as other [[commands]].

## Configuring
To configure Redis, navigate to your [`config.yml`](Config-Files) file and modify the properties under `redis`. Change the `broker_type` to `REDIS`.

<details>
<summary>Database options (config.yml)</summary>

```yaml
# Type of network message broker to ues for data synchronization (PLUGIN_MESSAGE or REDIS)
broker_type: REDIS
# Settings for if you're using REDIS as your message broker
redis:
  host: localhost
  port: 6379
  # Password for your Redis server. Leave blank if you're not using a password.
  password: ''
  use_ssl: false
  # Settings for if you're using Redis Sentinels.
  # If you're not sure what this is, please ignore this section.
  sentinel:
    master_name: ''
    # List of host:port pairs
    nodes: []
    password: ''
```
</details>


### Credentials
Enter the hostname, port, and default user password of your Redis server.

If your Redis default user doesn't have a password, leave the password field blank (`password: ''`') and the plugin will attempt to connect without a password.

### Default user password
Depending on the version of Redis you've installed, Redis may or may not set a random default user password. Please check this in your Redis server config. You can clear the password of the default user with the below command in `redis-cli`.

```bash
requirepass thepassword
user default on nopass ~* &* +@all
```

### Using Redis Sentinel
If you're using [Redis Sentinel](https://redis.io/docs/latest/operate/oss_and_stack/management/sentinel/), set this up by filling out the properties under the `sentinel` subsection.

You'll need to supply your master set name, your sentinel password, and a list of hosts/ports in the format `host:port`.

## Getting a Redis Server
A Redis server is not required to use HuskHomes, but we recommend it for better cross-server networking performance. A Redis server also lets you utilise cross-server RTP, which is not supported with the default plugin messaging protocol. Instructions for getting Redis on different servers are detailed below. HuskHomes is tested for the official Redis package, but should also work with Redis forks or other compatible software.

For the best results, we recommend a Redis server with 1GB of RAM, hosted locally (on the same machine as all your other servers). If your setup has multiple machines, install Redis on the machine with your Velocity/BungeeCord/Waterfall proxy server.

### If you're using a Minecraft server hosting provider
Please contact your host's customer support and request Redis. You can direct them to this page if you wish. Looking for a Minecraft Server host that supports Redis? We maintain a list of [server hosts which offer Redis](https://william278.net/docs/website/redis-hosts).

### Redis on Linux or macOS
You can [install Redis](https://redis.io/docs/latest/operate/oss_and_stack/install/install-redis/install-redis-on-linux/) on your distribution of Linux. Redis is widely available on most package manager repositories.

You can also [install Redis](https://redis.io/docs/latest/operate/oss_and_stack/install/install-redis/install-redis-on-mac-os/) on your macOS server.

### Redis on Windows
Redis isn't officially supported on Windows, but there's a number of [unofficial ports](https://github.com/tporadowski/redis/releases) you can install which work great and run Redis as a Windows service.

You can also [install Redis via WSL](https://redis.io/docs/latest/operate/oss_and_stack/install/install-redis/install-redis-on-windows/) if you prefer.

### Pterodactyl / Pelican panel hosts
If you're self-hosting your server on a Pterodactyl or Pelican panel, you will already have Redis installed and can use this server for HuskHomes, too.

If you are hosting your Redis server on the same node as your servers, you need to use `172.18.0.1` as your host (or equivalent if you changed your network settings), and bind it in the Redis config `nano /etc/redis/redis.conf`.

You will also need to uncomment the `requirepass` directive and set a password to allow outside connections, or disable `protected-mode`. Once a password is set and Redis is restarted `systemctl restart redis`, you will also need to update the password in your pterodactyl `.env` (`nano /var/www/pterodactyl/.env`) and refresh the cache `cd /var/www/pterodactyl && php artisan config:clear`.

You may also need to allow connections from your firewall depending on your Linux distribution.