/* Create the metadata table if it does not exist */
CREATE TABLE IF NOT EXISTS "%meta_data%"
(
    schema_version integer NOT NULL,

    PRIMARY KEY (schema_version)
);

/* Create the positions table if it does not exist */
CREATE TABLE IF NOT EXISTS "%position_data%"
(
    id          SERIAL,
    x           double precision NOT NULL,
    y           double precision NOT NULL,
    z           double precision NOT NULL,
    yaw         real             NOT NULL,
    pitch       real             NOT NULL,
    world_name  varchar(255)     NOT NULL,
    world_uuid  char(36)         NOT NULL,
    server_name varchar(255)     NOT NULL,

    PRIMARY KEY (id)
);

/* Create the players table if it does not exist */
CREATE TABLE IF NOT EXISTS "%player_data%"
(
    uuid              char(36)    NOT NULL UNIQUE,
    username          varchar(16) NOT NULL,
    last_position     integer              DEFAULT NULL,
    offline_position  integer              DEFAULT NULL,
    respawn_position  integer              DEFAULT NULL,
    home_slots        integer     NOT NULL DEFAULT 0,
    ignoring_requests boolean     NOT NULL DEFAULT FALSE,

    PRIMARY KEY (uuid),
    FOREIGN KEY (last_position) REFERENCES "%position_data%" (id) ON DELETE SET NULL,
    FOREIGN KEY (offline_position) REFERENCES "%position_data%" (id) ON DELETE SET NULL,
    FOREIGN KEY (respawn_position) REFERENCES "%position_data%" (id) ON DELETE SET NULL
);

/* Create the cooldowns table if it does not exist */
CREATE TABLE IF NOT EXISTS "%player_cooldowns_data%"
(
    id              SERIAL,
    player_uuid     char(36)     NOT NULL,
    type            varchar(255) NOT NULL,
    start_timestamp timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_timestamp   timestamp    NOT NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (player_uuid) REFERENCES "%player_data%" (uuid) ON DELETE CASCADE ON UPDATE CASCADE
);

/* Create the current cross-server teleports table if it does not exist */
CREATE TABLE IF NOT EXISTS "%teleport_data%"
(
    player_uuid    char(36) NOT NULL UNIQUE,
    destination_id integer  NOT NULL,
    type           integer  NOT NULL DEFAULT 0,

    PRIMARY KEY (player_uuid),
    FOREIGN KEY (player_uuid) REFERENCES "%player_data%" (uuid) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (destination_id) REFERENCES "%position_data%" (id) ON DELETE CASCADE
);

/* Create the saved positions table if it does not exist */
CREATE TABLE IF NOT EXISTS "%saved_position_data%"
(
    id          SERIAL,
    position_id integer      NOT NULL,
    name        varchar(64)  NOT NULL,
    description varchar(255) NOT NULL,
    tags        text                  DEFAULT NULL,
    timestamp   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    FOREIGN KEY (position_id) REFERENCES "%position_data%" (id) ON DELETE CASCADE
);

/* Create the homes table if it does not exist */
CREATE TABLE IF NOT EXISTS "%home_data%"
(
    uuid              char(36) NOT NULL UNIQUE,
    saved_position_id integer  NOT NULL,
    owner_uuid        char(36) NOT NULL,
    public            boolean  NOT NULL DEFAULT FALSE,

    PRIMARY KEY (uuid),
    FOREIGN KEY (owner_uuid) REFERENCES "%player_data%" (uuid) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (saved_position_id) REFERENCES "%saved_position_data%" (id) ON DELETE CASCADE
);

/* Create the warps table if it does not exist */
CREATE TABLE IF NOT EXISTS "%warp_data%"
(
    uuid              char(36) NOT NULL UNIQUE,
    saved_position_id integer  NOT NULL,

    PRIMARY KEY (uuid),
    FOREIGN KEY (saved_position_id) REFERENCES "%saved_position_data%" (id) ON DELETE CASCADE
);