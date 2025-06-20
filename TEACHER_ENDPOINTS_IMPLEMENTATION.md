# Implementación de Endpoints para Teachers - Usuarios Relacionados

## Resumen de la Implementación

Hemos implementado una solución completa para que los teachers puedan ver todos los usuarios relacionados con ellos (estudiantes y guardianes) a través de los classrooms y enrollments.

## Arquitectura Implementada

### 1. Controlador Especializado: `TeacherController`
- **Ubicación**: `src/main/java/com/gamified/application/user/controller/TeacherController.java`
- **Endpoints principales**:
  - `GET /api/teachers/my-related-users` - Obtiene todos los usuarios relacionados
  - `GET /api/teachers/my-classrooms` - Lista los classrooms del teacher
  - `GET /api/teachers/classrooms/{classroomId}/students` - Estudiantes de un classroom específico
  - `GET /api/teachers/my-stats` - Estadísticas del teacher

### 2. Servicio Especializado: `TeacherService`
- **Interfaz**: `src/main/java/com/gamified/application/user/service/TeacherService.java`  
- **Implementación**: `src/main/java/com/gamified/application/user/service/TeacherServiceImpl.java`
- **Funcionalidades**:
  - Obtención de usuarios relacionados con verificación de permisos
  - Consultas optimizadas usando las relaciones de BD
  - Estadísticas en tiempo real

### 3. DTOs Especializados
- **Ubicación**: Agregados a `UserResponseDto.java`
- **Nuevos DTOs**:
  - `TeacherRelatedUsersDto` - Respuesta completa con todos los usuarios relacionados
  - `ClassroomWithStudentsDto` - Classroom con sus estudiantes
  - `ClassroomDto` - Información básica de classroom
  - `StudentWithGuardianDto` - Estudiante con información de su guardián
  - `GuardianSummaryDto` - Resumen de guardián
  - `TeacherStatsDto` - Estadísticas del teacher

## Flujo de Datos Implementado

Basándose en el diagrama de BD, el flujo es:

```
Teacher (user) 
    ↓ 
TeacherProfile 
    ↓ 
Classroom (1:N)
    ↓ 
Enrollment (1:N)
    ↓ 
StudentProfile (N:1)
    ↓ 
GuardianProfile (N:1) 
    ↓ 
Guardian (user)
```

## Consultas SQL Implementadas

### 1. Obtener Classrooms del Teacher
```sql
SELECT c.id, c.name, c.grade, c.section, c.year, c.status, c.created_at, c.updated_at,
       COUNT(e.student_profile_id) as enrolled_count
FROM classroom c
LEFT JOIN enrollment e ON c.id = e.classroom_id
WHERE c.teacher_profile_id = ? AND c.status = 1
GROUP BY c.id, c.name, c.grade, c.section, c.year, c.status, c.created_at, c.updated_at
ORDER BY c.created_at DESC
```

### 2. Obtener Estudiantes con Guardianes
```sql
SELECT 
    u.id as student_user_id, sp.id as student_profile_id,
    u.first_name, u.last_name, u.email, u.profile_picture_url,
    sp.username, sp.birth_date, sp.points_amount,
    u.status, u.email_verified, u.last_login_at, e.joined_at,
    -- Guardian info
    gu.id as guardian_user_id, gp.id as guardian_profile_id,
    gu.first_name as guardian_first_name, gu.last_name as guardian_last_name,
    gu.email as guardian_email, gp.phone as guardian_phone,
    gu.profile_picture_url as guardian_picture, gu.last_login_at as guardian_last_login
FROM enrollment e
JOIN student_profile sp ON e.student_profile_id = sp.id
JOIN [user] u ON sp.user_id = u.id
LEFT JOIN guardian_profile gp ON sp.guardian_profile_id = gp.id
LEFT JOIN [user] gu ON gp.user_id = gu.id
WHERE e.classroom_id = ? AND e.status = 1
ORDER BY u.first_name, u.last_name
```

### 3. Estadísticas del Teacher
- Total de classrooms, estudiantes y guardianes
- Estudiantes con/sin guardián asignado
- Porcentaje de asignación de guardianes
- Usuarios activos en la última semana

