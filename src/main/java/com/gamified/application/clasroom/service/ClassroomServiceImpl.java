package com.gamified.application.clasroom.service;

import com.gamified.application.clasroom.model.dto.request.ClassroomRequestDto;
import com.gamified.application.clasroom.model.dto.response.ClassroomResponseDto;
import com.gamified.application.clasroom.model.entity.Classroom;
import com.gamified.application.clasroom.model.entity.Enrollment;
import com.gamified.application.clasroom.repository.ClassroomRepository;
import com.gamified.application.user.repository.composite.CompleteUserRepository;
import com.gamified.application.user.model.entity.composite.CompleteTeacher;
import com.gamified.application.user.model.entity.composite.CompleteStudent;
import com.gamified.application.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del servicio de gestión de aulas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final CompleteUserRepository completeUserRepository;

    // ===================================================================
    // CLASSROOM MANAGEMENT
    // ===================================================================

    @Override
    @Transactional
    public ClassroomResponseDto.ClassroomDto createClassroom(Long teacherUserId, ClassroomRequestDto.CreateClassroomRequestDto request) {
        try {
            log.info("Creating classroom for teacher {}: {}", teacherUserId, request);

            // Obtener teacher profile ID
            Integer teacherProfileId = getTeacherProfileId(teacherUserId);

            // Crear entidad Classroom
            Classroom classroom = Classroom.builder()
                    .teacherProfileId(teacherProfileId)
                    .grade(request.getGrade())
                    .section(request.getSection())
                    .year(request.getYear())
                    .name(request.getName())
                    .status(1) // Active
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // Guardar en base de datos
            Integer classroomId = classroomRepository.createClassroom(classroom);

            // Obtener el classroom creado para devolver la respuesta completa
            Optional<Classroom> createdClassroom = classroomRepository.findClassroomById(classroomId);
            if (createdClassroom.isEmpty()) {
                throw new RuntimeException("Error al recuperar el aula creada");
            }

            // Convertir a DTO de respuesta
            return convertToClassroomDto(createdClassroom.get(), 0);

        } catch (Exception e) {
            log.error("Error creating classroom for teacher {}: {}", teacherUserId, e.getMessage(), e);
            throw new RuntimeException("Error al crear el aula: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ClassroomResponseDto.ClassroomDto createClassroomByAdmin(Long adminUserId, ClassroomRequestDto.CreateClassroomRequestDto request) {
        try {
            log.info("Creating classroom by admin {}: {}", adminUserId, request);

            // Para ADMINs, necesitamos que especifiquen el teacherProfileId en el request
            // Si no se especifica, lanzamos excepción
            if (request.getTeacherProfileId() == null) {
                throw new IllegalArgumentException("El teacherProfileId es requerido para crear aulas como administrador");
            }

            // Verificar que el teacher profile existe
            // Nota: Aquí podríamos hacer una verificación adicional si es necesario
            
            // Crear entidad Classroom
            Classroom classroom = Classroom.builder()
                    .teacherProfileId(request.getTeacherProfileId())
                    .grade(request.getGrade())
                    .section(request.getSection())
                    .year(request.getYear())
                    .name(request.getName())
                    .status(1) // Active
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // Guardar en base de datos
            Integer classroomId = classroomRepository.createClassroom(classroom);

            // Obtener el classroom creado para devolver la respuesta completa
            Optional<Classroom> createdClassroom = classroomRepository.findClassroomById(classroomId);
            if (createdClassroom.isEmpty()) {
                throw new RuntimeException("Error al recuperar el aula creada");
            }

            // Convertir a DTO de respuesta
            return convertToClassroomDto(createdClassroom.get(), 0);

        } catch (Exception e) {
            log.error("Error creating classroom by admin {}: {}", adminUserId, e.getMessage(), e);
            throw new RuntimeException("Error al crear el aula: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ClassroomResponseDto.ClassroomDto> getTeacherClassrooms(Long teacherUserId) {
        try {
            log.debug("Getting classrooms for teacher {}", teacherUserId);

            Integer teacherProfileId = getTeacherProfileId(teacherUserId);
            return classroomRepository.findClassroomsByTeacher(teacherProfileId);

        } catch (Exception e) {
            log.error("Error getting classrooms for teacher {}: {}", teacherUserId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener las aulas: " + e.getMessage(), e);
        }
    }

    @Override
    public ClassroomResponseDto.ClassroomDetailDto getClassroomDetail(Long teacherUserId, Integer classroomId) {
        try {
            log.debug("Getting classroom detail {} for teacher {}", classroomId, teacherUserId);

            // Verificar ownership
            if (!verifyClassroomOwnership(teacherUserId, classroomId)) {
                throw new IllegalArgumentException("El aula no pertenece al profesor especificado");
            }

            Optional<ClassroomResponseDto.ClassroomDetailDto> detail = classroomRepository.findClassroomDetailById(classroomId);
            if (detail.isEmpty()) {
                throw new ResourceNotFoundException("Aula no encontrada con ID: " + classroomId);
            }

            return detail.get();

        } catch (Exception e) {
            log.error("Error getting classroom detail {} for teacher {}: {}", classroomId, teacherUserId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener detalles del aula: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ClassroomResponseDto.ClassroomDto updateClassroom(Long teacherUserId, Integer classroomId, ClassroomRequestDto.UpdateClassroomRequestDto request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Transactional
    public boolean deactivateClassroom(Long teacherUserId, Integer classroomId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // ===================================================================
    // STUDENT ENROLLMENT
    // ===================================================================

    @Override
    @Transactional
    public ClassroomResponseDto.EnrollmentResponseDto enrollStudent(Long teacherUserId, Integer classroomId, ClassroomRequestDto.EnrollStudentRequestDto request) {
        try {
            log.info("Enrolling student {} in classroom {} by teacher {}", request.getStudentProfileId(), classroomId, teacherUserId);

            // Verificar ownership del classroom
            if (!verifyClassroomOwnership(teacherUserId, classroomId)) {
                throw new IllegalArgumentException("El aula no pertenece al profesor especificado");
            }

            // Verificar que el classroom existe
            Optional<Classroom> classroom = classroomRepository.findClassroomById(classroomId);
            if (classroom.isEmpty()) {
                throw new ResourceNotFoundException("Aula no encontrada con ID: " + classroomId);
            }

            // Verificar que el estudiante no está ya inscrito
            if (classroomRepository.isStudentEnrolled(classroomId, request.getStudentProfileId())) {
                return ClassroomResponseDto.EnrollmentResponseDto.builder()
                        .success(false)
                        .message("El estudiante ya está inscrito en esta aula")
                        .classroomId(classroomId)
                        .studentProfileId(request.getStudentProfileId())
                        .classroomName(classroom.get().getName())
                        .build();
            }

            // Crear enrollment
            Enrollment enrollment = Enrollment.builder()
                    .classroomId(classroomId)
                    .studentProfileId(request.getStudentProfileId())
                    .joinedAt(LocalDateTime.now())
                    .status(1) // Active
                    .build();

            Integer enrollmentId = classroomRepository.enrollStudent(enrollment);

            // Obtener nombre del estudiante para la respuesta
            String studentName = getStudentName(request.getStudentProfileId());

            return ClassroomResponseDto.EnrollmentResponseDto.builder()
                    .enrollmentId(enrollmentId)
                    .classroomId(classroomId)
                    .studentProfileId(request.getStudentProfileId())
                    .studentName(studentName)
                    .classroomName(classroom.get().getName())
                    .success(true)
                    .message("Estudiante inscrito exitosamente")
                    .enrolledAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error enrolling student {} in classroom {} by teacher {}: {}", 
                    request.getStudentProfileId(), classroomId, teacherUserId, e.getMessage(), e);
            throw new RuntimeException("Error al inscribir estudiante: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ClassroomResponseDto.EnrollmentResponseDto enrollStudentByAdmin(Long adminUserId, Integer classroomId, ClassroomRequestDto.EnrollStudentRequestDto request) {
        try {
            log.info("Enrolling student {} in classroom {} by admin {}", request.getStudentProfileId(), classroomId, adminUserId);

            // Para ADMINs, NO verificamos ownership - pueden inscribir estudiantes en cualquier aula

            // Verificar que el classroom existe
            Optional<Classroom> classroom = classroomRepository.findClassroomById(classroomId);
            if (classroom.isEmpty()) {
                throw new ResourceNotFoundException("Aula no encontrada con ID: " + classroomId);
            }

            // Verificar que el estudiante no está ya inscrito
            if (classroomRepository.isStudentEnrolled(classroomId, request.getStudentProfileId())) {
                return ClassroomResponseDto.EnrollmentResponseDto.builder()
                        .success(false)
                        .message("El estudiante ya está inscrito en esta aula")
                        .classroomId(classroomId)
                        .studentProfileId(request.getStudentProfileId())
                        .classroomName(classroom.get().getName())
                        .build();
            }

            // Crear enrollment
            Enrollment enrollment = Enrollment.builder()
                    .classroomId(classroomId)
                    .studentProfileId(request.getStudentProfileId())
                    .joinedAt(LocalDateTime.now())
                    .status(1) // Active
                    .build();

            Integer enrollmentId = classroomRepository.enrollStudent(enrollment);

            // Obtener nombre del estudiante para la respuesta
            String studentName = getStudentName(request.getStudentProfileId());

            return ClassroomResponseDto.EnrollmentResponseDto.builder()
                    .enrollmentId(enrollmentId)
                    .classroomId(classroomId)
                    .studentProfileId(request.getStudentProfileId())
                    .studentName(studentName)
                    .classroomName(classroom.get().getName())
                    .success(true)
                    .message("Estudiante inscrito exitosamente por administrador")
                    .enrolledAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error enrolling student {} in classroom {} by admin {}: {}", 
                    request.getStudentProfileId(), classroomId, adminUserId, e.getMessage(), e);
            throw new RuntimeException("Error al inscribir estudiante: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ClassroomResponseDto.StudentInClassroomDto> getClassroomStudents(Long teacherUserId, Integer classroomId) {
        try {
            log.debug("Getting students for classroom {} by teacher {}", classroomId, teacherUserId);

            // Verificar ownership
            if (!verifyClassroomOwnership(teacherUserId, classroomId)) {
                throw new IllegalArgumentException("El aula no pertenece al profesor especificado");
            }

            return classroomRepository.findStudentsByClassroom(classroomId);

        } catch (Exception e) {
            log.error("Error getting students for classroom {} by teacher {}: {}", classroomId, teacherUserId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener estudiantes del aula: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public boolean unenrollStudent(Long teacherUserId, Integer classroomId, Integer studentProfileId) {
        try {
            log.info("Unenrolling student {} from classroom {} by teacher {}", studentProfileId, classroomId, teacherUserId);

            // Verificar ownership del classroom
            if (!verifyClassroomOwnership(teacherUserId, classroomId)) {
                throw new IllegalArgumentException("El aula no pertenece al profesor especificado");
            }

            // Verificar que el estudiante está inscrito
            if (!classroomRepository.isStudentEnrolled(classroomId, studentProfileId)) {
                throw new IllegalArgumentException("El estudiante no está inscrito en esta aula");
            }

            // Desinscribir estudiante
            return classroomRepository.unenrollStudent(classroomId, studentProfileId);

        } catch (Exception e) {
            log.error("Error unenrolling student {} from classroom {} by teacher {}: {}", 
                    studentProfileId, classroomId, teacherUserId, e.getMessage(), e);
            throw new RuntimeException("Error al desinscribir estudiante: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public boolean unenrollStudentByAdmin(Long adminUserId, Integer classroomId, Integer studentProfileId) {
        try {
            log.info("Unenrolling student {} from classroom {} by admin {}", studentProfileId, classroomId, adminUserId);

            // Para ADMINs, NO verificamos ownership - pueden desinscribir estudiantes de cualquier aula

            // Verificar que el classroom existe
            Optional<Classroom> classroom = classroomRepository.findClassroomById(classroomId);
            if (classroom.isEmpty()) {
                throw new ResourceNotFoundException("Aula no encontrada con ID: " + classroomId);
            }

            // Verificar que el estudiante está inscrito
            if (!classroomRepository.isStudentEnrolled(classroomId, studentProfileId)) {
                throw new IllegalArgumentException("El estudiante no está inscrito en esta aula");
            }

            // Desinscribir estudiante
            return classroomRepository.unenrollStudent(classroomId, studentProfileId);

        } catch (Exception e) {
            log.error("Error unenrolling student {} from classroom {} by admin {}: {}", 
                    studentProfileId, classroomId, adminUserId, e.getMessage(), e);
            throw new RuntimeException("Error al desinscribir estudiante: " + e.getMessage(), e);
        }
    }

    // ===================================================================
    // STUDENT OPERATIONS
    // ===================================================================

    @Override
    public List<ClassroomResponseDto.ClassroomDto> getStudentClassrooms(Long studentUserId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // ===================================================================
    // STATISTICS AND ANALYTICS
    // ===================================================================

    @Override
    public ClassroomResponseDto.ClassroomStatsDto getClassroomStats(Long teacherUserId, Integer classroomId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // ===================================================================
    // VALIDATION METHODS
    // ===================================================================

    @Override
    public boolean verifyClassroomOwnership(Long teacherUserId, Integer classroomId) {
        try {
            Integer teacherProfileId = getTeacherProfileId(teacherUserId);
            return classroomRepository.verifyClassroomOwnership(classroomId, teacherProfileId);
        } catch (Exception e) {
            log.error("Error verifying classroom ownership for teacher {} and classroom {}: {}", 
                    teacherUserId, classroomId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isStudentEnrolled(Integer classroomId, Integer studentProfileId) {
        return classroomRepository.isStudentEnrolled(classroomId, studentProfileId);
    }

    // ===================================================================
    // PRIVATE HELPER METHODS
    // ===================================================================

    private Integer getTeacherProfileId(Long teacherUserId) {
        Optional<CompleteTeacher> teacherOpt = completeUserRepository.findCompleteTeacherById(teacherUserId);
        if (teacherOpt.isEmpty()) {
            throw new ResourceNotFoundException("Profesor no encontrado con ID: " + teacherUserId);
        }
        return teacherOpt.get().getTeacherProfile().getId().intValue();
    }

    private Integer getStudentProfileId(Long studentUserId) {
        Optional<CompleteStudent> studentOpt = completeUserRepository.findCompleteStudentById(studentUserId);
        if (studentOpt.isEmpty()) {
            throw new ResourceNotFoundException("Estudiante no encontrado con ID: " + studentUserId);
        }
        return studentOpt.get().getStudentProfile().getId().intValue();
    }

    private String getStudentName(Integer studentProfileId) {
        // Obtener el nombre real del estudiante desde la base de datos
        try {
            Optional<String> studentName = classroomRepository.findStudentNameByProfileId(studentProfileId);
            return studentName.orElse("Estudiante " + studentProfileId);
        } catch (Exception e) {
            log.warn("Error obteniendo nombre del estudiante con ID {}: {}", studentProfileId, e.getMessage());
            return "Estudiante " + studentProfileId;
        }
    }

    private ClassroomResponseDto.ClassroomDto convertToClassroomDto(Classroom classroom, Integer enrolledCount) {
        return ClassroomResponseDto.ClassroomDto.builder()
                .classroomId(classroom.getId())
                .name(classroom.getName())
                .grade(classroom.getGrade())
                .section(classroom.getSection())
                .year(classroom.getYear())
                .enrolledStudentsCount(enrolledCount)
                .status(classroom.getStatus() == 1)
                .createdAt(classroom.getCreatedAt())
                .updatedAt(classroom.getUpdatedAt())
                .build();
    }

    @Override
    public Map<String, Object> getClassroomDataByUserId(int userId){
        return classroomRepository.getClassroomDataByUserId(userId);
    }

    @Override
    public List<Map<String, Object>> getClassmatesByUserId(int userId){
        return classroomRepository.getClassmatesByUserId(userId);
    }
} 