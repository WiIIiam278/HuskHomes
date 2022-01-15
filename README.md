[![Header](https://i.imgur.com/jh0IKyM.png "Header")](https://www.spigotmc.org/resources/huskhomes.83767/)
# HuskHomes
[![Maven CI](https://github.com/WiIIiam278/HuskHomes2/actions/workflows/maven.yml/badge.svg)](https://github.com/WiIIiam278/HuskHomes2/actions/workflows/maven.yml)
[![Jitpack](https://jitpack.io/v/WiIIiam278/HuskHomes2.svg)](https://jitpack.io/#WiIIiam278/HuskHomes2)
[![GitLocalized](https://gitlocalize.com/repo/6997/whole_project/badge.svg)](https://gitlocalize.com/repo/6997/whole_project?utm_source=badge)
[![Discord](https://img.shields.io/discord/818135932103557162?color=7289da&logo=discord)](https://discord.gg/tVYhJfyDWG)

**HuskHomes** is a powerful, intuitive and flexible teleportation plugin for SpigotMC Minecraft servers. The plugin supports a wide array of features; from teleporting between players, teleport requests, public and private homes, warps, random teleporting and more. It also supports this between multiple Spigot servers on a Bungee network.

## Features
* Supports a wide array of commands players will understand in seconds.
* Simple and intuitive interface; no clunky chest GUIs! Instead, a robust clickable JSON chat link system to view and edit homes and warps.
* Supports teleporting to homes and players across multiple servers on a Bungee network.
* Teleport requests (/tpa; /tpahere; /tpaccept; /tpdeny), supported through multiple servers.
* Has a /back command to let players return to their previous position, or where they died if they have the right permissions
* Show the location of public homes and warps on your server's Dynmap, BlueMap or Squaremap
* Integrate your economy by setting costs to set homes, make them public and random teleporting costs
* Fully customisable plugin messages, with multi-language and custom hexadecimal color support via MineDown formatting
* Set warps for your server with the /setwarp and /warp commands
* Set a /spawn location per server where players will also be put when they first join
* Configurable random teleport command allowing players to get out into the wild quickly
* Full TAB completion support across all commands to make it even easier and faster to use
* Easily fine-tune each feature to your server's needs with permission nodes and a well-documented plugin Wiki
* Supports migration from EssentialsX

## Showcase
[<img src="https://i.imgur.com/LSo16N7.gif" height="240" />](https://i.imgur.com/nDpxLgZ.mp4)

[<img src="https://i.imgur.com/cNoXKcE.gif" height="240" />](https://i.imgur.com/uvqnC5q.mp4)

[<img src="https://i.imgur.com/JDz1s2Q.gif" height="240" />](https://i.imgur.com/4oyTN7x.mp4)

## Commands
* /home, /sethome, /edithome, /homelist
* /publichome, /publichomelist
* /warp, /setwarp, /editwarp, /warplist
* /tpa, /tpahere, /tpaccept, /tpdeny
* /tp, /tphere, /tpall, /tpaall
* /spawn, /rtp
* And more...

## Setup
### Setup for a single server
1. Download HuskHomes.jar from the resource page
2. Place HuskHomes.jar in your server's plugin folder
3. (re)Start the server, then stop it again
4. Make configuration changes to the HuskHomes/config.yml file as neccessary
5. If you're using a permissions plugin, ensure permissions are set correctly
6. Start the server again and you are good to start using HuskHomes!

### Setup for a Bungee network
You will need a mySQL Database setup to enable Bungee features
1. Download HuskHomes.jar from the resource page
2. Place the plugin in the plugin folders of **all** the servers you wish to run HuskHomes on
3. (re)Start all the servers you added the HuskHomes.jar to, then turn them off again
4. For each server, navigate to HuskHomes/config.yml and change the following settings
    1. Under `data_storage_options`, change the `storage_type` from `SQLite` to `mySQL`
    2. Fill in your mySQL credentials under `mysql_credentials`
    3. Under `bungee_options:`, set `enable_bungee_mode` to `true` and change the `server_id` to match the name of that server on the bungee network (e.g if you move to it using /server lobby, put "lobby" there)
    4. Modify other settings as appropriate
5. If you're using a permissions plugin, ensure permissions are set correctly
6. Start the servers you installed HuskHomes on and you should be good to go!

### bStats
This plugin uses bStats to provide me with metrics about it's usage. You can turn this off by navigating to `plugins/bStats/config.yml` and editing the config to disable plugin metrics.

View bStats metrics: [Click to View](https://bstats.org/plugin/bukkit/HuskHomes/8430)

### Help and Support
* Report bugs: [Click here](https://github.com/WiIIiam278/HuskHomes2/issues)
* Check the HuskHomes Wiki: [Click here](https://github.com/WiIIiam278/HuskHomes2/wiki)
* If you still need support, join the [HuskHelp Discord](https://discord.gg/tVYhJfyDWG)!
