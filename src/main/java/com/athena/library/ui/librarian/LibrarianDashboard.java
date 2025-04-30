package com.athena.library.ui.librarian;

import com.athena.library.firebase.BookService;
import com.athena.library.firebase.MessageService;
import com.athena.library.firebase.StudentService;
import com.athena.library.firebase.TransactionService;
import com.athena.library.models.Librarian;
import com.athena.library.ui.BaseDashboard;
import com.athena.library.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dashboard for librarian (admin) users
 */
public class LibrarianDashboard extends BaseDashboard {
    private Librarian librarian;
    private StudentService studentService;
    private BookService bookService;
    private TransactionService transactionService;
    private MessageService messageService;

    // Panels for different sections
    private LibrarianProfilePanel profilePanel;
    private StudentManagementPanel studentManagementPanel;
    private BookManagementPanel bookManagementPanel;
    private TransactionManagementPanel transactionManagementPanel;
    private LibrarianInboxPanel inboxPanel;
    private ReportsPanel reportsPanel;

    // Navigation buttons
    private JButton profileButton;
    private JButton studentManagementButton;
    private JButton bookManagementButton;
    private JButton transactionManagementButton;
    private JButton inboxButton;
    private JButton reportsButton;

    /**
     * Creates the librarian dashboard
     * @param librarian Authenticated librarian
     */
    public LibrarianDashboard(Librarian librarian) {
        super("Athena Library - Librarian Dashboard", librarian.getFullName(),
                librarian.isAdmin() ? "Admin" : librarian.getRole());

        this.librarian = librarian;

        // Initialize services
        this.studentService = new StudentService();
        this.bookService = new BookService();
        this.transactionService = new TransactionService(bookService, studentService);
        this.messageService = new MessageService();

        // Initialize panels
        initializePanels();

        // Show profile panel by default
        profileButton.doClick();

        // Check for unread messages
        checkUnreadMessages();
    }

    /**
     * Initializes the different content panels
     */
    private void initializePanels() {
        // Create panels
        profilePanel = new LibrarianProfilePanel(this, librarian);
        studentManagementPanel = new StudentManagementPanel(this, librarian, studentService, messageService);
        bookManagementPanel = new BookManagementPanel(this, librarian, bookService);
        transactionManagementPanel = new TransactionManagementPanel(this, librarian,
                transactionService, studentService, bookService);
        inboxPanel = new LibrarianInboxPanel(this, librarian, messageService, studentService);
        reportsPanel = new ReportsPanel(this, librarian, studentService, bookService, transactionService);
    }

