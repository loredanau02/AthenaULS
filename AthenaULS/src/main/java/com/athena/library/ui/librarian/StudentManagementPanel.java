match = student.getStudentId().toLowerCase().contains(searchText);
                        break;
                                case "Department":
match = student.getDepartment().toLowerCase().contains(searchText);
                        break;
                                case "Email":
match = student.getEmail().toLowerCase().contains(searchText);
                        break;
                                }

                                if (match) {
        filteredStudents.add(student);
                }
                        }
                        }

// Update the table
updateStudentsTable();

// Update status
        dashboard.updateStatus("Found " + filteredStudents.size() + " matching students", false);
        }

/**
 * Clears all filters and search criteria
 */
private void clearFilters() {
    searchField.setText("");
    searchTypeComboBox.setSelectedIndex(0);

    // Reset filtered students to all students
    filteredStudents = new ArrayList<>(allStudents);
    updateStudentsTable();

    // Update status
    dashboard.updateStatus("Showing all " + allStudents.size() + " students", false);
}

/**
 * Refreshes the data from the database
 */
public void refreshData() {
    loadStudents();
}

/**
 * Shows the add student dialog
 */
public void showAddStudentDialog() {
    AddEditStudentDialog dialog = new AddEditStudentDialog(
            SwingUtilities.getWindowAncestor(this), null);
    dialog.setVisible(true);

    // Refresh data if a student was added
    if (dialog.isStudentSaved()) {
        refreshData();
    }
}

/**
 * Shows the edit student dialog for the selected student
 */
private void editStudent() {
    int selectedRow = studentsTable.getSelectedRow();
    if (selectedRow >= 0) {
        // Convert view index to model index
        int modelRow = studentsTable.convertRowIndexToModel(selectedRow);

        // Get the student
        Student student = filteredStudents.get(modelRow);

        // Show edit dialog
        AddEditStudentDialog dialog = new AddEditStudentDialog(
                SwingUtilities.getWindowAncestor(this), student);
        dialog.setVisible(true);

        // Refresh data if the student was edited
        if (dialog.isStudentSaved()) {
            refreshData();
        }
    } else {
        UIUtils.showWarningDialog(this, "Please select a student to edit.", "No Student Selected");
    }
}

/**
 * Deletes the selected student
 */
private void deleteStudent() {
    int selectedRow = studentsTable.getSelectedRow();
    if (selectedRow >= 0) {
        // Convert view index to model index
        int modelRow = studentsTable.convertRowIndexToModel(selectedRow);

        // Get the student
        Student student = filteredStudents.get(modelRow);

        // Check if student has borrowed books
        if (student.hasBorrowedBooks()) {
            UIUtils.showWarningDialog(this,
                    "This student has borrowed books and cannot be deleted. " +
                            "Please ensure all books are returned first.",
                    "Cannot Delete Student");
            return;
        }

        // Confirm deletion
        boolean confirm = UIUtils.showConfirmDialog(this,
                "You are about to delete student " + student.getFullName() +
                        " (ID: " + student.getStudentId() + ").\n" +
                        "This action cannot be reversed. Continue?",
                "Confirm Delete");

        if (confirm) {
            // Show loading indicator
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            dashboard.updateStatus("Deleting student...", false);

            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    // Delete the student's auth record first
                    boolean authDeleted = authService.deleteAuthRecord(student.getId());
                    if (!authDeleted) {
                        return false;
                    }

                    // Then delete the student record
                    return studentService.deleteStudent(student.getId());
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            // Remove from lists
                            allStudents.remove(student);
                            filteredStudents.remove(student);

                            // Update the table
                            updateStudentsTable();

                            // Show success message
                            UIUtils.showInfoDialog(StudentManagementPanel.this,
                                    "Student " + student.getFullName() + " has been deleted.",
                                    "Student Deleted");

                            dashboard.updateStatus("Student deleted successfully", false);
                        } else {
                            UIUtils.showErrorDialog(StudentManagementPanel.this,
                                    "Failed to delete student. Please try again.",
                                    "Deletion Failed");

                            dashboard.updateStatus("Error deleting student", true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        UIUtils.showErrorDialog(StudentManagementPanel.this,
                                "Error deleting student: " + e.getMessage(), "Error");
                        dashboard.updateStatus("Error: " + e.getMessage(), true);
                    } finally {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            };

            worker.execute();
        }
    } else {
        UIUtils.showWarningDialog(this, "Please select a student to delete.", "No Student Selected");
    }
}

/**
 * Shows the student details dialog for the selected student
 */
private void viewStudentDetails() {
    int selectedRow = studentsTable.getSelectedRow();
    if (selectedRow >= 0) {
        // Convert view index to model index
        int modelRow = studentsTable.convertRowIndexToModel(selectedRow);

        // Get the student
        Student student = filteredStudents.get(modelRow);

        // Show details dialog
        StudentDetailsDialog dialog = new StudentDetailsDialog(
                SwingUtilities.getWindowAncestor(this), student);
        dialog.setVisible(true);

        // Refresh data if the student was edited
        if (dialog.isStudentModified()) {
            refreshData();
        }
    } else {
        UIUtils.showWarningDialog(this, "Please select a student to view details.", "No Student Selected");
    }
}

/**
 * Dialog for adding or editing a student
 */
private class AddEditStudentDialog extends JDialog {
    private Student student;
    private boolean studentSaved = false;

