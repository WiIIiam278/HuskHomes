/* Create the metadata table if it does not exist */
CREATE TABLE IF NOT EXISTS "%meta_data%"
(
    schema_version integer NOT NULL,

    PRIMARY KEY (schema_version)
);