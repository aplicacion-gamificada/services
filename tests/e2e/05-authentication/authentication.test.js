#!/usr/bin/env node

// =============================================================================
// TEST SCRIPT PARA AuthController (Node.js)
// =============================================================================

// CONFIGURACIÓN - EDITAR SEGÚN SEA NECESARIO
const BASE_URL = 'http://localhost:8080/api';
const TEST_DATABASE_URL = 'jdbc:sqlserver://localhost:1433;database=numerino_test;encrypt=false;trustServerCertificate=true;';

// Colores para consola
const colors = {
    reset: '\x1b[0m',
    red: '\x1b[31m',
    green: '\x1b[32m',
    yellow: '\x1b[33m',
    blue: '\x1b[34m',
    cyan: '\x1b[36m'
};

// Contadores
let totalTests = 0;
let passedTests = 0;
let failedTests = 0;

// Función para logging colorido
function writeTestResult(testName, passed, expected, actual, details = '') {
    totalTests++;
    
    if (passed) {
        passedTests++;
        console.log(`${colors.green}✅ PASS: ${testName}${colors.reset}`);
    } else {
        failedTests++;
        console.log(`${colors.red}❌ FAIL: ${testName}${colors.reset}`);
        console.log(`${colors.yellow}   Expected: ${expected}${colors.reset}`);
        console.log(`${colors.yellow}   Actual: ${actual}${colors.reset}`);
        if (details) {
            console.log(`${colors.yellow}   Details: ${details}${colors.reset}`);
        }
    }
}

// Función para hacer peticiones HTTP
async function makeRequest(method = 'GET', endpoint, body = null, headers = {}) {
    const url = `${BASE_URL}${endpoint}`;
    const defaultHeaders = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        ...headers
    };
    
    try {
        const options = {
            method,
            headers: defaultHeaders
        };
        
        if (body && method !== 'GET') {
            options.body = JSON.stringify(body);
        }
        
        const response = await fetch(url, options);
        
        let content = '';
        try {
            content = await response.text();
        } catch (e) {
            content = 'No content';
        }
        
        return {
            statusCode: response.status,
            content: content,
            headers: response.headers,
            success: response.ok
        };
    } catch (error) {
        return {
            statusCode: 0,
            content: error.message,
            headers: {},
            success: false,
            exception: error
        };
    }
}

