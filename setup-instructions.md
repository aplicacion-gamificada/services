# Setup Instructions for Gamified Learning Platform

## Database Setup

1. **Copy SQL Scripts**:
   - Run the `copy-scripts.bat` batch file to copy all SQL scripts to the correct location.

2. **Set Up Database**:
   - Run the `setup-database.bat` batch file to set up the SQL Server database.
   - Enter your SQL Server connection details when prompted.

3. **Verify Database Setup**:
   - Check that the `tesis` database has been created.
   - Confirm that all tables and stored procedures exist.

## Application Configuration

1. **Update Database Connection**:
   - Open `src/main/resources/application.properties`.
   - Update the database connection properties with your actual values:
     ```properties
     spring.datasource.url=jdbc:sqlserver://your-server:1433;databaseName=tesis;encrypt=false
     spring.datasource.username=your-username
     spring.datasource.password=your-password
     ```

2. **Update JWT Secret**:
   - In the same file, replace the JWT secret with a strong, unique value:
     ```properties
     jwt.secret=your-actual-256-bit-secret-key-for-production
     ```

## Running the Application

1. **Build the Application**:
   ```bash
   mvn clean package
   ```

2. **Run the Application**:
   ```bash
   mvn spring-boot:run
   ```

3. **Access Swagger UI**:
   - Open your browser and navigate to: http://localhost:8080/api/swagger-ui.html
   - This will display all available API endpoints for testing.

## Test Endpoints

The following test endpoints are available to verify functionality:

1. **Health Check**:
   - `GET /api/auth/health`
   - Verifies if the auth service is running properly.

2. **Database Connection Test**:
   - `GET /api/auth/db-test`
   - Tests the connection to the database.

3. **Stored Procedure Test**:
   - `GET /api/auth/sp-test`
   - Tests if stored procedures are working.

## Implementation Summary

### Completed Items

1. **Database Configuration**:
   - Created SQL scripts for database schema
   - Implemented stored procedures for authentication
   - Added schema update scripts to ensure compatibility

2. **Security Configuration**:
   - Implemented JWT-based authentication
   - Created security filters and configurations
   - Set up CORS and other security settings

3. **OpenAPI/Swagger Integration**:
   - Added OpenAPI documentation
   - Configured Swagger UI for API exploration

### Items That Need Attention

1. **Schema/Stored Procedure Compatibility**:
   - The database schema and stored procedures have some column name discrepancies.
   - Schema updates script attempts to resolve these, but manual verification is needed.

2. **User Details Implementation**:
   - A basic implementation exists, but needs to be integrated with the authentication flow.

3. **Integration with Frontend**:
   - Need to test with the actual frontend application.

## Troubleshooting

1. **Database Connection Issues**:
   - Verify SQL Server is running and accessible.
   - Check connection string, username, and password.
   - Ensure the firewall allows connections on the SQL Server port.

2. **Authentication Issues**:
   - Check that the JWT token is being properly generated and validated.
   - Verify user details are being correctly loaded from the database.

3. **API Issues**:
   - Use Swagger UI to test API endpoints.
   - Check server logs for detailed error messages. 