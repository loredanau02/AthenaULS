package com.athena.library.firebase;

import com.athena.library.models.Book;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Service class to handle all Book-related operations with Firebase
 */
public class BookService {
    private static final String COLLECTION_NAME = "books";

    /**
     * Adds a new book to the database
     * @param book Book object to add
     * @return true if successful, false otherwise
     */
    public boolean addBook(Book book) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME).document(book.getId()).set(book);

            // Wait for the operation to complete
            future.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error adding book: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets a book by ID
     * @param id Book ID to look up
     * @return Book object if found, null otherwise
     */
    public Book getBookById(String id) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            DocumentReference docRef = db.collection(COLLECTION_NAME).document(id);
            ApiFuture<DocumentSnapshot> future = docRef.get();

            DocumentSnapshot document = future.get();
            if (document.exists()) {
                return document.toObject(Book.class);
            } else {
                return null;
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting book: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets a book by ISBN
     * @param isbn ISBN to look up
     * @return Book object if found, null otherwise
     */
    public Book getBookByIsbn(String isbn) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            Query query = db.collection(COLLECTION_NAME).whereEqualTo("isbn", isbn);
            ApiFuture<QuerySnapshot> future = query.get();

            QuerySnapshot querySnapshot = future.get();
            if (!querySnapshot.isEmpty()) {
                return querySnapshot.getDocuments().get(0).toObject(Book.class);
            } else {
                return null;
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting book by ISBN: " + e.getMessage());
            return null;
        }
    }

    /**
     * Updates an existing book's information
     * @param book Updated book object
     * @return true if successful, false otherwise
     */
    public boolean updateBook(Book book) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME).document(book.getId()).set(book);

            // Wait for the operation to complete
            future.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error updating book: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates only specific fields of a book document
     * @param bookId ID of the book to update
     * @param updates Map of field names to new values
     * @return true if successful, false otherwise
     */
    public boolean updateBookFields(String bookId, Map<String, Object> updates) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            DocumentReference docRef = db.collection(COLLECTION_NAME).document(bookId);

            ApiFuture<WriteResult> future = docRef.update(updates);
            future.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error updating book fields: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a book from the database
     * @param bookId ID of the book to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteBook(String bookId) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME).document(bookId).delete();

            // Wait for the operation to complete
            future.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error deleting book: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets a list of all books
     * @return List of all books
     */
    public List<Book> getAllBooks() {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();

            List<Book> books = new ArrayList<>();
            QuerySnapshot querySnapshot = future.get();
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                books.add(document.toObject(Book.class));
            }

            return books;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting all books: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Searches for books by title
     * @param title Title to search for
     * @return List of matching books
     */
    public List<Book> searchBooksByTitle(String title) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();

            // Convert to lowercase for case-insensitive search
            String searchTitle = title.toLowerCase();

            // Firebase doesn't support direct substring search, so we need to do this manually
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();

            List<Book> matchingBooks = new ArrayList<>();
            QuerySnapshot querySnapshot = future.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Book book = document.toObject(Book.class);
                if (book != null && book.getTitle().toLowerCase().contains(searchTitle)) {
                    matchingBooks.add(book);
                }
            }

            return matchingBooks;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error searching books by title: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Searches for books by author
     * @param author Author to search for
     * @return List of matching books
     */
    public List<Book> searchBooksByAuthor(String author) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();

            // Convert to lowercase for case-insensitive search
            String searchAuthor = author.toLowerCase();

            // Firebase doesn't support direct array containment search for substrings
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();

            List<Book> matchingBooks = new ArrayList<>();
            QuerySnapshot querySnapshot = future.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Book book = document.toObject(Book.class);
                if (book != null) {
                    // Check if any author contains the search term
                    for (String bookAuthor : book.getAuthors()) {
                        if (bookAuthor.toLowerCase().contains(searchAuthor)) {
                            matchingBooks.add(book);
                            break;
                        }
                    }
                }
            }

            return matchingBooks;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error searching books by author: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets books by genre
     * @param genre Genre to filter by
     * @return List of matching books
     */
    public List<Book> getBooksByGenre(String genre) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            Query query = db.collection(COLLECTION_NAME).whereArrayContains("genres", genre);
            ApiFuture<QuerySnapshot> future = query.get();

            List<Book> books = new ArrayList<>();
            QuerySnapshot querySnapshot = future.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                books.add(document.toObject(Book.class));
            }

            return books;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting books by genre: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets books by publication year
     * @param year Publication year to filter by
     * @return List of matching books
     */
    public List<Book> getBooksByYear(int year) {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            Query query = db.collection(COLLECTION_NAME).whereEqualTo("publicationYear", year);
            ApiFuture<QuerySnapshot> future = query.get();

            List<Book> books = new ArrayList<>();
            QuerySnapshot querySnapshot = future.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                books.add(document.toObject(Book.class));
            }

            return books;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting books by year: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets only available books (with at least one copy available)
     * @return List of available books
     */
    public List<Book> getAvailableBooks() {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            Query query = db.collection(COLLECTION_NAME).whereGreaterThan("availableCopies", 0);
            ApiFuture<QuerySnapshot> future = query.get();

            List<Book> books = new ArrayList<>();
            QuerySnapshot querySnapshot = future.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                books.add(document.toObject(Book.class));
            }

            return books;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting available books: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets the total number of books in the database
     * @return Number of books
     */
    public int getBookCount() {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();

            QuerySnapshot querySnapshot = future.get();
            return querySnapshot.size();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting book count: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Gets the total number of unique titles in the database
     * @return Number of unique titles
     */
    public int getUniqueTitleCount() {
        return getBookCount(); // Since each document is a unique title
    }

    /**
     * Gets the total number of book copies (both available and borrowed)
     * @return Total number of book copies
     */
    public int getTotalCopiesCount() {
        try {
            Firestore db = FirebaseConfig.getFirestoreInstance();
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();

            int totalCopies = 0;
            QuerySnapshot querySnapshot = future.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Book book = document.toObject(Book.class);
                if (book != null) {
                    totalCopies += book.getTotalCopies();
                }
            }

            return totalCopies;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting total copies count: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Generates a unique ID for a new book
     * @return A unique ID string
     */
    public String generateUniqueId() {
        Firestore db = FirebaseConfig.getFirestoreInstance();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();
        return docRef.getId();
    }
}