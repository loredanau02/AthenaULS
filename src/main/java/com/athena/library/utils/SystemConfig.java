package com.athena.library.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Configuration manager for the application
 * Handles loading and saving settings
 */
public class ConfigManager {
    // Constants
    private static final String CONFIG_DIR = "config";
    private static final String CONFIG_FILE = "athena.properties";

    // Default configuration values
    private static final int DEFAULT_LOAN_PERIOD_DAYS = 14;
    private static final double DEFAULT_FINE_PER_DAY = 0.50;
    private static final int DEFAULT_MAX_BOOKS_PER_STUDENT = 5;
    private static final int DEFAULT_PASSWORD_RESET_EXPIRE_DAYS = 3;

    // Singleton instance
    private static ConfigManager instance;

    // Properties object to store configuration
    private final Properties properties;

    /**
     * Private constructor for singleton pattern
     */
    private ConfigManager() {
        properties = new Properties();
        loadConfiguration();
    }

    /**
     * Gets the singleton instance
     * @return ConfigManager instance
     */
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    /**
     * Loads configuration from file
     * If file doesn't exist, creates it with default values
     */
    private void loadConfiguration() {
        // Ensure config directory exists
        Path configDir = Paths.get(CONFIG_DIR);
        if (!Files.exists(configDir)) {
            try {
                Files.createDirectories(configDir);
            } catch (IOException e) {
                ErrorHandler.handleException(e, null,
                        "Could not create configuration directory",
                        ErrorHandler.ErrorType.FILE);
            }
        }

        // Config file path
        Path configFile = configDir.resolve(CONFIG_FILE);

        // If file exists, load it
        if (Files.exists(configFile)) {
            try (InputStream input = new FileInputStream(configFile.toFile())) {
                properties.load(input);
                ErrorHandler.logInfo("Configuration loaded from " + configFile);
            } catch (IOException e) {
                ErrorHandler.handleException(e, null,
                        "Could not load configuration file",
                        ErrorHandler.ErrorType.FILE);
            }
        } else {
            // File doesn't exist, create with defaults
            setDefaultValues();
            saveConfiguration();
        }
    }

    /**
     * Sets default values for all configuration properties
     */
    private void setDefaultValues() {
        properties.setProperty("library.loan.period.days", String.valueOf(DEFAULT_LOAN_PERIOD_DAYS));
        properties.setProperty("library.fine.amount.per.day", String.valueOf(DEFAULT_FINE_PER_DAY));
        properties.setProperty("library.max.books.per.student", String.valueOf(DEFAULT_MAX_BOOKS_PER_STUDENT));
        properties.setProperty("library.password.reset.expire.days", String.valueOf(DEFAULT_PASSWORD_RESET_EXPIRE_DAYS));
        properties.setProperty("ui.theme", "default");
        properties.setProperty("email.notifications.enabled", "true");
    }

    /**
     * Saves current configuration to file
     */
    public void saveConfiguration() {
        try (FileOutputStream output = new FileOutputStream(CONFIG_DIR + "/" + CONFIG_FILE)) {
            properties.store(output, "Athena University Library System Configuration");
            ErrorHandler.logInfo("Configuration saved to " + CONFIG_DIR + "/" + CONFIG_FILE);
        } catch (IOException e) {
            ErrorHandler.handleException(e, null,
                    "Could not save configuration file",
                    ErrorHandler.ErrorType.FILE);
        }
    }

    /**
     * Gets a string property
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Property value
     */
    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Gets an integer property
     * @param key Property key
     * @param defaultValue Default value if property not found or invalid
     * @return Property value
     */
    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            ErrorHandler.logWarning("Invalid integer configuration value for " + key,
                    ErrorHandler.ErrorType.GENERAL);
            return defaultValue;
        }
    }

    /**
     * Gets a double property
     * @param key Property key
     * @param defaultValue Default value if property not found or invalid
     * @return Property value
     */
    public double getDouble(String key, double defaultValue) {
        try {
            return Double.parseDouble(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            ErrorHandler.logWarning("Invalid double configuration value for " + key,
                    ErrorHandler.ErrorType.GENERAL);
            return defaultValue;
        }
    }

    /**
     * Gets a boolean property
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Property value
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key, String.valueOf(defaultValue));
        return "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "1".equals(value);
    }

    /**
     * Sets a property value
     * @param key Property key
     * @param value New value
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * Gets the loan period in days
     * @return Loan period in days
     */
    public int getLoanPeriodDays() {
        return getInt("library.loan.period.days", DEFAULT_LOAN_PERIOD_DAYS);
    }

    /**
     * Gets the fine amount per day
     * @return Fine amount per day
     */
    public double getFineAmountPerDay() {
        return getDouble("library.fine.amount.per.day", DEFAULT_FINE_PER_DAY);
    }

    /**
     * Gets the maximum books a student can borrow
     * @return Maximum books per student
     */
    public int getMaxBooksPerStudent() {
        return getInt("library.max.books.per.student", DEFAULT_MAX_BOOKS_PER_STUDENT);
    }

    /**
     * Gets the UI theme name
     * @return UI theme name
     */
    public String getUiTheme() {
        return getString("ui.theme", "default");
    }

    /**
     * Checks if email notifications are enabled
     * @return true if enabled, false otherwise
     */
    public boolean isEmailNotificationsEnabled() {
        return getBoolean("email.notifications.enabled", true);
    }
}