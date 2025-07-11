const config = require('../config/test-config');

class TestHelpers {
    static writeTestResult(testName, passed, expected, actual, details = '', requestBody = '', responseBody = '') {
        const status = passed ? `${config.colors.green}✅ PASS${config.colors.reset}` : `${config.colors.red}❌ FAIL${config.colors.reset}`;
        console.log(`${status} - ${testName}`);
        console.log(`   Expected: ${config.colors.cyan}${expected}${config.colors.reset}`);
        console.log(`   Actual: ${actual === expected ? config.colors.green : config.colors.red}${actual}${config.colors.reset}`);
        
        if (requestBody) {
            console.log(`   ${config.colors.yellow}Request:${config.colors.reset} ${requestBody}`);
        }
        
        if (responseBody) {
            console.log(`   ${config.colors.yellow}Response:${config.colors.reset} ${responseBody}`);
        }
        
        if (details && details.trim() !== '') {
            console.log(`   ${config.colors.cyan}Details:${config.colors.reset} ${details.substring(0, 200)}${details.length > 200 ? '...' : ''}`);
        }
        
        console.log('');
    }

    static generateUniqueTestData() {
        const timestamp = Date.now();
        const random = Math.floor(Math.random() * 1000);
        
        return {
            email: `test_${timestamp}_${random}@example.com`,
            username: `user_${timestamp}_${random}`,
            firstName: `TestUser_${random}`,
            lastName: `LastName_${timestamp}`,
            phone: `+51300${random}${timestamp.toString().slice(-4)}`
        };
    }

    static parseTestOutput(output) {
        const stats = { total: 0, passed: 0, failed: 0 };
        
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

    static async delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    static generateRandomEmail() {
        const timestamp = Date.now();
        return `test${timestamp}@example.com`;
    }

    static generateRandomUsername() {
        const timestamp = Date.now();
        return `user${timestamp}`;
    }
}

module.exports = TestHelpers;