    // Form fields
    private JTextField studentIdField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextArea addressArea;
    private JTextField departmentField;
    private JTextField programField;
    private JSpinner yearSpinner;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JCheckBox generatePasswordCheckBox;

    /**
     * Creates a dialog for adding a new student or editing an existing one
     * @param parent Parent window
     * @param student Student to edit, or null for a new student
     */
    public AddEditStudentDialog(Window parent, Student student) {
        super(parent, student == null ? "Add New Student" : "Edit Student", ModalityType.APPLICATION_MODAL);
        this.student = student;

        setSize(600, 650);
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(UIUtils.BACKGROUND_COLOR);

        // Form panel
        JPanel formPanel = createFormPanel();

        // Scroll pane for form
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Populate fields if editing an existing student
        if (student != null) {
            populateFields();
        }
    }

    /**
     * Creates the form panel
     * @return The form panel
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIUtils.BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Personal Information section
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel personalLabel = new JLabel("Personal Information");
        personalLabel.setFont(UIUtils.SUBHEADER_FONT);
        personalLabel.setForeground(UIUtils.PRIMARY_COLOR);
        panel.add(personalLabel, gbc);

        // Add separator
        gbc.gridy = 1;
        panel.add(new JSeparator(), gbc);

        // Student ID
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Student ID:"), gbc);

        gbc.gridx = 1;
        studentIdField = new JTextField(20);
        panel.add(studentIdField, gbc);

        // First Name
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("First Name:"), gbc);

        gbc.gridx = 1;
        firstNameField = new JTextField(20);
        panel.add(firstNameField, gbc);

        // Last Name
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Last Name:"), gbc);

        gbc.gridx = 1;
        lastNameField = new JTextField(20);
        panel.add(lastNameField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        emailField = new JTextField(20);
        panel.add(emailField, gbc);

        // Phone
        gbc.gridx = 0;
        gbc.gridy = 6;
        panel.add(new JLabel("Phone:"), gbc);

        gbc.gridx = 1;
        phoneField = new JTextField(20);
        panel.add(phoneField, gbc);

        // Address
        gbc.gridx = 0;
        gbc.gridy = 7;
        panel.add(new JLabel("Address:"), gbc);

        gbc.gridx = 1;
        addressArea = new JTextArea(3, 20);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        JScrollPane addressScroll = new JScrollPane(addressArea);
        panel.add(addressScroll, gbc);

        // Academic Information section
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        JLabel academicLabel = new JLabel("Academic Information");
        academicLabel.setFont(UIUtils.SUBHEADER_FONT);
        academicLabel.setForeground(UIUtils.PRIMARY_COLOR);
        panel.add(academicLabel, gbc);

        // Add separator
        gbc.gridy = 9;
        panel.add(new JSeparator(), gbc);

        // Department
        gbc.gridy = 10;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Department:"), gbc);

        gbc.gridx = 1;
        departmentField = new JTextField(20);
        panel.add(departmentField, gbc);

        // Program
        gbc.gridx = 0;
        gbc.gridy = 11;
        panel.add(new JLabel("Program:"), gbc);

        gbc.gridx = 1;
        programField = new JTextField(20);
        panel.add(programField, gbc);

        // Year
        gbc.gridx = 0;
        gbc.gridy = 12;
        panel.add(new JLabel("Year:"), gbc);

        gbc.gridx = 1;
        SpinnerNumberModel yearModel = new SpinnerNumberModel(1, 1, 8, 1);
        yearSpinner = new JSpinner(yearModel);
        panel.add(yearSpinner, gbc);

        // Only show password fields for new students
        if (student == null) {
            // Password section
            gbc.gridx = 0;
            gbc.gridy = 13;
            gbc.gridwidth = 2;
            JLabel passwordLabel = new JLabel("Account Information");
            passwordLabel.setFont(UIUtils.SUBHEADER_FONT);
            passwordLabel.setForeground(UIUtils.PRIMARY_COLOR);
            panel.add(passwordLabel, gbc);

            // Add separator
            gbc.gridy = 14;
            panel.add(new JSeparator(), gbc);

            // Generate password checkbox
            gbc.gridy = 15;
            gbc.gridwidth = 2;
            generatePasswordCheckBox = new JCheckBox("Generate random password");
            generatePasswordCheckBox.setBackground(UIUtils.BACKGROUND_COLOR);
            generatePasswordCheckBox.setSelected(true);
            generatePasswordCheckBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean generate = generatePasswordCheckBox.isSelected();
                    passwordField.setEnabled(!generate);
                    confirmPasswordField.setEnabled(!generate);
                }
            });
            panel.add(generatePasswordCheckBox, gbc);

            // Password
            gbc.gridx = 0;
            gbc.gridy = 16;
            gbc.gridwidth = 1;
            panel.add(new JLabel("Password:"), gbc);

            gbc.gridx = 1;
            passwordField = new JPasswordField(20);
            passwordField.setEnabled(false); // Disabled by default since generate is selected
            panel.add(passwordField, gbc);

            // Confirm Password
            gbc.gridx = 0;
            gbc.gridy = 17;
            panel.add(new JLabel("Confirm Password:"), gbc);

            gbc.gridx = 1;
            confirmPasswordField = new JPasswordField(20);
            confirmPasswordField.setEnabled(false); // Disabled by default since generate is selected
            panel.add(confirmPasswordField, gbc);
        }

        return panel;
    }

    /**
     * Creates the button panel
     * @return The button panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(UIUtils.BACKGROUND_COLOR);

        JButton saveButton = new JButton(student == null ? "Add Student" : "Save Changes");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveStudent();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

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
        yearSpinner.setValue(student.getYear());
    }

    /**
     * Validates the form inputs
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validateInputs() {
        // Check required fields
        if (studentIdField.getText().trim().isEmpty()) {
            UIUtils.showWarningDialog(this, "Please enter a Student ID.", "Missing Information");
            studentIdField.requestFocus();
            return false;
        }

        if (firstNameField.getText().trim().isEmpty()) {
            UIUtils.showWarningDialog(this, "Please enter a First Name.", "Missing Information");
            firstNameField.requestFocus();
            return false;
        }

        if (lastNameField.getText().trim().isEmpty()) {
            UIUtils.showWarningDialog(this, "Please enter a Last Name.", "Missing Information");
            lastNameField.requestFocus();
            return false;
        }

        if (emailField.getText().trim().isEmpty()) {
            UIUtils.showWarningDialog(this, "Please enter an Email address.", "Missing Information");
            emailField.requestFocus();
            return false;
        }

        if (departmentField.getText().trim().isEmpty()) {
            UIUtils.showWarningDialog(this, "Please enter a Department.", "Missing Information");
            departmentField.requestFocus();
            return false;
        }

        if (programField.getText().trim().isEmpty()) {
            UIUtils.showWarningDialog(this, "Please enter a Program.", "Missing Information");
            programField.requestFocus();
            return false;
        }

        // Validate email format
        if (!isValidEmail(emailField.getText().trim())) {
            UIUtils.showWarningDialog(this, "Please enter a valid email address.", "Invalid Email");
            emailField.requestFocus();
            return false;
        }

        // If adding a new student and not generating a password, validate password
        if (student == null && !generatePasswordCheckBox.isSelected()) {
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (password.isEmpty()) {
                UIUtils.showWarningDialog(this, "Please enter a password.", "Missing Password");
                passwordField.requestFocus();
                return false;
            }

            if (!password.equals(confirmPassword)) {
                UIUtils.showWarningDialog(this, "Passwords do not match.", "Password Mismatch");
                passwordField.setText("");
                confirmPasswordField.setText("");
                passwordField.requestFocus();
                return false;
            }

            if (!authService.validatePasswordStrength(password)) {
                UIUtils.showWarningDialog(this,
                        "Password must be at least 8 characters and include a digit, " +
                                "a lowercase letter, an uppercase letter, and a special character.",
                        "Weak Password");
                passwordField.setText("");
                confirmPasswordField.setText("");
                passwordField.requestFocus();
                return false;
            }
        }

        return true;
    }

    /**
     * Saves the student (adds new or updates existing)
     */
    private void saveStudent() {
        if (!validateInputs()) {
            return;
        }

        // Check if editing a student's ID or email, which requires notification
        String oldStudentId = student != null ? student.getStudentId() : null;
        String oldEmail = student != null ? student.getEmail() : null;

        // Get form values
        String studentId = studentIdField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressArea.getText().trim();
        String department = departmentField.getText().trim();
        String program = programField.getText().trim();
        int year = (Integer) yearSpinner.getValue();

        // Check for student ID uniqueness if adding or changing ID
        if (student == null || !studentId.equals(oldStudentId)) {
            Student existingStudent = studentService.getStudentByStudentId(studentId);
            if (existingStudent != null) {
                UIUtils.showWarningDialog(this,
                        "A student with this ID already exists. Please choose a different ID.",
                        "Duplicate Student ID");
                studentIdField.requestFocus();
                return;
            }
        }

        // Show loading indicator
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    if (student == null) {
                        // Adding a new student

                        // Generate a unique ID for the student in Firebase
                        String firebaseId = studentService.generateUniqueId();

                        // Create the student object
                        Student newStudent = new Student(
                                firebaseId, firstName, lastName, email, phone, address,
                                studentId, department, program, year, new Date()
                        );

                        // Add the student to the database
                        boolean studentAdded = studentService.addStudent(newStudent);
                        if (!studentAdded) {
                            return false;
                        }

                        // Create auth record
                        String password;
                        if (generatePasswordCheckBox.isSelected()) {
                            // Generate a random password
                            password = generateRandomPassword();
                        } else {
                            // Use the provided password
                            password = new String(passwordField.getPassword());
                        }

                        boolean authCreated = authService.createAuthRecord(firebaseId, password);
                        if (!authCreated) {
                            // If auth creation failed, delete the student record
                            studentService.deleteStudent(firebaseId);
                            return false;
                        }

                        // Store the generated password for display
                        if (generatePasswordCheckBox.isSelected()) {
                            SwingUtilities.invokeLater(() -> {
                                UIUtils.showInfoDialog(AddEditStudentDialog.this,
                                        "Student added successfully. Generated password: " + password +
                                                "\n\nPlease provide this password to the student.",
                                        "Student Added");
                            });
                        }

                        return true;
                    } else {
                        // Editing an existing student

                        // Check if ID or email changed, which requires notification
                        boolean idChanged = !studentId.equals(oldStudentId);
                        boolean emailChanged = !email.equals(oldEmail);

                        // Update the student object
                        student.setStudentId(studentId);
                        student.setFirstName(firstName);
                        student.setLastName(lastName);
                        student.setEmail(email);
                        student.setPhoneNumber(phone);
                        student.setAddress(address);
                        student.setDepartment(department);
                        student.setProgram(program);
                        student.setYear(year);

                        // Update the student in the database
                        boolean updated = studentService.updateStudent(student);

                        // Send notifications if needed
                        if (updated && (idChanged || emailChanged)) {
                            if (idChanged) {
                                messageService.sendUpdateNotification(
                                        librarian.getId(), librarian.getFullName(),
                                        student.getId(), student.getFullName(),
                                        oldStudentId, studentId, "studentId");
                            }

                            if (emailChanged) {
                                messageService.sendUpdateNotification(
                                        librarian.getId(), librarian.getFullName(),
                                        student.getId(), student.getFullName(),
                                        oldEmail, email, "email");
                            }
                        }

                        return updated;
                    }
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
                        studentSaved = true;

                        // Close the dialog if not showing generated password
                        if (student != null || !generatePasswordCheckBox.isSelected()) {
                            dispose();
                        }
                    } else {
                        UIUtils.showErrorDialog(AddEditStudentDialog.this,
                                "Failed to save student. Please try again.",
                                "Save Failed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    UIUtils.showErrorDialog(AddEditStudentDialog.this,
                            "Error saving student: " + e.getMessage(), "Error");
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };

