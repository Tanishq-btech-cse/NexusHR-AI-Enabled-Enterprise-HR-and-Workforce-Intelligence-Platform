-- 1. Ensure the app_users record exists
INSERT INTO app_users (id, email, password_hash, enabled, created_at, updated_at)
VALUES ('3f8b89c2-5b94-4b82-901d-7201b1de31c0', 'admin@nexushr.local', '$2a$10$vXG2wW7YshNms14gA2B3yeP8fG1kQ2VvjS9I1C0VpIuXG65f2zSFe', true, NOW(), NOW())
    ON CONFLICT (email) DO NOTHING;

-- 2. Fetch the user's ID dynamically and bind the ADMIN role to it
INSERT INTO app_user_roles (user_id, role)
SELECT id, 'ADMIN' FROM app_users WHERE LOWER(email) = 'admin@nexushr.local'
    ON CONFLICT DO NOTHING;