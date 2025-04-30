package com.athena.library.ui;

import com.athena.library.auth.AuthService;
import com.athena.library.models.Librarian;
import com.athena.library.models.Student;
import com.athena.library.utils.ErrorHandler;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the login screen
 */
public class LoginScreenFX {
    private final Stage stage;
    private final AuthService authService;

    // UI components
    private TextField userIdField;
    private PasswordField passwordField;
    private RadioButton studentRadioButton;
    private RadioButton librarianRadioButton;
    private Button loginButton;
    private Button cancelButton;
    private Text statusText;

    /**
     * Creates a new login screen
     * @param stage The primary stage
     */
    public LoginScreenFX(Stage stage) {
        this.stage = stage;
        this.authService = AuthService.getInstance();
    }

    /**
     * Shows the login screen
     */
    public void show() {
        // Create main container
        BorderPane mainPane = new BorderPane();
        mainPane.setPadding(new Insets(20));
        mainPane.setStyle("-fx-background-color: #F5F5F5;"); // BACKGROUND_COLOR

        // Create header with logo and title
        HBox headerBox = createHeaderBox();
        mainPane.setTop(headerBox);

        // Create login form
        VBox formBox = createLoginForm();
        mainPane.setCenter(formBox);

        // Create button panel
        HBox buttonBox = createButtonBox();
        mainPane.setBottom(buttonBox);

        // Create scene
        Scene scene = new Scene(mainPane, 500, 400);
        scene.getStylesheets().add(getClass().getResource("/styles/athena-styles.css").toExternalForm());

        // Set scene on stage
        stage.setScene(scene);
        stage.setTitle("Athena University Library System - Login");
        stage.setResizable(false);
        stage.show();

        // Request focus to user ID field
        Platform.runLater(() -> userIdField.requestFocus());
    }

    /**
     * Creates the header box with logo and title
     * @return The header box
     */
    private HBox createHeaderBox() {
        HBox headerBox = new HBox(20);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // Logo placeholder (replace with actual logo)
        ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/images/logo.png")));
        logoView.setFitHeight(80);
        logoView.setFitWidth(80);
        logoView.setPreserveRatio(true);

        // Title
        Label titleLabel = new Label("Athena University Library System");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#1a3c5a")); // PRIMARY_COLOR

        headerBox.getChildren().addAll(logoView, titleLabel);
        return headerBox;
    }

    /**
     * Creates the login form
     * @return The login form box
     */
    private VBox createLoginForm() {
        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(20));
        formBox.setAlignment(Pos.TOP_CENTER);

        // Create a bordered pane for the form
        VBox formContainer = new VBox(15);
        formContainer.setPadding(new Insets(20));
        formContainer.setStyle("-fx-background-color: white; -fx-border-color: #1a3c5a; -fx-border-width: 1px; -fx-border-radius: 5px;");

        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(3.0);
        dropShadow.setOffsetY(3.0);
        dropShadow.setColor(Color.color(0.4, 0.4, 0.4, 0.5));
        formContainer.setEffect(dropShadow);

        // User type selection
        HBox userTypeBox = new HBox(20);
        userTypeBox.setAlignment(Pos.CENTER);

        ToggleGroup userTypeGroup = new ToggleGroup();
        studentRadioButton = new RadioButton("Student");
        studentRadioButton.setToggleGroup(userTypeGroup);
        studentRadioButton.setSelected(true);

        librarianRadioButton = new RadioButton("Librarian");
        librarianRadioButton.setToggleGroup(userTypeGroup);

        userTypeBox.getChildren().addAll(new Label("Login as:"), studentRadioButton, librarianRadioButton);

        // User ID field
        HBox userIdBox = new HBox(10);
        userIdBox.setAlignment(Pos.CENTER_LEFT);
        Label userIdLabel = new Label("ID:");
        userIdLabel.setPrefWidth(80);
        userIdField = new TextField();
        userIdField.setPromptText("Enter your Student ID");
        userIdField.setPrefWidth(250);
        userIdBox.getChildren().addAll(userIdLabel, userIdField);

        // Password field
        HBox passwordBox = new HBox(10);
        passwordBox.setAlignment(Pos.CENTER_LEFT);
        Label passwordLabel = new Label("Password:");
        passwordLabel.setPrefWidth(80);
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefWidth(250);
        passwordBox.getChildren().addAll(passwordLabel, passwordField);

