package com.athena.library.models;

/**
 * Represents a librarian (admin) in the library system
 */
public class Librarian extends User {
    private String staffId;
    private String department;
    private String role; // Admin, Senior Librarian, Assistant, etc.
    private boolean isAdmin;

    /**
     * Default constructor
     */
    public Librarian() {
        super();
    }

    /**
     * Constructor with essential fields
     */
    public Librarian(String id, String firstName, String lastName, String email,
                     String staffId, String department, String role, boolean isAdmin) {
        super(id, firstName, lastName, email, null, null);
        this.staffId = staffId;
        this.department = department;
        this.role = role;
        this.isAdmin = isAdmin;
    }

    /**
     * Full constructor
     */
    public Librarian(String id, String firstName, String lastName, String email,
                     String phoneNumber, String address, String staffId,
                     String department, String role, boolean isAdmin) {
        super(id, firstName, lastName, email, phoneNumber, address);
        this.staffId = staffId;
        this.department = department;
        this.role = role;
        this.isAdmin = isAdmin;
    }

    // Getters and Setters

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    /**
     * Checks if the librarian has sufficient privileges for an admin action
     * @return true if the librarian is an admin, false otherwise
     */
    public boolean hasAdminPrivileges() {
        return isAdmin || "Admin".equalsIgnoreCase(role) || "Senior Librarian".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "Librarian{" +
                "id='" + getId() + '\'' +
                ", staffId='" + staffId + '\'' +
                ", name='" + getFullName() + '\'' +
                ", department='" + department + '\'' +
                ", role='" + role + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }
}