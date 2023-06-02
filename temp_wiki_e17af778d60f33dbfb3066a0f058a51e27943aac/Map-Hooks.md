HuskHomes supports having homes and warps be displayed as markers on server web map plugins. The following map plugins are supported:
* [Dynmap](https://github.com/webbukkit/dynmap)
* [BlueMap](https://github.com/BlueMap-Minecraft/BlueMap)
* [Pl3xMap](https://github.com/BillyGalbreath/Pl3xMap) (Paper 1.19.4+ servers only)

To enable support for map hooks, edit your [`config.yml`](config-files) file under the `map_hook` section so that `enabled` is `true`. You can customize whether to show public homes, warps or both on the map.

## Dynmap
![Dynmap markers screenshot](https://raw.githubusercontent.com/WiIIiam278/HuskHomes2/master/images/dynmap-hook.png)
To enable Dynmap support, ensure that the `map_hook` is enabled in config.yml and that the latest version of Dynmap is installed on your server. Once installed, restart your server and public homes and warps to be populated on your map.

You can click on the markers to view a popup containing information about the public home/warp.

## BlueMap
![BlueMap markers screenshot](https://raw.githubusercontent.com/WiIIiam278/HuskHomes2/master/images/bluemap-hook.png)
To enable BlueMap support, ensure that the `map_hook` is enabled in config.yml and that the latest version of BlueMap is installed on your server. Once installed, restart your server and public homes and warps to be populated on your map.

## Pl3xMap
To enable Pl3xMap support, ensure that the `map_hook` is enabled in config.yml and that the latest version of Pl3xMap is installed and that you are running Paper on Minecraft 1.19.4+. Once installed, restart your server and public homes and warps to be populated on your map.

You can click on the markers to view a popup containing information about the public home/warp.