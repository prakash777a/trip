-- UserEntity maps role as a String, so keep the MySQL column compatible with
-- Hibernate schema validation while preserving the existing role values.
ALTER TABLE users
    MODIFY role VARCHAR(20) NOT NULL DEFAULT 'ROLE_TOURIST';
