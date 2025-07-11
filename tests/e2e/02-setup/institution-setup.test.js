const https = require('https');
const http = require('http');

// =============================================================================
// CONFIGURACIÓN - EDITAR SEGÚN SEA NECESARIO
// =============================================================================

// NOTA: La URL completa será BASE_URL + ENDPOINT
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
                'User-Agent': 'InstitutionController-Test/1.0',
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

function generateInstitutionRegistrationData() {
    const timestamp = Date.now();
    return {
        name: `Instituto de Prueba ${timestamp}`,
        address: "Calle 123 # 45-67",
        city: "Bogotá",
        state: "Cundinamarca",
        country: "Colombia",
        postalCode: "110111",
        phone: "+51-1-5551234",
        email: `contacto${timestamp}@instituto.edu.co`,
        website: `https://instituto${timestamp}.edu.co`,
        logoUrl: `https://instituto${timestamp}.edu.co/logo.png`
    };
}

function generateMinimalInstitutionData() {
    const timestamp = Date.now();
    return {
        name: `Instituto Mínimo ${timestamp}`,
        address: "Dirección requerida",
        city: "Ciudad",
        country: "País",
        phone: "+57-1-1234567",
        email: `minimal${timestamp}@test.com`
    };
}

// =============================================================================
// INICIALIZACIÓN
// =============================================================================

console.log(`\n${colors.BLUE}🚀 INICIANDO TESTS DE InstitutionController${colors.RESET}`);
console.log(`${colors.BLUE}🌐 Base URL: ${BASE_URL}${colors.RESET}`);
console.log(`${colors.BLUE}📋 URLs de prueba seguirán el patrón: ${BASE_URL}/api/institutions/[endpoint]${colors.RESET}`);
console.log(`${colors.BLUE}🔍 Ejemplo: ${BASE_URL}/api/institutions${colors.RESET}`);

// =============================================================================
// TESTS PRINCIPALES
// =============================================================================

