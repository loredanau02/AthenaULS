package com.athena.library;

import com.athena.library.firebase.FirebaseConfig;
import com.athena.library.ui.javafx.LoginScreenFX;
import com.athena.library.utils.ConfigManager;
import com.athena.library.utils.ErrorHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application class for the JavaFX version of Athena ULS
 */
public class MainFX extends Application {
    private Stage primaryStage;

    /**
     * Application entry point
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Athena University Library System");

        // Load configuration
        ConfigManager.getInstance();

        // Show loading screen
        showLoadingScreen();

        // Initialize Firebase in background thread
        Task<Boolean> initTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    // Initialize Firebase
                    FirebaseConfig.initialize();

                    // Simulate longer loading time for demo purposes
                    Thread.sleep(2000);

                    return true;
                } catch (IOException e) {
                    ErrorHandler.handleException(e, null,
                            "Failed to initialize Firebase. Check your connection and credentials.",
                            ErrorHandler.ErrorType.DATABASE);
                    return false;
                }
            }
        };

        // Handle task completion
        initTask.setOnSucceeded(event -> {
            if (initTask.getValue()) {
                // Show login screen
                Platform.runLater(this::showLoginScreen);
            } else {
                // Show error and exit
                showErrorAlert();
            }
        });

        initTask.setOnFailed(event -> {
            // Show error and exit
            showErrorAlert();
        });

        // Start initialization task
        Thread initThread = new Thread(initTask);
        initThread.setDaemon(true);
        initThread.start();
    }

    /**
     * Shows the loading screen
     */
    private void showLoadingScreen() {
        StackPane loadingPane = new StackPane();
        loadingPane.setStyle("-fx-background-color: #1a3c5a;"); // PRIMARY_COLOR

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setProgress(-1); // Indeterminate progress
        loadingPane.getChildren().add(progressIndicator);

        Scene scene = new Scene(loadingPane, 500, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Shows the login screen
     */
    private void showLoginScreen() {
        try {
            LoginScreenFX loginScreen = new LoginScreenFX(primaryStage);
            loginScreen.show();
        } catch (Exception e) {
            ErrorHandler.handleException(e, null,
                    "Failed to load login screen.",
                    ErrorHandler.ErrorType.UI);
        }
    }

    /**
     * Shows an error alert and exits the application
     */
    private void showErrorAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Initialization Error");
            alert.setHeaderText("Failed to Initialize");
            alert.setContentText("The application could not be initialized. Please check your network connection and try again.");
            alert.showAndWait().ifPresent(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    Platform.exit();
                }
            });
        });
    }

    @Override
    public void stop() {
        // Clean up resources
        ErrorHandler.shutdown();
    }
}