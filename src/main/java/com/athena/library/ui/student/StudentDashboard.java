package com.athena.library.ui.student;

import com.athena.library.firebase.MessageService;
import com.athena.library.models.Student;
import com.athena.library.ui.BaseDashboard;
import com.athena.library.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dashboard for student users
 */
public class StudentDashboard extends BaseDashboard {
    private Student student;
    private MessageService messageService;

    // Panels for different sections
    private StudentProfilePanel profilePanel;
    private StudentBookCataloguePanel bookCataloguePanel;
    private StudentBorrowReturnPanel borrowReturnPanel;
    private StudentInboxPanel inboxPanel;

    // Navigation buttons
    private JButton profileButton;
    private JButton bookCatalogueButton;
    private JButton borrowReturnButton;
    private JButton inboxButton;

    /**
     * Creates the student dashboard
     * @param student Authenticated student
     */
    public StudentDashboard(Student student) {
        super("Athena Library - Student Dashboard", student.getFullName(), "Student");
        this.student = student;
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
        profilePanel = new StudentProfilePanel(this, student);
        bookCataloguePanel = new StudentBookCataloguePanel(this, student);
        borrowReturnPanel = new StudentBorrowReturnPanel(this, student);
        inboxPanel = new StudentInboxPanel(this, student);
    }

    @Override
    protected JPanel createSidebarPanel() {
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(UIUtils.BACKGROUND_COLOR);
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIUtils.PRIMARY_COLOR));
        sidebarPanel.setPreferredSize(new Dimension(220, -1));

        // Create a panel for the student's photo (or avatar)
        JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        photoPanel.setBackground(UIUtils.BACKGROUND_COLOR);
        photoPanel.setPreferredSize(new Dimension(220, 180));

        // Placeholder for student photo
        JLabel photoLabel = new JLabel();
        photoLabel.setIcon(UIUtils.createImageIcon("/images/student_avatar.png", "Student Photo"));
        photoLabel.setHorizontalAlignment(JLabel.CENTER);
        photoPanel.add(photoLabel);

        // Add student ID below photo
        JLabel studentIdLabel = new JLabel("ID: " + student.getStudentId());
        studentIdLabel.setFont(UIUtils.NORMAL_FONT);
        studentIdLabel.setHorizontalAlignment(JLabel.CENTER);
        photoPanel.add(studentIdLabel);

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

        bookCatalogueButton = createSidebarButton("Book Catalogue", null, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPanel(bookCataloguePanel);
                updateButtonSelection(bookCatalogueButton);
                updateStatus("Browsing book catalogue", false);
            }
        });

        borrowReturnButton = createSidebarButton("Borrow/Return", null, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                borrowReturnPanel.refreshData(); // Refresh data when tab is selected
                showPanel(borrowReturnPanel);
                updateButtonSelection(borrowReturnButton);
                updateStatus("Managing borrowed books", false);
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

        // Add buttons to sidebar
        sidebarPanel.add(profileButton);
        sidebarPanel.add(bookCatalogueButton);
        sidebarPanel.add(borrowReturnButton);
        sidebarPanel.add(inboxButton);

        // Add glue to push everything to the top
        sidebarPanel.add(Box.createVerticalGlue());

        return sidebarPanel;
    }

    /**
     * Updates the visual selection state of the sidebar buttons
     * @param selectedButton The currently selected button
     */
    private void updateButtonSelection(JButton selectedButton) {
        // Reset all buttons
        profileButton.setBackground(UIUtils.BACKGROUND_COLOR);
        bookCatalogueButton.setBackground(UIUtils.BACKGROUND_COLOR);
        borrowReturnButton.setBackground(UIUtils.BACKGROUND_COLOR);
        inboxButton.setBackground(UIUtils.BACKGROUND_COLOR);

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
                return messageService.getUnreadMessageCount(student.getId());
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
     * Gets the student associated with this dashboard
     * @return The student
     */
    public Student getStudent() {
        return student;
    }

    /**
     * Updates the student data and refreshes the UI
     * @param updatedStudent Updated student data
     */
    public void updateStudent(Student updatedStudent) {
        this.student = updatedStudent;

        // Update panels with new student data
        profilePanel.updateStudentData(updatedStudent);
        borrowReturnPanel.updateStudentData(updatedStudent);

        // Update header
        userNameLabel.setText(updatedStudent.getFullName());
    }
}