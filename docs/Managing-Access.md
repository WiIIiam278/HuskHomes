To manage access to HuskHomes' features on your server, you should use a permission management plugin. However, if you're just throwing up a server with a home plugin for a group of mates, you can also just use the plugin on it's own without one and change some settings in `config.yml`.

### LuckPerms
[LuckPerms](https://luckperms.net) is the standard, stable and recommended permissions plugin, and works on Spigot, Fabric and Sponge servers. Follow the LuckPerms setup guide to get started. You can then use the `/lp editor` to assign the permissions detailed on this page to groups. A lot of HuskHomes' permissions are enabled for default users ([see below](#without-a-permissions-plugin)). You can set permissions to "false" to revoke a node from a group.

### Without a permissions plugin
All commands have default operator level access grants to let you use them without a permissions plugin. The following commands require operator (`/op <name>`):
* /tp
* /tphere
* /tpaall
* /tpall
* /setspawn
* /setwarp
* /delwarp
* /editwarp
* /tpoffline

All other commands (but _not all sub-commands_) may be used by players by default; you can [disable them outright](commands#disabling-commands) to prevent players from using them, or install a permissions plugin if you'd like finer control (_recommended_).