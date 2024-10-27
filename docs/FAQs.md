This page addresses a number of frequently asked questions.

## How do I make it so players always rejoin the last server they left from?
In other words, you'd like it so that when players disconnect from your proxy network, that when they reconnect they are put on the same server as the one they were on when they disconnected, rather than your lobby/fallback server. This feature isn't controlled by HuskHomes, but rather on your proxy.

* On Velocity proxies: Install and configure [Yunfaremember](https://modrinth.com/plugin/yunfaremember).
* On BungeeCord proxies: Ensure `force_default_server` is disabled, and that your server is able to write the necessary player server cache file(s) to disk.

If this isn't working, make sure you've not configured priority servers which can override this.