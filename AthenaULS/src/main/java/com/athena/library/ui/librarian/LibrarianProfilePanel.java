package com.athena.library.ui.librarian;

import com.athena.library.auth.AuthService;
import com.athena.library.firebase.FirebaseConfig;
import com.athena.library.models.Librarian;
import com.athena.library.utils.UIUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Panel for displaying and editing librarian profile information
 */
public class LibrarianProfilePanel extends JPanel {
    private final LibrarianDashboard dashboard;
    private Librarian librarian;
    private final AuthService authService;

    // Form fields
    private JTextField staffIdField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextArea addressArea;
    private JTextField departmentField;
    private JTextField roleField;
    private JCheckBox adminCheckBox;

    // Password fields
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    // Buttons
    private JButton editButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton changePasswordButton;

    /**
     * Creates a new librarian profile panel
     * @param dashboard The parent dashboard
     * @param librarian The librarian whose profile to display
     */
    public LibrarianProfilePanel(LibrarianDashboard dashboard, Librarian librarian) {
        this.dashboard = dashboard;
        this.librarian = librarian;
        this.authService = AuthService.getInstance();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initializeUI();
        populateFields();
        setFieldsEditable(false); // Start in view mode
    }

    /**
     * Initializes the UI components
     */
    private void initializeUI() {
        // Profile header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main form panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        // Profile information section
        JPanel formPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        // Personal information section
        JPanel personalInfoPanel = createPersonalInfoPanel();
        formPanel.add(personalInfoPanel);

        // Work information section
        JPanel workInfoPanel = createWorkInfoPanel();
        formPanel.add(workInfoPanel);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Password section
        JPanel passwordPanel = createPasswordPanel();
        mainPanel.add(passwordPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates the profile header panel
     * @return The header panel
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtils.PRIMARY_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Librarian Profile");
        titleLabel.setFont(UIUtils.HEADER_FONT);
        titleLabel.setForeground(UIUtils.LIGHT_TEXT_COLOR);
        panel.add(titleLabel, BorderLayout.WEST);

        return panel;
    }

    /**
     * Creates the personal information panel
     * @return The personal info panel
     */
    private JPanel createPersonalInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                UIUtils.PANEL_BORDER,
                "Personal Information",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                UIUtils.SUBHEADER_FONT,
                UIUtils.PRIMARY_COLOR));
        panel.setBackground(UIUtils.BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Staff ID (read-only)
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Staff ID:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        staffIdField = new JTextField(15);
        staffIdField.setEditable(false); // Always read-only
        staffIdField.setBackground(UIUtils.BACKGROUND_COLOR.brighter());
        panel.add(staffIdField, gbc);

        // First Name (read-only)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("First Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        firstNameField = new JTextField(15);
        firstNameField.setEditable(false); // Always read-only
        firstNameField.setBackground(UIUtils.BACKGROUND_COLOR.brighter());
        panel.add(firstNameField, gbc);

        // Last Name (read-only)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Last Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        lastNameField = new JTextField(15);
        lastNameField.setEditable(false); // Always read-only
        lastNameField.setBackground(UIUtils.BACKGROUND_COLOR.brighter());
        panel.add(lastNameField, gbc);

        // Email (editable)
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        emailField = new JTextField(15);
        panel.add(emailField, gbc);

        // Phone (editable)
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Phone:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        phoneField = new JTextField(15);
        panel.add(phoneField, gbc);

        // Address (editable)
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Address:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridheight = 3;
        addressArea = new JTextArea(3, 15);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        JScrollPane addressScrollPane = new JScrollPane(addressArea);
        panel.add(addressScrollPane, gbc);

        return panel;
    }

    /**
     * Creates the work information panel
     * @return The work info panel
     */
    private JPanel createWorkInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                UIUtils.PANEL_BORDER,
                "Work Information",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                UIUtils.SUBHEADER_FONT,
                UIUtils.PRIMARY_COLOR));
        panel.setBackground(UIUtils.BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Department (read-only)
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Department:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        departmentField = new JTextField(20);
        departmentField.setEditable(false);
        departmentField.setBackground(UIUtils.BACKGROUND_COLOR.brighter());
        panel.add(departmentField, gbc);

        // Role (read-only)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Role:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        roleField = new JTextField(20);
        roleField.setEditable(false);
        roleField.setBackground(UIUtils.BACKGROUND_COLOR.brighter());
        panel.add(roleField, gbc);

        // Admin status (read-only)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Admin Status:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        adminCheckBox = new JCheckBox("Administrator");
        adminCheckBox.setEnabled(false);
        adminCheckBox.setBackground(UIUtils.BACKGROUND_COLOR);
        panel.add(adminCheckBox, gbc);

        return panel;
    }

    /**
     * Creates the password panel
     * @return The password panel
     */
    private JPanel createPasswordPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                UIUtils.PANEL_BORDER,
                "Change Password",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                UIUtils.SUBHEADER_FONT,
                UIUtils.PRIMARY_COLOR));
        panel.setBackground(UIUtils.BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Current password
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Current Password:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        currentPasswordField = new JPasswordField(15);
        panel.add(currentPasswordField, gbc);

        // New password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("New Password:"), gbc);

        gbc.gridx = 1;
        newPasswordField = new JPasswordField(15);
        panel.add(newPasswordField, gbc);

        // Confirm new password
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Confirm Password:"), gbc);

        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(15);
        panel.add(confirmPasswordField, gbc);

        // Change password button
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        changePasswordButton = new JButton("Change Password");
        changePasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changePassword();
            }
        });
        panel.add(changePasswordButton, gbc);

        return panel;
    }

    /**
     * Creates the button panel
     * @return The button panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(UIUtils.BACKGROUND_COLOR);

        editButton = new JButton("Edit Profile");
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setFieldsEditable(true);
                editButton.setVisible(false);
                saveButton.setVisible(true);
                cancelButton.setVisible(true);
            }
        });

        saveButton = new JButton("Save Changes");
        saveButton.setVisible(false);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveChanges();
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.setVisible(false);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelEditing();
            }
        });

        panel.add(editButton);
        panel.add(saveButton);
        panel.add(cancelButton);

        return panel;
    }

    /**
     * Populates the form fields with librarian data
     */
    private void populateFields() {
        staffIdField.setText(librarian.getStaffId());
        firstNameField.setText(librarian.getFirstName());
        lastNameField.setText(librarian.getLastName());
        emailField.setText(librarian.getEmail());
        phoneField.setText(librarian.getPhoneNumber());
        addressArea.setText(librarian.getAddress());
        departmentField.setText(librarian.getDepartment());
        roleField.setText(librarian.getRole());
        adminCheckBox.setSelected(librarian.isAdmin());

        // Clear password fields
        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }

    /**
     * Sets the editable state of the form fields
     * @param editable Whether fields should be editable
     */
    private void setFieldsEditable(boolean editable) {
        // Only allow editing of email, phone, and address
        emailField.setEditable(editable);
        phoneField.setEditable(editable);
        addressArea.setEditable(editable);

        // Visual feedback for editable fields
        Color bgColor = editable ? Color.WHITE : UIUtils.BACKGROUND_COLOR;
        emailField.setBackground(bgColor);
        phoneField.setBackground(bgColor);
        addressArea.setBackground(bgColor);
    }

    /**
     * Saves changes to the librarian profile
     */
    private void saveChanges() {
        // Validate email format
        String email = emailField.getText().trim();
        if (!isValidEmail(email)) {
            UIUtils.showWarningDialog(this, "New password and confirmation do not match.", "Password Mismatch");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
            newPasswordField.requestFocus();
            return;
        }

        // Validate password strength
        if (!authService.validatePasswordStrength(newPassword)) {
            UIUtils.showWarningDialog(this,
                    "Password must be at least 8 characters and include a digit, " +
                            "a lowercase letter, an uppercase letter, and a special character.",
                    "Weak Password");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
            newPasswordField.requestFocus();
            return;
        }

        // Change password in background
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return authService.updatePassword(librarian.getId(), currentPassword, newPassword);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        // Clear password fields
                        currentPasswordField.setText("");
                        newPasswordField.setText("");
                        confirmPasswordField.setText("");

                        UIUtils.showInfoDialog(LibrarianProfilePanel.this,
                                "Your password has been changed successfully.", "Password Changed");
                    } else {
                        UIUtils.showErrorDialog(LibrarianProfilePanel.this,
                                "Failed to change password. Please check your current password and try again.",
                                "Password Change Failed");
                    }
                } catch (Exception e) {
                    UIUtils.showErrorDialog(LibrarianProfilePanel.this,
                            "Error changing password: " + e.getMessage(), "Error");
                }
            }
        };

        worker.execute();
    }

    /**
     * Cancels editing and reverts changes
     */
    private void cancelEditing() {
        // Revert to original values
        populateFields();

        // Return to view mode
        setFieldsEditable(false);
        editButton.setVisible(true);
        saveButton.setVisible(false);
        cancelButton.setVisible(false);
    }

    /**
     * Validates an email address format
     * @param email Email to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    /**
     * Updates the librarian data and refreshes the UI
     * @param updatedLibrarian Updated librarian data
     */
    public void updateLibrarianData(Librarian updatedLibrarian) {
        this.librarian = updatedLibrarian;
        populateFields();
    }
}this, "Please enter a valid email address.", "Invalid Email");
        emailField.requestFocus();
            return;
                    }

