package com.athena.library.ui.student;

import com.athena.library.firebase.StudentService;
import com.athena.library.models.Student;
import com.athena.library.ui.BaseDashboard;
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
 * Panel for displaying and editing student profile information
 */
public class StudentProfilePanel extends JPanel {
    private final StudentDashboard dashboard;
    private Student student;
    private final StudentService studentService;

    // Form fields
    private JTextField studentIdField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextArea addressArea;
    private JTextField departmentField;
    private JTextField programField;
    private JTextField yearField;
    private JTextField enrollmentDateField;

    // Buttons
    private JButton editButton;
    private JButton saveButton;
    private JButton cancelButton;

    // Read-only fields
    private JLabel borrowedBooksLabel;
    private JLabel fineBalanceLabel;

    /**
     * Creates a new student profile panel
     * @param dashboard The parent dashboard
     * @param student The student whose profile to display
     */
    public StudentProfilePanel(StudentDashboard dashboard, Student student) {
        this.dashboard = dashboard;
        this.student = student;
        this.studentService = new StudentService();

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
        JPanel formPanel = new JPanel(new BorderLayout(10, 10));

        // Personal information section
        JPanel personalInfoPanel = createPersonalInfoPanel();
        formPanel.add(personalInfoPanel, BorderLayout.WEST);

        // Academic information section
        JPanel academicInfoPanel = createAcademicInfoPanel();
        formPanel.add(academicInfoPanel, BorderLayout.CENTER);

        // Library information section
        JPanel libraryInfoPanel = createLibraryInfoPanel();
        formPanel.add(libraryInfoPanel, BorderLayout.EAST);

        add(formPanel, BorderLayout.CENTER);

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

        JLabel titleLabel = new JLabel("Student Profile");
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

        // Student ID (read-only)
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Student ID:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        studentIdField = new JTextField(15);
        studentIdField.setEditable(false); // Always read-only
        studentIdField.setBackground(UIUtils.BACKGROUND_COLOR.brighter());
        panel.add(studentIdField, gbc);

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
     * Creates the academic information panel
     * @return The academic info panel
     */
    private JPanel createAcademicInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                UIUtils.PANEL_BORDER,
                "Academic Information",
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

        // Program (read-only)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Program:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        programField = new JTextField(20);
        programField.setEditable(false);
        programField.setBackground(UIUtils.BACKGROUND_COLOR.brighter());
        panel.add(programField, gbc);

        // Year (read-only)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Year:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        yearField = new JTextField(20);
        yearField.setEditable(false);
        yearField.setBackground(UIUtils.BACKGROUND_COLOR.brighter());
        panel.add(yearField, gbc);

        // Enrollment Date (read-only)
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Enrollment Date:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        enrollmentDateField = new JTextField(20);
        enrollmentDateField.setEditable(false);
        enrollmentDateField.setBackground(UIUtils.BACKGROUND_COLOR.brighter());
        panel.add(enrollmentDateField, gbc);

        return panel;
    }

    /**
     * Creates the library information panel
     * @return The library info panel
     */
    private JPanel createLibraryInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                UIUtils.PANEL_BORDER,
                "Library Information",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                UIUtils.SUBHEADER_FONT,
                UIUtils.PRIMARY_COLOR));
        panel.setBackground(UIUtils.BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Borrowed Books
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Borrowed Books:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        borrowedBooksLabel = new JLabel("0");
        borrowedBooksLabel.setFont(UIUtils.NORMAL_FONT);
        panel.add(borrowedBooksLabel, gbc);

        // Fine Balance
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Fine Balance:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        fineBalanceLabel = new JLabel("$0.00");
        fineBalanceLabel.setFont(UIUtils.NORMAL_FONT);
        panel.add(fineBalanceLabel, gbc);

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
     * Populates the form fields with student data
     */
    private void populateFields() {
        studentIdField.setText(student.getStudentId());
        firstNameField.setText(student.getFirstName());
        lastNameField.setText(student.getLastName());
        emailField.setText(student.getEmail());
        phoneField.setText(student.getPhoneNumber());
        addressArea.setText(student.getAddress());
        departmentField.setText(student.getDepartment());
        programField.setText(student.getProgram());
        yearField.setText(String.valueOf(student.getYear()));
        enrollmentDateField.setText(UIUtils.formatDate(student.getEnrollmentDate()));

        borrowedBooksLabel.setText(String.valueOf(student.getBorrowedBooksCount()));
        fineBalanceLabel.setText(String.format("$%.2f", student.getFineBalance()));

        // Set warning color for fines
        if (student.getFineBalance() > 0) {
            fineBalanceLabel.setForeground(UIUtils.WARNING_COLOR);
        } else {
            fineBalanceLabel.setForeground(UIUtils.TEXT_COLOR);
        }
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
     * Saves changes to the student profile
     */
    private void saveChanges() {
        // Validate email format
        String email = emailField.getText().trim();
        if (!isValidEmail(email)) {
            UIUtils.showWarningDialog(this, "Please enter a valid email address.", "Invalid Email");
            emailField.requestFocus();
            return;
        }

        // Create map of updates
        Map<String, Object> updates = new HashMap<>();
        updates.put("email", email);
        updates.put("phoneNumber", phoneField.getText().trim());
        updates.put("address", addressArea.getText().trim());

        // Save changes in background
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return studentService.updateStudentFields(student.getId(), updates);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        // Update the student object with new values
                        student.setEmail(email);
                        student.setPhoneNumber(phoneField.getText().trim());
                        student.setAddress(addressArea.getText().trim());

                        // Update the dashboard
                        dashboard.updateStudent(student);

                        // Return to view mode
                        setFieldsEditable(false);
                        editButton.setVisible(true);
                        saveButton.setVisible(false);
                        cancelButton.setVisible(false);

                        UIUtils.showInfoDialog(StudentProfilePanel.this,
                                "Your profile has been updated successfully.", "Profile Updated");
                    } else {
                        UIUtils.showErrorDialog(StudentProfilePanel.this,
                                "Failed to update profile. Please try again.", "Update Failed");
                    }
                } catch (Exception e) {
                    UIUtils.showErrorDialog(StudentProfilePanel.this,
                            "Error updating profile: " + e.getMessage(), "Error");
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
     * Updates the student data and refreshes the UI
     * @param updatedStudent Updated student data
     */
    public void updateStudentData(Student updatedStudent) {
        this.student = updatedStudent;
        populateFields();
    }
}