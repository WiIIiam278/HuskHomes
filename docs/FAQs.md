This page addresses a number of frequently asked questions about HuskHomes.

## Frequently Asked Questions

<details>
<summary>&nbsp;<b>How do I make it so players always rejoin the last server they left from?</b></summary>

In other words, you'd like it so that when players disconnect from your proxy network, that when they reconnect they are put on the same server as the one they were on when they disconnected, rather than your lobby/fallback server. This feature isn't controlled by HuskHomes, but rather on your proxy.

* On Velocity proxies: Install and configure [Yunfaremember](https://modrinth.com/plugin/yunfaremember).
* On BungeeCord proxies: Ensure `force_default_server` is disabled, and that your server is able to write the necessary player server cache file(s) to disk.

If this isn't working, make sure you've not configured priority servers which can override this.

</details>

<details>
<summary>&nbsp;<b>What's the difference between a warp and a public home?</b></summary>

### Warps:
- Nobody owns a warp.
- It is just an object on the server.
- Intended to be created by admins; only able to be created by admins by default.
- No way of limiting number that can be set.
- Accessible with /warp.
- All warp names must be unique globally.

### Public homes:
- Owned by a single player.
- Effectively, just a regular player home that has been made public.
- Can only be edited/deleted/etc by that player (unless you have admin perms).
- Counts towards the players home slot totals. You can also limit the number of homes a player can make public with perms & via config.
- Accessible with /phome, or /home (by the home owner)
- Names must be unique **per-player**; otherwise non-unique p-homes can be accessed with /phome OWNER_USERNAME.HOME_NAME.

</details>

<details>
<summary>&nbsp;<b>Is a Database required? What Databases are supported?</b></summary>

A database is not required for using HuskHomes on a single-server. If you want to use the plugin cross-server, however, a MySQL, MariaDB or PostgreSQL [[database]] are required.

HuskHomes supports the following database types. Databases marked (local) are only supported on a single-server setup:
* SQLite (local)
* H2 (local)
* MySQL v8.0+
* MariaDB v5.0+
* PostgreSQL

</details>