// Create a copy of the librarian with updated fields
Librarian updatedLibrarian = new Librarian(
        librarian.getId(),
        librarian.getFirstName(),
        librarian.getLastName(),
        email,
        phoneField.getText().trim(),
        addressArea.getText().trim(),
        librarian.getStaffId(),
        librarian.getDepartment(),
        librarian.getRole(),
        librarian.isAdmin()
);

// Save changes in background
SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
    @Override
    protected Boolean doInBackground() throws Exception {
        try {
            // Get Firestore instance
            return FirebaseConfig.getFirestoreInstance()
                    .collection("librarians")
                    .document(librarian.getId())
                    .set(updatedLibrarian)
                    .get() != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void done() {
        try {
            boolean success = get();
            if (success) {
                // Update the librarian reference
                librarian = updatedLibrarian;

                // Update the dashboard
                dashboard.updateLibrarian(librarian);

                // Return to view mode
                setFieldsEditable(false);
                editButton.setVisible(true);
                saveButton.setVisible(false);
                cancelButton.setVisible(false);

                UIUtils.showInfoDialog(LibrarianProfilePanel.this,
                        "Your profile has been updated successfully.", "Profile Updated");
            } else {
                UIUtils.showErrorDialog(LibrarianProfilePanel.this,
                        "Failed to update profile. Please try again.", "Update Failed");
            }
        } catch (Exception e) {
            UIUtils.showErrorDialog(LibrarianProfilePanel.this,
                    "Error updating profile: " + e.getMessage(), "Error");
        }
    }
};

        worker.execute();
    }

/**
 * Changes the librarian's password
 */
private void changePassword() {
    String currentPassword = new String(currentPasswordField.getPassword());
    String newPassword = new String(newPasswordField.getPassword());
    String confirmPassword = new String(confirmPasswordField.getPassword());

    // Validate inputs
    if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
        UIUtils.showWarningDialog(this, "Please fill in all password fields.", "Missing Information");
        return;
    }

    if (!newPassword.equals(confirmPassword)) {
        UIUtils.showWarningDialog(