        worker.execute();
    }

    /**
     * Generates a random password
     * @return A random password
     */
    private String generateRandomPassword() {
        String upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerChars = "abcdefghijklmnopqrstuvwxyz";
        String numberChars = "0123456789";
        String specialChars = "!@#$%^&*()-_=+[]{}|;:,.<>?";
        String allChars = upperChars + lowerChars + numberChars + specialChars;

        Random random = new Random();
        StringBuilder password = new StringBuilder();

        // Ensure at least one of each character type
        password.append(upperChars.charAt(random.nextInt(upperChars.length())));
        password.append(lowerChars.charAt(random.nextInt(lowerChars.length())));
        password.append(numberChars.charAt(random.nextInt(numberChars.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        // Fill the rest with random characters
        for (int i = 0; i < 4; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // Shuffle the password
        char[] passwordArray = password.toString().toCharArray();
        for (int i = 0; i < passwordArray.length; i++) {
            int j = random.nextInt(passwordArray.length);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
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
     * Checks if a student was saved
     * @return true if a student was saved, false otherwise
     */
    public boolean isStudentSaved() {
        return studentSaved;
    }
}

/**
 * Dialog for viewing student details
 */
private class StudentDetailsDialog extends JDialog {
    private Student student;
    private boolean studentModified = false;

    /**
     * Creates a dialog for viewing student details
     * @param parent Parent window
     * @param student Student to view
     */
    public StudentDetailsDialog(Window parent, Student student) {
        super(parent, "Student Details: " + student.getFullName(), ModalityType.APPLICATION_MODAL);
        this.student = student;

        setSize(800, 600);
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(UIUtils.BACKGROUND_COLOR);

        // Student info panel
        JPanel infoPanel = createInfoPanel();
        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // Tabs panel for borrowing history, fines, etc.
        JTabbedPane tabbedPane = createTabbedPane();
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    /**
     * Creates the student info panel
     * @return The info panel
     */
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIUtils.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        UIUtils.PANEL_BORDER,
                        "Student Information",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        UIUtils.SUBHEADER_FONT,
                        UIUtils.PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Left panel for basic info
        JPanel leftPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        leftPanel.setBackground(UIUtils.BACKGROUND_COLOR);

        leftPanel.add(new JLabel("Student ID:"));
        leftPanel.add(new JLabel(student.getStudentId()));

        leftPanel.add(new JLabel("Name:"));
        leftPanel.add(new JLabel(student.getFullName()));

        leftPanel.add(new JLabel("Email:"));
        leftPanel.add(new JLabel(student.getEmail()));

        leftPanel.add(new JLabel("Phone:"));
        leftPanel.add(new JLabel(student.getPhoneNumber() != null ? student.getPhoneNumber() : ""));

        panel.add(leftPanel, BorderLayout.WEST);

        // Right panel for academic info
        JPanel rightPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        rightPanel.setBackground(UIUtils.BACKGROUND_COLOR);

        rightPanel.add(new JLabel("Department:"));
        rightPanel.add(new JLabel(student.getDepartment()));

        rightPanel.add(new JLabel("Program:"));
        rightPanel.add(new JLabel(student.getProgram()));

        rightPanel.add(new JLabel("Year:"));
        rightPanel.add(new JLabel(String.valueOf(student.getYear())));

        rightPanel.add(new JLabel("Enrollment Date:"));
        rightPanel.add(new JLabel(UIUtils.formatDate(student.getEnrollmentDate())));

        panel.add(rightPanel, BorderLayout.EAST);

        // Address panel
        JPanel addressPanel = new JPanel(new BorderLayout(5, 0));
        addressPanel.setBackground(UIUtils.BACKGROUND_COLOR);

        addressPanel.add(new JLabel("Address:"), BorderLayout.NORTH);

        JTextArea addressArea = new JTextArea(2, 20);
        addressArea.setText(student.getAddress() != null ? student.getAddress() : "");
        addressArea.setEditable(false);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setBackground(UIUtils.BACKGROUND_COLOR);

        addressPanel.add(new JScrollPane(addressArea), BorderLayout.CENTER);

        panel.add(addressPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates the tabbed pane for different student details
     * @return The tabbed pane
     */
    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Borrowed books tab
        JPanel borrowedBooksPanel = createBorrowedBooksPanel();
        tabbedPane.addTab("Borrowed Books", borrowedBooksPanel);

        // Borrowing history tab
        JPanel borrowingHistoryPanel = createBorrowingHistoryPanel();
        tabbedPane.addTab("Borrowing History", borrowingHistoryPanel);

        // Fines tab
        JPanel finesPanel = createFinesPanel();
        tabbedPane.addTab("Fines", finesPanel);

        return tabbedPane;
    }

    /**
     * Creates the panel for currently borrowed books
     * @return The borrowed books panel
     */
    private JPanel createBorrowedBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIUtils.BACKGROUND_COLOR);

        // Create table model with columns
        String[] columns = {"Book Title", "Borrow Date", "Due Date", "Days Remaining", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        // Create table
        JTable booksTable = new JTable(tableModel);
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        booksTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(booksTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Load data in background
        loadBorrowedBooks(tableModel);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(UIUtils.BACKGROUND_COLOR);

        JButton processReturnButton = new JButton("Process Return");
        processReturnButton.addActionListener(e -> {
            int row = booksTable.getSelectedRow();
            if (row >= 0) {
                // Get transaction ID from table
                String transactionId = (String) tableModel.getValueAt(row, 5); // Hidden column
                processBookReturn(transactionId);
            } else {
                UIUtils.showWarningDialog(this, "Please select a book to return.", "No Book Selected");
            }
        });

        JButton renewButton = new JButton("Renew Book");
        renewButton.addActionListener(e -> {
            int row = booksTable.getSelectedRow();
            if (row >= 0) {
                // Get transaction ID from table
                String transactionId = (String) tableModel.getValueAt(row, 5); // Hidden column
                renewBook(transactionId);
            } else {
                UIUtils.showWarningDialog(this, "Please select a book to renew.", "No Book Selected");
            }
        });

        buttonPanel.add(renewButton);
        buttonPanel.add(processReturnButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Loads the student's borrowed books into the table
     * @param tableModel Table model to populate
     */
    private void loadBorrowedBooks(DefaultTableModel tableModel) {
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> rows = new ArrayList<>();

                // Get active transactions for this student
                List<Transaction> transactions = transactionService.getActiveTransactionsByStudent(student.getId());

                // For each transaction, get the book details
                for (Transaction transaction : transactions) {
                    Book book = bookService.getBookById(transaction.getBookId());
                    if (book != null) {
                        int daysRemaining = transaction.getDaysRemaining();
                        String status;
                        Color statusColor;

                        if (transaction.getStatus() == Transaction.Status.OVERDUE) {
                            status = "OVERDUE";
                            statusColor = UIUtils.ERROR_COLOR;
                        } else if (daysRemaining <= 3) {
                            status = "DUE SOON";
                            statusColor = UIUtils.WARNING_COLOR;
                        } else {
                            status = "BORROWED";
                            statusColor = UIUtils.TEXT_COLOR;
                        }

                        Object[] row = {
                                book.getTitle(),
                                UIUtils.formatDate(transaction.getBorrowDate()),
                                UIUtils.formatDate(transaction.getDueDate()),
                                daysRemaining,
                                status,
                                transaction.getId() // Hidden column for transaction ID
                        };

                        rows.add(row);
                    }
                }

                return rows;
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> rows = get();

                    // Clear existing rows
                    tableModel.setRowCount(0);

                    // Add rows to table
                    for (Object[] row : rows) {
                        tableModel.addRow(row);
                    }

                    // Hide the transaction ID column
                    if (tableModel.getColumnCount() > 5) {
                        booksTable.getColumnModel().removeColumn(booksTable.getColumnModel().getColumn(5));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    /**
     * Creates the panel for borrowing history
     * @return The borrowing history panel
     */
    private JPanel createBorrowingHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIUtils.BACKGROUND_COLOR);

        // Create table model with columns
        String[] columns = {"Book Title", "Borrow Date", "Due Date", "Return Date", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        // Create table
        JTable historyTable = new JTable(tableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Load data in background
        loadBorrowingHistory(tableModel);

        return panel;
    }

    /**
     * Loads the student's borrowing history into the table
     * @param tableModel Table model to populate
     */
    private void loadBorrowingHistory(DefaultTableModel tableModel) {
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> rows = new ArrayList<>();

                // Get all transactions for this student
                List<Transaction> transactions = transactionService.getTransactionsByStudent(student.getId());

                // Sort by borrow date (newest first)
                transactions.sort((t1, t2) -> t2.getBorrowDate().compareTo(t1.getBorrowDate()));

                // For each transaction, get the book details
                for (Transaction transaction : transactions) {
                    Book book = bookService.getBookById(transaction.getBookId());
                    if (book != null) {
                        String status = transaction.getStatus().toString();

                        Object[] row = {
                                book.getTitle(),
                                UIUtils.formatDate(transaction.getBorrowDate()),
                                UIUtils.formatDate(transaction.getDueDate()),
                                transaction.getReturnDate() != null ? UIUtils.formatDate(transaction.getReturnDate()) : "",
                                status
                        };

                        rows.add(row);
                    }
                }

                return rows;
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> rows = get();

                    // Clear existing rows
                    tableModel.setRowCount(0);

                    // Add rows to table
                    for (Object[] row : rows) {
                        tableModel.addRow(row);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    /**
     * Creates the panel for fines
     * @return The fines panel
     */
    private JPanel createFinesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIUtils.BACKGROUND_COLOR);

        // Summary panel at top
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        summaryPanel.setBackground(UIUtils.BACKGROUND_COLOR);

        JLabel totalFinesLabel = new JLabel("Total Fines: $" + String.format("%.2f", student.getFineBalance()));
        totalFinesLabel.setFont(UIUtils.SUBHEADER_FONT);
        if (student.getFineBalance() > 0) {
            totalFinesLabel.setForeground(UIUtils.ERROR_COLOR);
        }
        summaryPanel.add(totalFinesLabel);

        // Add pay fines button if there are fines
        if (student.getFineBalance() > 0) {
            JButton payFinesButton = new JButton("Process Payment");
            payFinesButton.addActionListener(e -> processPayment());
            summaryPanel.add(payFinesButton);
        }

        panel.add(summaryPanel, BorderLayout.NORTH);

        // Fine history table
        String[] columns = {"Date", "Book", "Amount", "Reason", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        JTable finesTable = new JTable(tableModel);
        finesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        finesTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(finesTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // TODO: Load fine history data (would require additional transaction tracking)
        // For now, just add a placeholder message if there are no fines
        if (student.getFineBalance() <= 0) {
            JPanel placeholderPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            placeholderPanel.setBackground(UIUtils.BACKGROUND_COLOR);

            JLabel placeholderLabel = new JLabel("No fines to display.");
            placeholderLabel.setFont(UIUtils.NORMAL_FONT);
            placeholderPanel.add(placeholderLabel);

            panel.add(placeholderPanel, BorderLayout.CENTER);
        }

        return panel;
    }

    /**
     * Creates the button panel
     * @return The button panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(UIUtils.BACKGROUND_COLOR);

        JButton editButton = new JButton("Edit Student");
        editButton.addActionListener(e -> {
            dispose();
            StudentManagementPanel.this.editStudent();
        });

        JButton sendMessageButton = new JButton("Send Message");
        sendMessageButton.addActionListener(e -> sendMessage());

        JButton resetPasswordButton = new JButton("Reset Password");
        resetPasswordButton.addActionListener(e -> resetPassword());

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());

        panel.add(sendMessageButton);
        panel.add(resetPasswordButton);
        panel.add(editButton);
        panel.add(closeButton);

        return panel;
    }

    /**
     * Processes a book return
     * @param transactionId ID of the transaction to process
     */
    private void processBookReturn(String transactionId) {
        boolean confirm = UIUtils.showConfirmDialog(this,
                "Are you sure you want to process this book return?",
                "Confirm Return");

        if (confirm) {
            // Show loading indicator
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return transactionService.returnBook(transactionId, librarian.getId());
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            UIUtils.showInfoDialog(StudentDetailsDialog.this,
                                    "Book return processed successfully.",
                                    "Return Processed");

                            // Refresh the student object to get updated fine balance
                            student = studentService.getStudentById(student.getId());

                            // Refresh the tabs
                            refreshTabs();

                            // Mark as modified so parent knows to refresh
                            studentModified = true;
                        } else {
                            UIUtils.showErrorDialog(StudentDetailsDialog.this,
                                    "Failed to process return. Please try again.",
                                    "Return Failed");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        UIUtils.showErrorDialog(StudentDetailsDialog.this,
                                "Error processing return: " + e.getMessage(),
                                "Error");
                    } finally {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            };

            worker.execute();
        }
    }

    /**
     * Renews a book loan
     * @param transactionId ID of the transaction to renew
     */
    private void renewBook(String transactionId) {
        // Create a dialog to get the number of days to extend
        JSpinner daysSpinner = new JSpinner(new SpinnerNumberModel(14, 1, 30, 1));

        Object[] message = {
                "How many days would you like to extend the loan?",
                daysSpinner
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Renew Book",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            int days = (Integer) daysSpinner.getValue();

            // Show loading indicator
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return transactionService.renewBook(transactionId, days);
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            UIUtils.showInfoDialog(StudentDetailsDialog.this,
                                    "Book loan renewed successfully for " + days + " days.",
                                    "Loan Renewed");

                            // Refresh the tabs
                            refreshTabs();

                            // Mark as modified so parent knows to refresh
                            studentModified = true;
                        } else {
                            UIUtils.showErrorDialog(StudentDetailsDialog.this,
                                    "Failed to renew loan. The book may be overdue.",
                                    "Renewal Failed");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        UIUtils.showErrorDialog(StudentDetailsDialog.this,
                                "Error renewing loan: " + e.getMessage(),
                                "Error");
                    } finally {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            };

            worker.execute();
        }
    }

    /**
     * Processes a fine payment
     */
    private void processPayment() {
        // Create dialog to get payment amount
        JSpinner amountSpinner = new JSpinner(new SpinnerNumberModel(
                student.getFineBalance(), 0.01, student.getFineBalance(), 0.01));

        Object[] message = {
                "Enter payment amount:",
                amountSpinner
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Process Payment",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            double amount = (Double) amountSpinner.getValue();

            // Process payment
            boolean success = student.payFine(amount);

            if (success) {
                // Update student in database
                boolean updated = studentService.updateStudent(student);

                if (updated) {
                    UIUtils.showInfoDialog(this,
                            "Payment of $" + String.format("%.2f", amount) + " processed successfully.",
                            "Payment Processed");

                    // Refresh the panel
                    refreshTabs();

                    // Mark as modified
                    studentModified = true;
                } else {
                    UIUtils.showErrorDialog(this,
                            "Failed to update student record after payment.",
                            "Update Failed");
                }
            } else {
                UIUtils.showErrorDialog(this,
                        "Invalid payment amount.",
                        "Payment Failed");
            }
        }
    }

    /**
     * Sends a message to the student
     */
    private void sendMessage() {
        // Create message dialog
        JTextField subjectField = new JTextField(30);
        JTextArea contentArea = new JTextArea(10, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);

        Object[] message = {
                "Subject:",
                subjectField,
                "Message:",
                new JScrollPane(contentArea)
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Send Message",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String subject = subjectField.getText().trim();
            String content = contentArea.getText().trim();

            if (subject.isEmpty() || content.isEmpty()) {
                UIUtils.showWarningDialog(this,
                        "Please enter both subject and message content.",
                        "Missing Information");
                return;
            }

            // Show loading indicator
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return messageService.sendNotificationToStudent(
                            librarian.getId(),
                            librarian.getFullName(),
                            student.getId(),
                            student.getFullName(),
                            subject,
                            content
                    );
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            UIUtils.showInfoDialog(StudentDetailsDialog.this,
                                    "Message sent successfully.",
                                    "Message Sent");
                        } else {
                            UIUtils.showErrorDialog(StudentDetailsDialog.this,
                                    "Failed to send message. Please try again.",
                                    "Send Failed");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        UIUtils.showErrorDialog(StudentDetailsDialog.this,
                                "Error sending message: " + e.getMessage(),
                                "Error");
                    } finally {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            };

            worker.execute();
        }
    }

    /**
     * Resets the student's password
     */
    private void resetPassword() {
        boolean confirm = UIUtils.showConfirmDialog(this,
                "Are you sure you want to reset " + student.getFullName() + "'s password?",
                "Confirm Password Reset");

        if (confirm) {
            // Generate a random password
            String newPassword = generateRandomPassword();

            // Show loading indicator
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return authService.resetPassword(student.getId(), newPassword);
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            UIUtils.showInfoDialog(StudentDetailsDialog.this,
                                    "Password reset successfully. New password: " + newPassword +
                                            "\n\nPlease provide this password to the student.",
                                    "Password Reset");

                            // Send a notification to the student
                            messageService.sendNotificationToStudent(
                                    librarian.getId(),
                                    librarian.getFullName(),
                                    student.getId(),
                                    student.getFullName(),
                                    "Your Password Has Been Reset",
                                    "Dear " + student.getFullName() + ",\n\n" +
                                            "Your password has been reset. Please contact the library to receive your new password.\n\n" +
                                            "Regards,\n" +
                                            librarian.getFullName() + "\n" +
                                            "Athena University Library"
                            );
                        } else {
                            UIUtils.showErrorDialog(StudentDetailsDialog.this,
                                    "Failed to reset password. Please try again.",
                                    "Reset Failed");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        UIUtils.showErrorDialog(StudentDetailsDialog.this,
                                "Error resetting password: " + e.getMessage(),
                                "Error");
                    } finally {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            };

            worker.execute();
        }
    }

    /**
     * Refreshes all tabs with the latest data
     */
    private void refreshTabs() {
        // Recreate the tabbed pane
        JTabbedPane tabbedPane = createTabbedPane();

        // Get the content pane
        Container contentPane = getContentPane();

        // Replace the existing tabbed pane
        contentPane.remove(1); // The tabbed pane is the second component
        contentPane.add(tabbedPane, BorderLayout.CENTER);

        // Refresh the UI
        contentPane.revalidate();
        contentPane.repaint();
    }

    /**
     * Generates a random password (same method as in AddEditStudentDialog)
     * @return A random password
     */
    private String generateRandomPassword() {
        String upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerChars = "abcdefghijklmnopqrstuvwxyz";
        String numberChars = "0123456789";
        String specialChars = "!@#$%^&*()-_=+[]{}|;:,.<>?";
        String allChars = upperChars + lowerChars + numberChars + specialChars;

        Random random = new Random();
        StringBuilder password = new StringBuilder();

        // Ensure at least one of each character type
        password.append(upperChars.charAt(random.nextInt(upperChars.length())));
        password.append(lowerChars.charAt(random.nextInt(lowerChars.length())));
        password.append(numberChars.charAt(random.nextInt(numberChars.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        // Fill the rest with random characters
        for (int i = 0; i < 4; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // Shuffle the password
        char[] passwordArray = password.toString().toCharArray();
        for (int i = 0; i < passwordArray.length; i++) {
            int j = random.nextInt(passwordArray.length);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }

    /**
     * Checks if the student was modified
     * @return true if the student was modified, false otherwise
     */
    public boolean isStudentModified() {
        return studentModified;
    }
}
}package com.athena.library.ui.librarian;

import com.athena.library.auth.AuthService;
import com.athena.library.firebase.MessageService;
import com.athena.library.firebase.StudentService;
import com.athena.library.models.Librarian;
import com.athena.library.models.Student;
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
import java.util.*;
        import java.util.List;
import java.util.regex.Pattern;

/**
 * Panel for managing students in the library system
 */
public class StudentManagementPanel extends JPanel {
    private final LibrarianDashboard dashboard;
    private final Librarian librarian;
    private final StudentService studentService;
    private final MessageService messageService;
    private final AuthService authService;

    // UI components
    private JTextField searchField;
    private JComboBox<String> searchTypeComboBox;
    private JTable studentsTable;
    private DefaultTableModel tableModel;
    private JButton addStudentButton;
    private JButton editStudentButton;
    private JButton deleteStudentButton;
    private JButton viewDetailsButton;
    private JButton refreshButton;

    // Data
    private List<Student> allStudents;
    private List<Student> filteredStudents;

    /**
     * Creates a new student management panel
     * @param dashboard The parent dashboard
     * @param librarian The current librarian
     * @param studentService Service for student operations
     * @param messageService Service for messaging operations
     */
    public StudentManagementPanel(LibrarianDashboard dashboard, Librarian librarian,
                                  StudentService studentService, MessageService messageService) {
        this.dashboard = dashboard;
        this.librarian = librarian;
        this.studentService = studentService;
        this.messageService = messageService;
        this.authService = AuthService.getInstance();
        this.allStudents = new ArrayList<>();
        this.filteredStudents = new ArrayList<>();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initializeUI();
        loadStudents();
    }

    /**
     * Initializes the UI components
     */
    private void initializeUI() {
        // Search panel at top
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);

        // Students table in center
        JPanel studentsPanel = createStudentsPanel();
        add(studentsPanel, BorderLayout.CENTER);

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
                "Search Students",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                UIUtils.SUBHEADER_FONT,
                UIUtils.PRIMARY_COLOR));
        panel.setBackground(UIUtils.BACKGROUND_COLOR);

        // Search controls
        JPanel searchControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchControlsPanel.setBackground(UIUtils.BACKGROUND_COLOR);

        // Search type dropdown
        searchTypeComboBox = new JComboBox<>(new String[] { "Name", "Student ID", "Department", "Email" });
        searchControlsPanel.add(new JLabel("Search by:"));
        searchControlsPanel.add(searchTypeComboBox);

        // Search field
        searchField = new JTextField(20);
        searchControlsPanel.add(searchField);

        // Search button
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilters();
            }
        });
        searchControlsPanel.add(searchButton);

        // Clear button
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFilters();
            }
        });
        searchControlsPanel.add(clearButton);

        // Refresh button
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadStudents();
            }
        });
        searchControlsPanel.add(refreshButton);

        panel.add(searchControlsPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates the students table panel
     * @return The students panel
     */
    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                UIUtils.PANEL_BORDER,
                "Student Records",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                UIUtils.SUBHEADER_FONT,
                UIUtils.PRIMARY_COLOR));
        panel.setBackground(UIUtils.BACKGROUND_COLOR);

        // Create table model with columns
        String[] columns = {"Student ID", "Name", "Department", "Program", "Year", "Email", "Borrowed Books"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        // Create table
        studentsTable = new JTable(tableModel);
        studentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentsTable.setRowHeight(25);
        studentsTable.getTableHeader().setReorderingAllowed(false);

        // Add double-click listener
        studentsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewStudentDetails();
                }
            }
        });

        // Create sorter
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        studentsTable.setRowSorter(sorter);

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(studentsTable);
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

        addStudentButton = new JButton("Add Student");
        addStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddStudentDialog();
            }
        });

        editStudentButton = new JButton("Edit Student");
        editStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editStudent();
            }
        });

        deleteStudentButton = new JButton("Delete Student");
        deleteStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteStudent();
            }
        });

        viewDetailsButton = new JButton("View Details");
        viewDetailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewStudentDetails();
            }
        });

        panel.add(viewDetailsButton);
        panel.add(addStudentButton);
        panel.add(editStudentButton);
        panel.add(deleteStudentButton);

        return panel;
    }

    /**
     * Loads all students from the database
     */
    private void loadStudents() {
        // Show loading indicator
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        dashboard.updateStatus("Loading students...", false);

        SwingWorker<List<Student>, Void> worker = new SwingWorker<List<Student>, Void>() {
            @Override
            protected List<Student> doInBackground() throws Exception {
                return studentService.getAllStudents();
            }

            @Override
            protected void done() {
                try {
                    allStudents = get();
                    filteredStudents = new ArrayList<>(allStudents);

                    // Populate the table
                    updateStudentsTable();

                    // Reset cursor and status
                    setCursor(Cursor.getDefaultCursor());
                    dashboard.updateStatus("Loaded " + allStudents.size() + " students", false);
                } catch (Exception e) {
                    e.printStackTrace();
                    UIUtils.showErrorDialog(StudentManagementPanel.this,
                            "Error loading students: " + e.getMessage(), "Error");
                    setCursor(Cursor.getDefaultCursor());
                    dashboard.updateStatus("Error loading students", true);
                }
            }
        };

        worker.execute();
    }

    /**
     * Updates the students table with current filtered students
     */
    private void updateStudentsTable() {
        // Clear the table
        tableModel.setRowCount(0);

        // Add students to table
        for (Student student : filteredStudents) {
            Object[] row = {
                    student.getStudentId(),
                    student.getFullName(),
                    student.getDepartment(),
                    student.getProgram(),
                    student.getYear(),
                    student.getEmail(),
                    student.getBorrowedBooksCount()
            };
            tableModel.addRow(row);
        }
    }

    /**
     * Applies the current search filter to the student list
     */
    private void applyFilters() {
        // Get filter values
        String searchText = searchField.getText().trim().toLowerCase();
        String searchType = searchTypeComboBox.getSelectedItem().toString();

        if (searchText.isEmpty()) {
            // If search text is empty, show all students
            filteredStudents = new ArrayList<>(allStudents);
        } else {
            // Apply filter based on search type
            filteredStudents = new ArrayList<>();

            for (Student student : allStudents) {
                boolean match = false;

                switch (searchType) {
                    case "Name":
                        String fullName = (student.getFirstName() + " " + student.getLastName()).toLowerCase();
                        match = fullName.contains(searchText);
                        break;
                    case "Student ID":
                        match = student.getStudentId().toLowerCase