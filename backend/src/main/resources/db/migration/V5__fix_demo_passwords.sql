UPDATE users
SET password_hash = '$2a$10$sGMcakmejN0urzRIXIVAsOhMhjDO8IKVPVbunfwFFvjipccCL/KN.'
WHERE email IN ('admin@tripcraft.test', 'tourist@tripcraft.test');
