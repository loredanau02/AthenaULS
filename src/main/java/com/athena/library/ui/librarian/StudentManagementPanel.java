package com.athena.library.ui.librarian;

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
}