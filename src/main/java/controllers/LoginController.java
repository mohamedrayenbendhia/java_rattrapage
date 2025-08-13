package controllers;

import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.stage.Stage;

import services.AuthService;
import services.RoleService;
import utils.SceneManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink registerLink;

    @FXML
    private Hyperlink forgotPasswordLink;

    private AuthService authService;
    private RoleService roleService;

    public LoginController() {
        // Initialiser les services
        authService = AuthService.getInstance();
        roleService = RoleService.getInstance();
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        // Vérifier que les champs ne sont pas vides
        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Please fill in all fields.");
            return;
        }

        try {
            // Vérifier les identifiants avec le service d'authentification
            User user = authService.login(email, password);

            if (user != null) {
                // Vérifier si le compte de l'utilisateur est vérifié
                if (!user.isVerified()) {
                    // Ask user if they want to configure two-factor authentication now
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Account Not Verified");
                    alert.setHeaderText("Your account has not been verified yet");
                    alert.setContentText("Would you like to configure two-factor authentication now?");

                    ButtonType buttonTypeYes = new ButtonType("Yes");
                    ButtonType buttonTypeNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

                    alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == buttonTypeYes) {
                        // Redirect to 2FA setup page
                        navigateTo2FASetup(user.getEmail());
                    }
                    return;
                }

                // Connexion réussie (2FA désactivé)
                showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Welcome " + user.getName() + "!");

                // Rediriger vers le tableau de bord approprié en fonction du rôle
                navigateToDashboard(user);
            } else {
                // Login failed
                showAlert(Alert.AlertType.ERROR, "Login Error", "Incorrect email or password.");
            }
        } catch (IllegalStateException e) {
            // Handle blocked account
            showAlert(Alert.AlertType.ERROR, "Account Blocked", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to load dashboard page.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        try {
            // Utiliser SceneManager pour naviguer avec une taille fixe
            Stage stage = (Stage) registerLink.getScene().getWindow();
            SceneManager.navigateToPage(stage, "/fxml/Register.fxml", "Registration");
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to load registration page.");
            e.printStackTrace();
        }
    }

    /**
     * Gère le clic sur le lien "Mot de passe oublié"
     */
    @FXML
    private void handleForgotPassword(ActionEvent event) {
        try {
            // Utiliser SceneManager pour naviguer avec une taille fixe
            Stage stage = (Stage) forgotPasswordLink.getScene().getWindow();
            SceneManager.navigateToPage(stage, "/fxml/ResetPassword.fxml", "Password Reset");
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error loading password reset page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToDashboard(User user) throws IOException {
        try {
            // Determine user type based on roles
            String userType = roleService.getUserType(user);
            String title;

            // Choose appropriate dashboard
            String fxmlPath;
            if (userType != null && (userType.equals(RoleService.ROLE_ADMIN) || userType.equals(RoleService.ROLE_SUPER_ADMIN))) {
                fxmlPath = "/fxml/admin/AdminDashboard.fxml";
                title = "Admin Dashboard";
            } else {
                // Default to user dashboard for ROLE_USER
                fxmlPath = "/fxml/ClientDashboard.fxml";
                title = "User Dashboard";
            }

            // Utiliser SceneManager pour naviguer avec une taille fixe
            Stage stage = (Stage) loginButton.getScene().getWindow();
            SceneManager.navigateToPage(stage, fxmlPath, title);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Role Error", "Unable to determine user role: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    /**
     * Navigue vers la page de configuration 2FA
     * @param email L'email de l'utilisateur
     */
    private void navigateTo2FASetup(String email) {
        try {
            // Charger la page de configuration 2FA
            File file = new File("src/main/resources/fxml/2fa.fxml");
            if (file.exists()) {
                URL url = file.toURI().toURL();
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();

                // Récupérer le contrôleur et lui passer l'email
                controller2fa controller = loader.getController();
                controller.setEmail(email);

                // Configurer la scène
                Stage stage = (Stage) loginButton.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Two-Factor Authentication Setup");
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "FXML file not found: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error loading 2FA page: " + e.getMessage());
            e.printStackTrace();
        }
    }




}
