package com.athena.library.auth;

import com.athena.library.firebase.FirebaseConfig;
import org.mindrot.jbcrypt.BCrypt;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Service class to handle authentication operations with improved security
 */
public class AuthService {
    private static final String STUDENTS_COLLECTION = "students";
    private static final String LIBRARIANS_COLLECTION = "librarians";
    private static final String AUTH_COLLECTION = "auth";

    // Workfactor for BCrypt (higher = more secure but slower)
    private static final int BCRYPT_WORKFACTOR = 12;

    private static AuthService instance;
    private String currentUserId;
    private String currentUserType; // "STUDENT" or "LIBRARIAN"

    /**
     * Private constructor for singleton pattern
     */
    private AuthService() {
        this.currentUserId = null;
        this.currentUserType = null;
    }

    /**
     * Gets the singleton instance
     * @return AuthService instance
     */
    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * Creates a new auth record for a user
     * @param userId System user ID
     * @param password Initial password
     * @return true if successful, false otherwise
     */
    public boolean createAuthRecord(String userId, String password) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            DocumentReference authRef = db.collection(AUTH_COLLECTION).document(userId);

            // Hash the password with BCrypt (salt is auto-generated and stored in the hash)
            String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_WORKFACTOR));

            // Create the auth record
            Map<String, Object> authData = new HashMap<>();
            authData.put("userId", userId);
            authData.put("passwordHash", passwordHash);
            authData.put("createdAt", FieldValue.serverTimestamp());
            authData.put("lastLogin", null);

            ApiFuture<WriteResult> future = authRef.set(authData);
            future.get();

            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error creating auth record: " + e.getMessage());
            return false;
        }
    }

    /**
     * Authenticates a student
     * @param studentId Student ID
     * @param password Password
     * @return Student object if authentication is successful, null otherwise
     */
    public Student authenticateStudent(String studentId, String password) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();

            // First, find the student by studentId
            Query query = db.collection(STUDENTS_COLLECTION).whereEqualTo("studentId", studentId);
            ApiFuture<QuerySnapshot> queryFuture = query.get();
            QuerySnapshot querySnapshot = queryFuture.get();

            if (querySnapshot.isEmpty()) {
                System.err.println("Student not found with ID: " + studentId);
                return null;
            }

            // Get the student document
            DocumentSnapshot studentDoc = querySnapshot.getDocuments().get(0);
            String systemId = studentDoc.getId();

            // Now get the auth record for this user
            DocumentReference authRef = db.collection(AUTH_COLLECTION).document(systemId);
            ApiFuture<DocumentSnapshot> authFuture = authRef.get();
            DocumentSnapshot authDoc = authFuture.get();

            if (!authDoc.exists()) {
                System.err.println("Auth record not found for student: " + studentId);
                return null;
            }

            // Get the stored password hash
            String storedHash = authDoc.getString("passwordHash");

            // Check the password using BCrypt
            if (BCrypt.checkpw(password, storedHash)) {
                // Authentication successful
                Student student = studentDoc.toObject(Student.class);

                // Update last login time
                Map<String, Object> updates = new HashMap<>();
                updates.put("lastLogin", FieldValue.serverTimestamp());
                authRef.update(updates);

                // Set current user
                this.currentUserId = systemId;
                this.currentUserType = "STUDENT";

                return student;
            } else {
                System.err.println("Invalid password for student: " + studentId);
                return null;
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error authenticating student: " + e.getMessage());
            return null;
        }
    }

    /**
     * Updates a user's password
     * @param userId System user ID
     * @param oldPassword Current password
     * @param newPassword New password
     * @return true if successful, false otherwise
     */
    public boolean updatePassword(String userId, String oldPassword, String newPassword) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            DocumentReference authRef = db.collection(AUTH_COLLECTION).document(userId);

            // Get the current auth record
            ApiFuture<DocumentSnapshot> future = authRef.get();
            DocumentSnapshot document = future.get();

            if (!document.exists()) {
                System.err.println("Auth record not found for user: " + userId);
                return false;
            }

            // Verify the old password
            String storedHash = document.getString("passwordHash");

            if (!BCrypt.checkpw(oldPassword, storedHash)) {
                System.err.println("Old password is incorrect for user: " + userId);
                return false;
            }

            // Hash the new password
            String newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(BCRYPT_WORKFACTOR));

            // Update the auth record
            Map<String, Object> updates = new HashMap<>();
            updates.put("passwordHash", newPasswordHash);
            updates.put("updatedAt", FieldValue.serverTimestamp());

            ApiFuture<WriteResult> updateFuture = authRef.update(updates);
            updateFuture.get();

            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error updating password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Resets a user's password to a given value (admin function)
     * @param userId System user ID
     * @param newPassword New password
     * @return true if successful, false otherwise
     */
    public boolean resetPassword(String userId, String newPassword) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            DocumentReference authRef = db.collection(AUTH_COLLECTION).document(userId);

            // Hash the new password
            String newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(BCRYPT_WORKFACTOR));

            // Update the auth record
            Map<String, Object> updates = new HashMap<>();
            updates.put("passwordHash", newPasswordHash);
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("passwordReset", true); // Flag to indicate a password reset

            ApiFuture<WriteResult> future = authRef.update(updates);
            future.get();

            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error resetting password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes an auth record for a user
     * @param userId System user ID
     * @return true if successful, false otherwise
     */
    public boolean deleteAuthRecord(String userId) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            ApiFuture<WriteResult> future = db.collection(AUTH_COLLECTION).document(userId).delete();

            future.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error deleting auth record: " + e.getMessage());
            return false;
        }
    }

    /**
     * Logs out the current user
     */
    public void logout() {
        this.currentUserId = null;
        this.currentUserType = null;
    }

    /**
     * Checks if a user is currently logged in
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return this.currentUserId != null;
    }

    /**
     * Gets the current user ID
     * @return Current user ID if logged in, null otherwise
     */
    public String getCurrentUserId() {
        return this.currentUserId;
    }

    /**
     * Gets the current user type
     * @return Current user type if logged in, null otherwise
     */
    public String getCurrentUserType() {
        return this.currentUserType;
    }

    /**
     * Checks if the current user is a student
     * @return true if the current user is a student, false otherwise
     */
    public boolean isCurrentUserStudent() {
        return "STUDENT".equals(this.currentUserType);
    }

    /**
     * Checks if the current user is a librarian
     * @return true if the current user is a librarian, false otherwise
     */
    public boolean isCurrentUserLibrarian() {
        return "LIBRARIAN".equals(this.currentUserType);
    }

    /**
     * Generates a random salt for password hashing
     * @return Base64-encoded salt
     */
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a password with a salt using SHA-256
     * @param password Password to hash
     * @param salt Salt to use
     * @return Base64-encoded hash
     */
    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error hashing password: " + e.getMessage());
            return null;
        }
    }

    /**
     * Validates password strength
     * @param password Password to validate
     * @return true if the password meets the strength requirements, false otherwise
     */
    public boolean validatePasswordStrength(String password) {
        // Password must be at least 8 characters long
        if (password.length() < 8) {
            return false;
        }

        // Password must contain at least one digit
        if (!password.matches(".*\\d.*")) {
            return false;
        }

        // Password must contain at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            return false;
        }

        // Password must contain at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }

        // Password must contain at least one special character
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            return false;
        }

        return true;
    }
}