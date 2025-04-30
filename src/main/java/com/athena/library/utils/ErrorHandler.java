package com.athena.library.utils;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Centralized error handling utility for the application
 */
public class ErrorHandler {
    private static final Logger LOGGER = Logger.getLogger("AthenaULS");
    private static FileHandler fileHandler;
    private static final String LOG_DIRECTORY = "logs";
    private static final String LOG_FILE_PREFIX = "athena-error-";

    // Error types for categorization
    public enum ErrorType {
        AUTHENTICATION("Authentication Error"),
        DATABASE("Database Error"),
        NETWORK("Network Error"),
        UI("UI Error"),
        FILE("File Error"),
        GENERAL("Application Error");

        private final String displayName;

        ErrorType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Initialize the logger
    static {
        try {
            // Ensure log directory exists
            File logDir = new File(LOG_DIRECTORY);
            if (!logDir.exists()) {
                logDir.mkdir();
            }

            // Create a log file with date in name
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
            String logFileName = LOG_FILE_PREFIX + dateFormat.format(new Date()) + ".log";

            // Set up file handler
            fileHandler = new FileHandler(LOG_DIRECTORY + File.separator + logFileName, true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.ALL);

            // Don't use parent handlers
            LOGGER.setUseParentHandlers(false);
        } catch (IOException e) {
            // Can't use our own logging system here since it's failing to initialize
            System.err.println("Failed to initialize error logging: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles an exception with customizable user message
     *
     * @param error The exception that occurred
     * @param component The UI component where the error occurred (for dialog positioning)
     * @param userMessage A user-friendly message to show
     * @param errorType Type of error for categorization
     */
    public static void handleException(Exception error, java.awt.Component component,
                                       String userMessage, ErrorType errorType) {
        // Log the error with details
        LOGGER.log(Level.SEVERE, errorType.getDisplayName() + ": " + userMessage, error);

        // Log additional context if available
        if (AuthService.getInstance().isLoggedIn()) {
            LOGGER.log(Level.INFO, "User context: " + AuthService.getInstance().getCurrentUserId()
                    + " (" + AuthService.getInstance().getCurrentUserType() + ")");
        }

        // Show user-friendly message
        SwingUtilities.invokeLater(() -> {
            UIUtils.showErrorDialog(component, userMessage, errorType.getDisplayName());
        });
    }

    /**
     * Handles an exception with a default user message derived from the exception
     *
     * @param error The exception that occurred
     * @param component The UI component where the error occurred (for dialog positioning)
     * @param errorType Type of error for categorization
     */
    public static void handleException(Exception error, java.awt.Component component, ErrorType errorType) {
        // Generate default message from exception
        String userMessage = "An error occurred: " + error.getMessage();
        handleException(error, component, userMessage, errorType);
    }

    /**
     * Logs a warning without showing a user message
     *
     * @param message The warning message
     * @param errorType Type of warning for categorization
     */
    public static void logWarning(String message, ErrorType errorType) {
        LOGGER.log(Level.WARNING, errorType.getDisplayName() + ": " + message);
    }

    /**
     * Logs information for tracking purposes
     *
     * @param message The information message
     */
    public static void logInfo(String message) {
        LOGGER.log(Level.INFO, message);
    }

    /**
     * Creates a detailed error report for support
     *
     * @param error The exception to report
     * @param userDescription User description of what happened
     * @return The path to the created report file
     */
    public static String createErrorReport(Exception error, String userDescription) {
        try {
            // Create reports directory if it doesn't exist
            File reportsDir = new File("error-reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdir();
            }

            // Create a report file with timestamp
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
            String reportFileName = "error-report-" + dateFormat.format(new Date()) + ".txt";
            File reportFile = new File(reportsDir, reportFileName);

            // Write report content
            try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile))) {
                writer.println("=== Athena ULS Error Report ===");
                writer.println("Date: " + new Date());
                writer.println("User ID: " + (AuthService.getInstance().isLoggedIn() ?
                        AuthService.getInstance().getCurrentUserId() : "Not logged in"));
                writer.println("User Type: " + (AuthService.getInstance().isLoggedIn() ?
                        AuthService.getInstance().getCurrentUserType() : "N/A"));
                writer.println("\n=== User Description ===");
                writer.println(userDescription);
                writer.println("\n=== Exception Details ===");
                writer.println("Exception: " + error.getClass().getName());
                writer.println("Message: " + error.getMessage());
                writer.println("\n=== Stack Trace ===");
                error.printStackTrace(writer);
                writer.println("\n=== System Information ===");
                writer.println("OS: " + System.getProperty("os.name") + " " +
                        System.getProperty("os.version"));
                writer.println("Java: " + System.getProperty("java.version"));
                writer.println("Memory: " +
                        (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /
                                (1024 * 1024) + "MB used of " +
                        (Runtime.getRuntime().totalMemory() / (1024 * 1024)) + "MB total");
            }

            return reportFile.getAbsolutePath();
        } catch (IOException e) {
            // If we can't create the report, just log it
            LOGGER.log(Level.SEVERE, "Failed to create error report", e);
            return null;
        }
    }

    /**
     * Ensures all logs are properly closed when the application exits
     */
    public static void shutdown() {
        if (fileHandler != null) {
            fileHandler.close();
        }
    }
}