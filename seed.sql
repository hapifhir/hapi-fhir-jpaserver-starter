CREATE DATABASE qtas_extras;

\c qtas_extras;

CREATE TABLE extra_data (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    max_value INT NOT NULL,
    min_value INT NOT NULL
);

INSERT INTO extra_data (name, max_value, min_value) VALUES 
    ('test1', 100, 0),
    ('test2', 200, 100),
    ('test3', 300, 200),
    ('test4', 400, 300),
    ('test5', 500, 400);