package com.athena.library.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Configuration class for Firebase initialization
 */
public class FirebaseConfig {
    private static Firestore firestoreInstance;

    /**
     * Initializes Firebase with service account credentials
     * @throws IOException if credentials file cannot be read
     */
    public static void initialize() throws IOException {
        // If already initialized, return
        if (FirebaseApp.getApps().size() > 0) {
            return;
        }

        // Load Firebase credentials from file
        // Note: In production, you would secure this file and not include it in version control
        InputStream serviceAccount =
                FirebaseConfig.class.getResourceAsStream("/firebase-credentials.json");

        // If the file is not found in resources, try to load from external file
        if (serviceAccount == null) {
            try {
                serviceAccount = new FileInputStream("firebase-credentials.json");
            } catch (IOException e) {
                throw new IOException("Firebase credentials file not found. Make sure firebase-credentials.json is in the classpath or root directory.", e);
            }
        }

        // Configure Firebase options
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://athenauls-default-rtdb.europe-west1.firebasedatabase.app")
                .build();

        // Initialize Firebase
        FirebaseApp.initializeApp(options);

        // Log initialization status
        System.out.println("Firebase initialized successfully!");
    }

    /**
     * Gets the Firestore database instance
     * @return Firestore instance
     */
    public static synchronized Firestore getFirestoreInstance() {
        if (firestoreInstance == null) {
            // Create instance if not already created
            firestoreInstance = FirestoreClient.getFirestore();
        }
        return firestoreInstance;
    }
}