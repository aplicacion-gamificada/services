#!/usr/bin/env node

// =============================================================================
// TEST SCRIPT PARA UserController CON AUTENTICACIÓN (Node.js)
// =============================================================================

const AuthHelper = require('../../utils/auth-helper');

// Verificar si fetch está disponible
if (typeof fetch === 'undefined') {
    console.log('❌ Error: Este script requiere Node.js 18+ que incluye fetch.');
    process.exit(1);
}

// Contadores
let totalTests = 0;
let passedTests = 0;
let failedTests = 0;

// Función para logging de resultados
function writeTestResult(testName, passed, expected, actual, details = '') {
    totalTests++;
    
    if (passed) {
        passedTests++;
        console.log(`✅ PASS: ${testName}`);
    } else {
        failedTests++;
        console.log(`❌ FAIL: ${testName}`);
        console.log(`   Expected: ${expected}`);
        console.log(`   Actual: ${actual}`);
        if (details) {
            console.log(`   Details: ${details}`);
        }
    }
    console.log('');
}

async function runUserManagementTests() {
    console.log('🚀 INICIANDO TESTS DE USER MANAGEMENT CON AUTENTICACIÓN\n');
    
    // =============================================================================
    // PASO 1: OBTENER TOKENS DE AUTENTICACIÓN
    // =============================================================================
    
    console.log('🔐 OBTENIENDO TOKENS DE AUTENTICACIÓN...');
    
    const users = AuthHelper.getTestUsers();
    
    // Login como estudiante
    const studentAuth = await AuthHelper.login(users.student.email, users.student.password, users.student.type);
    if (!studentAuth.success) {
        console.log('❌ Error: No se pudo autenticar estudiante. Abortando tests.');
        return;
    }
    
    // Login como profesor
    const teacherAuth = await AuthHelper.login(users.teacher.email, users.teacher.password, users.teacher.type);
    if (!teacherAuth.success) {
        console.log('⚠️  Warning: No se pudo autenticar profesor. Algunos tests serán saltados.');
    }
    
    console.log('✅ Autenticación completada. Iniciando tests...\n');
    
    // =============================================================================
    // TESTS DE PERFIL DE USUARIO
    // =============================================================================
    
    console.log('👤 TESTS DE PERFIL DE USUARIO');
    console.log('='.repeat(50));
    
    // Test 1: Obtener perfil actual (estudiante)
    console.log('📋 Test: GET /api/users/profile - Obtener perfil del estudiante actual');
    const profileResponse = await AuthHelper.makeAuthenticatedRequest(
        'GET', 
        '/api/users/profile', 
        studentAuth.token
    );
    
    const profilePassed = profileResponse.success && profileResponse.statusCode === 200;
    writeTestResult(
        'Get Current Student Profile',
        profilePassed,
        '200',
        profileResponse.statusCode,
        profilePassed ? 'Perfil obtenido exitosamente' : JSON.stringify(profileResponse.data)
    );
    
    let studentUserId = null;
    if (profilePassed && profileResponse.data && profileResponse.data.id) {
        studentUserId = profileResponse.data.id;
        console.log(`ℹ️  Student User ID obtenido: ${studentUserId}`);
    }
    
    // Test 2: Obtener perfil específico de estudiante
    if (studentUserId) {
        console.log(`📋 Test: GET /api/users/students/${studentUserId} - Obtener perfil específico`);
        const specificProfileResponse = await AuthHelper.makeAuthenticatedRequest(
            'GET',
            `/api/users/students/${studentUserId}`,
            studentAuth.token
        );
        
        const specificProfilePassed = specificProfileResponse.success && specificProfileResponse.statusCode === 200;
        writeTestResult(
            'Get Specific Student Profile',
            specificProfilePassed,
            '200',
            specificProfileResponse.statusCode,
            specificProfilePassed ? 'Perfil específico obtenido' : JSON.stringify(specificProfileResponse.data)
        );
    }
    
    // =============================================================================
    // TESTS DE ACTUALIZACIÓN DE PERFIL
    // =============================================================================
    
    console.log('📝 TESTS DE ACTUALIZACIÓN DE PERFIL');
    console.log('='.repeat(50));
    
    // Test 3: Actualizar perfil de estudiante
    if (studentUserId) {
        console.log(`📋 Test: PUT /api/users/students/${studentUserId} - Actualizar perfil`);
        const updateData = {
            firstName: "Ana Actualizada",
            lastName: "Estudiante Test",
            profilePictureUrl: "https://example.com/new-avatar.jpg"
        };
        
        const updateResponse = await AuthHelper.makeAuthenticatedRequest(
            'PUT',
            `/api/users/students/${studentUserId}`,
            studentAuth.token,
            updateData
        );
        
        const updatePassed = updateResponse.success && updateResponse.statusCode === 200;
        writeTestResult(
            'Update Student Profile',
            updatePassed,
            '200',
            updateResponse.statusCode,
            updatePassed ? 'Perfil actualizado exitosamente' : JSON.stringify(updateResponse.data)
        );
    }
    
    // Test 4: Actualizar foto de perfil
    if (studentUserId) {
        console.log(`📋 Test: PUT /api/users/${studentUserId}/profile-picture - Actualizar foto`);
        const pictureUpdateResponse = await AuthHelper.makeAuthenticatedRequest(
            'PUT',
            `/api/users/${studentUserId}/profile-picture`,
            studentAuth.token,
            "https://example.com/updated-avatar.jpg"
        );
        
        const pictureUpdatePassed = pictureUpdateResponse.success && pictureUpdateResponse.statusCode === 200;
        writeTestResult(
            'Update Profile Picture',
            pictureUpdatePassed,
            '200',
            pictureUpdateResponse.statusCode,
            pictureUpdatePassed ? 'Foto actualizada exitosamente' : JSON.stringify(pictureUpdateResponse.data)
        );
    }
    
    // =============================================================================
    // TESTS DE BÚSQUEDA Y LISTADO
    // =============================================================================
    
    console.log('🔍 TESTS DE BÚSQUEDA Y LISTADO');
    console.log('='.repeat(50));
    
    // Test 5: Buscar usuarios (solo si tenemos token de profesor)
    if (teacherAuth && teacherAuth.success) {
        console.log('📋 Test: GET /api/users/search - Buscar usuarios');
        const searchResponse = await AuthHelper.makeAuthenticatedRequest(
            'GET',
            '/api/users/search?searchTerm=test&limit=10',
            teacherAuth.token
        );
        
        const searchPassed = searchResponse.success;
        writeTestResult(
            'Search Users (Teacher)',
            searchPassed,
            '200 or 403',
            searchResponse.statusCode,
            searchPassed ? 'Búsqueda ejecutada' : JSON.stringify(searchResponse.data)
        );
    } else {
        console.log('⚠️  Saltando test de búsqueda - Token de profesor no disponible');
    }
    
    // =============================================================================
    // TESTS DE AUTORIZACIÓN
    // =============================================================================
    
    console.log('🔒 TESTS DE AUTORIZACIÓN');
    console.log('='.repeat(50));
    
    // Test 6: Intentar acceder a perfil de profesor sin permisos
    console.log('📋 Test: GET /api/users/teachers/999 - Acceso sin permisos');
    const unauthorizedResponse = await AuthHelper.makeAuthenticatedRequest(
        'GET',
        '/api/users/teachers/999',
        studentAuth.token
    );
    
    const unauthorizedPassed = unauthorizedResponse.statusCode === 403 || unauthorizedResponse.statusCode === 404;
    writeTestResult(
        'Unauthorized Access Test',
        unauthorizedPassed,
        '403 or 404',
        unauthorizedResponse.statusCode,
        'Acceso correctamente denegado o recurso no encontrado'
    );
    
    // =============================================================================
    // RESUMEN FINAL
    // =============================================================================
    
    console.log('='.repeat(80));
    console.log('RESUMEN DE TESTS - UserController con Autenticación');
    console.log('='.repeat(80));
    console.log(`Total Tests: ${totalTests}`);
    console.log(`Passed: ${passedTests}`);
    console.log(`Failed: ${failedTests}`);
    
    const successRate = totalTests > 0 ? (passedTests / totalTests) * 100 : 0;
    console.log(`Success Rate: ${successRate.toFixed(2)}%`);
    
    console.log('\n📋 Para ejecutar este script:');
    console.log('1. Asegúrate de que el backend esté ejecutándose en http://localhost:8080');
    console.log('2. Ejecuta: node tests/e2e/04-user-management/authenticated-user-test.js');
    console.log('3. Revisa los resultados arriba');
    
    if (failedTests > 0) {
        console.log('\n⚠️  Algunos tests fallaron. Revisa los detalles arriba.');
        process.exit(1);
    } else {
        console.log('\n🎉 ¡Todos los tests pasaron!');
        process.exit(0);
    }
}

// Ejecutar tests
runUserManagementTests().catch(error => {
    console.error('❌ Error ejecutando tests:', error.message);
    process.exit(1);
}); 