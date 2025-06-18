#!/usr/bin/env node

// =============================================================================
// MASTER TEST RUNNER - Ejecuta todos los tests en orden secuencial
// =============================================================================

const { spawn } = require('child_process');
const fs = require('fs');
const path = require('path');
const sharedData = require('../utils/shared-test-data');

// Colores para consola
const colors = {
    reset: '\x1b[0m',
    red: '\x1b[31m',
    green: '\x1b[32m',
    yellow: '\x1b[33m',
    blue: '\x1b[34m',
    cyan: '\x1b[36m',
    bold: '\x1b[1m'
};

// Configuración de tests
const TEST_CONFIG = {
    BASE_URL: "http://localhost:8080/api",
    FALLBACK_INSTITUTION_ID: 3,
    MAX_RETRIES: 2,
    TIMEOUT: 60000
};

// Orden de ejecución de tests
const TEST_SEQUENCE = [
    {
        name: "Health Checks",
        file: "./e2e/01-health/health-connectivity.test.js",
        description: "Verificar conectividad y servicios básicos",
        args: ["--health-only"],
        required: true
    },
    {
        name: "Institution Setup",
        file: "./e2e/02-setup/institution-setup.test.js", 
        description: "Crear institución base para tests",
        args: [],
        required: true
    },
    {
        name: "User Registration",
        file: "./e2e/03-registration/user-registration.test.js",
        description: "Registrar usuarios (Teacher, Guardian, Student)",
        args: [],
        required: true
    },
    {
        name: "User Management",
        file: "./e2e/04-user-management/user-crud.test.js",
        description: "Tests CRUD en usuarios creados",
        args: [],
        required: false
    },
    {
        name: "Authentication",
        file: "./e2e/05-authentication/authentication.test.js",
        description: "Tests de autenticación con usuarios reales",
        args: ["--full"],
        required: false
    }
];

// Estadísticas globales
let globalStats = {
    totalTests: 0,
    passedTests: 0,
    failedTests: 0,
    totalTime: 0,
    phases: []
};

// Función para ejecutar un test individual
function runSingleTest(testConfig) {
    return new Promise((resolve, reject) => {
        console.log(`\n${colors.bold}${colors.blue}🔍 EJECUTANDO: ${testConfig.name}${colors.reset}`);
        console.log(`${colors.cyan}📄 Archivo: ${testConfig.file}${colors.reset}`);
        console.log(`${colors.cyan}📝 Descripción: ${testConfig.description}${colors.reset}`);
        
        const startTime = Date.now();
        const args = ['--color', ...testConfig.args];
        
        const child = spawn('node', [testConfig.file, ...args], {
            stdio: 'pipe',
            cwd: path.join(process.cwd(), 'tests')
        });
        
        let output = '';
        let errorOutput = '';
        
        child.stdout.on('data', (data) => {
            const text = data.toString();
            output += text;
            process.stdout.write(text); // Mostrar output en tiempo real
        });
        
        child.stderr.on('data', (data) => {
            const text = data.toString();
            errorOutput += text;
            process.stderr.write(text); // Mostrar errores en tiempo real
        });
        
        const timeout = setTimeout(() => {
            child.kill('SIGTERM');
            reject(new Error(`Test ${testConfig.name} timeout después de ${TEST_CONFIG.TIMEOUT}ms`));
        }, TEST_CONFIG.TIMEOUT);
        
        child.on('close', (code) => {
            clearTimeout(timeout);
            const endTime = Date.now();
            const duration = endTime - startTime;
            
            // Parseear estadísticas del output
            const stats = parseTestOutput(output);
            
            const result = {
                name: testConfig.name,
                file: testConfig.file,
                code: code,
                duration: duration,
                output: output,
                errorOutput: errorOutput,
                stats: stats,
                success: code === 0
            };
            
            // Actualizar estadísticas globales
            globalStats.totalTests += stats.total || 0;
            globalStats.passedTests += stats.passed || 0;
            globalStats.failedTests += stats.failed || 0;
            globalStats.totalTime += duration;
            globalStats.phases.push(result);
            
            if (code === 0) {
                console.log(`${colors.green}✅ ${testConfig.name} completado exitosamente (${duration}ms)${colors.reset}`);
                resolve(result);
            } else {
                console.log(`${colors.red}❌ ${testConfig.name} falló con código ${code} (${duration}ms)${colors.reset}`);
                if (testConfig.required) {
                    reject(new Error(`Test requerido ${testConfig.name} falló`));
                } else {
                    resolve(result); // Continuar aunque falle si no es requerido
                }
            }
        });
        
        child.on('error', (error) => {
            clearTimeout(timeout);
            reject(new Error(`Error ejecutando ${testConfig.name}: ${error.message}`));
        });
    });
}

