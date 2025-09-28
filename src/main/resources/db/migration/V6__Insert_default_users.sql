-- Insert default users for production
-- This ensures admin and user accounts exist in production mode

-- Insert default roles if they don't exist
INSERT INTO roles (name, description, created_at, updated_at) VALUES
('ADMIN', 'Administrator with full system access', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('USER', 'Regular user with basic permissions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MODERATOR', 'Moderator with content management permissions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- Insert default admin user (password is bcrypt encoded 'admin123')
INSERT INTO users (username, email, password, first_name, last_name, enabled, created_at, updated_at) VALUES
('admin', 'admin@example.com', '$2y$05$dkAdBSqragQ5WsUjWc3UvuQUi9YCJci4aT3y13Op3P4sMrk5Xtzu.', 'Admin', 'User', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- Insert default regular user (password is bcrypt encoded 'user123')
INSERT INTO users (username, email, password, first_name, last_name, enabled, created_at, updated_at) VALUES
('user', 'user@example.com', '$2y$05$WvAsyf3F50.oFbYgFYcgQ.ncJ2fTXZxE5Al8wQYfWXddsbx0X99NS', 'Test', 'User', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- Insert default moderator user (password is bcrypt encoded 'moderator123')
INSERT INTO users (username, email, password, first_name, last_name, enabled, created_at, updated_at) VALUES
('moderator', 'moderator@example.com', '$2y$05$EgQglI0OTwpf8AqYC54zh.jAAwppZSPTwgEDXR1tfYXOxnHxQRtB6', 'Moderator', 'User', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'user' AND r.name = 'USER'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'moderator' AND r.name = 'MODERATOR'
ON CONFLICT (user_id, role_id) DO NOTHING;