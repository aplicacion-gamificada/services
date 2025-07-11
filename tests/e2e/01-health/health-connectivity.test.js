#!/usr/bin/env node

// =============================================================================
// TEST SCRIPT PARA AuthController (Node.js)
// =============================================================================

// CONFIGURACIÓN - EDITAR SEGÚN SEA NECESARIO
const BASE_URL = 'http://localhost:8080';
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
    console.log('\n📋 Test: GET /auth/health - Should return service health status');
    let response = await makeRequest('GET', '/auth/health');
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
    console.log('\n📋 Test: GET /auth/db-test - Should test database connectivity');
    response = await makeRequest('GET', '/auth/db-test');
    passed = response.statusCode === 200;
    writeTestResult('Database Connection Test', passed, '200', response.statusCode, response.content);
    
    // Test 3: Stored Procedures Test
    console.log('\n📋 Test: GET /auth/sp-test - Should test stored procedures');
    response = await makeRequest('GET', '/auth/sp-test');
    passed = response.statusCode === 200;
    writeTestResult('Stored Procedures Test', passed, '200', response.statusCode, response.content);
}
    
    // =============================================================================
    // TESTS DE AUTHENTICATION
    // =============================================================================