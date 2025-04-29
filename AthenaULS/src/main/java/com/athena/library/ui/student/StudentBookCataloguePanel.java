package com.athena.library.ui.student;

import com.athena.library.firebase.BookService;
import com.athena.library.firebase.TransactionService;
import com.athena.library.models.Book;
import com.athena.library.models.Student;
import com.athena.library.models.Transaction;
import com.athena.library.utils.UIUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for displaying and searching the book catalogue
 */
public class StudentBookCataloguePanel extends JPanel {
    private final StudentDashboard dashboard;
    private final Student student;
    private final BookService bookService;
    private final TransactionService transactionService;

    // UI components
    private JTextField searchField;
    private JComboBox<String> searchTypeComboBox;
    private JComboBox<String> genreFilterComboBox;
    private JCheckBox availableOnlyCheckBox;
    private JTable booksTable;
    private DefaultTableModel tableModel;
    private JButton searchButton;
    private JButton clearButton;
    private JButton viewDetailsButton;
    private JButton reserveButton;

    // Data
    private List<Book> allBooks;
    private List<Book> filteredBooks;

    /**
     * Creates a new book catalogue panel
     * @param dashboard The parent dashboard
     * @param student The current student
     */
    public StudentBookCataloguePanel(StudentDashboard dashboard, Student student) {
        this.dashboard = dashboard;
        this.student = student;
        this.bookService = new BookService();
        this.transactionService = new TransactionService();
        this.allBooks = new ArrayList<>();
        this.filteredBooks = new ArrayList<>();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initializeUI();
        loadBooks();
    }

    /**
     * Initializes the UI components
     */
    private void initializeUI() {
        // Search panel at top
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);

        // Books table in center
        JPanel booksPanel = createBooksPanel();
        add(booksPanel, BorderLayout.CENTER);

        // Action buttons at bottom
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates the search panel
     * @return The search panel
     */
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBorder(BorderFactory.createTitledBorder(
                UIUtils.PANEL_BORDER,
                "Search Books",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                UIUtils.SUBHEADER_FONT,
                UIUtils.PRIMARY_COLOR));
        panel.setBackground(UIUtils.BACKGROUND_COLOR);

        // Search controls
        JPanel searchControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchControlsPanel.setBackground(UIUtils.BACKGROUND_COLOR);

        // Search type dropdown
        searchTypeComboBox = new JComboBox<>(new String[] { "Title", "Author", "ISBN", "Year" });
        searchControlsPanel.add(new JLabel("Search by:"));
        searchControlsPanel.add(searchTypeComboBox);

        // Search field
        searchField = new JTextField(20);
        searchControlsPanel.add(searchField);

