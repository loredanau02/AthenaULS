package com.athena.library;

import com.athena.library.ui.LoginScreen;
import com.athena.library.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Main class for the Athena University Library System
 */
public class Main {

    /**
     * Application entry point
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Set up look and feel
        setupLookAndFeel();

        // Show splash screen
        SplashScreen splashScreen = new SplashScreen();
        splashScreen.setVisible(true);

        // Initialize Firebase in a background thread
        SwingWorker<Boolean, Void> initWorker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    // Initialize Firebase


                    // Simulate longer loading time for demo purposes
                    Thread.sleep(2000);

                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void done() {
                // Close splash screen
                splashScreen.dispose();

                try {
                    boolean success = get();
                    if (success) {
                        // Start the application
                        SwingUtilities.invokeLater(() -> new LoginScreen());
                    } else {
                        // Show error message
                        JOptionPane.showMessageDialog(null,
                                "Failed to initialize Firebase. Please check your connection and try again.",
                                "Initialization Error",
                                JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "An error occurred: " + e.getMessage(),
                            "Application Error",
                            JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            }
        };

        initWorker.execute();
    }

    /**
     * Sets up the look and feel for the application
     */
    private static void setupLookAndFeel() {
        try {
            // Try to use system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Customize the UI manager
            UIManager.put("Panel.background", UIUtils.BACKGROUND_COLOR);
            UIManager.put("OptionPane.background", UIUtils.BACKGROUND_COLOR);
            UIManager.put("Button.background", UIUtils.PRIMARY_COLOR);
            UIManager.put("Button.foreground", UIUtils.LIGHT_TEXT_COLOR);
            UIManager.put("Button.font", UIUtils.NORMAL_FONT);
            UIManager.put("Label.font", UIUtils.NORMAL_FONT);
            UIManager.put("TextField.font", UIUtils.NORMAL_FONT);
            UIManager.put("TextArea.font", UIUtils.NORMAL_FONT);
            UIManager.put("Table.font", UIUtils.NORMAL_FONT);
            UIManager.put("TableHeader.font", UIUtils.SUBHEADER_FONT);
            UIManager.put("TitledBorder.font", UIUtils.SUBHEADER_FONT);
            UIManager.put("TitledBorder.titleColor", UIUtils.PRIMARY_COLOR);
        } catch (Exception e) {
            System.err.println("Failed to set look and feel: " + e.getMessage());
        }
    }

    /**
     * Splash screen shown during application startup
     */
    static class SplashScreen extends JWindow {
        /**
         * Creates the splash screen
         */
        public SplashScreen() {
            // Set size and position
            setSize(500, 300);
            setLocationRelativeTo(null);

            // Create content panel
            JPanel content = new JPanel(new BorderLayout());
            content.setBackground(UIUtils.PRIMARY_COLOR);
            content.setBorder(BorderFactory.createLineBorder(UIUtils.SECONDARY_COLOR, 2));

            // Add logo (placeholder)
            JLabel logoLabel = new JLabel("Athena University");  // Using text instead of image for now
            logoLabel.setFont(new Font("Arial", Font.BOLD, 36));
            logoLabel.setForeground(UIUtils.LIGHT_TEXT_COLOR);
            logoLabel.setHorizontalAlignment(JLabel.CENTER);
            content.add(logoLabel, BorderLayout.CENTER);

            // Add application title
            JLabel titleLabel = new JLabel("Library Management System");
            titleLabel.setFont(UIUtils.HEADER_FONT);
            titleLabel.setForeground(UIUtils.LIGHT_TEXT_COLOR);
            titleLabel.setHorizontalAlignment(JLabel.CENTER);
            content.add(titleLabel, BorderLayout.NORTH);

            // Add loading text
            JLabel loadingLabel = new JLabel("Initializing, please wait...");
            loadingLabel.setFont(UIUtils.NORMAL_FONT);
            loadingLabel.setForeground(UIUtils.LIGHT_TEXT_COLOR);
            loadingLabel.setHorizontalAlignment(JLabel.CENTER);
            content.add(loadingLabel, BorderLayout.SOUTH);

            setContentPane(content);
        }
    }
}