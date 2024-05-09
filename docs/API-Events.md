HuskHomes provides a number of API events your plugin can listen to when certain actions are performed. Most of these events are cancellable, letting you stop them from executing if you wish.

## List of API events
| Bukkit Event class             | Since | Cancellable | Description                                                              |
|--------------------------------|:-----:|:-----------:|--------------------------------------------------------------------------|
| `HomeCreateEvent`              |  4.0  |      ✅      | Called when a player sets a home                                         |
| `HomeListEvent`                |  3.0  |      ✅      | Called when a player requests to view a list of homes / public homes     |
| `HomeEditEvent`                |  4.0  |      ✅      | Called when a player edits a home (privacy, relocate, description, name) |
| `HomeDeleteEvent`              |  3.0  |      ✅      | Called when a player deletes a home&dagger;                              |
| `DeleteAllHomesEvent`          | 3.2.1 |      ✅      | Called when a player uses `/delhome all` to delete all their homes       |
| `WarpCreateEvent`              |  4.0  |      ✅      | Called when a player sets a warp                                         |
| `WarpListEvent`                |  3.0  |      ✅      | Called when a player requests to view a list of warps                    |
| `WarpEditEvent`                |  4.0  |      ✅      | Called when a player edits a warp (relocate, description, name)          |
| `WarpDeleteEvent`              |  3.0  |      ✅      | Called when a player deletes a warp&dagger;                              |
| `DeleteAllWarpsEvent`          | 3.2.1 |      ✅      | Called when a player uses `/delwarp all` to delete all warps             |
| `SendTeleportRequestEvent`     |  4.1  |      ✅      | Called when a player sends a teleport request (`/tpa`)                   |
| `ReceiveTeleportRequestEvent`  |  4.1  |      ✅      | Called when a player receives a teleport request from someone            |
| `ReplyTeleportRequestEvent`    |  4.1  |      ✅      | Called when a player accepts or declines a teleport request              |
| `TeleportWarmupEvent`          |  3.0  |      ✅      | Called when a player starts a teleport warmup countdown                  |
| `TeleportWarmupCancelledEvent` | 4.6.3 |      ✅      | Called when a player cancels the teleport warmup                         |
| `TeleportEvent`                |  3.0  |      ✅      | Called when a player is teleported&ddagger;                              |
| `TeleportBackEvent`            |  4.1  |      ✅      | Called when a player teleports to their last position (`/back`)&ddagger; |

&dagger; If the player uses `/delhome all` or `/delwarp all` to delete all their homes or all the warps, a single `DeleteAllHomesEvent` or `DeleteAllWarpsEvent` is fired instead.
&ddagger; Called on the server the player *is teleported from*; not necessarily where the executor of the teleport is.

## Events on Sponge & Fabric
> **Note:** Check the [[API]] introduction for details on targeting platforms

Sponge, which has a similar Event api as Bukkit, has equivalent events for all of the above, prefixed with `Sponge` (so HomeCreateEvent on Bukkit is SpongeHomeCreateEvent on Sponge).

Fabric uses callbacks for handling events. HuskHomes provides callback equivalents for all the above events. For instance, to handle what to do when someone creates a home, you can register a callback to handle what to do as follows:
<details>
<summary>HuskHomes and Fabric callbacks</summary>

```java
HomeCreateCallback.EVENT.register((player, home) -> {
    // Do something with the player and home
    return ActionResult.SUCCESS; // Return an appropriate ActionResult
});.
```
</details>