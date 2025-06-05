-- Define the schema for example_table
DROP TABLE IF EXISTS example_table;

CREATE TABLE example_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    data VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- You can add other tables or initial data here if needed for jOOQ generation
-- or for the application itself if this script is also used for runtime initialization.
-- For jOOQ generation, only the table structure (DDL) is essential.