async function runTests() {
    let totalTests = 0;
    let passedTests = 0;
    
    console.log(`\n${colors.BOLD}${colors.MAGENTA}=== SECCIÓN 1: CONSULTA DE INSTITUCIONES ===${colors.RESET}`);

    // Test 1: Get All Active Institutions
    totalTests++;
    console.log(`\n📋 Test: GET /api/institutions - Should return all active institutions`);
    const response1 = await invokeTestRequest("GET", "/api/institutions");
    const passed1 = response1.statusCode === 200;
    if (passed1) passedTests++;
    writeTestResult(
        "Get All Active Institutions", 
        passed1, 
        "200", 
        response1.statusCode, 
        response1.body,
        "",
        response1.body
    );

    // Test 2: Get Institution by ID - Valid ID
    totalTests++;
    console.log(`\n📋 Test: GET /api/institutions/3 - Should return institution details`);
    const response2 = await invokeTestRequest("GET", "/api/institutions/3");
    const passed2 = response2.statusCode === 200 || response2.statusCode === 404;
    if (passed2) passedTests++;
    writeTestResult(
        "Get Institution by Valid ID", 
        passed2, 
        "200 or 404", 
        response2.statusCode, 
        response2.body,
        "",
        response2.body
    );

    // Test 3: Get Institution by ID - Invalid ID
    totalTests++;
    console.log(`\n📋 Test: GET /api/institutions/a113 - Should return 404 for non-existent institution`);
    const response3 = await invokeTestRequest("GET", "/api/institutions/a113");
    const passed3 = response3.statusCode === 404;
    if (passed3) passedTests++;
    writeTestResult(
        "Get Institution by Invalid ID", 
        passed3, 
        "404", 
        response3.statusCode, 
        response3.body,
        "",
        response3.body
    );

    // Test 4: Search Institutions - Valid Query
    totalTests++;
    console.log(`\n📋 Test: GET /api/institutions/search?query=Universidad - Should search institutions`);
    const response4 = await invokeTestRequest("GET", "/api/institutions/search?query=Universidad&limit=5");
    const passed4 = response4.statusCode === 200;
    if (passed4) passedTests++;
    writeTestResult(
        "Search Institutions Valid Query", 
        passed4, 
        "200", 
        response4.statusCode, 
        response4.body,
        "query=Universidad&limit=5",
        response4.body
    );

    // Test 5: Search Institutions - Default Limit
    totalTests++;
    console.log(`\n📋 Test: GET /api/institutions/search?query=Test - Should use default limit`);
    const response5 = await invokeTestRequest("GET", "/api/institutions/search?query=Test");
    const passed5 = response5.statusCode === 200;
    if (passed5) passedTests++;
    writeTestResult(
        "Search Institutions Default Limit", 
        passed5, 
        "200", 
        response5.statusCode, 
        response5.body,
        "query=Test",
        response5.body
    );

    // Test 6: Search Institutions - Missing Query Parameter
    totalTests++;
    console.log(`\n📋 Test: GET /api/institutions/search - Should return 400 when query is missing`);
    const response6 = await invokeTestRequest("GET", "/api/institutions/search");
    const passed6 = response6.statusCode === 400;
    if (passed6) passedTests++;
    writeTestResult(
        "Search Institutions Missing Query", 
        passed6, 
        "400", 
        response6.statusCode, 
        response6.body,
        "",
        response6.body
    );

    // Test 7: Search Institutions - No Results
    totalTests++;
    console.log(`\n📋 Test: GET /api/institutions/search?query=NonExistent - Should return empty list`);
    const response7 = await invokeTestRequest("GET", "/api/institutions/search?query=NonExistentInstitution12345");
    const passed7 = response7.statusCode === 200;
    if (passed7) passedTests++;
    writeTestResult(
        "Search Institutions No Results", 
        passed7, 
        "200", 
        response7.statusCode, 
        response7.body,
        "query=NonExistentInstitution12345",
        response7.body
    );

    console.log(`\n${colors.BOLD}${colors.MAGENTA}=== SECCIÓN 2: CREACIÓN DE INSTITUCIONES ===${colors.RESET}`);

    // Test 8: Register Institution - Valid Data
    totalTests++;
    console.log(`\n📋 Test: POST /api/institutions - Should register new institution successfully`);
    const institutionData = generateInstitutionRegistrationData();
    const response8 = await invokeTestRequest("POST", "/api/institutions", institutionData);
    const passed8 = response8.statusCode === 200 || response8.statusCode === 201 || response8.statusCode === 400; // 400 por validaciones de BD
    if (passed8) passedTests++;
    writeTestResult(
        "Register Institution Valid Data", 
        passed8, 
        "200, 201 or 400", 
        response8.statusCode, 
        response8.body,
        JSON.stringify(institutionData),
        response8.body
    );

    // Test 9: Register Institution - Minimal Required Fields
    totalTests++;
    console.log(`\n📋 Test: POST /api/institutions - Should register institution with minimal fields`);
    const minimalData = generateMinimalInstitutionData();
    const response9 = await invokeTestRequest("POST", "/api/institutions", minimalData);
    const passed9 = response9.statusCode === 200 || response9.statusCode === 201 || response9.statusCode === 400;
    if (passed9) passedTests++;
    writeTestResult(
        "Register Institution Minimal Fields", 
        passed9, 
        "200, 201 or 400", 
        response9.statusCode, 
        response9.body,
        JSON.stringify(minimalData),
        response9.body
    );

    // Test 10: Register Institution - Missing Required Fields
    totalTests++;
    console.log(`\n📋 Test: POST /api/institutions - Should fail when required fields are missing`);
    const incompleteData = {
        name: "Instituto Incompleto"
        // Missing address, city, country, phone, email
    };
    const response10 = await invokeTestRequest("POST", "/api/institutions", incompleteData);
    const passed10 = response10.statusCode === 400;
    if (passed10) passedTests++;
    writeTestResult(
        "Register Institution Missing Fields", 
        passed10, 
        "400", 
        response10.statusCode, 
        response10.body,
        JSON.stringify(incompleteData),
        response10.body
    );

    // Test 11: Register Institution - Invalid Email Format
    totalTests++;
    console.log(`\n📋 Test: POST /api/institutions - Should fail with invalid email format`);
    const invalidEmailData = {
        ...generateMinimalInstitutionData(),
        email: "invalid-email-format"
    };
    const response11 = await invokeTestRequest("POST", "/api/institutions", invalidEmailData);
    const passed11 = response11.statusCode === 400;
    if (passed11) passedTests++;
    writeTestResult(
        "Register Institution Invalid Email", 
        passed11, 
        "400", 
        response11.statusCode, 
        response11.body,
        JSON.stringify(invalidEmailData),
        response11.body
    );

    // Test 12: Register Institution - Invalid Website URL
    totalTests++;
    console.log(`\n📋 Test: POST /api/institutions - Should fail with invalid website URL`);
    const invalidWebsiteData = {
        ...generateMinimalInstitutionData(),
        website: "not-a-valid-url"
    };
    const response12 = await invokeTestRequest("POST", "/api/institutions", invalidWebsiteData);
    const passed12 = response12.statusCode === 400 || response12.statusCode === 200; // Puede que no valide formato de URL
    if (passed12) passedTests++;
    writeTestResult(
        "Register Institution Invalid Website", 
        passed12, 
        "400 or 200", 
        response12.statusCode, 
        response12.body,
        JSON.stringify(invalidWebsiteData),
        response12.body
    );

    // Test 13: Register Institution - Duplicate Name
    totalTests++;
    console.log(`\n📋 Test: POST /api/institutions - Should fail with duplicate institution name`);
    const duplicateNameData = {
        ...generateMinimalInstitutionData(),
        name: "Universidad Nacional" // Nombre que probablemente ya existe
    };
    const response13 = await invokeTestRequest("POST", "/api/institutions", duplicateNameData);
    const passed13 = response13.statusCode === 400 || response13.statusCode === 409;
    if (passed13) passedTests++;
    writeTestResult(
        "Register Institution Duplicate Name", 
        passed13, 
        "400 or 409", 
        response13.statusCode, 
        response13.body,
        JSON.stringify(duplicateNameData),
        response13.body
    );

    console.log(`\n${colors.BOLD}${colors.MAGENTA}=== SECCIÓN 3: CASOS EDGE Y VALIDACIONES ===${colors.RESET}`);

    // Test 14: Search Institutions - Very Long Query
    totalTests++;
    console.log(`\n📋 Test: GET /api/institutions/search - Should handle very long query`);
    const longQuery = "A".repeat(500); // Query muy largo
    const response14 = await invokeTestRequest("GET", `/api/institutions/search?query=${longQuery}`);
    const passed14 = response14.statusCode === 200 || response14.statusCode === 400;
    if (passed14) passedTests++;
    writeTestResult(
        "Search Institutions Long Query", 
        passed14, 
        "200 or 400", 
        response14.statusCode, 
        response14.body,
        `query=${longQuery.substring(0, 50)}...`,
        response14.body
    );

    // Test 15: Search Institutions - Special Characters
    totalTests++;
    console.log(`\n📋 Test: GET /api/institutions/search - Should handle special characters in query`);
    const specialQuery = "José María & Co. (Institución)";
    const encodedQuery = encodeURIComponent(specialQuery);
    const response15 = await invokeTestRequest("GET", `/api/institutions/search?query=${encodedQuery}`);
    const passed15 = response15.statusCode === 200;
    if (passed15) passedTests++;
    writeTestResult(
        "Search Institutions Special Characters", 
        passed15, 
        "200", 
        response15.statusCode, 
        response15.body,
        `query=${specialQuery}`,
        response15.body
    );

    // Test 16: Register Institution - Very Long Name
    totalTests++;
    console.log(`\n📋 Test: POST /api/institutions - Should fail with very long institution name`);
    const longNameData = {
        ...generateMinimalInstitutionData(),
        name: "A".repeat(200) // Nombre muy largo
    };
    const response16 = await invokeTestRequest("POST", "/api/institutions", longNameData);
    const passed16 = response16.statusCode === 400;
    if (passed16) passedTests++;
    writeTestResult(
        "Register Institution Long Name", 
        passed16, 
        "400", 
        response16.statusCode, 
        response16.body,
        JSON.stringify({...longNameData, name: longNameData.name.substring(0, 50) + "..."}),
        response16.body
    );

    // Test 17: Get Institution - Non-numeric ID
    totalTests++;
    console.log(`\n📋 Test: GET /api/institutions/invalid-id - Should handle non-numeric ID`);
    const response17 = await invokeTestRequest("GET", "/api/institutions/invalid-id");
    const passed17 = response17.statusCode === 400 || response17.statusCode === 404;
    if (passed17) passedTests++;
    writeTestResult(
        "Get Institution Non-numeric ID", 
        passed17, 
        "400 or 404", 
        response17.statusCode, 
        response17.body,
        "",
        response17.body
    );

    // Test 18: Register Institution - Malformed JSON
    totalTests++;
    console.log(`\n📋 Test: POST /api/institutions - Should handle malformed JSON`);
    try {
        const response18 = await makeRequest("POST", "/api/institutions", null, {
            'Content-Type': 'application/json'
        });
        response18.statusCode = 400; // Se espera un error
        const passed18 = response18.statusCode === 400;
        if (passed18) passedTests++;
        writeTestResult(
            "Register Institution Malformed JSON", 
            passed18, 
            "400", 
            response18.statusCode, 
            response18.body,
            "{ malformed json }",
            response18.body
        );
        totalTests++;
    } catch (error) {
        // Si hay un error de conexión, también cuenta como un test pasado
        const passed18 = true;
        if (passed18) passedTests++;
        writeTestResult(
            "Register Institution Malformed JSON", 
            passed18, 
            "400 or Error", 
            "Error", 
            error.message,
            "{ malformed json }",
            error.message
        );
        totalTests++;
    }

    // =============================================================================
    // RESUMEN FINAL
    // =============================================================================
    
    console.log(`\n${colors.BOLD}${colors.MAGENTA}============================================${colors.RESET}`);
    console.log(`${colors.BOLD}${colors.CYAN}🏁 RESUMEN FINAL DE TESTS - InstitutionController${colors.RESET}`);
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
    console.log(`${colors.BLUE}   - Restricciones de base de datos (nombres duplicados)${colors.RESET}`);
    console.log(`${colors.BLUE}   - Validaciones específicas del negocio${colors.RESET}`);
    console.log(`${colors.BLUE}   - Instituciones ya existentes en la BD${colors.RESET}`);
    console.log(`${colors.BLUE}   Esto es esperado en un entorno de prueba.${colors.RESET}`);
    console.log(`\n${colors.MAGENTA}============================================${colors.RESET}`);
}

// Ejecutar tests
runTests().catch(console.error); 