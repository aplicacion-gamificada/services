module.exports = {
    auth: {
        health: '/auth/health',
        dbTest: '/auth/db-test',
        spTest: '/auth/sp-test',
        login: '/auth/login',
        studentLogin: '/auth/student-login',
        logout: '/auth/logout',
        refreshToken: '/auth/refresh-token',
        verifyEmail: '/auth/verify-email',
        resendVerification: '/auth/resend-verification',
        forgotPassword: '/auth/forgot-password',
        resetPassword: '/auth/reset-password'
    },
    institutions: {
        base: '/institutions',
        byId: '/institutions/{id}',
        search: '/institutions/search'
    },
    registration: {
        students: '/register/students',
        teachers: '/register/teachers',
        guardians: '/register/guardians',
        checkEmail: '/register/check-email',
        checkUsername: '/register/check-username',
        associateStudentToGuardian: '/register/associate-student-to-guardian',
        debugUser: '/register/debug/user/{id}',
        fixStudentProfile: '/register/fix-student-profile',
        alterStudentProfileTable: '/register/alter-student-profile-table'
    },
    users: {
        profile: '/users/profile',
        search: '/users/search',
        students: '/users/students/{id}',
        teachers: '/users/teachers/{id}',
        guardians: '/users/guardians/{id}',
        guardianStudents: '/users/guardians/{id}/students',
        associateStudent: '/users/guardians/associate-student',
        updatePassword: '/users/{id}/password',
        updateProfilePicture: '/users/{id}/profile-picture',
        deactivate: '/users/{id}'
    },
    audit: {
        search: '/audit/search',
        loginHistory: '/audit/login-history',
        userActivity: '/audit/activity',
        suspiciousActivity: '/audit/suspicious-activity'
    },
    sessions: {
        active: '/sessions',
        rename: '/sessions/{id}/rename',
        revoke: '/sessions/{id}',
        revokeAll: '/sessions/all',
        revokeAllExceptCurrent: '/sessions/all-except-current',
        revokeUserSessions: '/sessions/user/{id}'
    }
};