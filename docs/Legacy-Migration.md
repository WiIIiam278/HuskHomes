> **Warning:** Migration from v2.11.x to v4.x is not supported; you must first download HuskHomes v3.2.1 and carry out the migration in that version, before upgrading to v4.x. No migration is needed between v3.x and v4.x as the two versions have the same database schema.

To upgrade from HuskHomes v2.11.2 to v3.x, please carry out the following steps:

* Delete the old `HuskHomes-2.x.jar` from your server(s) `plugins` folder(s).
* Drag in the new HuskHomes v3.x jar file to the folder.
* In your `plugins/HuskHomes/` folder, delete the `messages.yml` file to let it regenerate.
* Start your server. If you are running multiple servers, start just one server. HuskHomes will automatically upgrade your data.
* If you are running on multiple servers:
  - Delete `plugins/HuskHomes/config.yml` from each of your other servers.
  - Start each server you deleted config.yml from.
  - Once they have started, stop them again and navigate to the newly generated config.yml file on each server and update it with your database credentials and set the database type to MYSQL from SQLITE. Be sure to enable cross_server mode, too.
* Update your Permissions plugin groups to make use of the [new permission nodes](https://william278.net/docs/huskhomes/Commands)

That's it! Enjoy HuskHomes v3.x.