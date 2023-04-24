The HuskHomes API provides methods and classes for:
* getting, creating and updating `Home`s and `Warp`s
* getting information about `SavedUser` data
* building and executing `Teleport`s, including cross-server teleports
* provide custom `RandomTeleportEngine`s for extending the `/rtp` command's functionality.

In addition, [[API Events]] are available for handling when certain actions take place.

This page will walk you through some of these. Note that this documentation is *incomplete*; if you'd like to help improve it, get in touch on our Discord.

## Project setup
### Creating a class to interface with the API
- Unless your plugin completely relies on HuskHomes, you shouldn't put HuskHomes API calls into your main class, otherwise if HuskHomes is not installed you'll encounter `ClassNotFoundException`s

<details>
<summary>Creating a hook class</summary>

```java
public class HuskHomesAPIHook {

    public HuskHomesAPIHook() {
        // Ready to do stuff with the API
    }

}
```
</details>

### Checking if HuskHomes is present and creating the hook
- Check to make sure the HuskHomes plugin is present before instantiating the API hook class

<details>
<summary>Instantiating your hook</summary>

```java
public class MyPlugin extends JavaPlugin {

    public HuskHomesAPIHook huskHomesHook;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("HuskHomes") != null) {
            this.huskHomesHook = new HuskHomesAPIHook();
        }
    }
}
```
</details>

### Getting an instance of the API
- You can now get the API instance by calling `HuskHomesAPI#getInstance()`

<details>
<summary>Getting an API instance</summary>

```java
import net.william278.huskhomes.api.HuskHomesAPI;

public class HuskHomesAPIHook {

    private final HuskHomesAPI huskHomesAPI;

    public HuskHomesAPIHook() {
        this.huskHomesAPI = HuskHomesAPI.getInstance();
    }

}
```
</details>

### The CompletableFuture
A number of methods from the HuskHomesAPI return asynchronously-executing [CompletableFutures](https://www.baeldung.com/java-completablefuture) to ensure the database queries they rely on do not block the main server thread. When a future has finished executing, you can accept the result through a `#thenAccept()`. 

You should never call `#join()` on futures returned from the HuskHomesAPI as futures are processed on server asynchronous tasks, which could lead to thread deadlock and crash your server if you attempt to lock the main thread to process them.

## Getting a player's homes
To get a list of a player's homes, you can call

<details>
<summary>Printing a home list to console</summary>

```java
public class HuskSyncAPIHook {

    private final HuskHomesAPI huskHomesAPI;

    // This method prints out a player's homes into console using stdout
    public void printPlayerHomes(UUID uuid) {
        // Use this to adapt an online player to an OnlineUser, which extends User (accepted by getUserHomes).
        // To get the homes of an offline user, use: User.of(uuid, username);
        OnlineUser user = huskHomesAPI.adaptUser(Bukkit.getPlayer(uuid));
        // A lot of HuskHomes' API methods return as futures which execute asynchronously.
        huskHomesAPI.getUserHomes(user).thenAccept(homeList -> { // Use #thenAccept(data) to run after the future has executed with data
            for (Home home : homeList) {
                // The home and warp object both extend SavedPosition, which maps a position object to a name and description
                System.out.println(home.meta.name); // It's best to use your plugin logger, but this is just an example.
            }
        });
    }

}
```
</details>

## Creating Teleports
The API provides a method for getting a `TeleportBuilder`, which can be used to build a `Teleport` (with toTeleport) or `TimedTeleport` (with toTimedTeleport; a teleport that requires the user to stand still for a period of time first). Teleports can be cross-server.

<details>
<summary>Building a teleport</summary>

```java
public class HuskSyncAPIHook {

    private final HuskHomesAPI huskHomesAPI;

    // This teleports a player to 128, 64, 128 on the server "server"
    public void teleportPlayer(Player player) {
        // Use this to adapt an online player to an OnlineUser, which extends User (accepted by getUserHomes).
        OnlineUser onlineUser = huskHomesAPI.adaptUser(player);

        // The TeleportBuilder accepts a class that (extends/is a) Position. This can be a Home, Warp or constructed Position.
        // --> Note that the World object needs the name and UID of the world.
        // --> The UID will be used if the world can't be found by name. You can just pass it a random UUID if you don't have it.
        Position position = Position.at(
            128, 64, 128,
            World.from("world", UUID.randomUUID()), "server"
        );

        // To construct a teleport, get a TeleportBuilder with #teleportBuilder
        try {
            huskHomesAPI.teleportBuilder()
                .teleporter(onlineUser) // The person being teleported
                .target(position) // The target position
                .toTimedTeleport()
                .execute(); // #execute() can throw a TeleportationException
        } catch(TeleportationException e) {
            e.printStackTrace();
        }
    }

}
```
</details>