package com.athena.library.firebase;

import com.athena.library.models.Student;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Service class to handle all Student-related operations with Firebase
 */
public class StudentService {
    private static final String COLLECTION_NAME = "students";

    /**
     * Adds a new student to the database
     * @param student Student object to add
     * @return true if successful, false otherwise
     */
    public boolean addStudent(Student student) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME).document(student.getId()).set(student);
            future.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error adding student: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets a student by ID
     * @param id Student ID to look up
     * @return Student object if found, null otherwise
     */
    public Student getStudentById(String id) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            DocumentReference docRef = db.collection(COLLECTION_NAME).document(id);
            ApiFuture<DocumentSnapshot> future = docRef.get();

            DocumentSnapshot document = future.get();
            if (document.exists()) {
                return document.toObject(Student.class);
            } else {
                return null;
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting student: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets a student by their university student ID (not the database ID)
     * @param studentId University student ID to look up
     * @return Student object if found, null otherwise
     */
    public Student getStudentByStudentId(String studentId) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            Query query = db.collection(COLLECTION_NAME).whereEqualTo("studentId", studentId);
            ApiFuture<QuerySnapshot> future = query.get();

            QuerySnapshot querySnapshot = future.get();
            if (!querySnapshot.isEmpty()) {
                return querySnapshot.getDocuments().get(0).toObject(Student.class);
            } else {
                return null;
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting student by student ID: " + e.getMessage());
            return null;
        }
    }

    /**
     * Updates an existing student's information
     * @param student Updated student object
     * @return true if successful, false otherwise
     */
    public boolean updateStudent(Student student) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME).document(student.getId()).set(student);

            // Wait for the operation to complete
            future.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error updating student: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates only specific fields of a student document
     * @param studentId ID of the student to update
     * @param updates Map of field names to new values
     * @return true if successful, false otherwise
     */
    public boolean updateStudentFields(String studentId, Map<String, Object> updates) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            DocumentReference docRef = db.collection(COLLECTION_NAME).document(studentId);

            ApiFuture<WriteResult> future = docRef.update(updates);
            future.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error updating student fields: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a student from the database
     * @param studentId ID of the student to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteStudent(String studentId) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME).document(studentId).delete();

            // Wait for the operation to complete
            future.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error deleting student: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets a list of all students
     * @return List of all students
     */
    public List<Student> getAllStudents() {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();

            List<Student> students = new ArrayList<>();
            QuerySnapshot querySnapshot = future.get();
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                students.add(document.toObject(Student.class));
            }

            return students;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting all students: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Searches for students by name
     * @param name Name to search for
     * @return List of matching students
     */
    public List<Student> searchStudentsByName(String name) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();

            // Convert to lowercase for case-insensitive search
            String searchName = name.toLowerCase();

            // Firebase doesn't support direct substring search, so we need to do this manually
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();

            List<Student> matchingStudents = new ArrayList<>();
            QuerySnapshot querySnapshot = future.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Student student = document.toObject(Student.class);
                if (student != null) {
                    // checks if first name last name or full name contains the search term
                    String firstName = student.getFirstName().toLowerCase();
                    String lastName = student.getLastName().toLowerCase();
                    String fullName = (firstName + " " + lastName).toLowerCase();

                    if (firstName.contains(searchName) ||
                            lastName.contains(searchName) ||
                            fullName.contains(searchName)) {
                        matchingStudents.add(student);
                    }
                }
            }

            return matchingStudents;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error searching students by name: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Searches for students by department
     * @param department Department to search for
     * @return List of matching students
     */
    public List<Student> getStudentsByDepartment(String department) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            Query query = db.collection(COLLECTION_NAME).whereEqualTo("department", department);
            ApiFuture<QuerySnapshot> future = query.get();

            List<Student> students = new ArrayList<>();
            QuerySnapshot querySnapshot = future.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                students.add(document.toObject(Student.class));
            }

            return students;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting students by department: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets the number of students in the database
     * @return Number of students
     */
    public int getStudentCount() {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();

            QuerySnapshot querySnapshot = future.get();
            return querySnapshot.size();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting student count: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Generates a unique ID for a new student
     * @return A unique ID string
     */
    public String generateUniqueId() {
        Firestore db = FirebaseConfig.getFirestoreInstance();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();
        return docRef.getId();
    }
}