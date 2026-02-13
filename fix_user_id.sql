-- Fix user_id to be AUTO_INCREMENT and set default values for fields

-- Run these commands to fix your database:

-- Fix user_id to be AUTO_INCREMENT
ALTER TABLE `user` MODIFY COLUMN `user_id` BIGINT NOT NULL AUTO_INCREMENT;

-- Set default value for verified field (0 = not verified)
ALTER TABLE `user` MODIFY COLUMN `verified` INT DEFAULT 0;

ALTER TABLE `accounts` MODIFY COLUMN `account_id` INT NOT NULL AUTO_INCREMENT;
