> **Warning:** Imported data will overwrite any existing data in HuskHomes. Please make a backup of your database before starting an import.

HuskHomes supports importing data from other plugins/mods through the `/huskhomes import` command.

| Name                        | Supported Import Data | Platforms          | Link                     |
|-----------------------------|-----------------------|--------------------|--------------------------|
| [EssentialsX](#essentialsx) | Homes, Warps, Users   | Spigot, Paper      | https://essentialsx.net/ |


## EssentialsX
HuskHomes supports importing Homes, Warps and User data from [EssentialsX](https://essentialsx.net/) (`v2.19.7`+).

Please note that imported data will overwrite any duplicate existing data (e.g. homes with the same name will be overwritten with imported Essentials homes). Follow the below instructions to import your data:

1. [Install the latest version of HuskHomes](setup) on your server.
2. Ensure EssentialsX v2.19.7+ is installed on your server and that user data is present. Restart your server if necessary.
3. Verify that the EssentialsX importer is available by typing `/huskhomes import list`
4. Run `/huskhomes import start EssentialsX` to start the importer. Progress will be displayed in chat and/or in console, including the amount of data that was imported.
5. Once the importer has finished, verify that the data has been imported correctly by typing `/huskhomes:warplist` and `/huskhomes:homelist <player>`

You should restart your server after completing the import.