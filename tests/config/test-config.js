module.exports = {
    BASE_URL: process.env.TEST_BASE_URL || "http://localhost:8080",
    TIMEOUT: process.env.TEST_TIMEOUT || 60000,
    RETRIES: process.env.TEST_RETRIES || 2,
    INSTITUTION_ID: process.env.TEST_INSTITUTION_ID || 3,
    PARALLEL_TESTS: process.env.PARALLEL_TESTS === 'true',
    LOG_LEVEL: process.env.LOG_LEVEL || 'info',
    SAVE_ARTIFACTS: process.env.SAVE_ARTIFACTS !== 'false',
    
    // Colors for console output
    colors: {
        reset: '\x1b[0m',
        red: '\x1b[31m',
        green: '\x1b[32m',
        yellow: '\x1b[33m',
        blue: '\x1b[34m',
        cyan: '\x1b[36m',
        bold: '\x1b[1m'
    }
};