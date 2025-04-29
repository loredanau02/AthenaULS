package com.athena.library.ui;

import com.athena.library.models.Librarian;
import com.athena.library.models.Student;
import com.athena.library.ui.librarian.LibrarianDashboard;
import com.athena.library.ui.student.StudentDashboard;
import com.athena.library.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Login screen for the Library Management System
 */
public class LoginScreen extends JFrame {
    private JPanel mainPanel;
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    private JRadioButton studentRadioButton;
    private JRadioButton librarianRadioButton;
    private JLabel statusLabel;


    /**
     * Creates the login screen
     */

    /**
     * Initializes the UI components
     */
    private void initializeUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Login form panel
        JPanel loginFormPanel = createLoginFormPanel();
        mainPanel.add(loginFormPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    /**
     * Creates the header panel with logo and title
     * @return Header panel
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Logo (placeholder for now)
        JLabel logoLabel = new JLabel();
        logoLabel.setIcon(UIUtils.createImageIcon("/images/logo.png", "Athena University Logo"));
        headerPanel.add(logoLabel, BorderLayout.WEST);

        // Title
        JLabel titleLabel = new JLabel("Athena University Library System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        return headerPanel;
    }

    /**
     * Creates the login form panel
     * @return Login form panel
     */
    private JPanel createLoginFormPanel() {
        JPanel loginFormPanel = new JPanel();
        loginFormPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // User role selection
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        ButtonGroup roleGroup = new ButtonGroup();

        studentRadioButton = new JRadioButton("Student");
        studentRadioButton.setSelected(true);
        librarianRadioButton = new JRadioButton("Librarian");

        roleGroup.add(studentRadioButton);
        roleGroup.add(librarianRadioButton);

        rolePanel.add(studentRadioButton);
        rolePanel.add(librarianRadioButton);

        // Event listeners for radio buttons to update labels
        studentRadioButton.addActionListener(e -> {
            userIdField.setToolTipText("Enter your Student ID");
        });

        librarianRadioButton.addActionListener(e -> {
            userIdField.setToolTipText("Enter your Staff ID");
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginFormPanel.add(rolePanel, gbc);

        // User ID field
        JLabel userIdLabel = new JLabel("ID:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        loginFormPanel.add(userIdLabel, gbc);

        userIdField = new JTextField(20);
        userIdField.setToolTipText("Enter your Student ID");
        gbc.gridx = 1;
        gbc.gridy = 1;
        loginFormPanel.add(userIdField, gbc);

        // Password field
        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        loginFormPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setToolTipText("Enter your password");
        gbc.gridx = 1;
        gbc.gridy = 2;
        loginFormPanel.add(passwordField, gbc);

        // Status label for showing error messages
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        loginFormPanel.add(statusLabel, gbc);

        return loginFormPanel;
    }

    /**
     * Creates the button panel
     * @return Button panel
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptLogin();
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);

        // Add action listener to password field to allow login with Enter key
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptLogin();
            }
        });

        return buttonPanel;
    }

    /**
     * Attempts to log in with the provided credentials
     */
    private void attemptLogin() {
        String userId = userIdField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Validate inputs
        if (userId.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both ID and password.");
            return;
        }

        // Disable login button and show loading message
        loginButton.setEnabled(false);
        statusLabel.setText("Authenticating...");
        statusLabel.setForeground(Color.BLUE);

        // Use SwingWorker to perform authentication in background


            @Override
            protected void done() {
                try {
                    Object result = get();

                    if (result == null) {
                        // Authentication failed
                        statusLabel.setText("Invalid ID or password. Please try again.");
                        statusLabel.setForeground(Color.RED);
                        loginButton.setEnabled(true);
                    } else {
                        // Authentication successful
                        statusLabel.setText("Login successful!");
                        statusLabel.setForeground(new Color(0, 128, 0)); // Dark green

                        // Open appropriate dashboard based on user type
                        if (result instanceof Student) {
                            Student student = (Student) result;
                            openStudentDashboard(student);
                        } else if (result instanceof Librarian) {
                            Librarian librarian = (Librarian) result;
                            openLibrarianDashboard(librarian);
                        }
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Error during login: " + ex.getMessage());
                    statusLabel.setForeground(Color.RED);
                    loginButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    /**
     * Opens the student dashboard
     * @param student Authenticated student
     */
    private void openStudentDashboard(Student student) {
        SwingUtilities.invokeLater(() -> {
            // Close the login screen
            dispose();

            // Open the student dashboard
            new StudentDashboard(student);
        });
    }

    /**
     * Opens the librarian dashboard
     * @param librarian Authenticated librarian
     */
    private void openLibrarianDashboard(Librarian librarian) {
        SwingUtilities.invokeLater(() -> {
            // Close the login screen
            dispose();

            // Open the librarian dashboard
            new LibrarianDashboard(librarian);
        });
    }
}