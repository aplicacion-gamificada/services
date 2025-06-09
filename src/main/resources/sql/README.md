# SQL Scripts - Fixes and Improvements

## Overview of Changes

This directory contains SQL scripts for creating and initializing the database for the EduGaming platform. Several fixes have been applied to address syntax errors and schema mismatches.

## Files and Purpose

- `schema_edugaming_create.sql`: Main schema creation script (renamed from db-tesis_create-needFix.sql)
- `schema_edugaming_updates.sql`: Contains updates to fix specific syntax errors, particularly the WAIT_STATS_CAPTURE_MODE issue
- `sp_auth_procedures.sql`: Stored procedures for authentication (fixed to match actual database schema)
- `init_edugaming_db.sql`: Initialization script that sets up the execution order

## Key Fixes Applied

1. **Fixed Stored Procedures**:
   - Corrected field references in stored procedures to match the actual database schema
   - Changed references to use the correct column names:
     - `is_locked` → `status` and `account_locked_until`
     - `is_email_verified` → `email_verified`
     - `password_hash` → `password`
   - Fixed profile creation procedures to match actual table fields

2. **Separate Authentication Flows**:
   - Added separate authentication procedures based on user roles:
     - `sp_authenticate_user_complete`: Email-based authentication for teachers, guardians, and admins
     - `sp_authenticate_student`: Username-based authentication specifically for students
   - Each procedure validates the appropriate credentials and returns user details upon success

3. **Fixed QUERY_STORE Syntax**:
   - Removed the incompatible `WAIT_STATS_CAPTURE_MODE` parameter in `schema_edugaming_updates.sql`

4. **Fixed Initialization Scripts**:
   - Updated `init_edugaming_db.sql` to reference the correct SQL files
   - Fixed syntax by replacing GO statements with semicolons where appropriate

## Authentication Process

The authentication system now has two separate flows:

1. **Student Authentication**:
   - Students authenticate using their username (stored in the student_profile table)
   - This separation provides better security as student usernames are separate from email accounts
   - On successful authentication, the student profile ID is also returned

2. **Staff/Guardian Authentication**:
   - Teachers, guardians, and administrators authenticate using their email address
   - The procedure validates that the user is not a student (students must use the student authentication flow)
   - Email verification is required for these users

## Usage Instructions

1. Run `init_edugaming_db.sql` first to create the database
2. Execute the following scripts in order:
   - `schema_edugaming_create.sql`
   - `schema_edugaming_updates.sql`
   - `sp_auth_procedures.sql`

## Notes

- The database name is `tesis`
- All tables are created in the `dbo` schema
- The scripts have been tested with Microsoft SQL Server 