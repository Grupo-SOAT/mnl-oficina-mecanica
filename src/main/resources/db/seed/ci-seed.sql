CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    user_name VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role TEXT[]
);

TRUNCATE TABLE users RESTART IDENTITY CASCADE;

INSERT INTO users (user_id, user_name, password, role) VALUES
(
    1,
    'admin',
    '$2a$10$IETgyptrfhwJ11oTxUZEf.JAW/km7JpgmN8g51oAp4clPB8LOWCPa',
    ARRAY['ADMIN']
);

SELECT setval(
    pg_get_serial_sequence('users', 'user_id'),
    (SELECT MAX(user_id) FROM users)
);
