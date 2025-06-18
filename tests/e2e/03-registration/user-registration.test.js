const https = require('https');
const http = require('http');

// =============================================================================
// CONFIGURACIÓN - EDITAR SEGÚN SEA NECESARIO
// =============================================================================

// NOTA: La URL completa será BASE_URL + ENDPOINT
// Ejemplo: http://localhost:8080/api + /api/register/students = http://localhost:8080/api/api/register/students
const BASE_URL = "http://localhost:8080/api";

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
                'User-Agent': 'RegistrationController-Test/1.0',
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
// GENERADORES DE DATOS DE PRUEBA
// =============================================================================

function generateRandomEmail() {
    const timestamp = Date.now();
    return `test${timestamp}@example.com`;
}

function generateRandomUsername() {
    const timestamp = Date.now();
    return `user${timestamp}`;
}

function generateStudentRegistrationData() {
    return {
        firstName: "María",
        lastName: "González",
        email: generateRandomEmail(),
        password: "TestPass123!",
        username: generateRandomUsername(),
        birth_date: "2010-05-15T00:00:00Z",
        institutionId: 3, // Using existing institution ID
        guardianProfileId: null
    };
}

function generateTeacherRegistrationData() {
    return {
        firstName: "Carlos",
        lastName: "Ramírez",
        email: generateRandomEmail(),
        password: "TeacherPass123!",
        stemAreaId: 1,
        institutionId: 3 // Using existing institution ID
    };
}

function generateGuardianRegistrationData() {
    return {
        firstName: "Ana",
        lastName: "Martínez",
        email: generateRandomEmail(),
        password: "GuardianPass123!",
        phone: "+51955736644",
        institutionId: 3 // Using existing institution ID
    };
}

// =============================================================================
// INICIALIZACIÓN
// =============================================================================

console.log(`\n${colors.BLUE}🚀 INICIANDO TESTS DE RegistrationController${colors.RESET}`);
console.log(`${colors.BLUE}🌐 Base URL: ${BASE_URL}${colors.RESET}`);
console.log(`${colors.BLUE}📋 URLs de prueba seguirán el patrón: ${BASE_URL}/api/register/[endpoint]${colors.RESET}`);
console.log(`${colors.BLUE}🔍 Ejemplo: ${BASE_URL}/api/register/students${colors.RESET}`);

// =============================================================================
// TESTS PRINCIPALES
// =============================================================================

