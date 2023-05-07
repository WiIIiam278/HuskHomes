> **Please Note:** The HuskHomes API itself is only available on Bukkit, but Fabric and Sponge do support [API events](api-events).

The HuskHomes API provides methods and classes for:
* getting, creating and updating `Home`s and `Warp`s
* getting information about `SavedUser` data
* building and executing (timed) `Teleport`s, including cross-server teleports
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

> **Warning:** You should never call `#join()` on futures returned from the HuskHomesAPI as futures are processed on server asynchronous tasks, which could lead to thread deadlock and crash your server if you attempt to lock the main thread to process them.

## Getting a player's homes
To get a list of a player's homes, you can call `#getUserHomes(user)` on the API instance. This method requires a `User` object, which can be constructed from a UUID and username, or adapted from an online player using `#adaptUser(player)`. The method returns a `CompletableFuture<List<Home>>`, which can be accepted with `#thenAccept()`.

Note that all the documentation for `Home`s also applies to `Warps` (e.g. `HuskHomesAPI#getWarps` to get all the warps). Since warps are globally owned, these methods do not require an owning `User`.

<details>
<summary>Printing a home list to console</summary>

```java
public class HuskHomesAPIHook {

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
        
        // You can also get a specific home by name using #getUserHome(user, name)
        huskHomesAPI.getUserHome(user, "example").thenAccept(optionalHome -> {
            // #getUserHome returns an Optional wrapper, so we need to run #ifPresent() first and call #get() to retrieve it if it exists
            if (optionalHome.isPresent()) {
                System.out.println("Found " + user.getUsername() + "'s home: " optionalHome.get().getName());
            } else {
                System.out.println("Home not found");
            }
        });
    }

}
```
</details>

### Creating a home
To create a home, you can call `#createHome(owner, name, position)` on the API instance. This method requires a user who owns the home, a valid name, and a Position.

<details>
<summary>Creating a home</summary>

```java
public class HuskHomesAPIHook {

    private final HuskHomesAPI huskHomesAPI;

    // This method creates a home with the name "example" at the player's current location
    public void createHome(Player owner) {
        // Use this to adapt an online player to an OnlineUser, which extends User (accepted by createHome).
        OnlineUser onlineUser = huskHomesAPI.adaptUser(player);
        
        // We can get an OnlineUser's Position with #getPosition, which we can pass here to createHome
        try {
            huskHomesAPI.createHome(onlineUser, "example", onlineUser.getPosition());
        } catch (ValidationException e) {
            // Homes will be validated, and if validation fails a ValidationException will be thrown.
            // This can happen if the user has too many homes, or if its metadata is invalid (name, description, etc)
            // You should catch ValidationExceptions, determine what caused it (#getType) and handle it appropriately.
            owner.sendMessage(ChatColor.RED + "Failed to create example home: " + e.getType());
        }
    }
}
```
</details>

## Creating Teleports
The API provides a method for getting a `TeleportBuilder`, which can be used to build a `Teleport` (with `#toTeleport`) or `TimedTeleport` (with toTimedTeleport; a teleport that requires the user to stand still for a period of time first). Teleports can be cross-server.

<details>
<summary>Building a teleport</summary>

```java
public class HuskHomesAPIHook {

    private final HuskHomesAPI huskHomesAPI;

    // This teleports a player to 128, 64, 128 on the server "server"
    public void teleportPlayer(Player player) {
        OnlineUser onlineUser = huskHomesAPI.adaptUser(player);

        // The TeleportBuilder accepts a class that extends Target. This can be a Username or a Position (or a Home/Warp, which extends Position)
        // * Note that the World object needs the name and UID of the world.
        // * The UID will be used if the world can't be found by name. You can just pass it a random UUID if you don't have it.
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
            e.printStackTrace(); // This exception will contain the reason why the teleport failed, so you can handle it gracefully.
        }
    }

}
```
</details>

### Timed Teleports
Timed teleports are teleports that require the user to stand still for a period of time before they are executed, so that players don't instantly teleport out of combat or dangerous situations. They can be created by calling `#toTimedTeleport()` on a `TeleportBuilder`. The time the player must stand still for is determined through the warmup time value set in the plugin config.

<details>
<summary>Building a timed teleport</summary>

```java
public class HuskHomesAPIHook {

    private final HuskHomesAPI huskHomesAPI;

    // This performs a timed teleport to tp a player to another online player with the username "William278"
    public void teleportPlayer(Player player) {
        OnlineUser onlineUser = huskHomesAPI.adaptUser(player);
        Target targetUsername = Target.username("William278"); // Get a target by a username, who can be online on this server/a server on the network (cross-server teleport).

        try {
            huskHomesAPI.teleportBuilder()
                .teleporter(onlineUser)
                .target(targetUsername)
                .toTimedTeleport()
                .execute(); // A timed teleport will throw a TeleportationException if the player moves/takes damage during the warmup, or if the target is not found.
        } catch(TeleportationException e) {
            e.printStackTrace(); // Note that the TimedTeleport will catch internal exceptions when executing the resultant Teleport (e.g. if the teleport is to an illegal position).
        }
    }

}
```
</details>