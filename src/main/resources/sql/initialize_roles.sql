-- Initialize roles in the database
-- This script creates the necessary roles and updates existing role references

-- Create roles table if it doesn't exist
CREATE TABLE IF NOT EXISTS roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert the three main roles
INSERT IGNORE INTO roles (name, description) VALUES
('ROLE_SUPER_ADMIN', 'Super Administrator with full system access'),
('ROLE_ADMIN', 'Administrator with management privileges'),
('ROLE_USER', 'Regular user with basic access');

-- Update existing role references in user_roles table if it exists
-- Replace old role names with new ones
UPDATE user_roles ur 
JOIN roles r ON ur.role_id = r.id 
SET ur.role_id = (SELECT id FROM roles WHERE name = 'ROLE_USER' LIMIT 1)
WHERE r.name IN ('CLIENT', 'client');

UPDATE user_roles ur 
JOIN roles r ON ur.role_id = r.id 
SET ur.role_id = (SELECT id FROM roles WHERE name = 'ROLE_ADMIN' LIMIT 1)
WHERE r.name IN ('ADMIN', 'admin');

UPDATE user_roles ur 
JOIN roles r ON ur.role_id = r.id 
SET ur.role_id = (SELECT id FROM roles WHERE name = 'ROLE_SUPER_ADMIN' LIMIT 1)
WHERE r.name IN ('SUPER_ADMIN', 'super_admin');

-- Remove old role entries that are no longer needed
DELETE FROM roles WHERE name IN ('CLIENT', 'client', 'ADMIN', 'admin', 'SUPER_ADMIN', 'super_admin', 'organizateur', 'ORGANISATEUR');
