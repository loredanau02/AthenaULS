package com.athena.library.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a student in the library system
 */
public class Student extends User {
    private String studentId; // University ID (different from system ID)
    private String department;
    private String program;
    private int year;
    private Date enrollmentDate;
    private List<String> borrowedBookIds;
    private double fineBalance;

    /**
     * Default constructor
     */
    public Student() {
        super();
        this.borrowedBookIds = new ArrayList<>();
        this.fineBalance = 0.0;
    }

    /**
     * Constructor with essential fields
     */
    public Student(String id, String firstName, String lastName, String email,
                   String studentId, String department, String program, int year) {
        super(id, firstName, lastName, email, null, null);
        this.studentId = studentId;
        this.department = department;
        this.program = program;
        this.year = year;
        this.enrollmentDate = new Date();
        this.borrowedBookIds = new ArrayList<>();
        this.fineBalance = 0.0;
    }

    /**
     * Full constructor
     */
    public Student(String id, String firstName, String lastName, String email,
                   String phoneNumber, String address, String studentId,
                   String department, String program, int year, Date enrollmentDate) {
        super(id, firstName, lastName, email, phoneNumber, address);
        this.studentId = studentId;
        this.department = department;
        this.program = program;
        this.year = year;
        this.enrollmentDate = enrollmentDate;
        this.borrowedBookIds = new ArrayList<>();
        this.fineBalance = 0.0;
    }

    // Getters and Setters

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Date getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(Date enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public List<String> getBorrowedBookIds() {
        return borrowedBookIds;
    }

    public void setBorrowedBookIds(List<String> borrowedBookIds) {
        this.borrowedBookIds = borrowedBookIds;
    }

    /**
     * Adds a book to the student's borrowed books list
     * @param bookId ID of the book to add
     */
    public void addBorrowedBook(String bookId) {
        if (this.borrowedBookIds == null) {
            this.borrowedBookIds = new ArrayList<>();
        }
        this.borrowedBookIds.add(bookId);
    }

    /**
     * Removes a book from the student's borrowed books list
     * @param bookId ID of the book to remove
     * @return true if the book was removed, false if it wasn't in the list
     */
    public boolean removeBorrowedBook(String bookId) {
        if (this.borrowedBookIds == null) {
            return false;
        }
        return this.borrowedBookIds.remove(bookId);
    }

    public double getFineBalance() {
        return fineBalance;
    }

    public void setFineBalance(double fineBalance) {
        this.fineBalance = fineBalance;
    }

    /**
     * Adds a fine amount to the student's balance
     * @param amount Amount to add
     */
    public void addFine(double amount) {
        this.fineBalance += amount;
    }

    /**
     * Pays a fine amount from the student's balance
     * @param amount Amount to pay
     * @return true if payment was successful, false if insufficient balance
     */
    public boolean payFine(double amount) {
        if (amount <= this.fineBalance) {
            this.fineBalance -= amount;
            return true;
        }
        return false;
    }

    /**
     * Checks if the student has any borrowed books
     * @return true if the student has borrowed books, false otherwise
     */
    public boolean hasBorrowedBooks() {
        return this.borrowedBookIds != null && !this.borrowedBookIds.isEmpty();
    }

    /**
     * Gets the number of books currently borrowed by the student
     * @return Number of borrowed books
     */
    public int getBorrowedBooksCount() {
        return this.borrowedBookIds == null ? 0 : this.borrowedBookIds.size();
    }

    @Override
    public String toString() {
        return "Student{" +
                "id='" + getId() + '\'' +
                ", studentId='" + studentId + '\'' +
                ", name='" + getFullName() + '\'' +
                ", department='" + department + '\'' +
                ", program='" + program + '\'' +
                ", year=" + year +
                ", borrowedBooks=" + getBorrowedBooksCount() +
                ", fineBalance=" + fineBalance +
                '}';
    }
}