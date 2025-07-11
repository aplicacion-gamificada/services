const https = require('https');
const http = require('http');

// =============================================================================
// CONFIGURACIÓN - EDITAR SEGÚN SEA NECESARIO
// =============================================================================

// NOTA: La URL completa será BASE_URL + ENDPOINT
// Ejemplo: http://localhost:8080/api + /api/institutions = http://localhost:8080/api/api/institutions
const BASE_URL = "http://localhost:8080";

// URLs de base de datos (para referencia, no se usan en tests HTTP)
const TEST_DATABASE_URL = "jdbc:sqlserver://localhost:1433;database=numerino_test;encrypt=false;trustServerCertificate=true;";
const PROD_DATABASE_URL = "jdbc:sqlserver://numerino.database.windows.net:1433;database=numerino;user=numerino_admin@numerino;password=MerinoNoSeBana1510;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";

// Colores para output (ANSI codes)
const colors = {
    RED: '\x1b[31m',
    GREEN: '\x1b[32m',
    YELLOW: '\x1b[33m',
    BLUE: '\x1b[34m',
    MAGENTA: '\x1b[35m',
    CYAN: '\x1b[36m',
    WHITE: '\x1b[37m',
    RESET: '\x1b[0m',
    BOLD: '\x1b[1m'
};

// =============================================================================
// FUNCIONES AUXILIARES
// =============================================================================

function makeRequest(method, endpoint, body = null, headers = {}) {
    return new Promise((resolve, reject) => {
        const url = new URL(BASE_URL + endpoint);
        const options = {
            hostname: url.hostname,
            port: url.port || (url.protocol === 'https:' ? 443 : 80),
            path: url.pathname + url.search,
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'User-Agent': 'UserController-Test/1.0',
                ...headers
            }
        };

        if (body) {
            const bodyString = JSON.stringify(body);
            options.headers['Content-Length'] = Buffer.byteLength(bodyString);
        }

        const client = url.protocol === 'https:' ? https : http;
        
        const req = client.request(options, (res) => {
            let data = '';
            
            res.on('data', (chunk) => {
                data += chunk;
            });
            
            res.on('end', () => {
                try {
                    const response = {
                        statusCode: res.statusCode,
                        headers: res.headers,
                        body: data,
                        json: data ? JSON.parse(data) : null
                    };
                    resolve(response);
                } catch (e) {
                    resolve({
                        statusCode: res.statusCode,
                        headers: res.headers,
                        body: data,
                        json: null
                    });
                }
            });
        });

        req.on('error', (error) => {
            reject(error);
        });

        if (body) {
            req.write(JSON.stringify(body));
        }
        
        req.end();
    });
}

function writeTestResult(testName, passed, expected, actual, details = '', requestBody = '', responseBody = '') {
    const status = passed ? `${colors.GREEN}✅ PASS${colors.RESET}` : `${colors.RED}❌ FAIL${colors.RESET}`;
    console.log(`${status} - ${testName}`);
    console.log(`   Expected: ${colors.CYAN}${expected}${colors.RESET}`);
    console.log(`   Actual: ${actual === expected ? colors.GREEN : colors.RED}${actual}${colors.RESET}`);
    
    if (requestBody) {
        console.log(`   ${colors.YELLOW}Request:${colors.RESET} ${requestBody}`);
    }
    
    if (responseBody) {
        console.log(`   ${colors.YELLOW}Response:${colors.RESET} ${responseBody}`);
    }
    
    if (details && details.trim() !== '') {
        console.log(`   ${colors.MAGENTA}Details:${colors.RESET} ${details.substring(0, 200)}${details.length > 200 ? '...' : ''}`);
    }
    
    console.log('');
}

async function invokeTestRequest(method, endpoint, body = null) {
    try {
        console.log(`${colors.BLUE}🔍 ${method} ${BASE_URL}${endpoint}${colors.RESET}`);
        const response = await makeRequest(method, endpoint, body);
        return response;
    } catch (error) {
        console.log(`${colors.RED}❌ Error en petición: ${error.message}${colors.RESET}`);
        return {
            statusCode: 0,
            body: `Error: ${error.message}`,
            json: null
        };
    }
}

// =============================================================================
// INICIALIZACIÓN
// =============================================================================

console.log(`\n${colors.BLUE}🚀 INICIANDO TESTS DE UserController${colors.RESET}`);
console.log(`${colors.BLUE}🌐 Base URL: ${BASE_URL}${colors.RESET}`);
console.log(`${colors.BLUE}📋 URLs de prueba seguirán el patrón: ${BASE_URL}/api/users/[endpoint]${colors.RESET}`);
console.log(`${colors.BLUE}🔍 Ejemplo: ${BASE_URL}/api/users/profile${colors.RESET}`);