        // Search button
        searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilters();
            }
        });
        searchControlsPanel.add(searchButton);

        // Clear button
        clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFilters();
            }
        });
        searchControlsPanel.add(clearButton);

        panel.add(searchControlsPanel, BorderLayout.NORTH);

        // Filter controls
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(UIUtils.BACKGROUND_COLOR);

        // Genre filter
        filterPanel.add(new JLabel("Genre:"));
        genreFilterComboBox = new JComboBox<>(new String[] { "All Genres" });
        filterPanel.add(genreFilterComboBox);

        // Available only checkbox
        availableOnlyCheckBox = new JCheckBox("Show Available Books Only");
        availableOnlyCheckBox.setBackground(UIUtils.BACKGROUND_COLOR);
        availableOnlyCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilters();
            }
        });
        filterPanel.add(availableOnlyCheckBox);

        panel.add(filterPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates the books table panel
     * @return The books panel
     */
    private JPanel createBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                UIUtils.PANEL_BORDER,
                "Available Books",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                UIUtils.SUBHEADER_FONT,
                UIUtils.PRIMARY_COLOR));
        panel.setBackground(UIUtils.BACKGROUND_COLOR);

        // Create table model with columns
        String[] columns = {"Title", "Author(s)", "Year", "Genre(s)", "Available", "Location"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        // Create table
        booksTable = new JTable(tableModel);
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        booksTable.setRowHeight(25);
        booksTable.getTableHeader().setReorderingAllowed(false);

        // Add double-click listener
        booksTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewBookDetails();
                }
            }
        });

        // Create sorter
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        booksTable.setRowSorter(sorter);

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(booksTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates the button panel
     * @return The button panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(UIUtils.BACKGROUND_COLOR);

        viewDetailsButton = new JButton("View Details");
        viewDetailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewBookDetails();
            }
        });

        reserveButton = new JButton("Reserve Book");
        reserveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reserveBook();
            }
        });

        panel.add(viewDetailsButton);
        panel.add(reserveButton);

        return panel;
    }

    /**
     * Loads all books from the database
     */
    private void loadBooks() {
        // Show loading indicator
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<List<Book>, Void> worker = new SwingWorker<List<Book>, Void>() {
            @Override
            protected List<Book> doInBackground() throws Exception {
                return bookService.getAllBooks();
            }

            @Override
            protected void done() {
                try {
                    allBooks = get();
                    filteredBooks = new ArrayList<>(allBooks);

                    // Populate the table
                    updateBooksTable();

                    // Extract and populate genres
                    populateGenreFilter();

                    // Reset cursor
                    setCursor(Cursor.getDefaultCursor());
                } catch (Exception e) {
                    e.printStackTrace();
                    UIUtils.showErrorDialog(StudentBookCataloguePanel.this,
                            "Error loading books: " + e.getMessage(), "Error");
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };

        worker.execute();
    }

    /**
     * Updates the books table with current filtered books
     */
    private void updateBooksTable() {
        // Clear the table
        tableModel.setRowCount(0);

        // Add books to table
        for (Book book : filteredBooks) {
            Object[] row = {
                    book.getTitle(),
                    book.getAuthorsAsString(),
                    book.getPublicationYear(),
                    book.getGenresAsString(),
                    book.getAvailableCopies() + " of " + book.getTotalCopies(),
                    book.getLocation()
            };
            tableModel.addRow(row);
        }

        // Update status message
        if (dashboard != null) {
            dashboard.updateStatus("Showing " + filteredBooks.size() + " books", false);
        }
    }

    /**
     * Populates the genre filter dropdown with unique genres
     */
    private void populateGenreFilter() {
        // Remember current selection
        String currentSelection = genreFilterComboBox.getSelectedItem() != null ?
                genreFilterComboBox.getSelectedItem().toString() : "All Genres";

        // Clear the combo box
        genreFilterComboBox.removeAllItems();
        genreFilterComboBox.addItem("All Genres");

        // Create a set of unique genres
        java.util.Set<String> genres = new java.util.HashSet<>();

        // Collect all genres
        for (Book book : allBooks) {
            if (book.getGenres() != null) {
                genres.addAll(book.getGenres());
            }
        }

        // Add genres to combo box
        for (String genre : genres) {
            genreFilterComboBox.addItem(genre);
        }

        // Restore selection if it exists
        for (int i = 0; i < genreFilterComboBox.getItemCount(); i++) {
            if (genreFilterComboBox.getItemAt(i).equals(currentSelection)) {
                genreFilterComboBox.setSelectedIndex(i);
                break;
            }
        }

        // Add action listener
        genreFilterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilters();
            }
        });
    }

    /**
     * Applies the current search and filters to the book list
     */
    private void applyFilters() {
        // Get filter values
        String searchText = searchField.getText().trim().toLowerCase();
        String searchType = searchTypeComboBox.getSelectedItem().toString();
        String genreFilter = genreFilterComboBox.getSelectedItem().toString();
        boolean availableOnly = availableOnlyCheckBox.isSelected();

        // Apply filters
        filteredBooks = new ArrayList<>();

        for (Book book : allBooks) {
            // Skip unavailable books if filter is active
            if (availableOnly && book.getAvailableCopies() <= 0) {
                continue;
            }

            // Filter by genre if not "All Genres"
            if (!genreFilter.equals("All Genres") &&
                    (book.getGenres() == null || !book.getGenres().contains(genreFilter))) {
                continue;
            }

            // Filter by search text
            if (!searchText.isEmpty()) {
                boolean match = false;

                switch (searchType) {
                    case "Title":
                        match = book.getTitle().toLowerCase().contains(searchText);
                        break;
                    case "Author":
                        if (book.getAuthors() != null) {
                            for (String author : book.getAuthors()) {
                                if (author.toLowerCase().contains(searchText)) {
                                    match = true;
                                    break;
                                }
                            }
                        }
                        break;
                    case "ISBN":
                        match = book.getIsbn() != null && book.getIsbn().toLowerCase().contains(searchText);
                        break;
                    case "Year":
                        match = String.valueOf(book.getPublicationYear()).contains(searchText);
                        break;
                }

                if (!match) {
                    continue;
                }
            }

            // If we get here, the book passed all filters
            filteredBooks.add(book);
        }

        // Update the table
        updateBooksTable();
    }

    /**
     * Clears all filters and search criteria
     */
    private void clearFilters() {
        searchField.setText("");
        searchTypeComboBox.setSelectedIndex(0);
        genreFilterComboBox.setSelectedIndex(0);
        availableOnlyCheckBox.setSelected(false);

        // Reset filtered books to all books
        filteredBooks = new ArrayList<>(allBooks);
        updateBooksTable();
    }

    /**
     * Opens the book details dialog for the selected book
     */
    private void viewBookDetails() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow >= 0) {
            // Convert view index to model index
            int modelRow = booksTable.convertRowIndexToModel(selectedRow);

            // Get the book
            Book book = filteredBooks.get(modelRow);

            // Show details dialog
            BookDetailsDialog dialog = new BookDetailsDialog(
                    SwingUtilities.getWindowAncestor(this), book);
            dialog.setVisible(true);
        } else {
            UIUtils.showWarningDialog(this, "Please select a book to view its details.", "No Book Selected");
        }
    }

    /**
     * Reserves a book for the current student
     */
    private void reserveBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow >= 0) {
            // Convert view index to model index
            int modelRow = booksTable.convertRowIndexToModel(selectedRow);

            // Get the book
            Book book = filteredBooks.get(modelRow);

            // Check if the book is available
            if (book.getAvailableCopies() <= 0) {
                UIUtils.showWarningDialog(this, "This book is currently not available.", "Book Unavailable");
                return;
            }

            // Check if student has reached borrowing limit
            if (student.getBorrowedBooksCount() >= 5) {
                UIUtils.showWarningDialog(this,
                        "You have reached the maximum number of books you can borrow (5).",
                        "Borrowing Limit Reached");
                return;
            }

            // Check if student has fines
            if (student.getFineBalance() > 0) {
                UIUtils.showWarningDialog(this,
                        "You have unpaid fines. Please pay your fines before borrowing more books.",
                        "Unpaid Fines");
                return;
            }

            // Confirm reservation
            boolean confirm = UIUtils.showConfirmDialog(this,
                    "Do you want to reserve \"" + book.getTitle() + "\"?",
                    "Confirm Reservation");

            if (confirm) {
                // Show loading indicator
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                SwingWorker<Transaction, Void> worker = new SwingWorker<Transaction, Void>() {
                    @Override
                    protected Transaction doInBackground() throws Exception {
                        return transactionService.borrowBook(student.getId(), book.getId(), null);
                    }

                    @Override
                    protected void done() {
                        try {
                            Transaction transaction = get();
                            if (transaction != null) {
                                // Update student's borrowed books
                                student.addBorrowedBook(book.getId());

                                // Update the book's availability
                                book.setAvailableCopies(book.getAvailableCopies() - 1);

                                // Update the table
                                updateBooksTable();

                                // Show success message
                                UIUtils.showInfoDialog(StudentBookCataloguePanel.this,
                                        "Book reserved successfully. Due date: " +
                                                UIUtils.formatDate(transaction.getDueDate()),
                                        "Book Reserved");
                            } else {
                                UIUtils.showErrorDialog(StudentBookCataloguePanel.this,
                                        "Failed to reserve book. Please try again.",
                                        "Reservation Failed");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            UIUtils.showErrorDialog(StudentBookCataloguePanel.this,
                                    "Error reserving book: " + e.getMessage(),
                                    "Error");
                        } finally {
                            setCursor(Cursor.getDefaultCursor());
                        }
                    }
                };

                worker.execute();
            }
        } else {
            UIUtils.showWarningDialog(this, "Please select a book to reserve.", "No Book Selected");
        }
    }

    /**
     * Inner class for the book details dialog
     */
    private class BookDetailsDialog extends JDialog {
        /**
         * Creates a new book details dialog
         * @param parent Parent window
         * @param book Book to display
         */
        public BookDetailsDialog(Window parent, Book book) {
            super(parent, "Book Details", ModalityType.APPLICATION_MODAL);
            setSize(600, 500);
            setLocationRelativeTo(parent);

            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            mainPanel.setBackground(UIUtils.BACKGROUND_COLOR);

            // Book title header
            JLabel titleLabel = new JLabel(book.getTitle());
            titleLabel.setFont(UIUtils.HEADER_FONT);
            titleLabel.setForeground(UIUtils.PRIMARY_COLOR);
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            // Book details panel
            JPanel detailsPanel = new JPanel(new GridBagLayout());
            detailsPanel.setBackground(UIUtils.BACKGROUND_COLOR);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // Authors
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0.0;
            detailsPanel.add(new JLabel("Author(s):"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            detailsPanel.add(new JLabel(book.getAuthorsAsString()), gbc);

            // ISBN
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 0.0;
            detailsPanel.add(new JLabel("ISBN:"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            detailsPanel.add(new JLabel(book.getIsbn()), gbc);

            // Publisher
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.weightx = 0.0;
            detailsPanel.add(new JLabel("Publisher:"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            detailsPanel.add(new JLabel(book.getPublisher()), gbc);

            // Publication Year
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.weightx = 0.0;
            detailsPanel.add(new JLabel("Publication Year:"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            detailsPanel.add(new JLabel(String.valueOf(book.getPublicationYear())), gbc);

            // Genres
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.weightx = 0.0;
            detailsPanel.add(new JLabel("Genre(s):"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            detailsPanel.add(new JLabel(book.getGenresAsString()), gbc);

            // Availability
            gbc.gridx = 0;
            gbc.gridy = 5;
            gbc.weightx = 0.0;
            detailsPanel.add(new JLabel("Availability:"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            JLabel availabilityLabel = new JLabel(
                    book.getAvailableCopies() + " of " + book.getTotalCopies() + " copies available");
            if (book.getAvailableCopies() == 0) {
                availabilityLabel.setForeground(UIUtils.ERROR_COLOR);
            } else if (book.getAvailableCopies() < book.getTotalCopies() / 3) {
                availabilityLabel.setForeground(UIUtils.WARNING_COLOR);
            } else {
                availabilityLabel.setForeground(UIUtils.SUCCESS_COLOR);
            }
            detailsPanel.add(availabilityLabel, gbc);

            // Location
            gbc.gridx = 0;
            gbc.gridy = 6;
            gbc.weightx = 0.0;
            detailsPanel.add(new JLabel("Location:"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            detailsPanel.add(new JLabel(book.getLocation()), gbc);

            // Description
            gbc.gridx = 0;
            gbc.gridy = 7;
            gbc.weightx = 0.0;
            detailsPanel.add(new JLabel("Description:"), gbc);

            gbc.gridx = 0;
            gbc.gridy = 8;
            gbc.gridwidth = 2;
            gbc.weightx = 1.0;
            JTextArea descriptionArea = new JTextArea(book.getDescription());
            descriptionArea.setEditable(false);
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setBackground(UIUtils.BACKGROUND_COLOR);
            descriptionArea.setRows(5);
            JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
            detailsPanel.add(descriptionScroll, gbc);

            mainPanel.add(detailsPanel, BorderLayout.CENTER);

            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(UIUtils.BACKGROUND_COLOR);

            JButton reserveButton = new JButton("Reserve Book");
            reserveButton.setEnabled(book.getAvailableCopies() > 0);
            reserveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose(); // Close dialog

                    // Find the book in the table and select it
                    for (int i = 0; i < filteredBooks.size(); i++) {
                        if (filteredBooks.get(i).getId().equals(book.getId())) {
                            int viewIndex = booksTable.convertRowIndexToView(i);
                            booksTable.setRowSelectionInterval(viewIndex, viewIndex);
                            break;
                        }
                    }

                    // Trigger the reserve action
                    reserveBook();
                }
            });

            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });

            buttonPanel.add(reserveButton);
            buttonPanel.add(closeButton);

            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            setContentPane(mainPanel);
        }
    }
}