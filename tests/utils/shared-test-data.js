// =============================================================================
// SHARED TEST DATA MANAGER
// =============================================================================

const fs = require('fs');
const path = require('path');

const DATA_FILE = path.join(__dirname, 'test-data.json');

class SharedTestData {
    constructor() {
        this.data = {
            institutionId: 3, // Using existing institution ID as fallback
            teacherId: null,
            teacherProfileId: null,
            teacherEmail: null,
            teacherPassword: null,
            guardianId: null,
            guardianProfileId: null,
            guardianEmail: null,
            guardianPassword: null,
            studentId: null,
            studentProfileId: null,
            studentEmail: null,
            studentUsername: null,
            studentPassword: null,
            createdEmails: [],
            createdUsernames: [],
            testRunId: null
        };
        
        this.loadData();
    }
    
    // Load existing data from file
    loadData() {
        try {
            if (fs.existsSync(DATA_FILE)) {
                const fileData = JSON.parse(fs.readFileSync(DATA_FILE, 'utf8'));
                this.data = { ...this.data, ...fileData };
            }
        } catch (error) {
            console.log('Could not load test data file, using defaults:', error.message);
        }
    }
    
    // Save data to file
    saveData() {
        try {
            fs.writeFileSync(DATA_FILE, JSON.stringify(this.data, null, 2));
        } catch (error) {
            console.error('Error saving test data:', error.message);
        }
    }
    
    // Initialize new test run
    initializeTestRun() {
        this.data.testRunId = Date.now();
        this.saveData();
        return this.data.testRunId;
    }
    
    // Get unique test data with timestamp
    generateUniqueTestData() {
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
    
    // Institution methods
    setInstitutionId(id) {
        this.data.institutionId = id;
        this.saveData();
    }
    
    getInstitutionId() {
        return this.data.institutionId;
    }
    
    // Teacher methods
    setTeacherData(userId, profileId, email, password) {
        this.data.teacherId = userId;
        this.data.teacherProfileId = profileId;
        this.data.teacherEmail = email;
        this.data.teacherPassword = password;
        this.addCreatedEmail(email);
        this.saveData();
    }
    
    getTeacherData() {
        return {
            userId: this.data.teacherId,
            profileId: this.data.teacherProfileId,
            email: this.data.teacherEmail,
            password: this.data.teacherPassword
        };
    }
    
    // Guardian methods
    setGuardianData(userId, profileId, email, password) {
        this.data.guardianId = userId;
        this.data.guardianProfileId = profileId;
        this.data.guardianEmail = email;
        this.data.guardianPassword = password;
        this.addCreatedEmail(email);
        this.saveData();
    }
    
    getGuardianData() {
        return {
            userId: this.data.guardianId,
            profileId: this.data.guardianProfileId,
            email: this.data.guardianEmail,
            password: this.data.guardianPassword
        };
    }
    
    // Student methods
    setStudentData(userId, profileId, email, username, password) {
        this.data.studentId = userId;
        this.data.studentProfileId = profileId;
        this.data.studentEmail = email;
        this.data.studentUsername = username;
        this.data.studentPassword = password;
        this.addCreatedEmail(email);
        this.addCreatedUsername(username);
        this.saveData();
    }
    
    getStudentData() {
        return {
            userId: this.data.studentId,
            profileId: this.data.studentProfileId,
            email: this.data.studentEmail,
            username: this.data.studentUsername,
            password: this.data.studentPassword
        };
    }
    
    // Helper methods
    addCreatedEmail(email) {
        if (!this.data.createdEmails.includes(email)) {
            this.data.createdEmails.push(email);
        }
    }
    
    addCreatedUsername(username) {
        if (!this.data.createdUsernames.includes(username)) {
            this.data.createdUsernames.push(username);
        }
    }
    
    isEmailUsed(email) {
        return this.data.createdEmails.includes(email);
    }
    
    isUsernameUsed(username) {
        return this.data.createdUsernames.includes(username);
    }
    
    // Cleanup method
    cleanup() {
        this.data = {
            institutionId: 3, // Keep fallback institution ID
            teacherId: null,
            teacherProfileId: null,
            teacherEmail: null,
            teacherPassword: null,
            guardianId: null,
            guardianProfileId: null,
            guardianEmail: null,
            guardianPassword: null,
            studentId: null,
            studentProfileId: null,
            studentEmail: null,
            studentUsername: null,
            studentPassword: null,
            createdEmails: [],
            createdUsernames: [],
            testRunId: null
        };
        this.saveData();
    }
    
    // Debug method
    printData() {
        console.log('=== SHARED TEST DATA ===');
        console.log(JSON.stringify(this.data, null, 2));
        console.log('========================');
    }
}

// Export singleton instance
module.exports = new SharedTestData(); 