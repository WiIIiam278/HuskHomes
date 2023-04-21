HuskHomes provides an option to enforce strict `/tpahere` requests. This alters the behaviour of the `/tpahere` command.

* Strict `/tpahere` requests **disabled**: When a player accepts a `/tpahere` request, they will be teleported to the current position of the player at the end of their warmup period.
* Strict `/tpahere` requests **enabled**: When a player accepts a `/tpahere` request, they will be teleported to the position of the player when they executed the `/tpahere` command.

You may wish to use strict `/tpahere` requests if your server makes use of contextual permission nodes to restrict where players can use `/tpahere`. You can set whether to use strict `/tpahere` requests using the `strict_tpa_here_requests` setting under `general` in the HuskHomes [`config.yml`](config-file) file.