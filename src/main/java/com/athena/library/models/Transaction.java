package com.athena.library.models;

import java.util.Date;

/**
 * Represents a book borrowing/returning transaction in the library system
 */
public class Transaction {
    public enum Status {
        BORROWED,
        RETURNED,
        OVERDUE,
        LOST,
        RENEWED
    }

    private String id;
    private String studentId;
    private String bookId;
    private Date borrowDate;
    private Date dueDate;
    private Date returnDate;
    private Status status;
    private String librarianId; // ID of the librarian who processed the transaction
    private double fine;
    private String notes;

    /**
     * Default constructor
     */
    public Transaction() {
    }

    /**
     * Constructor for a new borrowing transaction
     */
    public Transaction(String id, String studentId, String bookId, String librarianId) {
        this.id = id;
        this.studentId = studentId;
        this.bookId = bookId;
        this.borrowDate = new Date();
        this.librarianId = librarianId;

        // Default due date is 14 days from borrowing
        Date due = new Date();
        due.setTime(due.getTime() + 14 * 24 * 60 * 60 * 1000);
        this.dueDate = due;

        this.status = Status.BORROWED;
        this.fine = 0.0;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public Date getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(Date borrowDate) {
        this.borrowDate = borrowDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getLibrarianId() {
        return librarianId;
    }

    public void setLibrarianId(String librarianId) {
        this.librarianId = librarianId;
    }

    public double getFine() {
        return fine;
    }

    public void setFine(double fine) {
        this.fine = fine;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Returns the book and marks the transaction as returned
     * @return true if the book was successfully returned, false if it was already returned
     */
    public boolean returnBook() {
        if (status != Status.RETURNED && status != Status.LOST) {
            this.returnDate = new Date();
            this.status = Status.RETURNED;
            return true;
        }
        return false;
    }

    /**
     * Renews the book by extending the due date
     * @param days Number of days to extend
     * @return true if renewal was successful, false if book cannot be renewed
     */
    public boolean renewBook(int days) {
        if (status == Status.BORROWED) {
            // Extend due date by the specified number of days
            Date newDueDate = new Date();
            newDueDate.setTime(dueDate.getTime() + days * 24 * 60 * 60 * 1000);
            this.dueDate = newDueDate;
            this.status = Status.RENEWED;
            return true;
        }
        return false;
    }

    /**
     * Calculates the fine for an overdue book
     * @param finePerDay Amount to charge per day overdue
     * @return The calculated fine amount
     */
    public double calculateFine(double finePerDay) {
        if (status != Status.RETURNED && status != Status.LOST) {
            Date now = new Date();
            if (now.after(dueDate)) {
                // Calculate days overdue
                long diff = now.getTime() - dueDate.getTime();
                long daysOverdue = diff / (24 * 60 * 60 * 1000);

                this.fine = daysOverdue * finePerDay;
                if (status != Status.OVERDUE) {
                    this.status = Status.OVERDUE;
                }
            }
        }
        return this.fine;
    }

    /**
     * Checks if the book is currently overdue
     * @return true if the book is overdue, false otherwise
     */
    public boolean isOverdue() {
        if (status != Status.RETURNED && status != Status.LOST) {
            Date now = new Date();
            return now.after(dueDate);
        }
        return false;
    }

    /**
     * Gets the number of days until the book is due (or overdue)
     * @return Positive number for days remaining, negative for days overdue
     */
    public int getDaysRemaining() {
        if (status != Status.RETURNED && status != Status.LOST) {
            Date now = new Date();
            long diff = dueDate.getTime() - now.getTime();
            return (int) (diff / (24 * 60 * 60 * 1000));
        }
        return 0;
    }

    /**
     * Marks the book as lost
     */
    public void markAsLost() {
        this.status = Status.LOST;
        // Usually a fixed fee for lost books would be applied here
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", studentId='" + studentId + '\'' +
                ", bookId='" + bookId + '\'' +
                ", borrowDate=" + borrowDate +
                ", dueDate=" + dueDate +
                ", status=" + status +
                ", fine=" + fine +
                '}';
    }