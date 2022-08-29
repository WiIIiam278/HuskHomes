/* SQLite migration script. */

-- Clear target database.
DELETE
FROM `%target_positions_table%`;
DELETE
FROM `%target_users_table%`;

-- Migrate positions to the new schema
INSERT INTO `%target_positions_table%` (`id`, `x`, `y`, `z`, `yaw`, `pitch`, `world_name`, `world_uuid`, `server_name`)
SELECT `location_id`                          AS `id`,
       `x`,
       `y`,
       `z`,
       `yaw`,
       `pitch`,
       `world`                                AS `world_name`,
       '00000000-0000-0000-0000-000000000000' AS `world_uuid`,
       `server`                               AS `server_name`
FROM `%source_positions_table%`;

-- Migrate users to the new schema
INSERT INTO `%target_users_table%` (`uuid`, `username`, `last_position`, `offline_position`, `home_slots`,
                                    `ignoring_requests`)
SELECT `user_uuid`            AS `uuid`,
       `username`,
       `last_location_id`     AS `last_position`,
       `offline_location_id`  AS `offline_position`,
       `home_slots`,
       `is_ignoring_requests` AS `ignoring_requests`
FROM `%source_users_table%`;

-- Migrate homes to the new schema
INSERT INTO `%target_saved_positions%` (`position_id`, `name`, `description`, `timestamp`)
SELECT `location_id`   AS `position_id`,
       `name`,
       `description`,
       `creation_time` AS `timestamp`
FROM `%source_homes_table%`;

INSERT INTO `%target_homes_table%` (`uuid`, `saved_position_id`, `owner_uuid`, `public`)
SELECT lower(hex(randomblob(4)) || '-' || hex(randomblob(2)) || '-' || '4' ||
             substr(hex(randomblob(2)), 2) || '-' ||
             substr('AB89', 1 + (abs(random()) % 4), 1) ||
             substr(hex(randomblob(2)), 2) || '-' ||
             hex(randomblob(6))) AS `uuid`,
       `id`                      AS `saved_position_id`,
       `user_uuid`               AS `owner_uuid`,
       `public`
FROM `%source_homes_table%`
         INNER JOIN `%target_saved_positions%`
                    ON `%source_homes_table%`.`location_id` = `%target_saved_positions%`.`position_id`
         INNER JOIN `%source_users_table%`
                    ON `%source_homes_table%`.`player_id` = `%source_users_table%`.`player_id`;

-- Migrate warps to the new schema
INSERT INTO `%target_saved_positions%` (`position_id`, `name`, `description`, `timestamp`)
SELECT `location_id`   AS `position_id`,
       `name`,
       `description`,
       `creation_time` AS `timestamp`
FROM `%source_warps_table%`;

INSERT INTO `%target_warps_table%` (`uuid`, `saved_position_id`)
SELECT lower(hex(randomblob(4)) || '-' || hex(randomblob(2)) || '-' || '4' ||
             substr(hex(randomblob(2)), 2) || '-' ||
             substr('AB89', 1 + (abs(random()) % 4), 1) ||
             substr(hex(randomblob(2)), 2) || '-' ||
             hex(randomblob(6))) AS `uuid`,
       `id`                      AS `saved_position_id`
FROM `%source_warps_table%`
         INNER JOIN `%target_saved_positions%`
                    ON `%source_warps_table%`.`location_id` = `%target_saved_positions%`.`position_id`;

-- Rename old tables
ALTER TABLE `%source_positions_table%`
    RENAME TO `old_%source_positions_table%`;
ALTER TABLE `%source_users_table%`
    RENAME TO `old_%source_users_table%`;
ALTER TABLE `%source_homes_table%`
    RENAME TO `old_%source_homes_table%`;
ALTER TABLE `%source_warps_table%`
    RENAME TO `old_%source_warps_table%`;