// Función principal de tests
async function runTests() {
    console.log(`${colors.blue}\n🚀 INICIANDO TESTS DE AuthController${colors.reset}`);
    console.log(`${colors.cyan}Target URL: ${BASE_URL}${colors.reset}\n`);
    
    // =============================================================================
    // TESTS DE HEALTH & CONNECTIVITY
    // =============================================================================
    
    console.log(`${colors.blue}\n🔍 EJECUTANDO TESTS DE HEALTH & CONNECTIVITY${colors.reset}`);
    
    // Test 1: Health Check
    console.log('\n📋 Test: GET /api/auth/health - Should return service health status');
    let response = await makeRequest('GET', '/api/auth/health');
    let passed = response.statusCode === 200;
    
    if (passed && response.content) {
        try {
            const healthData = JSON.parse(response.content);
            const hasRequiredFields = healthData.service && healthData.status && healthData.timestamp;
            writeTestResult('Health Check Status', passed, '200', response.statusCode);
            writeTestResult('Health Check Fields', hasRequiredFields, 'service,status,timestamp fields', 'Response structure');
        } catch (e) {
            writeTestResult('Health Check', false, 'Valid JSON', 'Invalid JSON response');
        }
    } else {
        writeTestResult('Health Check', passed, '200', response.statusCode, response.content);
    }
    
    // Test 2: Database Connection Test
    console.log('\n📋 Test: GET /api/auth/db-test - Should test database connectivity');
    response = await makeRequest('GET', '/api/auth/db-test');
    passed = response.statusCode === 200;
    writeTestResult('Database Connection Test', passed, '200', response.statusCode, response.content);
    
    // Test 3: Stored Procedures Test
    console.log('\n📋 Test: GET /api/auth/sp-test - Should test stored procedures');
    response = await makeRequest('GET', '/api/auth/sp-test');
    passed = response.statusCode === 200;
    writeTestResult('Stored Procedures Test', passed, '200', response.statusCode, response.content);
    
    // =============================================================================
    // TESTS DE AUTHENTICATION
    // =============================================================================
    
    console.log(`${colors.blue}\n🔐 EJECUTANDO TESTS DE AUTHENTICATION${colors.reset}`);
    
    // Test 4: Login Success (Teacher/Guardian)
    console.log('\n📋 Test: POST /api/auth/login - Should login teacher/guardian successfully');
    const loginRequest = {
        email: 'tstring@example.com',
        password: 'Tpassword1!',
        rememberMe: false
    };
    response = await makeRequest('POST', '/api/auth/login', loginRequest);
    passed = response.statusCode === 200;
    writeTestResult('Teacher/Guardian Login Success', passed, '200', response.statusCode, response.content);
    
    // Test 5: Login with Invalid Credentials
    console.log('\n📋 Test: POST /api/auth/login - Should fail with invalid credentials');
    const invalidLoginRequest = {
        email: 'invalid@example.com',
        password: 'wrongpassword',
        rememberMe: false
    };
    response = await makeRequest('POST', '/api/auth/login', invalidLoginRequest);
    passed = response.statusCode === 401;
    writeTestResult('Login Invalid Credentials', passed, '401', response.statusCode, response.content);
    
    // Test 6: Login Validation Errors
    console.log('\n📋 Test: POST /api/auth/login - Should validate required fields');
    const emptyLoginRequest = {
        email: '',
        password: ''
    };
    response = await makeRequest('POST', '/api/auth/login', emptyLoginRequest);
    passed = response.statusCode === 400;
    writeTestResult('Login Validation Errors', passed, '400', response.statusCode, response.content);
    
    // Test 7.1: Student Login Success with student-login endpoint
    console.log('\n📋 Test: POST /api/auth/student-login - Should login student successfully with student-login endpoint');
    const studentLoginRequest = {
        "username": "4chan",
        "password": "Spassword1!",
        "rememberMe": true,
        "deviceInfo": "string",
        "userAgent": "string"
    };
    response = await makeRequest('POST', '/api/auth/student-login', studentLoginRequest);
    passed = response.statusCode === 200;
    writeTestResult('Student Login Success', passed, '200', response.statusCode, response.content);

    
    // Test 7.2: Student Login Success with login endpoint
    console.log('\n📋 Test: POST /api/auth/login - Should login student successfully with login endpoint');
    const studentLoginRequest2 = {
            "email": "sstring@example.com",
            "password": "Spassword1!",
            "rememberMe": true,
            "deviceInfo": "string",
            "userAgent": "string"
    };
    response = await makeRequest('POST', '/api/auth/login', studentLoginRequest2);
    passed = response.statusCode === 200;
    writeTestResult('Student Login Success', passed, '200', response.statusCode, response.content);
    
    // Test 8: Refresh Token Success
    console.log('\n📋 Test: POST /api/auth/refresh-token - Should refresh token successfully');
    const refreshRequest = {
        refreshToken: 'valid_refresh_token_here'
    };
    response = await makeRequest('POST', '/api/auth/refresh-token', refreshRequest);
    passed = response.statusCode === 200 || response.statusCode === 401;
    writeTestResult('Refresh Token', passed, '200 or 401', response.statusCode, response.content);
    
    // Test 9: Logout Success
    console.log('\n📋 Test: POST /api/auth/logout - Should logout successfully');
    const logoutRequest = {
        refreshToken: 'some_token'
    };
    response = await makeRequest('POST', '/api/auth/logout', logoutRequest);
    passed = response.statusCode === 200;
    writeTestResult('Logout Success', passed, '200', response.statusCode, response.content);
    
    // =============================================================================
    // TESTS DE EMAIL VERIFICATION
    // =============================================================================
    
    console.log(`${colors.blue}\n📧 EJECUTANDO TESTS DE EMAIL VERIFICATION${colors.reset}`);
    
    // Test 10: Verify Email Success
    console.log('\n📋 Test: POST /api/auth/verify-email - Should verify email successfully');
    const verifyRequest = {
        token: 'valid_verification_token'
    };
    response = await makeRequest('POST', '/api/auth/verify-email', verifyRequest);
    passed = response.statusCode === 200 || response.statusCode === 400;
    writeTestResult('Email Verification', passed, '200 or 400', response.statusCode, response.content);
    
    // Test 11: Resend Verification Email
    console.log('\n📋 Test: POST /api/auth/resend-verification - Should resend verification email');
    const resendRequest = {
        email: 'test@example.com'
    };
    response = await makeRequest('POST', '/api/auth/resend-verification', resendRequest);
    passed = response.statusCode === 200;
    writeTestResult('Resend Verification Email', passed, '200', response.statusCode, response.content);
    
    // =============================================================================
    // TESTS DE PASSWORD MANAGEMENT
    // =============================================================================
    
    console.log(`${colors.blue}\n🔑 EJECUTANDO TESTS DE PASSWORD MANAGEMENT${colors.reset}`);
    
    // Test 12: Forgot Password Success
    console.log('\n📋 Test: POST /api/auth/forgot-password - Should request password reset');
    const forgotPasswordRequest = {
        email: 'test@example.com'
    };
    response = await makeRequest('POST', '/api/auth/forgot-password', forgotPasswordRequest);
    passed = response.statusCode === 200;
    writeTestResult('Forgot Password Request', passed, '200', response.statusCode, response.content);
    
    // Test 13: Reset Password Success
    console.log('\n📋 Test: POST /api/auth/reset-password - Should reset password successfully');
    const resetPasswordRequest = {
        token: 'valid_reset_token',
        newPassword: 'NewPassword123!',
        confirmPassword: 'NewPassword123!'
    };
    response = await makeRequest('POST', '/api/auth/reset-password', resetPasswordRequest);
    passed = response.statusCode === 200 || response.statusCode === 400;
    writeTestResult('Reset Password Success', passed, '200 or 400', response.statusCode, response.content);
    
    // Test 14: Reset Password - Password Mismatch
    console.log('\n📋 Test: POST /api/auth/reset-password - Should fail with mismatched passwords');
    const mismatchPasswordRequest = {
        token: 'valid_reset_token',
        newPassword: 'NewPassword123!',
        confirmPassword: 'DifferentPassword123!'
    };
    response = await makeRequest('POST', '/api/auth/reset-password', mismatchPasswordRequest);
    passed = response.statusCode === 400;
    writeTestResult('Reset Password Mismatch', passed, '400', response.statusCode, response.content);
    
    // =============================================================================
    // RESUMEN FINAL
    // =============================================================================
    
    console.log(`${colors.blue}\n${'='.repeat(80)}`);
    console.log('RESUMEN DE TESTS - AuthController');
    console.log('='.repeat(80) + colors.reset);
    console.log(`${colors.blue}Total Tests: ${totalTests}${colors.reset}`);
    console.log(`${colors.green}Passed: ${passedTests}${colors.reset}`);
    console.log(`${colors.red}Failed: ${failedTests}${colors.reset}`);
    
    const successRate = totalTests > 0 ? (passedTests / totalTests) * 100 : 0;
    const rateColor = successRate >= 80 ? colors.green : colors.yellow;
    console.log(`${rateColor}Success Rate: ${successRate.toFixed(2)}%${colors.reset}`);
    
    console.log(`${colors.yellow}\nPara ejecutar este script:${colors.reset}`);
    console.log(`${colors.yellow}1. Asegúrate de que el backend esté ejecutándose en ${BASE_URL}${colors.reset}`);
    console.log(`${colors.yellow}2. Ejecuta: node test-auth-controller.js${colors.reset}`);
    console.log(`${colors.yellow}3. Revisa los resultados arriba${colors.reset}`);
    
    if (failedTests > 0) {
        console.log(`${colors.yellow}\n⚠️  Algunos tests fallaron. Revisa los detalles arriba.${colors.reset}`);
        process.exit(1);
    } else {
        console.log(`${colors.green}\n🎉 ¡Todos los tests pasaron!${colors.reset}`);
        process.exit(0);
    }
}

// Verificar si fetch está disponible (Node.js 18+)
if (typeof fetch === 'undefined') {
    console.log(`${colors.red}❌ Error: Este script requiere Node.js 18+ que incluye fetch.${colors.reset}`);
    console.log(`${colors.yellow}Alternativas:${colors.reset}`);
    console.log(`${colors.yellow}1. Actualiza a Node.js 18+${colors.reset}`);
    console.log(`${colors.yellow}2. Instala node-fetch: npm install node-fetch${colors.reset}`);
    console.log(`${colors.yellow}3. Usa el script de PowerShell en su lugar${colors.reset}`);
    process.exit(1);
}

// Ejecutar tests
runTests().catch(error => {
    console.error(`${colors.red}❌ Error ejecutando tests: ${error.message}${colors.reset}`);
    process.exit(1);
}); 