For networks of connected servers, HuskHomes supports Redis (v5.0+) as an alternative to the default Plugin messaging protocol, for servers that would like to use it.

## Setup
To make use of Redis, your network must first be correctly configured in `cross_server` mode. Then, follow these steps:
1. Turn off all your servers.
2. Ensure your Redis server is online.
3. Modify the `config.yml` files of each server, filling in the redis `credentials` in the `cross_server` section.
   - Most Redis installations don't have a password by default. You can probably leave `password` blank (`''`).
5. Set the `messenger_type` to `REDIS`
6. Save your config files and turn on each server. Check to make sure the plugin enabled the network messenger successfully on startup. If it didn't, check your credentials (try without setting the password and SSL mode off if neccessary).