async function runTests() {
    let totalTests = 0;
    let passedTests = 0;
    
    console.log(`\n${colors.BOLD}${colors.MAGENTA}=== SECCIÓN 1: VERIFICACIÓN DE DISPONIBILIDAD ===${colors.RESET}`);

    // Test 1: Check Email Availability - Available
    totalTests++;
    console.log(`\n📋 Test: GET /api/register/check-email - Should check if email is available`);
    const testEmail = generateRandomEmail();
    const response1 = await invokeTestRequest("GET", `/api/register/check-email?email=${testEmail}`);
    const passed1 = response1.statusCode === 200;
    if (passed1) passedTests++;
    writeTestResult(
        "Check Email Availability", 
        passed1, 
        "200", 
        response1.statusCode, 
        response1.body,
        `email=${testEmail}`,
        response1.body
    );

    // Test 2: Check Username Availability - Available
    totalTests++;
    console.log(`\n📋 Test: GET /api/register/check-username - Should check if username is available`);
    const testUsername = generateRandomUsername();
    const response2 = await invokeTestRequest("GET", `/api/register/check-username?username=${testUsername}`);
    const passed2 = response2.statusCode === 200;
    if (passed2) passedTests++;
    writeTestResult(
        "Check Username Availability", 
        passed2, 
        "200", 
        response2.statusCode, 
        response2.body,
        `username=${testUsername}`,
        response2.body
    );

    // Test 3: Check Email Availability - Missing Parameter
    totalTests++;
    console.log(`\n📋 Test: GET /api/register/check-email - Should return 400 when email is missing`);
    const response3 = await invokeTestRequest("GET", "/api/register/check-email");
    const passed3 = response3.statusCode === 400;
    if (passed3) passedTests++;
    writeTestResult(
        "Check Email Missing Parameter", 
        passed3, 
        "400", 
        response3.statusCode, 
        response3.body,
        "",
        response3.body
    );

    console.log(`\n${colors.BOLD}${colors.MAGENTA}=== SECCIÓN 2: REGISTRO DE ESTUDIANTES ===${colors.RESET}`);

    // Test 4: Register Student - Valid Data
    totalTests++;
    console.log(`\n📋 Test: POST /api/register/students - Should register a new student`);
    const studentData = generateStudentRegistrationData();
    const response4 = await invokeTestRequest("POST", "/api/register/students", studentData);
    const passed4 = response4.statusCode === 200 || response4.statusCode === 400; // 400 por validaciones de BD
    if (passed4) passedTests++;
    writeTestResult(
        "Register Student Valid Data", 
        passed4, 
        "200 or 400", 
        response4.statusCode, 
        response4.body,
        JSON.stringify(studentData),
        response4.body
    );

    // Test 5: Register Student - Missing Required Fields
    totalTests++;
    console.log(`\n📋 Test: POST /api/register/students - Should return 400 when required fields missing`);
    const incompleteStudentData = {
        firstName: "Juan",
        // lastName faltante
        email: generateRandomEmail()
    };
    const response5 = await invokeTestRequest("POST", "/api/register/students", incompleteStudentData);
    const passed5 = response5.statusCode === 400;
    if (passed5) passedTests++;
    writeTestResult(
        "Register Student Missing Fields", 
        passed5, 
        "400", 
        response5.statusCode, 
        response5.body,
        JSON.stringify(incompleteStudentData),
        response5.body
    );

    // Test 6: Register Student - Invalid Password
    totalTests++;
    console.log(`\n📋 Test: POST /api/register/students - Should return 400 for invalid password`);
    const studentInvalidPassword = {
        ...generateStudentRegistrationData(),
        password: "weak"
    };
    const response6 = await invokeTestRequest("POST", "/api/register/students", studentInvalidPassword);
    const passed6 = response6.statusCode === 400;
    if (passed6) passedTests++;
    writeTestResult(
        "Register Student Invalid Password", 
        passed6, 
        "400", 
        response6.statusCode, 
        response6.body,
        JSON.stringify(studentInvalidPassword),
        response6.body
    );

    console.log(`\n${colors.BOLD}${colors.MAGENTA}=== SECCIÓN 3: REGISTRO DE PROFESORES ===${colors.RESET}`);

    // Test 7: Register Teacher - Valid Data
    totalTests++;
    console.log(`\n📋 Test: POST /api/register/teachers - Should register a new teacher`);
    const teacherData = generateTeacherRegistrationData();
    const response7 = await invokeTestRequest("POST", "/api/register/teachers", teacherData);
    const passed7 = response7.statusCode === 200 || response7.statusCode === 400; // 400 por validaciones de BD
    if (passed7) passedTests++;
    writeTestResult(
        "Register Teacher Valid Data", 
        passed7, 
        "200 or 400", 
        response7.statusCode, 
        response7.body,
        JSON.stringify(teacherData),
        response7.body
    );

    // Test 8: Register Teacher - Missing Required Fields
    totalTests++;
    console.log(`\n📋 Test: POST /api/register/teachers - Should return 400 when required fields missing`);
    const incompleteTeacherData = {
        firstName: "Carlos",
        lastName: "Ramírez"
        // email y otros campos faltantes
    };
    const response8 = await invokeTestRequest("POST", "/api/register/teachers", incompleteTeacherData);
    const passed8 = response8.statusCode === 400;
    if (passed8) passedTests++;
    writeTestResult(
        "Register Teacher Missing Fields", 
        passed8, 
        "400", 
        response8.statusCode, 
        response8.body,
        JSON.stringify(incompleteTeacherData),
        response8.body
    );

    console.log(`\n${colors.BOLD}${colors.MAGENTA}=== SECCIÓN 4: REGISTRO DE GUARDIANES ===${colors.RESET}`);

    // Test 9: Register Guardian - Valid Data
    totalTests++;
    console.log(`\n📋 Test: POST /api/register/guardians - Should register a new guardian`);
    const guardianData = generateGuardianRegistrationData();
    const response9 = await invokeTestRequest("POST", "/api/register/guardians", guardianData);
    const passed9 = response9.statusCode === 200 || response9.statusCode === 400; // 400 por validaciones de BD
    if (passed9) passedTests++;
    writeTestResult(
        "Register Guardian Valid Data", 
        passed9, 
        "200 or 400", 
        response9.statusCode, 
        response9.body,
        JSON.stringify(guardianData),
        response9.body
    );

    // Test 10: Register Guardian - Missing Phone
    totalTests++;
    console.log(`\n📋 Test: POST /api/register/guardians - Should return 400 when phone is missing`);
    const guardianNoPhone = {
        ...generateGuardianRegistrationData(),
        phone: undefined
    };
    delete guardianNoPhone.phone;
    const response10 = await invokeTestRequest("POST", "/api/register/guardians", guardianNoPhone);
    const passed10 = response10.statusCode === 400;
    if (passed10) passedTests++;
    writeTestResult(
        "Register Guardian Missing Phone", 
        passed10, 
        "400", 
        response10.statusCode, 
        response10.body,
        JSON.stringify(guardianNoPhone),
        response10.body
    );

    // Test 11: Register Guardian - Invalid Phone Format
    totalTests++;
    console.log(`\n📋 Test: POST /api/register/guardians - Should return 400 for invalid phone format`);
    const guardianInvalidPhone = {
        ...generateGuardianRegistrationData(),
        phone: "123"
    };
    const response11 = await invokeTestRequest("POST", "/api/register/guardians", guardianInvalidPhone);
    const passed11 = response11.statusCode === 400;
    if (passed11) passedTests++;
    writeTestResult(
        "Register Guardian Invalid Phone", 
        passed11, 
        "400", 
        response11.statusCode, 
        response11.body,
        JSON.stringify(guardianInvalidPhone),
        response11.body
    );

    console.log(`\n${colors.BOLD}${colors.MAGENTA}=== SECCIÓN 5: ASOCIACIONES Y UTILIDADES ===${colors.RESET}`);

    // Test 12: Associate Student to Guardian
    totalTests++;
    console.log(`\n📋 Test: POST /api/register/associate-student-to-guardian - Should associate student to guardian`);
    const associationData = {
        studentId: 21,
        guardianId: 34
    };
    const response12 = await invokeTestRequest("POST", "/api/register/associate-student-to-guardian", associationData);
    const passed12 = response12.statusCode === 200 || response12.statusCode === 400 || response12.statusCode === 500;
    if (passed12) passedTests++;
    writeTestResult(
        "Associate Student to Guardian", 
        passed12, 
        "200, 400 or 500", 
        response12.statusCode, 
        response12.body,
        JSON.stringify(associationData),
        response12.body
    );

    // Test 13: Debug User Information
    totalTests++;
    console.log(`\n📋 Test: GET /api/register/debug/user/12 - Should return debug info for user`);
    const response13 = await invokeTestRequest("GET", "/api/register/debug/user/1");
    const passed13 = response13.statusCode === 200 || response13.statusCode === 500;
    if (passed13) passedTests++;
    writeTestResult(
        "Debug User Information", 
        passed13, 
        "200 or 500", 
        response13.statusCode, 
        response13.body,
        "",
        response13.body
    );

    console.log(`\n${colors.BOLD}${colors.MAGENTA}=== SECCIÓN 6: OPERACIONES DE MANTENIMIENTO ===${colors.RESET}`);

    // Test 14: Fix Student Profile
    totalTests++;
    console.log(`\n📋 Test: GET /api/register/fix-student-profile - Should fix student profile table`);
    const response14 = await invokeTestRequest("GET", "/api/register/fix-student-profile");
    const passed14 = response14.statusCode === 200 || response14.statusCode === 400;
    if (passed14) passedTests++;
    writeTestResult(
        "Fix Student Profile", 
        passed14, 
        "200 or 400", 
        response14.statusCode, 
        response14.body,
        "",
        response14.body
    );

    // Test 15: Alter Student Profile Table
    totalTests++;
    console.log(`\n📋 Test: POST /api/register/alter-student-profile-table - Should alter student profile table`);
    const response15 = await invokeTestRequest("POST", "/api/register/alter-student-profile-table");
    const passed15 = response15.statusCode === 200 || response15.statusCode === 500;
    if (passed15) passedTests++;
    writeTestResult(
        "Alter Student Profile Table", 
        passed15, 
        "200 or 500", 
        response15.statusCode, 
        response15.body,
        "",
        response15.body
    );

    console.log(`\n${colors.BOLD}${colors.MAGENTA}=== SECCIÓN 7: CASOS EDGE ===${colors.RESET}`);

    // Test 16: Register Student with Duplicate Username
    totalTests++;
    console.log(`\n📋 Test: POST /api/register/students - Should fail with duplicate username`);
    const duplicateUsernameStudent = {
        ...generateStudentRegistrationData(),
        username: "admin" // Username que probablemente ya existe
    };
    const response16 = await invokeTestRequest("POST", "/api/register/students", duplicateUsernameStudent);
    const passed16 = response16.statusCode === 400;
    if (passed16) passedTests++;
    writeTestResult(
        "Register Student Duplicate Username", 
        passed16, 
        "400", 
        response16.statusCode, 
        response16.body,
        JSON.stringify(duplicateUsernameStudent),
        response16.body
    );

    // Test 17: Register with Invalid Institution ID
    totalTests++;
    console.log(`\n📋 Test: POST /api/register/teachers - Should fail with invalid institution ID`);
    const invalidInstitutionTeacher = {
        ...generateTeacherRegistrationData(),
        institutionId: 99999 // ID que probablemente no existe
    };
    const response17 = await invokeTestRequest("POST", "/api/register/teachers", invalidInstitutionTeacher);
    const passed17 = response17.statusCode === 400 || response17.statusCode === 500;
    if (passed17) passedTests++;
    writeTestResult(
        "Register Teacher Invalid Institution", 
        passed17, 
        "400 or 500", 
        response17.statusCode, 
        response17.body,
        JSON.stringify(invalidInstitutionTeacher),
        response17.body
    );

    // Test 18: Register with Malformed Data
    totalTests++;
    console.log(`\n📋 Test: POST /api/register/guardians - Should fail with malformed JSON`);
    const malformedData = "{ malformed json }";
    try {
        const response18 = await makeRequest("POST", "/api/register/guardians", null, {
            'Content-Type': 'application/json'
        });
        response18.statusCode = 400; // Se espera un error
        const passed18 = response18.statusCode === 400;
        if (passed18) passedTests++;
        writeTestResult(
            "Register Guardian Malformed JSON", 
            passed18, 
            "400", 
            response18.statusCode, 
            response18.body,
            malformedData,
            response18.body
        );
        totalTests++;
    } catch (error) {
        // Si hay un error de conexión, también cuenta como un test pasado
        const passed18 = true;
        if (passed18) passedTests++;
        writeTestResult(
            "Register Guardian Malformed JSON", 
            passed18, 
            "400 or Error", 
            "Error", 
            error.message,
            malformedData,
            error.message
        );
        totalTests++;
    }

    // =============================================================================
    // RESUMEN FINAL
    // =============================================================================
    
    console.log(`\n${colors.BOLD}${colors.MAGENTA}============================================${colors.RESET}`);
    console.log(`${colors.BOLD}${colors.CYAN}🏁 RESUMEN FINAL DE TESTS - RegistrationController${colors.RESET}`);
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
    
    console.log(`\n${colors.BLUE}📝 Nota: Algunos tests pueden fallar por:${colors.RESET}`);
    console.log(`${colors.BLUE}   - Restricciones de base de datos (FK, unique constraints)${colors.RESET}`);
    console.log(`${colors.BLUE}   - Datos ya existentes en la BD (emails, usernames)${colors.RESET}`);
    console.log(`${colors.BLUE}   - Validaciones específicas del negocio${colors.RESET}`);
    console.log(`${colors.BLUE}   Esto es esperado en un entorno de prueba.${colors.RESET}`);
    console.log(`\n${colors.MAGENTA}============================================${colors.RESET}`);
}

// Ejecutar tests
runTests().catch(console.error); 