// Función para parsear output de tests y extraer estadísticas
function parseTestOutput(output) {
    const stats = { total: 0, passed: 0, failed: 0 };
    
    // Buscar líneas con estadísticas
    const lines = output.split('\n');
    for (const line of lines) {
        if (line.includes('Total Tests:')) {
            const match = line.match(/Total Tests: (\d+)/);
            if (match) stats.total = parseInt(match[1]);
        }
        if (line.includes('Passed:')) {
            const match = line.match(/Passed: (\d+)/);
            if (match) stats.passed = parseInt(match[1]);
        }
        if (line.includes('Failed:')) {
            const match = line.match(/Failed: (\d+)/);
            if (match) stats.failed = parseInt(match[1]);
        }
        // También buscar tests individuales
        if (line.includes('✅ PASS') || line.includes('PASS:')) {
            stats.passed = Math.max(stats.passed, (output.match(/✅/g) || []).length);
        }
        if (line.includes('❌ FAIL') || line.includes('FAIL:')) {
            stats.failed = Math.max(stats.failed, (output.match(/❌.*FAIL/g) || []).length);
        }
    }
    
    if (stats.total === 0 && (stats.passed > 0 || stats.failed > 0)) {
        stats.total = stats.passed + stats.failed;
    }
    
    return stats;
}

// Función para mostrar resumen final
function showFinalSummary() {
    console.log(`\n${colors.bold}${colors.blue}${'='.repeat(80)}`);
    console.log('RESUMEN FINAL DE TODOS LOS TESTS');
    console.log(`${'='.repeat(80)}${colors.reset}`);
    
    console.log(`\n${colors.bold}📊 ESTADÍSTICAS GLOBALES:${colors.reset}`);
    console.log(`${colors.cyan}Total de tests ejecutados: ${globalStats.totalTests}${colors.reset}`);
    console.log(`${colors.green}Tests pasados: ${globalStats.passedTests}${colors.reset}`);
    console.log(`${colors.red}Tests fallados: ${globalStats.failedTests}${colors.reset}`);
    console.log(`${colors.yellow}Tiempo total: ${(globalStats.totalTime / 1000).toFixed(2)}s${colors.reset}`);
    
    const successRate = globalStats.totalTests > 0 ? 
        (globalStats.passedTests / globalStats.totalTests) * 100 : 0;
    const rateColor = successRate >= 90 ? colors.green : 
                      successRate >= 70 ? colors.yellow : colors.red;
    console.log(`${rateColor}Tasa de éxito: ${successRate.toFixed(2)}%${colors.reset}`);
    
    console.log(`\n${colors.bold}📋 RESULTADOS POR FASE:${colors.reset}`);
    globalStats.phases.forEach((phase, index) => {
        const status = phase.success ? '✅' : '❌';
        const timeStr = `(${(phase.duration / 1000).toFixed(2)}s)`;
        console.log(`${index + 1}. ${status} ${phase.name} ${timeStr}`);
        
        if (phase.stats.total > 0) {
            console.log(`   📊 Tests: ${phase.stats.total} | ✅ ${phase.stats.passed} | ❌ ${phase.stats.failed}`);
        }
    });
    
    // Mostrar datos compartidos creados
    console.log(`\n${colors.bold}📦 DATOS CREADOS:${colors.reset}`);
    const institutionId = sharedData.getInstitutionId();
    const teacherData = sharedData.getTeacherData();
    const guardianData = sharedData.getGuardianData();
    const studentData = sharedData.getStudentData();
    
    console.log(`${colors.cyan}Institution ID: ${institutionId}${colors.reset}`);
    if (teacherData.userId) {
        console.log(`${colors.cyan}Teacher ID: ${teacherData.userId} (Profile: ${teacherData.profileId})${colors.reset}`);
    }
    if (guardianData.userId) {
        console.log(`${colors.cyan}Guardian ID: ${guardianData.userId} (Profile: ${guardianData.profileId})${colors.reset}`);
    }
    if (studentData.userId) {
        console.log(`${colors.cyan}Student ID: ${studentData.userId} (Profile: ${studentData.profileId})${colors.reset}`);
    }
    
    console.log(`\n${colors.blue}${'='.repeat(80)}${colors.reset}`);
}

