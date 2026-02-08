-- Fix user_id to be AUTO_INCREMENT and set default values for fields

-- Run these commands to fix your database:

-- Fix user_id to be AUTO_INCREMENT
ALTER TABLE `user` MODIFY COLUMN `user_id` BIGINT NOT NULL AUTO_INCREMENT;

-- Set default value for verified field (0 = not verified)
ALTER TABLE `user` MODIFY COLUMN `verified` INT DEFAULT 0;

-- Set default value for create_at to current timestamp
ALTER TABLE `user` MODIFY COLUMN `create_at` DATETIME DEFAULT CURRENT_TIMESTAMP;

-- Set other timestamp fields to allow NULL
ALTER TABLE `user` MODIFY COLUMN `verified_at` DATE NULL DEFAULT NULL;
ALTER TABLE `user` MODIFY COLUMN `updated_at` DATETIME NULL DEFAULT NULL;
