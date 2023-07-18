/* Create the positions table if it does not exist */
CREATE TABLE IF NOT EXISTS `%positions_table%`
(
    `id`          INT          NOT NULL,
    `x`           DOUBLE       NOT NULL,
    `y`           DOUBLE       NOT NULL,
    `z`           DOUBLE       NOT NULL,
    `yaw`         REAL         NOT NULL,
    `pitch`       REAL         NOT NULL,
    `world_name`  VARCHAR(255) NOT NULL,
    `world_uuid`  UUID         NOT NULL,
    `server_name` VARCHAR(255) NOT NULL,

    PRIMARY KEY (`id`)
);

/* Create the players table if it does not exist */
CREATE TABLE IF NOT EXISTS `%players_table%`
(
    `uuid`              UUID        NOT NULL PRIMARY KEY,
    `username`          VARCHAR(16) NOT NULL,
    `last_position`     INT         NULL,
    `offline_position`  INT         NULL,
    `respawn_position`  INT         NULL,
    `home_slots`        INT         NOT NULL DEFAULT 0,
    `ignoring_requests` BOOLEAN     NOT NULL DEFAULT FALSE,

    FOREIGN KEY (`last_position`) REFERENCES `%positions_table%` (`id`) ON DELETE SET NULL ON UPDATE NO ACTION,
    FOREIGN KEY (`offline_position`) REFERENCES `%positions_table%` (`id`) ON DELETE SET NULL ON UPDATE NO ACTION,
    FOREIGN KEY (`respawn_position`) REFERENCES `%positions_table%` (`id`) ON DELETE SET NULL ON UPDATE NO ACTION
);

/* Create the cooldowns table if it does not exist */
CREATE TABLE IF NOT EXISTS `%cooldowns_table%`
(
    `id`              INT          NOT NULL,
    `player_uuid`     UUID         NOT NULL,
    `type`            VARCHAR(255) NOT NULL,
    `start_timestamp` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `end_timestamp`   TIMESTAMP    NOT NULL,

    PRIMARY KEY (`id`),
    FOREIGN KEY (`player_uuid`) REFERENCES `%players_table%` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
);

/* Create the current cross-server teleports table if it does not exist */
CREATE TABLE IF NOT EXISTS `%teleports_table%`
(
    `player_uuid`    UUID NOT NULL PRIMARY KEY,
    `destination_id` INT  NOT NULL,
    `type`           INT  NOT NULL DEFAULT 0,

    FOREIGN KEY (`player_uuid`) REFERENCES `%players_table%` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`destination_id`) REFERENCES `%positions_table%` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
);

/* Create the saved positions table if it does not exist */
CREATE TABLE IF NOT EXISTS `%saved_positions_table%`
(
    `id`          INT          NOT NULL,
    `position_id` INT          NOT NULL,
    `name`        VARCHAR(64)  NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    `tags`        CLOB         NULL,
    `timestamp`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    FOREIGN KEY (`position_id`) REFERENCES `%positions_table%` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
);

/* Create the homes table if it does not exist */
CREATE TABLE IF NOT EXISTS `%homes_table%`
(
    `uuid`              UUID    NOT NULL PRIMARY KEY,
    `saved_position_id` INT     NOT NULL,
    `owner_uuid`        UUID    NOT NULL,
    `public`            BOOLEAN NOT NULL DEFAULT FALSE,

    FOREIGN KEY (`owner_uuid`) REFERENCES `%players_table%` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`saved_position_id`) REFERENCES `%saved_positions_table%` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
);

/* Create the warps table if it does not exist */
CREATE TABLE IF NOT EXISTS `%warps_table%`
(
    `uuid`              UUID NOT NULL PRIMARY KEY,
    `saved_position_id` INT  NOT NULL,

    FOREIGN KEY (`saved_position_id`) REFERENCES `%saved_positions_table%` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
);