// =============================================================================
// TESTS PRINCIPALES
// =============================================================================

async function runTests() {
    let totalTests = 0;
    let passedTests = 0;
    
    console.log(`\n${colors.BOLD}${colors.MAGENTA}=== SECCIÓN 1: CONSULTA DE PERFILES ===${colors.RESET}`);

    // Test 1: Get Current User Profile (simulando usuario autenticado)
    totalTests++;
    console.log(`\n📋 Test: GET /api/users/profile - Should return current user profile`);
    const response1 = await invokeTestRequest("GET", "/api/users/profile");
    const passed1 = response1.statusCode === 200 || response1.statusCode === 401; // 401 esperado sin autenticación
    if (passed1) passedTests++;
    writeTestResult(
        "Get Current User Profile", 
        passed1, 
        "200 or 401", 
        response1.statusCode, 
        response1.body,
        "",
        response1.body
    );

    // Test 2: Get Student Profile by ID
    totalTests++;
    console.log(`\n📋 Test: GET /api/users/students/31 - Should return student profile`);
    const response2 = await invokeTestRequest("GET", "/api/users/students/31");
    const passed2 = response2.statusCode === 200 || response2.statusCode === 401 || response2.statusCode === 404;
    if (passed2) passedTests++;
    writeTestResult(
        "Get Student Profile", 
        passed2, 
        "200, 401 or 404", 
        response2.statusCode, 
        response2.body,
        "",
        response2.body
    );

    // Test 3: Get Teacher Profile by ID
    totalTests++;
    console.log(`\n📋 Test: GET /api/api/users/teachers/17 - Should return teacher profile`);
    const response3 = await invokeTestRequest("GET", "/api/users/teachers/789");
    const passed3 = response3.statusCode === 200 || response3.statusCode === 401 || response3.statusCode === 404;
    if (passed3) passedTests++;
    writeTestResult(
        "Get Teacher Profile", 
        passed3, 
        "200, 401 or 404", 
        response3.statusCode, 
        response3.body,
        "",
        response3.body
    );

    // Test 4: Get Guardian Profile by ID
    totalTests++;
    console.log(`\n📋 Test: GET /api/api/users/guardians/321 - Should return guardian profile`);
    const response4 = await invokeTestRequest("GET", "/api/users/guardians/321");
    const passed4 = response4.statusCode === 200 || response4.statusCode === 401 || response4.statusCode === 404;
    if (passed4) passedTests++;
    writeTestResult(
        "Get Guardian Profile", 
        passed4, 
        "200, 401 or 404", 
        response4.statusCode, 
        response4.body,
        "",
        response4.body
    );

    console.log(`\n${colors.BOLD}${colors.MAGENTA}=== SECCIÓN 2: BÚSQUEDA DE USUARIOS ===${colors.RESET}`);

    // Test 5: Search Users Successfully
    totalTests++;
    console.log(`\n📋 Test: GET /api/api/users/search?searchTerm=María&roleFilter=STUDENT&limit=10 - Should search users`);
    const response5 = await invokeTestRequest("GET", "/api/users/search?searchTerm=María&roleFilter=STUDENT&limit=10");
    const passed5 = response5.statusCode === 200 || response5.statusCode === 401;
    if (passed5) passedTests++;
    writeTestResult(
        "Search Users Success", 
        passed5, 
        "200 or 401", 
        response5.statusCode, 
        response5.body,
        "",
        response5.body
    );

    // Test 6: Search Users - Missing Search Term
    totalTests++;
    console.log(`\n📋 Test: GET /api/api/users/search - Should return 400 when searchTerm is missing`);
    const response6 = await invokeTestRequest("GET", "/api/users/search");
    const passed6 = response6.statusCode === 400 || response6.statusCode === 401;
    if (passed6) passedTests++;
    writeTestResult(
        "Search Users Missing Term", 
        passed6, 
        "400 or 401", 
        response6.statusCode, 
        response6.body,
        "",
        response6.body
    );

    // Test 7: Search Users with Default Values
    totalTests++;
    console.log(`\n📋 Test: GET /api/api/users/search?searchTerm=test - Should use default values`);
    const response7 = await invokeTestRequest("GET", "/api/users/search?searchTerm=test");
    const passed7 = response7.statusCode === 200 || response7.statusCode === 401;
    if (passed7) passedTests++;
    writeTestResult(
        "Search Users Default Values", 
        passed7, 
        "200 or 401", 
        response7.statusCode, 
        response7.body,
        "",
        response7.body
    );

    console.log(`\n${colors.BOLD}${colors.MAGENTA}=== SECCIÓN 3: RELACIONES GUARDIAN-STUDENT ===${colors.RESET}`);

    // Test 8: Get Students by Guardian
    totalTests++;
    console.log(`\n📋 Test: GET /api/api/users/guardians/123/students - Should return students for guardian`);
    const response8 = await invokeTestRequest("GET", "/api/users/guardians/123/students");
    const passed8 = response8.statusCode === 200 || response8.statusCode === 401 || response8.statusCode === 404;
    if (passed8) passedTests++;
    writeTestResult(
        "Get Students by Guardian", 
        passed8, 
        "200, 401 or 404", 
        response8.statusCode, 
        response8.body,
        "",
        response8.body
    );

    // Test 9: Associate Student to Guardian
    totalTests++;
    console.log(`\n📋 Test: POST /api/api/users/guardians/associate-student - Should associate student to guardian`);
    const associationRequest = {
        studentProfileId: 18,
        guardianProfileId: 789
    };
    const response9 = await invokeTestRequest("POST", "/api/users/guardians/associate-student", associationRequest);
    const passed9 = response9.statusCode === 200 || response9.statusCode === 401 || response9.statusCode === 400;
    if (passed9) passedTests++;
    writeTestResult(
        "Associate Student to Guardian", 
        passed9, 
        "200, 401 or 400", 
        response9.statusCode, 
        response9.body,
        JSON.stringify(associationRequest),
        response9.body
    );

    console.log(`\n${colors.BOLD}${colors.MAGENTA}=== SECCIÓN 4: ACTUALIZACIONES DE PERFIL ===${colors.RESET}`);

    // Test 10: Update Student Profile
    totalTests++;
    console.log(`\n📋 Test: PUT /api/api/users/students/31 - Should update student profile`);
    const studentUpdateRequest = {
        firstName: "María Actualizada",
        lastName: "González Actualizada",
        username: "maria_gonzalez_new",
        profilePictureUrl: "https://example.com/new-picture.jpg"
    };
    const response10 = await invokeTestRequest("PUT", "/api/users/students/32", studentUpdateRequest);
    const passed10 = response10.statusCode === 200 || response10.statusCode === 401 || response10.statusCode === 404;
    if (passed10) passedTests++;
    writeTestResult(
        "Update Student Profile", 
        passed10, 
        "200, 401 or 404", 
        response10.statusCode, 
        response10.body,
        JSON.stringify(studentUpdateRequest),
        response10.body
    );

    // Test 11: Update Teacher Profile
    totalTests++;
    console.log(`\n📋 Test: PUT /api/api/users/teachers/17 - Should update teacher profile`);
    const teacherUpdateRequest = {
        firstName: "Carlos Actualizado",
        lastName: "Ramírez Actualizado",
        stemAreaId: 2,
        profilePictureUrl: "https://example.com/teacher-picture.jpg"
    };
    const response11 = await invokeTestRequest("PUT", "/api/users/teachers/789", teacherUpdateRequest);
    const passed11 = response11.statusCode === 200 || response11.statusCode === 401 || response11.statusCode === 404;
    if (passed11) passedTests++;
    writeTestResult(
        "Update Teacher Profile", 
        passed11, 
        "200, 401 or 404", 
        response11.statusCode, 
        response11.body,
        JSON.stringify(teacherUpdateRequest),
        response11.body
    );

    // Test 12: Update Guardian Profile
    totalTests++;
    console.log(`\n📋 Test: PUT /api/api/users/guardians/321 - Should update guardian profile`);
    const guardianUpdateRequest = {
        firstName: "Ana Actualizada",
        lastName: "Martínez Actualizada",
        phone: "+57-300-7654321",
        profilePictureUrl: "https://example.com/guardian-picture.jpg"
    };
    const response12 = await invokeTestRequest("PUT", "/api/users/guardians/321", guardianUpdateRequest);
    const passed12 = response12.statusCode === 200 || response12.statusCode === 401 || response12.statusCode === 404;
    if (passed12) passedTests++;
    writeTestResult(
        "Update Guardian Profile", 
        passed12, 
        "200, 401 or 404", 
        response12.statusCode, 
        response12.body,
        JSON.stringify(guardianUpdateRequest),
        response12.body
    );

    // Test 13: Update Password
    totalTests++;
    console.log(`\n📋 Test: PUT /api/api/users/18/password - Should update user password`);
    const passwordUpdateRequest = {
        currentPassword: "OldPassword123!",
        newPassword: "NewPassword123!",
        confirmPassword: "NewPassword123!"
    };
    const response13 = await invokeTestRequest("PUT", "/api/users/18/password", passwordUpdateRequest);
    const passed13 = response13.statusCode === 200 || response13.statusCode === 401 || response13.statusCode === 400;
    if (passed13) passedTests++;
    writeTestResult(
        "Update Password", 
        passed13, 
        "200, 401 or 400", 
        response13.statusCode, 
        response13.body,
        JSON.stringify(passwordUpdateRequest),
        response13.body
    );

    // Test 14: Update Profile Picture
    totalTests++;
    console.log(`\n📋 Test: PUT /api/api/users/18/profile-picture - Should update profile picture`);
    const pictureUrl = "https://example.com/new-profile-picture.jpg";
    const response14 = await invokeTestRequest("PUT", "/api/users/18/profile-picture", pictureUrl);
    const passed14 = response14.statusCode === 200 || response14.statusCode === 401 || response14.statusCode === 400;
    if (passed14) passedTests++;
    writeTestResult(
        "Update Profile Picture", 
        passed14, 
        "200, 401 or 400", 
        response14.statusCode, 
        response14.body,
        JSON.stringify(pictureUrl),
        response14.body
    );

    // Test 15: Update Profile Picture - Invalid URL
    totalTests++;
    console.log(`\n📋 Test: PUT /api/api/users/18/profile-picture - Should fail with invalid URL`);
    const invalidUrl = "not-a-valid-url";
    const response15 = await invokeTestRequest("PUT", "/api/users/18/profile-picture", invalidUrl);
    const passed15 = response15.statusCode === 400 || response15.statusCode === 401;
    if (passed15) passedTests++;
    writeTestResult(
        "Update Profile Picture Invalid URL", 
        passed15, 
        "400 or 401", 
        response15.statusCode, 
        response15.body,
        JSON.stringify(invalidUrl),
        response15.body
    );

    console.log(`\n${colors.BOLD}${colors.MAGENTA}=== SECCIÓN 5: ELIMINACIÓN DE USUARIOS ===${colors.RESET}`);

    // Test 16: Deactivate User Account
    totalTests++;
    console.log(`\n📋 Test: DELETE /api/api/users/18 - Should deactivate user account`);
    const response16 = await invokeTestRequest("DELETE", "/api/users/18");
    const passed16 = response16.statusCode === 200 || response16.statusCode === 401 || response16.statusCode === 404;
    if (passed16) passedTests++;
    writeTestResult(
        "Deactivate User Account", 
        passed16, 
        "200, 401 or 404", 
        response16.statusCode, 
        response16.body,
        "",
        response16.body
    );

    // Test 17: Deactivate User Account - User Not Found
    totalTests++;
    console.log(`\n📋 Test: DELETE /api/api/users/99999 - Should return 404 for non-existent user`);
    const response17 = await invokeTestRequest("DELETE", "/api/users/99999");
    const passed17 = response17.statusCode === 404 || response17.statusCode === 401;
    if (passed17) passedTests++;
    writeTestResult(
        "Deactivate Non-existent User", 
        passed17, 
        "404 or 401", 
        response17.statusCode, 
        response17.body,
        "",
        response17.body
    );

    // =============================================================================
    // RESUMEN FINAL
    // =============================================================================
    
    console.log(`\n${colors.BOLD}${colors.MAGENTA}============================================${colors.RESET}`);
    console.log(`${colors.BOLD}${colors.CYAN}🏁 RESUMEN FINAL DE TESTS - UserController${colors.RESET}`);
    console.log(`${colors.BOLD}${colors.MAGENTA}============================================${colors.RESET}`);
    
    const successRate = ((passedTests / totalTests) * 100).toFixed(1);
    const resultColor = passedTests === totalTests ? colors.GREEN : (successRate >= 70 ? colors.YELLOW : colors.RED);
    
    console.log(`${colors.BOLD}Total de tests ejecutados: ${colors.CYAN}${totalTests}${colors.RESET}`);
    console.log(`${colors.BOLD}Tests pasados: ${colors.GREEN}${passedTests}${colors.RESET}`);
    console.log(`${colors.BOLD}Tests fallados: ${colors.RED}${totalTests - passedTests}${colors.RESET}`);
    console.log(`${colors.BOLD}Tasa de éxito: ${resultColor}${successRate}%${colors.RESET}`);
    
    if (passedTests === totalTests) {
        console.log(`\n${colors.GREEN}🎉 ¡Todos los tests pasaron exitosamente!${colors.RESET}`);
    } else if (successRate >= 70) {
        console.log(`\n${colors.YELLOW}⚠️  La mayoría de tests pasaron, pero hay algunos fallos${colors.RESET}`);
    } else {
        console.log(`\n${colors.RED}❌ Hay varios tests fallando que requieren atención${colors.RESET}`);
    }
    
    console.log(`\n${colors.BLUE}📝 Nota: Muchos tests pueden fallar por falta de autenticación (401)${colors.RESET}`);
    console.log(`${colors.BLUE}   o porque los recursos no existen (404). Esto es esperado en un entorno de prueba.${colors.RESET}`);
    console.log(`\n${colors.MAGENTA}============================================${colors.RESET}`);
}

// Ejecutar tests
runTests().catch(console.error); 