    @Override
    protected JPanel createSidebarPanel() {
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(UIUtils.BACKGROUND_COLOR);
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIUtils.PRIMARY_COLOR));
        sidebarPanel.setPreferredSize(new Dimension(220, -1));

        // Create a panel for the librarian's photo (or avatar)
        JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        photoPanel.setBackground(UIUtils.BACKGROUND_COLOR);
        photoPanel.setPreferredSize(new Dimension(220, 180));

        // Placeholder for librarian photo
        JLabel photoLabel = new JLabel();
        photoLabel.setIcon(UIUtils.createImageIcon("/images/librarian_avatar.png", "Librarian Photo"));
        photoLabel.setHorizontalAlignment(JLabel.CENTER);
        photoPanel.add(photoLabel);

        // Add librarian ID below photo
        JLabel staffIdLabel = new JLabel("Staff ID: " + librarian.getStaffId());
        staffIdLabel.setFont(UIUtils.NORMAL_FONT);
        staffIdLabel.setHorizontalAlignment(JLabel.CENTER);
        photoPanel.add(staffIdLabel);

        // Add admin badge if applicable
        if (librarian.isAdmin()) {
            JLabel adminLabel = new JLabel("Administrator");
            adminLabel.setFont(UIUtils.SMALL_FONT);
            adminLabel.setForeground(UIUtils.SECONDARY_COLOR);
            adminLabel.setHorizontalAlignment(JLabel.CENTER);
            photoPanel.add(adminLabel);
        }

        sidebarPanel.add(photoPanel);

        // Add a separator
        sidebarPanel.add(new JSeparator());

        // Create navigation buttons
        profileButton = createSidebarButton("Profile", null, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPanel(profilePanel);
                updateButtonSelection(profileButton);
                updateStatus("Viewing profile", false);
            }
        });

        studentManagementButton = createSidebarButton("Student Management", null, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                studentManagementPanel.refreshData(); // Refresh data when tab is selected
                showPanel(studentManagementPanel);
                updateButtonSelection(studentManagementButton);
                updateStatus("Managing students", false);
            }
        });

        bookManagementButton = createSidebarButton("Book Management", null, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bookManagementPanel.refreshData(); // Refresh data when tab is selected
                showPanel(bookManagementPanel);
                updateButtonSelection(bookManagementButton);
                updateStatus("Managing books", false);
            }
        });

        transactionManagementButton = createSidebarButton("Borrow/Return", null, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                transactionManagementPanel.refreshData(); // Refresh data when tab is selected
                showPanel(transactionManagementPanel);
                updateButtonSelection(transactionManagementButton);
                updateStatus("Managing transactions", false);
            }
        });

        inboxButton = createSidebarButton("Inbox", null, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inboxPanel.refreshMessages(); // Refresh messages when tab is selected
                showPanel(inboxPanel);
                updateButtonSelection(inboxButton);
                updateStatus("Viewing inbox", false);
            }
        });

        reportsButton = createSidebarButton("Reports", null, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reportsPanel.refreshData(); // Refresh data when tab is selected
                showPanel(reportsPanel);
                updateButtonSelection(reportsButton);
                updateStatus("Viewing reports", false);
            }
        });

        // Add buttons to sidebar
        sidebarPanel.add(profileButton);
        sidebarPanel.add(studentManagementButton);
        sidebarPanel.add(bookManagementButton);
        sidebarPanel.add(transactionManagementButton);
        sidebarPanel.add(inboxButton);
        sidebarPanel.add(reportsButton);

        // Add glue to push everything to the top
        sidebarPanel.add(Box.createVerticalGlue());

        // Add quick actions panel at the bottom
        JPanel quickActionsPanel = createQuickActionsPanel();
        sidebarPanel.add(quickActionsPanel);

        return sidebarPanel;
    }

    /**
     * Creates a panel with quick action buttons
     * @return Quick actions panel
     */
    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIUtils.PRIMARY_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel quickActionsLabel = new JLabel("Quick Actions");
        quickActionsLabel.setFont(UIUtils.SMALL_FONT);
        quickActionsLabel.setForeground(UIUtils.LIGHT_TEXT_COLOR);
        quickActionsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(quickActionsLabel);

        panel.add(Box.createVerticalStrut(10));

        // Add new student button
        JButton addStudentButton = new JButton("Add New Student");
        addStudentButton.setFont(UIUtils.SMALL_FONT);
        addStudentButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                studentManagementButton.doClick(); // First, navigate to student management
                studentManagementPanel.showAddStudentDialog(); // Then show add student dialog
            }
        });
        panel.add(addStudentButton);

        panel.add(Box.createVerticalStrut(5));

        // Add new book button
        JButton addBookButton = new JButton("Add New Book");
        addBookButton.setFont(UIUtils.SMALL_FONT);
        addBookButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bookManagementButton.doClick(); // First, navigate to book management
                bookManagementPanel.showAddBookDialog(); // Then show add book dialog
            }
        });
        panel.add(addBookButton);

        panel.add(Box.createVerticalStrut(5));

        // Process return button
        JButton processReturnButton = new JButton("Process Return");
        processReturnButton.setFont(UIUtils.SMALL_FONT);
        processReturnButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        processReturnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                transactionManagementButton.doClick(); // First, navigate to transactions
                transactionManagementPanel.showReturnBookDialog(); // Then show return book dialog
            }
        });
        panel.add(processReturnButton);

        return panel;
    }

    /**
     * Updates the visual selection state of the sidebar buttons
     * @param selectedButton The currently selected button
     */
    private void updateButtonSelection(JButton selectedButton) {
        // Reset all buttons
        profileButton.setBackground(UIUtils.BACKGROUND_COLOR);
        profileButton.setForeground(UIUtils.TEXT_COLOR);
        studentManagementButton.setBackground(UIUtils.BACKGROUND_COLOR);
        studentManagementButton.setForeground(UIUtils.TEXT_COLOR);
        bookManagementButton.setBackground(UIUtils.BACKGROUND_COLOR);
        bookManagementButton.setForeground(UIUtils.TEXT_COLOR);
        transactionManagementButton.setBackground(UIUtils.BACKGROUND_COLOR);
        transactionManagementButton.setForeground(UIUtils.TEXT_COLOR);
        inboxButton.setBackground(UIUtils.BACKGROUND_COLOR);
        inboxButton.setForeground(UIUtils.TEXT_COLOR);
        reportsButton.setBackground(UIUtils.BACKGROUND_COLOR);
        reportsButton.setForeground(UIUtils.TEXT_COLOR);

        // Highlight selected button
        selectedButton.setBackground(UIUtils.SECONDARY_COLOR);
        selectedButton.setForeground(UIUtils.LIGHT_TEXT_COLOR);
    }

    /**
     * Checks for unread messages and updates the inbox button
     */
    private void checkUnreadMessages() {
        SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return messageService.getUnreadMessageCount(librarian.getId());
            }

            @Override
            protected void done() {
                try {
                    int unreadCount = get();
                    if (unreadCount > 0) {
                        inboxButton.setText("Inbox (" + unreadCount + ")");
                        inboxButton.setForeground(UIUtils.WARNING_COLOR);
                    } else {
                        inboxButton.setText("Inbox");
                        inboxButton.setForeground(UIUtils.TEXT_COLOR);
                    }
                } catch (Exception e) {
                    System.err.println("Error checking unread messages: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }

    /**
     * Gets the librarian associated with this dashboard
     * @return The librarian
     */
    public Librarian getLibrarian() {
        return librarian;
    }

    /**
     * Updates the librarian data and refreshes the UI
     * @param updatedLibrarian Updated librarian data
     */
    public void updateLibrarian(Librarian updatedLibrarian) {
        this.librarian = updatedLibrarian;

        // Update panels with new librarian data
        profilePanel.updateLibrarianData(updatedLibrarian);

        // Updates header
        userNameLabel.setText(updatedLibrarian.getFullName());
        userRoleLabel.setText(" (" + (updatedLibrarian.isAdmin() ? "Admin" : updatedLibrarian.getRole()) + ")");
    }
}