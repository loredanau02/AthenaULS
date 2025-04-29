package com.athena.library.firebase;

import com.athena.library.models.Message;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Service class to handle all Message-related operations with Firebase
 */
public class MessageService {
    private static final String COLLECTION_NAME = "messages";

    /**
     * Sends a new message
     * @param message Message object to send
     * @return true if successful, false otherwise
     */
    public boolean sendMessage(Message message) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            DocumentReference docRef = db.collection(COLLECTION_NAME).document();

            // Set the message ID
            message.setId(docRef.getId());

            // Ensure sent date is set
            if (message.getSentDate() == null) {
                message.setSentDate(new Date());
            }

            ApiFuture<WriteResult> future = docRef.set(message);

            // Wait for the operation to complete
            future.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error sending message: " + e.getMessage());
            return false;
        }
    }

    /**
     * Creates a notification message from librarian to student
     * @param librarianId ID of the sending librarian
     * @param librarianName Name of the sending librarian
     * @param studentId ID of the receiving student
     * @param studentName Name of the receiving student
     * @param subject Message subject
     * @param content Message content
     * @return true if successful, false otherwise
     */
    public boolean sendNotificationToStudent(
            String librarianId, String librarianName,
            String studentId, String studentName,
            String subject, String content) {

        Message notification = new Message(
                null, // ID will be set in sendMessage
                librarianId,
                librarianName,
                "LIBRARIAN",
                studentId,
                studentName,
                "STUDENT",
                subject,
                content
        );

        // Mark as important since it's a notification
        notification.setImportant(true);

        return sendMessage(notification);
    }

    /**
     * Creates an update notification for student ID or email change
     * @param librarianId ID of the librarian making the change
     * @param librarianName Name of the librarian
     * @param studentId ID of the affected student
     * @param studentName Name of the student
     * @param oldValue Previous value (studentId or email)
     * @param newValue New value
     * @param fieldChanged Field that was changed ("studentId" or "email")
     * @return true if successful, false otherwise
     */
    public boolean sendUpdateNotification(
            String librarianId, String librarianName,
            String studentId, String studentName,
            String oldValue, String newValue,
            String fieldChanged) {

        String subject = "Your " + (fieldChanged.equals("studentId") ? "Student ID" : "Email") + " Has Been Updated";

        String content = "Dear " + studentName + ",\n\n" +
                "This is to inform you that your " +
                (fieldChanged.equals("studentId") ? "Student ID" : "Email") +
                " has been updated in the Athena University Library System.\n\n" +
                "Previous value: " + oldValue + "\n" +
                "New value: " + newValue + "\n\n" +
                "If you have any questions about this change, please contact the library.\n\n" +
                "Regards,\n" +
                librarianName + "\n" +
                "Athena University Library";

        return sendNotificationToStudent(librarianId, librarianName, studentId, studentName, subject, content);
    }

    /**
     * Gets a message by ID
     * @param id Message ID to look up
     * @return Message object if found, null otherwise
     */
    public Message getMessageById(String id) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            DocumentReference docRef = db.collection(COLLECTION_NAME).document(id);
            ApiFuture<DocumentSnapshot> future = docRef.get();

            DocumentSnapshot document = future.get();
            if (document.exists()) {
                return document.toObject(Message.class);
            } else {
                return null;
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting message: " + e.getMessage());
            return null;
        }
    }

    /**
     * Updates a message (usually to mark as read)
     * @param message Updated message object
     * @return true if successful, false otherwise
     */
    public boolean updateMessage(Message message) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME).document(message.getId()).set(message);

            future.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error updating message: " + e.getMessage());
            return false;
        }
    }

    /**
     * Marks a message as read
     * @param messageId ID of the message to mark as read
     * @return true if successful, false otherwise
     */
    public boolean markMessageAsRead(String messageId) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            DocumentReference docRef = db.collection(COLLECTION_NAME).document(messageId);

            Map<String, Object> updates = new HashMap<>();
            updates.put("read", true);

            ApiFuture<WriteResult> future = docRef.update(updates);
            future.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error marking message as read: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a message
     * @param messageId ID of the message to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteMessage(String messageId) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME).document(messageId).delete();

            future.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error deleting message: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets all messages for a specific recipient
     * @param receiverId ID of the recipient
     * @return List of messages
     */
    public List<Message> getMessagesForReceiver(String receiverId) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            Query query = db.collection(COLLECTION_NAME)
                    .whereEqualTo("receiverId", receiverId)
                    .orderBy("sentDate", Query.Direction.DESCENDING);

            ApiFuture<QuerySnapshot> future = query.get();

            List<Message> messages = new ArrayList<>();
            QuerySnapshot querySnapshot = future.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                messages.add(document.toObject(Message.class));
            }

            return messages;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting messages for receiver: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets all unread messages for a specific recipient
     * @param receiverId ID of the recipient
     * @return List of unread messages
     */
    public List<Message> getUnreadMessagesForReceiver(String receiverId) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            Query query = db.collection(COLLECTION_NAME)
                    .whereEqualTo("receiverId", receiverId)
                    .whereEqualTo("read", false)
                    .orderBy("sentDate", Query.Direction.DESCENDING);

            ApiFuture<QuerySnapshot> future = query.get();

            List<Message> messages = new ArrayList<>();
            QuerySnapshot querySnapshot = future.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                messages.add(document.toObject(Message.class));
            }

            return messages;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting unread messages: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets all messages sent by a specific sender
     * @param senderId ID of the sender
     * @return List of messages
     */
    public List<Message> getMessagesBySender(String senderId) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            Query query = db.collection(COLLECTION_NAME)
                    .whereEqualTo("senderId", senderId)
                    .orderBy("sentDate", Query.Direction.DESCENDING);

            ApiFuture<QuerySnapshot> future = query.get();

            List<Message> messages = new ArrayList<>();
            QuerySnapshot querySnapshot = future.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                messages.add(document.toObject(Message.class));
            }

            return messages;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting messages by sender: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets the conversation between two users
     * @param userId1 ID of the first user
     * @param userId2 ID of the second user
     * @return List of messages in the conversation
     */
    public List<Message> getConversation(String userId1, String userId2) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();

            // Get messages where user1 is sender and user2 is receiver
            Query query1 = db.collection(COLLECTION_NAME)
                    .whereEqualTo("senderId", userId1)
                    .whereEqualTo("receiverId", userId2);

            // Get messages where user2 is sender and user1 is receiver
            Query query2 = db.collection(COLLECTION_NAME)
                    .whereEqualTo("senderId", userId2)
                    .whereEqualTo("receiverId", userId1);

            // Execute both queries
            List<Message> conversation = new ArrayList<>();

            ApiFuture<QuerySnapshot> future1 = query1.get();
            QuerySnapshot querySnapshot1 = future1.get();
            for (DocumentSnapshot document : querySnapshot1.getDocuments()) {
                conversation.add(document.toObject(Message.class));
            }

            ApiFuture<QuerySnapshot> future2 = query2.get();
            QuerySnapshot querySnapshot2 = future2.get();
            for (DocumentSnapshot document : querySnapshot2.getDocuments()) {
                conversation.add(document.toObject(Message.class));
            }

            // Sort the combined list by sent date
            conversation.sort(Comparator.comparing(Message::getSentDate));

            return conversation;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting conversation: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets the count of unread messages for a recipient
     * @param receiverId ID of the recipient
     * @return Number of unread messages
     */
    public int getUnreadMessageCount(String receiverId) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            Query query = db.collection(COLLECTION_NAME)
                    .whereEqualTo("receiverId", receiverId)
                    .whereEqualTo("read", false);

            ApiFuture<QuerySnapshot> future = query.get();
            QuerySnapshot querySnapshot = future.get();

            return querySnapshot.size();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting unread message count: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Marks all messages for a recipient as read
     * @param receiverId ID of the recipient
     * @return Number of messages marked as read
     */
    public int markAllMessagesAsRead(String receiverId) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            Query query = db.collection(COLLECTION_NAME)
                    .whereEqualTo("receiverId", receiverId)
                    .whereEqualTo("read", false);

            ApiFuture<QuerySnapshot> future = query.get();
            QuerySnapshot querySnapshot = future.get();

            int count = 0;
            WriteBatch batch = db.batch();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                DocumentReference docRef = db.collection(COLLECTION_NAME).document(document.getId());
                batch.update(docRef, "read", true);
                count++;
            }

            // Commit the batch
            if (count > 0) {
                batch.commit().get();
            }

            return count;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error marking all messages as read: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Generates a unique ID for a new message
     * @return A unique ID string
     */
    public String generateUniqueId() {
        Firestore db = FirebaseConfig.getFirestoreInstance();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();
        return docRef.getId();
    }
}