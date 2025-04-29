package com.athena.library.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a book in the library system
 */
public class Book {
    private String id;
    private String title;
    private List<String> authors;
    private String publisher;
    private String isbn;
    private int publicationYear;
    private List<String> genres;
    private String description;
    private int totalCopies;
    private int availableCopies;
    private String location; // Shelf number or section in the library
    private Date addedDate;
    private Date lastUpdated;
    private String coverImageUrl;

    /**
     * Default constructor
     */
    public Book() {
        this.authors = new ArrayList<>();
        this.genres = new ArrayList<>();
        this.addedDate = new Date();
        this.lastUpdated = new Date();
    }

    /**
     * Constructor with essential fields
     */
    public Book(String id, String title, List<String> authors, String publisher,
                String isbn, int publicationYear, int totalCopies) {
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.publisher = publisher;
        this.isbn = isbn;
        this.publicationYear = publicationYear;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
        this.genres = new ArrayList<>();
        this.addedDate = new Date();
        this.lastUpdated = new Date();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.lastUpdated = new Date();
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
        this.lastUpdated = new Date();
    }

    /**
     * Gets a formatted string of all authors
     * @return String containing all authors separated by commas
     */
    public String getAuthorsAsString() {
        if (authors == null || authors.isEmpty()) {
            return "Unknown";
        }
        return String.join(", ", authors);
    }

    /**
     * Adds an author to the book
     * @param author Author name to add
     */
    public void addAuthor(String author) {
        if (this.authors == null) {
            this.authors = new ArrayList<>();
        }
        this.authors.add(author);
        this.lastUpdated = new Date();
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
        this.lastUpdated = new Date();
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
        this.lastUpdated = new Date();
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
        this.lastUpdated = new Date();
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
        this.lastUpdated = new Date();
    }

    /**
     * Gets a formatted string of all genres
     * @return String containing all genres separated by commas
     */
    public String getGenresAsString() {
        if (genres == null || genres.isEmpty()) {
            return "Uncategorized";
        }
        return String.join(", ", genres);
    }

    /**
     * Adds a genre to the book
     * @param genre Genre to add
     */
    public void addGenre(String genre) {
        if (this.genres == null) {
            this.genres = new ArrayList<>();
        }
        this.genres.add(genre);
        this.lastUpdated = new Date();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.lastUpdated = new Date();
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
        this.lastUpdated = new Date();
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
        this.lastUpdated = new Date();
    }

    /**
     * Checks if the book is available for borrowing
     * @return true if there are available copies, false otherwise
     */
    public boolean isAvailable() {
        return availableCopies > 0;
    }

    /**
     * Borrows a copy of this book
     * @return true if a copy was successfully borrowed, false if no copies available
     */
    public boolean borrowCopy() {
        if (availableCopies > 0) {
            availableCopies--;
            this.lastUpdated = new Date();
            return true;
        }
        return false;
    }

    /**
     * Returns a copy of this book
     */
    public void returnCopy() {
        if (availableCopies < totalCopies) {
            availableCopies++;
            this.lastUpdated = new Date();
        }
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        this.lastUpdated = new Date();
    }

    public Date getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
        this.lastUpdated = new Date();
    }

    /**
     * Updates the last updated timestamp to now
     */
    public void updateLastUpdated() {
        this.lastUpdated = new Date();
    }

    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", authors=" + getAuthorsAsString() +
                ", publisher='" + publisher + '\'' +
                ", isbn='" + isbn + '\'' +
                ", publicationYear=" + publicationYear +
                ", available=" + availableCopies + "/" + totalCopies +
                '}';
    }
}