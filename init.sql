-- Create database if not exists (this runs automatically with docker-compose)
-- Database initialization script

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- The tables will be created automatically by Exposed ORM
-- This file is mainly for any custom database setup

-- Insert initial configuration data (commented out - will be handled by app initialization)
-- Tables will be created automatically by Exposed ORM when the app starts
-- INSERT INTO config (key, value, description) VALUES 
--     ('app_version', '1.0.0', 'Application version'),
--     ('migration_status', 'pending', 'Status of data migration from Firebase'),
--     ('created_at_timestamp', EXTRACT(EPOCH FROM NOW())::TEXT, 'Database initialization timestamp')
-- ON CONFLICT (key) DO NOTHING;
