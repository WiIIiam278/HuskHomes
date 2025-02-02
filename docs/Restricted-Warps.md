HuskHomes supports restricting warps behind permission nodes through the **Permission-Restrict Warps** feature.

## Permissions
With `permission_restrict_warps` enabled in [`config.yml`](config-files), players are required to have a corresponding `huskhomes.warp.<name>` node. Note that this is a _different permission_ from the one required to use the `/warp` command, which is `huskhomes.command.warp`.

With the `huskhomes.warp.<name>` permission node, where `<name>` is the name of the warp, users can:

* Teleport to the warp with `/warp <name>`
* View the warp in the `/warplist`, or TAB-complete the warp
* Delete, or edit warps with that name, if they have access to the respective [[commands]]

If users have permission to set warps, they will still be able to set warps with names they do not have permission for, but be advised they will not be able to edit or delete the warp once set without permission. If you're using permission restricted warps, we therefore recommend also granting the wildcard warp permission (see below).

Note that the name of the warp is case-sensitive (i.e. the `huskhomes.warp.Home` permission node is different to the `huskhomes.warp.home` permission node). 

Users with the `huskhomes.warp.*` permission will be able to use all warps.

## Configuring
To enable permission-restricted warps, set `permission_restrict_warps` to `true` under `general`. Don't forget to grant users the necessary permission nodes (`huskhomes.warp.<name>`).

<details>
<summary>Permission Restrict Warps &ndash; config.yml</summary>

```yaml
  # Whether users require a permission (huskhomes.warp.<warp_name>) to use warps
  permission_restrict_warps: false
```
</details>