// Función principal
async function runAllTests() {
    console.log(`${colors.bold}${colors.blue}🚀 INICIANDO SECUENCIA COMPLETA DE TESTS${colors.reset}`);
    console.log(`${colors.cyan}🌐 Target URL: ${TEST_CONFIG.BASE_URL}${colors.reset}`);
    console.log(`${colors.cyan}🏢 Institution ID base: ${TEST_CONFIG.FALLBACK_INSTITUTION_ID}${colors.reset}`);
    
    // Inicializar run de tests
    const runId = sharedData.initializeTestRun();
    console.log(`${colors.cyan}🆔 Test Run ID: ${runId}${colors.reset}`);
    
    try {
        // Ejecutar tests en secuencia
        for (let i = 0; i < TEST_SEQUENCE.length; i++) {
            const testConfig = TEST_SEQUENCE[i];
            
            console.log(`\n${colors.bold}${colors.yellow}📍 FASE ${i + 1}/${TEST_SEQUENCE.length}${colors.reset}`);
            
            await runSingleTest(testConfig);
            
            // Pausa breve entre tests
            await new Promise(resolve => setTimeout(resolve, 1000));
        }
        
        // Mostrar resumen final
        showFinalSummary();
        
        if (globalStats.failedTests === 0) {
            console.log(`\n${colors.green}🎉 ¡Todos los tests completados exitosamente!${colors.reset}`);
            process.exit(0);
        } else {
            console.log(`\n${colors.yellow}⚠️  Tests completados con algunos fallos${colors.reset}`);
            process.exit(1);
        }
        
    } catch (error) {
        console.error(`\n${colors.red}❌ Error en secuencia de tests: ${error.message}${colors.reset}`);
        showFinalSummary();
        process.exit(1);
    }
}

// Verificar argumentos de línea de comandos
if (process.argv.includes('--cleanup')) {
    console.log(`${colors.yellow}🧹 Limpiando datos de tests...${colors.reset}`);
    sharedData.cleanup();
    console.log(`${colors.green}✅ Datos limpiados${colors.reset}`);
    process.exit(0);
}

if (process.argv.includes('--help')) {
    console.log(`${colors.blue}📖 MASTER TEST RUNNER${colors.reset}`);
    console.log('\nUso:');
    console.log('  node run-all-tests.js        # Ejecutar todos los tests');
    console.log('  node run-all-tests.js --cleanup  # Limpiar datos de tests');
    console.log('  node run-all-tests.js --help     # Mostrar esta ayuda');
    process.exit(0);
}

// Ejecutar tests
runAllTests().catch(error => {
    console.error(`${colors.red}❌ Error fatal: ${error.message}${colors.reset}`);
    process.exit(1);
}); 