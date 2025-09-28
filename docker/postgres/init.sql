-- Initialize database for demo-spring application
-- This script runs when PostgreSQL container starts for the first time

-- Create additional databases if needed
-- CREATE DATABASE demo_spring_test;

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE demo_spring_prod TO demo_user;

-- Create extensions if needed
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";