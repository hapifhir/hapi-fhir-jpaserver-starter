CREATE TABLE extra_data (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    max_value INT NOT NULL,
    min_value INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO extra_data (name, max_value, min_value) VALUES 
    ('test1', 100, 0),
    ('test2', 200, 100),
    ('test3', 300, 200),
    ('test4', 400, 300),
    ('test5', 500, 400);