        // Status text for errors/messages
        statusText = new Text();
        statusText.setFill(Color.RED);

        // Add event listeners for radio buttons to update prompts
        studentRadioButton.setOnAction(e ->
                userIdField.setPromptText("Enter your Student ID"));

        librarianRadioButton.setOnAction(e ->
                userIdField.setPromptText("Enter your Staff ID"));

        // Add components to form
        formContainer.getChildren().addAll(userTypeBox, userIdBox, passwordBox, statusText);
        formBox.getChildren().add(formContainer);

        return formBox;
    }

    /**
     * Creates the button box
     * @return The button box
     */
    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        buttonBox.setAlignment(Pos.CENTER);

        loginButton = new Button("Login");
        loginButton.setPrefWidth(100);
        loginButton.getStyleClass().add("primary-button");
        loginButton.setOnAction(e -> attemptLogin());

        cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(100);
        cancelButton.setOnAction(e -> Platform.exit());

        buttonBox.getChildren().addAll(loginButton, cancelButton);

        // Add enter key handler for password field
        passwordField.setOnAction(e -> loginButton.fire());

        return buttonBox;
    }

    /**
     * Attempts to log in with the provided credentials
     */
    private void attemptLogin() {
        String userId = userIdField.getText().trim();
        String password = passwordField.getText();

        // Validate inputs
        if (userId.isEmpty() || password.isEmpty()) {
            statusText.setText("Please enter both ID and password.");
            statusText.setFill(Color.RED);
            return;
        }

        // Disable login button and show loading message
        loginButton.setDisable(true);
        statusText.setText("Authenticating...");
        statusText.setFill(Color.BLUE);

        // Determine login type
        boolean isStudentLogin = studentRadioButton.isSelected();

        // Create authentication task
        Task<Object> authTask = new Task<Object>() {
            @Override
            protected Object call() throws Exception {
                if (isStudentLogin) {
                    return authService.authenticateStudent(userId, password);
                } else {
                    return authService.authenticateLibrarian(userId, password);
                }
            }
        };

        // Handle task completion
        authTask.setOnSucceeded(event -> {
            Object result = authTask.getValue();

            if (result == null) {
                // Authentication failed
                Platform.runLater(() -> {
                    statusText.setText("Invalid ID or password. Please try again.");
                    statusText.setFill(Color.RED);
                    loginButton.setDisable(false);
                });
            } else {
                // Authentication successful
                Platform.runLater(() -> {
                    statusText.setText("Login successful!");
                    statusText.setFill(Color.GREEN);
                });

                // Open appropriate dashboard based on user type
                if (result instanceof Student) {
                    openStudentDashboard((Student) result);
                } else if (result instanceof Librarian) {
                    openLibrarianDashboard((Librarian) result);
                }
            }
        });

        authTask.setOnFailed(event -> {
            // Authentication error
            Platform.runLater(() -> {
                Throwable exception = authTask.getException();
                ErrorHandler.handleException((Exception) exception, null,
                        "Error during login process",
                        ErrorHandler.ErrorType.AUTHENTICATION);

                statusText.setText("Error during login: " + exception.getMessage());
                statusText.setFill(Color.RED);
                loginButton.setDisable(false);
            });
        });

        // Start authentication task
        Thread authThread = new Thread(authTask);
        authThread.setDaemon(true);
        authThread.start();
    }

    /**
     * Opens the student dashboard
     * @param student Authenticated student
     */
    private void openStudentDashboard(Student student) {
        Platform.runLater(() -> {
            try {
                // Close login screen
                stage.hide();

                // Open student dashboard
                StudentDashboardFX dashboard = new StudentDashboardFX(student);
                dashboard.show();
            } catch (Exception e) {
                ErrorHandler.handleException(e, null,
                        "Failed to open student dashboard",
                        ErrorHandler.ErrorType.UI);
            }
        });
    }

    /**
     * Opens the librarian dashboard
     * @param librarian Authenticated librarian
     */
    private void openLibrarianDashboard(Librarian librarian) {
        Platform.runLater(() -> {
            try {
                // Close login screen
                stage.hide();

                // Open librarian dashboard
                LibrarianDashboardFX dashboard = new LibrarianDashboardFX(librarian);
                dashboard.show();
            } catch (Exception e) {
                ErrorHandler.handleException(e, null,
                        "Failed to open librarian dashboard",
                        ErrorHandler.ErrorType.UI);
            }
        });
    }
}