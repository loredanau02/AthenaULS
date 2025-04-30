package com.athena.library.firebase;

import com.athena.library.models.Book;
import com.athena.library.models.Student;
import com.athena.library.models.Transaction;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Service class to handle all Transaction-related operations with Firebase
 */
public class TransactionService {
    private static final String COLLECTION_NAME = "transactions";
    private static final int DEFAULT_LOAN_PERIOD_DAYS = 14;
    private static final double DEFAULT_FINE_PER_DAY = 0.50; // $0.50 per day

    private BookService bookService;
    private StudentService studentService;

    /**
     * Default constructor
     */
    public TransactionService() {
        this.bookService = new BookService();
        this.studentService = new StudentService();
    }

    /**
     * Constructor with services
     */
    public TransactionService(BookService bookService, StudentService studentService) {
        this.bookService = bookService;
        this.studentService = studentService;
    }

    /**
     * Borrows a book for a student
     * @param studentId ID of the student borrowing the book
     * @param bookId ID of the book to borrow
     * @param librarianId ID of the librarian processing the transaction
     * @return The created transaction if successful, null otherwise
     */
    public Transaction borrowBook(String studentId, String bookId, String librarianId) {
        try {
            // Check if the book exists and is available
            Book book = bookService.getBookById(bookId);
            if (book == null || !book.isAvailable()) {
                System.err.println("Book not available for borrowing: " + bookId);
                return null;
            }

            // Check if the student exists
            Student student = studentService.getStudentById(studentId);
            if (student == null) {
                System.err.println("Student not found: " + studentId);
                return null;
            }

            // Create a new transaction
            Firestore db = FirebaseConfig.getFirestoreInstance();
            DocumentReference docRef = db.collection(COLLECTION_NAME).document();
            String transactionId = docRef.getId();

            Transaction transaction = new Transaction(transactionId, studentId, bookId, librarianId);

            // Update book availability
            book.borrowCopy();
            bookService.updateBook(book);

            // Update student's borrowed books
            student.addBorrowedBook(bookId);
            studentService.updateStudent(student);

            // Save the transaction
            ApiFuture<WriteResult> future = docRef.set(transaction);
            future.get();

            return transaction;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error borrowing book: " + e.getMessage());
            return null;
        }
    }

    /**
     * Returns a borrowed book
     * @param transactionId ID of the transaction to process
     * @param librarianId ID of the librarian processing the return
     * @return true if successful, false otherwise
     */
    public boolean returnBook(String transactionId, String librarianId) {
        try {
            // Get the transaction
            Transaction transaction = getTransactionById(transactionId);
            if (transaction == null || transaction.getStatus() == Transaction.Status.RETURNED) {
                System.err.println("Transaction not found or already returned: " + transactionId);
                return false;
            }

            // Get the book and student
            Book book = bookService.getBookById(transaction.getBookId());
            Student student = studentService.getStudentById(transaction.getStudentId());

            if (book == null || student == null) {
                System.err.println("Book or student not found for transaction: " + transactionId);
                return false;
            }

            // Check if the book is overdue and calculate fine
            if (transaction.isOverdue()) {
                double fine = transaction.calculateFine(DEFAULT_FINE_PER_DAY);
                if (fine > 0) {
                    student.addFine(fine);
                }
            }

            // Update the transaction status
            transaction.returnBook();
            transaction.setLibrarianId(librarianId); // Update the librarian who processed the return

            // Update the book availability
            book.returnCopy();

            // Update the student's borrowed books
            student.removeBorrowedBook(transaction.getBookId());

            // Save all changes
            Firestore db = FirebaseConfig.getFirestoreInstance();
            ApiFuture<WriteResult> transactionFuture = db.collection(COLLECTION_NAME).document(transactionId).set(transaction);
            bookService.updateBook(book);
            studentService.updateStudent(student);

            transactionFuture.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error returning book: " + e.getMessage());
            return false;
        }
    }

