HuskHomes supports displaying homes and warps as markers on server web mapping plugins. Currently, [Dynmap](https://github.com/webbukkit/dynmap) and [BlueMap](https://github.com/BlueMap-Minecraft/BlueMap) are supported. 

Pl3xMap/SquareMap and the wake of forks left after the original plugin was discontinued are unsupported due to lack of a consistent API for them at the moment. Support for [Pl3xMap v2](https://github.com/BillyGalbreath/Pl3xMap/) is planned for when that API matures.

To enable support for map hooks, edit your config.yml file under the `map_hook` section so that `enabled` is `true`. You can customise whether to show public homes, warps or both on the map.

## Dynmap
![Dynmap markers screenshot](https://raw.githubusercontent.com/WiIIiam278/HuskHomes2/master/images/dynmap-hook.png)
To enable Dynmap support, ensure that the `map_hook` is enabled in config.yml and that the latest version of Dynmap is installed on your server. Then, restart your server and your map will be populated with HuskHomes' markers. 

You can click on the markers to view a popup containing information about the public home/warp.

## BlueMap
![BlueMap markers screenshot](https://raw.githubusercontent.com/WiIIiam278/HuskHomes2/master/images/bluemap-hook.png)
To enable BlueMap support, ensure that the `map_hook` is enabled in config.yml and that the latest version of BlueMap is installed on your server. Then, restart your server and your map will be populated with HuskHomes' markers.