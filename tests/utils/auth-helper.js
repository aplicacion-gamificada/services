const config = require('../config/test-config');

class AuthHelper {
    static async login(email, password, type = 'general') {
        const BASE_URL = 'http://localhost:8080';
        
        try {
            const loginRequest = {
                email: email,
                password: password,
                rememberMe: true,
                deviceInfo: "Test Device",
                userAgent: "Test Browser"
            };

            const endpoint = type === 'student' ? '/api/auth/student-login' : '/api/auth/login';
            
            const response = await fetch(`${BASE_URL}${endpoint}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(loginRequest)
            });

            const data = await response.json();
            
            if (response.ok && data.success && data.data.accessToken) {
                console.log(`${config.colors.green}✅ Login exitoso para: ${email}${config.colors.reset}`);
                console.log(`${config.colors.cyan}🔑 Bearer Token:${config.colors.reset}`);
                console.log(`${data.data.accessToken}`);
                console.log('');
                
                return {
                    success: true,
                    token: data.data.accessToken,
                    refreshToken: data.data.refreshToken,
                    user: data.data.user,
                    expiresIn: data.data.expiresIn
                };
            } else {
                console.log(`${config.colors.red}❌ Login fallido para: ${email}${config.colors.reset}`);
                console.log(`${config.colors.yellow}Response: ${JSON.stringify(data, null, 2)}${config.colors.reset}`);
                return {
                    success: false,
                    error: data.message || 'Login failed'
                };
            }
        } catch (error) {
            console.log(`${config.colors.red}❌ Error en login: ${error.message}${config.colors.reset}`);
            return {
                success: false,
                error: error.message
            };
        }
    }

    static async makeAuthenticatedRequest(method, endpoint, token, body = null) {
        const BASE_URL = 'http://localhost:8080';
        
        try {
            const options = {
                method: method,
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            };

            if (body && method !== 'GET') {
                options.body = JSON.stringify(body);
            }

            const response = await fetch(`${BASE_URL}${endpoint}`, options);
            const data = await response.text();
            
            let jsonData = null;
            try {
                jsonData = JSON.parse(data);
            } catch (e) {
                // Response is not JSON
            }

            return {
                statusCode: response.status,
                success: response.ok,
                data: jsonData || data,
                headers: response.headers
            };
        } catch (error) {
            console.log(`${config.colors.red}❌ Error en petición autenticada: ${error.message}${config.colors.reset}`);
            return {
                statusCode: 0,
                success: false,
                error: error.message
            };
        }
    }

    static getTestUsers() {
        return {
            student: {
                email: "sstring@example.com",
                password: "Spassword1!",
                type: "student"
            },
            teacher: {
                email: "tstring@example.com", 
                password: "Tpassword1!",
                type: "general"
            },
            guardian: {
                email: "maria.guardian@example.com",
                password: "GuardianPass123!",
                type: "general"
            }
        };
    }

    static printTokenUsageExamples(token) {
        console.log(`${config.colors.blue}📋 Ejemplos de uso del token:${config.colors.reset}\n`);
        
        console.log(`${config.colors.yellow}1. Obtener perfil actual:${config.colors.reset}`);
        console.log(`curl -X 'GET' \\`);
        console.log(`  'http://localhost:8080/api/users/profile' \\`);
        console.log(`  -H 'accept: */*' \\`);
        console.log(`  -H 'Authorization: Bearer ${token}'`);
        console.log('');
        
        console.log(`${config.colors.yellow}2. Actualizar perfil de estudiante:${config.colors.reset}`);
        console.log(`curl -X 'PUT' \\`);
        console.log(`  'http://localhost:8080/api/users/students/31' \\`);
        console.log(`  -H 'accept: application/json' \\`);
        console.log(`  -H 'Authorization: Bearer ${token}' \\`);
        console.log(`  -H 'Content-Type: application/json' \\`);
        console.log(`  -d '{"firstName": "Nombre Actualizado", "lastName": "Apellido Actualizado"}'`);
        console.log('');
        
        console.log(`${config.colors.yellow}3. Cambiar contraseña:${config.colors.reset}`);
        console.log(`curl -X 'PUT' \\`);
        console.log(`  'http://localhost:8080/api/users/31/password' \\`);
        console.log(`  -H 'accept: application/json' \\`);
        console.log(`  -H 'Authorization: Bearer ${token}' \\`);
        console.log(`  -H 'Content-Type: application/json' \\`);
        console.log(`  -d '{"currentPassword": "Spassword1!", "newPassword": "NewPassword123!", "confirmPassword": "NewPassword123!"}'`);
        console.log('');
    }
}

module.exports = AuthHelper; 