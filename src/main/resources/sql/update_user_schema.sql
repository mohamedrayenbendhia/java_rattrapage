-- Update User table schema to match new field names
-- This script updates the existing user table structure

-- Check if columns exist before renaming to avoid errors
SET @sql = '';

-- Rename 'nom' to 'name' if it exists
SELECT COUNT(*) INTO @cnt FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'nom';
IF @cnt > 0 THEN
    SET @sql = 'ALTER TABLE user CHANGE COLUMN nom name VARCHAR(255)';
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END IF;

-- Rename 'telephone' to 'phone_number' if it exists
SELECT COUNT(*) INTO @cnt FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'telephone';
IF @cnt > 0 THEN
    SET @sql = 'ALTER TABLE user CHANGE COLUMN telephone phone_number VARCHAR(8)';
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END IF;

-- Rename 'imageUrl' to 'image' if it exists
SELECT COUNT(*) INTO @cnt FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'imageUrl';
IF @cnt > 0 THEN
    SET @sql = 'ALTER TABLE user CHANGE COLUMN imageUrl image VARCHAR(500)';
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END IF;

-- Drop columns that are no longer needed if they exist
SELECT COUNT(*) INTO @cnt FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'prenom';
IF @cnt > 0 THEN
    ALTER TABLE user DROP COLUMN prenom;
END IF;

SELECT COUNT(*) INTO @cnt FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'adresse';
IF @cnt > 0 THEN
    ALTER TABLE user DROP COLUMN adresse;
END IF;

SELECT COUNT(*) INTO @cnt FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'role';
IF @cnt > 0 THEN
    ALTER TABLE user DROP COLUMN role;
END IF;

-- Add validation constraints (if they don't already exist)
-- Note: MySQL will give an error if constraint already exists, so we need to check first
-- For simplicity, you may need to run these manually if constraints already exist

-- Phone number validation (exactly 8 digits)
-- ALTER TABLE user ADD CONSTRAINT chk_phone_number CHECK (phone_number REGEXP '^[0-9]{8}$');

-- Name length validation (3-20 characters)
-- ALTER TABLE user ADD CONSTRAINT chk_name_length CHECK (CHAR_LENGTH(name) >= 3 AND CHAR_LENGTH(name) <= 20);
