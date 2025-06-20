#!/usr/bin/env node

// =============================================================================
// SCRIPT PARA OBTENER BEARER TOKEN PARA USER MANAGEMENT
// =============================================================================

const AuthHelper = require('./auth-helper');

// Verificar si fetch está disponible
if (typeof fetch === 'undefined') {
    console.log('❌ Error: Este script requiere Node.js 18+ que incluye fetch.');
    console.log('Alternativas:');
    console.log('1. Actualiza a Node.js 18+');
    console.log('2. Instala node-fetch: npm install node-fetch');
    process.exit(1);
}

async function demonstrateTokenUsage() {
    console.log('🚀 OBTENIENDO BEARER TOKEN PARA USER MANAGEMENT\n');
    
    const users = AuthHelper.getTestUsers();
    
    console.log('📋 Usuarios de prueba disponibles:');
    console.log('- Estudiante:', users.student.email);
    console.log('- Profesor:', users.teacher.email);
    console.log('- Guardián:', users.guardian.email);
    console.log('');
    
    // Login como estudiante
    console.log('🔐 Haciendo login como estudiante...');
    const studentAuth = await AuthHelper.login(users.student.email, users.student.password, users.student.type);
    
    if (studentAuth.success) {
        console.log('✅ Token obtenido exitosamente!');
        console.log('👤 Usuario autenticado:', studentAuth.user);
        console.log('⏰ Expira en:', studentAuth.expiresIn, 'segundos');
        console.log('');
        
        // Mostrar ejemplos de uso
        AuthHelper.printTokenUsageExamples(studentAuth.token);
        
        // Probar una petición autenticada
        console.log('🧪 Probando petición autenticada...');
        const profileResponse = await AuthHelper.makeAuthenticatedRequest('GET', '/api/users/profile', studentAuth.token);
        
        if (profileResponse.success) {
            console.log('✅ Petición exitosa - Perfil obtenido:');
            console.log(JSON.stringify(profileResponse.data, null, 2));
        } else {
            console.log('❌ Error en petición:', profileResponse.statusCode);
            console.log('Response:', profileResponse.data);
        }
        
        console.log('\n🔑 Bearer Token copiado abajo (para usar en CURL):');
        console.log('='.repeat(80));
        console.log(studentAuth.token);
        console.log('='.repeat(80));
        
    } else {
        console.log('❌ Error en login:', studentAuth.error);
        console.log('');
        console.log('💡 Asegúrate de que:');
        console.log('1. El backend esté ejecutándose en http://localhost:8080');
        console.log('2. Los usuarios de prueba estén registrados en la base de datos');
        console.log('3. Las credenciales sean correctas');
    }
}

// Ejecutar demostración
demonstrateTokenUsage().catch(error => {
    console.error('❌ Error ejecutando script:', error.message);
    process.exit(1);
}); 