package controllers;

import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.AuthService;
import services.RoleService;
import utils.ValidationUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneNumberField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField repeatPasswordField;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink loginLink;

    private AuthService authService;
    private RoleService roleService;

    public RegisterController() {
        // Initialize services
        authService = AuthService.getInstance();
        roleService = RoleService.getInstance();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // No longer need to initialize roleComboBox
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        // Get field values
        String name = nameField.getText();
        String email = emailField.getText();
        String phoneNumber = phoneNumberField.getText();
        String password = passwordField.getText();
        String repeatPassword = repeatPasswordField.getText();

        // Validate all fields
        if (!validateAllFields(name, email, phoneNumber, password, repeatPassword)) {
            return;
        }

        try {
            // Check if email already exists
            if (authService.emailExists(email)) {
                showAlert(Alert.AlertType.ERROR, "Registration Error", "This email is already in use.");
                return;
            }

            // Check if phone number already exists
            if (authService.phoneExists(phoneNumber)) {
                showAlert(Alert.AlertType.ERROR, "Registration Error", "This phone number is already in use.");
                return;
            }

            // Create new user
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPhone_number(phoneNumber);
            user.setPassword(password);
            user.setVerified(false);
            user.setCreated_at(new Timestamp(System.currentTimeMillis()));

            // Add default role
            List<String> roles = new ArrayList<>();
            roles.add("ROLE_USER");
            user.setRole(roles);

            // Register user with authentication service
            authService.register(user);

            // Add role to user in database
            try {
                // Get newly created user ID
                User createdUser = authService.getUserByEmail(email);
                if (createdUser != null) {
                    roleService.addRoleToUser(createdUser.getId(), "ROLE_USER");
                }
            } catch (SQLException ex) {
                System.err.println("Error adding role: " + ex.getMessage());
                // Don't block registration if role addition fails
            }

            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "Your account has been created successfully. You will be redirected to the two-factor authentication setup.");

            // Redirect to 2FA setup page
            navigateTo2FASetup(email);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "An error occurred during registration: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to load the login page.");
            e.printStackTrace();
        }
    }

    private boolean validateAllFields(String name, String email, String phoneNumber, String password, String repeatPassword) {
        // Validate name
        String nameError = ValidationUtils.getNameValidationError(name);
        if (nameError != null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", nameError);
            return false;
        }

        // Validate email
        String emailError = ValidationUtils.getEmailValidationError(email);
        if (emailError != null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", emailError);
            return false;
        }

        // Validate phone number
        String phoneError = ValidationUtils.getPhoneValidationError(phoneNumber);
        if (phoneError != null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", phoneError);
            return false;
        }

        // Validate password
        String passwordError = ValidationUtils.getPasswordValidationError(password);
        if (passwordError != null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", passwordError);
            return false;
        }

        // Validate repeat password
        String repeatPasswordError = ValidationUtils.getRepeatPasswordValidationError(password, repeatPassword);
        if (repeatPasswordError != null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", repeatPasswordError);
            return false;
        }

        return true;
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        try {
            // Load login page
            File file = new File("src/main/resources/fxml/Login.fxml");
            if (file.exists()) {
                URL url = file.toURI().toURL();
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();

                // Configure scene
                Stage stage = (Stage) loginLink.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Login");
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "FXML file not found: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to load the login page.");
            e.printStackTrace();
        }
    }

    private void navigateToLogin() throws IOException {
        // Load login page
        File file = new File("src/main/resources/fxml/Login.fxml");
        if (file.exists()) {
            URL url = file.toURI().toURL();
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            // Configure scene
            Stage stage = (Stage) registerButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        } else {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "FXML file not found: " + file.getAbsolutePath());
        }
    }

    /**
     * Navigate to 2FA setup page
     * @param email The user's email
     * @throws IOException In case of error loading the page
     */
    private void navigateTo2FASetup(String email) throws IOException {
        // Load 2FA setup page
        File file = new File("src/main/resources/fxml/2fa.fxml");
        if (file.exists()) {
            URL url = file.toURI().toURL();
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            // Get controller and pass email
            controller2fa controller = loader.getController();
            controller.setEmail(email);

            // Configure scene
            Stage stage = (Stage) registerButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Two-Factor Authentication Setup");
            stage.show();
        } else {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "FXML file not found: " + file.getAbsolutePath());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