## Características de Seguridad

1. **Verificación de Permisos**: Solo teachers pueden acceder a sus propios datos
2. **Validación de Ownership**: Verificación de que el classroom pertenece al teacher
3. **Autenticación JWT**: Extracción del userId del token
4. **Autorización**: Uso de `@PreAuthorize("hasRole('TEACHER')")`

## Beneficios de esta Arquitectura

### 1. Separación de Responsabilidades
- **UserController**: Operaciones generales de usuarios
- **TeacherController**: Operaciones específicas de teachers
- **InstitutionController**: Operaciones administrativas

### 2. Escalabilidad
- Servicios especializados por rol
- Consultas optimizadas con JOINs
- DTOs específicos para cada caso de uso

### 3. Mantenimiento
- Código organizado por funcionalidad
- Fácil extensión para nuevas funcionalidades
- Separación clara entre capas

## Próximos Pasos Recomendados

### 1. Implementar Tablas Faltantes
Si las tablas `classroom` y `enrollment` no existen, crear el siguiente script:

```sql
-- Tabla classroom
CREATE TABLE classroom (
    id int IDENTITY(1,1) PRIMARY KEY,
    teacher_profile_id int NOT NULL,
    grade varchar(20),
    section varchar(20),
    year varchar(9),
    name varchar(50),
    status int DEFAULT 1,
    created_at datetime2 DEFAULT GETDATE(),
    updated_at datetime2 DEFAULT GETDATE(),
    FOREIGN KEY (teacher_profile_id) REFERENCES teacher_profile(id)
);

-- Tabla enrollment
CREATE TABLE enrollment (
    id int IDENTITY(1,1) PRIMARY KEY,
    classroom_id int NOT NULL,
    student_profile_id int NOT NULL,
    joined_at datetime2 DEFAULT GETDATE(),
    status int DEFAULT 1,
    FOREIGN KEY (classroom_id) REFERENCES classroom(id),
    FOREIGN KEY (student_profile_id) REFERENCES student_profile(id)
);
```

### 2. Crear Endpoints Adicionales
- `POST /api/teachers/classrooms` - Crear classroom
- `POST /api/teachers/classrooms/{classroomId}/enroll` - Inscribir estudiante
- `DELETE /api/teachers/classrooms/{classroomId}/students/{studentId}` - Desinscribir estudiante

### 3. Crear Módulo Classroom Completo
- `ClassroomController`
- `ClassroomService` 
- `ClassroomRepository`
- Entidades `Classroom` y `Enrollment`

### 4. Implementar Notificaciones
- Notificar a guardianes cuando se inscriben estudiantes
- Notificar cambios en classrooms
- Sistema de alertas para teachers

## Testing

### Endpoints para Probar
1. **GET /api/teachers/my-related-users**
   - Requiere: Token JWT de teacher
   - Respuesta: Todos los usuarios relacionados

2. **GET /api/teachers/my-classrooms**
   - Requiere: Token JWT de teacher  
   - Respuesta: Lista de classrooms

3. **GET /api/teachers/my-stats**
   - Requiere: Token JWT de teacher
   - Respuesta: Estadísticas del teacher

### Datos de Prueba Necesarios
- Teacher con teacher_profile creado
- Classrooms asignados al teacher
- Estudiantes inscriptos en los classrooms
- Guardianes asignados a los estudiantes

## Consideraciones de Performance

1. **Consultas Optimizadas**: Uso de JOINs en lugar de consultas múltiples
2. **Índices Recomendados**:
   - `classroom.teacher_profile_id`
   - `enrollment.classroom_id`
   - `enrollment.student_profile_id`
   - `student_profile.guardian_profile_id`
3. **Paginación**: Implementar para listas grandes
4. **Cache**: Considerar cache para estadísticas

## Conclusión

Esta implementación proporciona una base sólida y escalable para que los teachers puedan visualizar y gestionar todos los usuarios relacionados con ellos. La arquitectura permite una fácil extensión y mantenimiento, mientras que las consultas optimizadas aseguran un buen rendimiento. 