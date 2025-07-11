# Gamified Learning Platform

A gamified learning platform built with Spring Boot, featuring:
- Authentication system with JWT
- Role-based access control
- Integration with SQL Server database
- API documentation with OpenAPI/Swagger

## Project Structure

This project is organized into modules:
- **Auth**: User authentication and authorization
- **User**: User management and profiles
- **Classroom**: Classroom management
- **Learning**: Learning materials and progress tracking
- **Achievement**: Gamification elements

## Setup Instructions

### Prerequisites

- Java 17 or higher
- Maven
- SQL Server 2019 or higher
- SQL Server Management Studio (optional, for database management)

### Database Setup

1. Run `copy-scripts.bat` to copy SQL scripts to the resources directory
2. Run `setup-database.bat` to initialize the database
3. Follow the prompts to provide SQL Server connection details

### Application Configuration

1. Open `src/main/resources/application.properties`
2. Update database connection details
3. Set a secure JWT secret key

### Running the Application

```bash
mvn spring-boot:run
```

### Testing the API

1. Access Swagger UI: http://localhost:8080/api/swagger-ui.html
2. Test the health endpoint: http://localhost:8080/api/auth/health
3. Test database connection: http://localhost:8080/api/auth/db-test

## Security Features

- JWT-based authentication
- Password encryption with BCrypt
- Role-based access control
- Suspicious login detection
- Audit logging

## Additional Documentation

For detailed setup instructions and troubleshooting, see [setup-instructions.md](setup-instructions.md).

## Correcciones Realizadas

### Procedimientos Almacenados
- Corregidos nombres de columnas incorrectos en `auth_stored_procedures.sql` y `sp_auth_procedures.sql`
- Reemplazados `is_locked` → `status`
- Reemplazados `is_email_verified` → `email_verified`
- Reemplazados `password_hash` → `password`
- Implementada separación de flujos de autenticación:
  - Estudiantes: autenticación por nombre de usuario (username)
  - Profesores/tutores/administradores: autenticación por email
  - Ambos métodos requieren contraseña

### Scripts de Esquema
- Corregida sintaxis SQL en los archivos `schema_updates.sql` y `schema_edugaming_updates.sql`
- Reemplazadas comillas dobles por corchetes al referenciar la tabla `user`
  - Ejemplo: `ALTER TABLE "user"` → `ALTER TABLE [user]`
- En SQL Server, "user" es una palabra reservada y debe estar entre corchetes cuando se usa como nombre de tabla
- Corregido error de parámetro `WAIT_STATS_CAPTURE_MODE` no compatible

### Archivos Nuevos
- Creado `sp_authenticate_student.sql` para la autenticación específica de estudiantes
- Creado `sp_get_student_profile_by_username.sql` para consultar perfiles por nombre de usuario

### Solución a problemas de base de datos
1. **Eliminado script problemático:**
   - Eliminado `fix_user_table.sql` que intentaba "corregir" columnas que ya tenían los nombres correctos

2. **Corregidos scripts de actualización:**
   - Modificados `schema_updates.sql` y `schema_edugaming_updates.sql`:
     - Cambiadas las instrucciones para usar los nombres correctos de columnas (`password` y `email_verified`)
     - Eliminadas las instrucciones que renombraban incorrectamente las columnas
     - Reemplazadas con comprobaciones para añadir las columnas si no existen

3. **Consistencia de nombres:**
   - Se mantiene consistencia en los nombres de columnas utilizados por los procedimientos almacenados:
     - `password` (en lugar de `password_hash`)
     - `email_verified` (en lugar de `is_email_verified` o `email_verification_status`)

### Resumen de cambios en la estructura
- La tabla `user` debe tener las columnas `password` y `email_verified` con estos nombres exactos
- Los procedimientos almacenados ya hacen referencia a estos nombres de columna
- Los scripts de actualización ahora verifican y añaden estas columnas (en lugar de renombrarlas)

### Archivos Java
- Corregidas referencias en `UserDetailsServiceImpl.java`:
  - Cambiado `password_hash` → `password`
  - Cambiado `is_email_verified` → `email_verified`
  - Cambiado `is_locked` → `status`
  - Corregido uso de comillas dobles por corchetes al referenciar la tabla `user`
- Corregidas referencias en `CoreEntityMapper.java`:
  - Cambiado `password_hash` → `password` en mapeo de histórico de contraseñas
- Corregida la entidad `PasswordHistory.java`:
  - Renombrado atributo `passwordHash` → `password`
  - Actualizados getter/setter correspondientes

Con estas correcciones, se ha completado la alineación entre los nombres de columnas en la base de datos SQL Server y el código Java. Esto garantiza que tanto los procedimientos almacenados como las consultas directas desde Java utilicen los mismos nombres de columnas y la sintaxis correcta para SQL Server, evitando errores en tiempo de ejecución.