    /**
     * Renews a borrowed book
     * @param transactionId ID of the transaction to renew
     * @param daysToExtend Number of days to extend the loan period
     * @return true if successful, false otherwise
     */
    public boolean renewBook(String transactionId, int daysToExtend) {
        try {
            // Get the transaction
            Transaction transaction = getTransactionById(transactionId);
            if (transaction == null ||
                    transaction.getStatus() == Transaction.Status.RETURNED ||
                    transaction.getStatus() == Transaction.Status.LOST) {
                System.err.println("Transaction not valid for renewal: " + transactionId);
                return false;
            }

            // Check if the book is overdue
            if (transaction.isOverdue()) {
                System.err.println("Overdue books cannot be renewed: " + transactionId);
                return false;
            }

            // Renew the book
            boolean renewed = transaction.renewBook(daysToExtend);
            if (!renewed) {
                return false;
            }

            // Save the updated transaction
            Firestore db = FirebaseConfig.getFirestoreInstance();
            ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME).document(transactionId).set(transaction);
            future.get();

            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error renewing book: " + e.getMessage());
            return false;
        }
    }

    /**
     * Marks a book as lost
     * @param transactionId ID of the transaction to mark as lost
     * @param librarianId ID of the librarian processing the loss
     * @param lossFee Fee to charge for the lost book
     * @return true if successful, false otherwise
     */
    public boolean markBookAsLost(String transactionId, String librarianId, double lossFee) {
        try {
            // Get the transaction
            Transaction transaction = getTransactionById(transactionId);
            if (transaction == null ||
                    transaction.getStatus() == Transaction.Status.RETURNED ||
                    transaction.getStatus() == Transaction.Status.LOST) {
                System.err.println("Transaction not valid for marking as lost: " + transactionId);
                return false;
            }

            // Get the student
            Student student = studentService.getStudentById(transaction.getStudentId());
            if (student == null) {
                System.err.println("Student not found for transaction: " + transactionId);
                return false;
            }

            // Mark the book as lost
            transaction.markAsLost();
            transaction.setLibrarianId(librarianId);
            transaction.setFine(lossFee); // Set the loss fee

            // Add the fee to the student's account
            student.addFine(lossFee);

            // Remove the book from the student's borrowed books
            student.removeBorrowedBook(transaction.getBookId());

            // No need to update book availability since the book is lost

            // Save all changes
            Firestore db = FirebaseConfig.getFirestoreInstance();
            ApiFuture<WriteResult> transactionFuture = db.collection(COLLECTION_NAME).document(transactionId).set(transaction);
            studentService.updateStudent(student);

            transactionFuture.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error marking book as lost: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets a transaction by ID
     * @param id Transaction ID to look up
     * @return Transaction object if found, null otherwise
     */
    public Transaction getTransactionById(String id) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            DocumentReference docRef = db.collection(COLLECTION_NAME).document(id);
            ApiFuture<DocumentSnapshot> future = docRef.get();

            DocumentSnapshot document = future.get();
            if (document.exists()) {
                return document.toObject(Transaction.class);
            } else {
                return null;
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting transaction: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets all transactions for a student
     * @param studentId ID of the student
     * @return List of transactions
     */
    public List<Transaction> getTransactionsByStudent(String studentId) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            Query query = db.collection(COLLECTION_NAME).whereEqualTo("studentId", studentId);
            ApiFuture<QuerySnapshot> future = query.get();

            List<Transaction> transactions = new ArrayList<>();
            QuerySnapshot querySnapshot = future.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                transactions.add(document.toObject(Transaction.class));
            }

            return transactions;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting transactions by student: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets all active (non-returned) transactions for a student
     * @param studentId ID of the student
     * @return List of active transactions
     */
    public List<Transaction> getActiveTransactionsByStudent(String studentId) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            Query query = db.collection(COLLECTION_NAME)
                    .whereEqualTo("studentId", studentId)
                    .whereNotEqualTo("status", Transaction.Status.RETURNED.toString());

            ApiFuture<QuerySnapshot> future = query.get();

            List<Transaction> transactions = new ArrayList<>();
            QuerySnapshot querySnapshot = future.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Transaction transaction = document.toObject(Transaction.class);
                // Double-check the status since Firebase might have issues with enum comparison
                if (transaction != null && transaction.getStatus() != Transaction.Status.RETURNED) {
                    transactions.add(transaction);
                }
            }

            return transactions;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting active transactions by student: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets all transactions for a book
     * @param bookId ID of the book
     * @return List of transactions
     */
    public List<Transaction> getTransactionsByBook(String bookId) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            Query query = db.collection(COLLECTION_NAME).whereEqualTo("bookId", bookId);
            ApiFuture<QuerySnapshot> future = query.get();

            List<Transaction> transactions = new ArrayList<>();
            QuerySnapshot querySnapshot = future.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                transactions.add(document.toObject(Transaction.class));
            }

            return transactions;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting transactions by book: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets all overdue transactions
     * @return List of overdue transactions
     */
    public List<Transaction> getOverdueTransactions() {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            // Get all non-returned transactions
            Query query = db.collection(COLLECTION_NAME)
                    .whereNotEqualTo("status", Transaction.Status.RETURNED.toString())
                    .whereNotEqualTo("status", Transaction.Status.LOST.toString());

            ApiFuture<QuerySnapshot> future = query.get();

            List<Transaction> overdueTransactions = new ArrayList<>();
            QuerySnapshot querySnapshot = future.get();

            Date now = new Date();
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Transaction transaction = document.toObject(Transaction.class);
                // Check if actually overdue
                if (transaction != null && transaction.getDueDate().before(now)) {
                    // Update the status to OVERDUE if not already
                    if (transaction.getStatus() != Transaction.Status.OVERDUE) {
                        transaction.setStatus(Transaction.Status.OVERDUE);
                        // Update in database
                        db.collection(COLLECTION_NAME).document(transaction.getId()).set(transaction);
                    }
                    overdueTransactions.add(transaction);
                }
            }

            return overdueTransactions;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting overdue transactions: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets all transactions processed by a specific librarian
     * @param librarianId ID of the librarian
     * @return List of transactions
     */
    public List<Transaction> getTransactionsByLibrarian(String librarianId) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            Query query = db.collection(COLLECTION_NAME).whereEqualTo("librarianId", librarianId);
            ApiFuture<QuerySnapshot> future = query.get();

            List<Transaction> transactions = new ArrayList<>();
            QuerySnapshot querySnapshot = future.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                transactions.add(document.toObject(Transaction.class));
            }

            return transactions;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting transactions by librarian: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets transactions by date range
     * @param startDate Start date
     * @param endDate End date
     * @return List of transactions in the date range
     */
    public List<Transaction> getTransactionsByDateRange(Date startDate, Date endDate) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            Query query = db.collection(COLLECTION_NAME)
                    .whereGreaterThanOrEqualTo("borrowDate", startDate)
                    .whereLessThanOrEqualTo("borrowDate", endDate);

            ApiFuture<QuerySnapshot> future = query.get();

            List<Transaction> transactions = new ArrayList<>();
            QuerySnapshot querySnapshot = future.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                transactions.add(document.toObject(Transaction.class));
            }

            return transactions;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting transactions by date range: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets the total number of transactions
     * @return Number of transactions
     */
    public int getTransactionCount() {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();

            QuerySnapshot querySnapshot = future.get();
            return querySnapshot.size();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting transaction count: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Generates a unique ID for a new transaction
     * @return A unique ID string
     */
    public String generateUniqueId() {
        Firestore db = FirebaseConfig.getFirestoreInstance();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();
        return docRef.getId();
    }
}