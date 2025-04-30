package com.athena.library.utils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for common UI operations
 */
public class UIUtils {
    // Color scheme
    public static final Color PRIMARY_COLOR = new Color(26, 60, 90); // Deep blue
    public static final Color SECONDARY_COLOR = new Color(243, 156, 18); // Warm amber
    public static final Color BACKGROUND_COLOR = new Color(245, 245, 245); // Light gray
    public static final Color SUCCESS_COLOR = new Color(39, 174, 96); // Green
    public static final Color WARNING_COLOR = new Color(230, 126, 34); // Orange
    public static final Color ERROR_COLOR = new Color(192, 57, 43); // Red
    public static final Color TEXT_COLOR = new Color(44, 62, 80); // Dark blue-gray
    public static final Color LIGHT_TEXT_COLOR = new Color(236, 240, 241); // Almost white

    // Font sizes
    public static final int HEADER_FONT_SIZE = 20;
    public static final int SUBHEADER_FONT_SIZE = 16;
    public static final int NORMAL_FONT_SIZE = 14;
    public static final int SMALL_FONT_SIZE = 12;

    // Standard fonts
    public static final Font HEADER_FONT = new Font("Arial", Font.BOLD, HEADER_FONT_SIZE);
    public static final Font SUBHEADER_FONT = new Font("Arial", Font.BOLD, SUBHEADER_FONT_SIZE);
    public static final Font NORMAL_FONT = new Font("Arial", Font.PLAIN, NORMAL_FONT_SIZE);
    public static final Font SMALL_FONT = new Font("Arial", Font.PLAIN, SMALL_FONT_SIZE);

    // Standard borders
    public static final Border PANEL_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
    );

    // Date formatters
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy");
    private static final SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    /**
     * Creates an ImageIcon from a resource path
     * @param path Resource path
     * @param description Description of the image
     * @return ImageIcon, or null if the path was invalid
     */
    public static ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = UIUtils.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Creates a button with standard styling
     * @param text Button text
     * @return Styled JButton
     */
    public static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(NORMAL_FONT);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(LIGHT_TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);

        return button;
    }

    /**
     * Creates a panel with standard border and background
     * @return Styled JPanel
     */
    public static JPanel createStyledPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(PANEL_BORDER);
        panel.setBackground(BACKGROUND_COLOR);

        return panel;
    }

    /**
     * Creates a header label with standard styling
     * @param text Label text
     * @return Styled JLabel
     */
    public static JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(HEADER_FONT);
        label.setForeground(PRIMARY_COLOR);

        return label;
    }

    /**
     * Creates a subheader label with standard styling
     * @param text Label text
     * @return Styled JLabel
     */
    public static JLabel createSubHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SUBHEADER_FONT);
        label.setForeground(PRIMARY_COLOR);

        return label;
    }

    /**
     * Formats a date in the standard date format (MM/dd/yyyy)
     * @param date Date to format
     * @return Formatted date string
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return DATE_FORMATTER.format(date);
    }

    /**
     * Formats a date in the standard date-time format (MM/dd/yyyy HH:mm)
     * @param date Date to format
     * @return Formatted date-time string
     */
    public static String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return DATE_TIME_FORMATTER.format(date);
    }

    /**
     * Shows a confirmation dialog with standard styling
     * @param parentComponent Parent component
     * @param message Dialog message
     * @param title Dialog title
     * @return true if user confirms, false otherwise
     */
    public static boolean showConfirmDialog(Component parentComponent, String message, String title) {
        int result = JOptionPane.showConfirmDialog(
                parentComponent,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Shows an error dialog with standard styling
     * @param parentComponent Parent component
     * @param message Error message
     * @param title Dialog title
     */
    public static void showErrorDialog(Component parentComponent, String message, String title) {
        JOptionPane.showMessageDialog(
                parentComponent,
                message,
                title,
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Shows a warning dialog with standard styling
     * @param parentComponent Parent component
     * @param message Warning message
     * @param title Dialog title
     */
    public static void showWarningDialog(Component parentComponent, String message, String title) {
        JOptionPane.showMessageDialog(
                parentComponent,
                message,
                title,
                JOptionPane.WARNING_MESSAGE
        );
    }

    /**
     * Shows an information dialog with standard styling
     * @param parentComponent Parent component
     * @param message Information message
     * @param title Dialog title
     */
    public static void showInfoDialog(Component parentComponent, String message, String title) {
        JOptionPane.showMessageDialog(
                parentComponent,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Creates a styled scroll pane
     * @param component Component to wrap
     * @return Styled JScrollPane
     */
    public static JScrollPane createStyledScrollPane(Component component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 1));
        return scrollPane;
    }
}