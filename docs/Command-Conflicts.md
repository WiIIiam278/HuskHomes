_Command conflicts_ can arise when you have multiple plugins providing similar commands. 

For instance, if you have both [EssentialsX](https://essentialsx.net/) and HuskHomes installed, you will have two `/home` commands. This can cause issues when players try to use the command, as the server will prioritise whichever plugin loaded first. In the case of Essentials, this means that players will be teleported to their Essentials home(s) instead of their HuskHomes home.

This page discusses a number of strategies to circumnavigate this issue.

## Disabling EssentialsX Commands
> **Applies to:** EssentialsX (Spigot, Paper)

[EssentialsX](https://essentialsx.net/) is a popular plugin which provides a number of commands, including `/home`, `/warp`, `/spawn`, and `/tpa`. If you have EssentialsX installed, you will need to disable these commands to prevent conflicts with HuskHomes.

To do this, you must add the Essentials commands you wish to disable to the `disabled-commands` section of your Essentials config file (located in `Essentials/config.yml`). Below is an example of a config file with all the Essentials commands disabled.

<details>
<summary>EssentialsX config.yml file</summary>

```yaml
# Disabling commands here will prevent Essentials handling the command, this will not affect command conflicts.
# You should not have to disable commands used in other plugins, they will automatically get priority.
# See https://bukkit.fandom.com/wiki/Commands.yml#aliases to map commands to other plugins.
disabled-commands:
  - home
  - homes
  - sethome
  - homelist
  - delhome
  - warp
  - setwarp
  - createwarp 
  - warplist
  - delwarp
  - tp
  - tphere
  - tpa
  - tpahere
  - tpaccept
  - tpdeny
  - tpno
  - rtp
  - tpignore
  - tpoffline
  - tpall
  - tpaall
  - spawn
  - setspawn
  - back
```
</details>

## Fallback namespace
> **Applies to:** Spigot, Paper, or Fabric

All commands are registered with a fallback namespace, so that you can still access them. For instance, `/minecraft:tp` will run the default Minecraft teleport command, while `/huskhomes:tp` will run HuskHomes' tp command.

If you have multiple home commands on your server, for instance, you can therefore use `/huskhomes:home` to run HuskHomes' home command instead of, for instance, Essentials'. However, this is a bit ugly and can be confusing for players to understand, so read below for how to create aliases to make this easier.

## Editing Commands.yml
> **Applies to:** Spigot, Paper

Spigot provides a [`commands.yml` file](https://bukkit.fandom.com/wiki/Commands.yml) which lets you define custom aliases for commands. This not only lets you define additional shortcuts for plugin commands, but also determine which commands should take priority during execution.

### Overrides for HuskHomes
Add the alias pack below to your `commands.yml` file's aliases section to ensure HuskHomes' commands take priority over other plugins' commands. This works by defining an alias for each un-namespaced command to the version with a fallback namespace.

Note that if you _want_ other plugin commands to take precedence over HuskHomes, you can swap out the `- "huskhomes:` prefix for the other plugin's identifier (e.g. for EssentialsX, use `- "essentials:`).

<details>
<summary>HuskHomes aliases for commands.yml</summary>

```yaml
aliases:
  home:
    - "huskhomes:home $1-"
  sethome:
    - "huskhomes:sethome $1-"
  homelist:
    - "huskhomes:homelist $1-"
  homes:
    - "huskhomes:homelist $1-"
  delhome:
    - "huskhomes:delhome $1-"
  edithome:
    - "huskhomes:edithome $1-"
  phome:
    - "huskhomes:phome $1-"
  phomelist:
    - "huskhomes:phomelist $1-"
  warp:
    - "huskhomes:warp $1-"
  setwarp:
    - "huskhomes:setwarp $1-"
  warplist:
    - "huskhomes:warplist $1-"
  delwarp:
    - "huskhomes:delwarp $1-"
  editwarp:
    - "huskhomes:editwarp $1-"
  tp:
    - "huskhomes:tp $1-"
  tphere:
    - "huskhomes:tphere $1-"
  tpa:
    - "huskhomes:tpa $1-"
  tpahere:
    - "huskhomes:tpahere $1-"
  tpaccept:
    - "huskhomes:tpaccept $1-"
  tpyes:
    - "huskhomes:tpaccept $1-"
  tpdecline:
    - "huskhomes:tpdecline $1-"
  tpno:
    - "huskhomes:tpdecline $1-"
  rtp:
    - "huskhomes:rtp $1-"
  tpignore:
    - "huskhomes:tpignore $1-"
  tpoffline:
    - "huskhomes:tpoffline $1-"
  tpall:
    - "huskhomes:tpall $1-"
  tpaall:
    - "huskhomes:tpaall $1-"
  spawn:
    - "huskhomes:spawn $1-"
  setspawn:
    - "huskhomes:setspawn $1-"
  back:
    - "huskhomes:back $1-"
  huskhomes:
    - "huskhomes:huskhomes $1-"
```
</details>
