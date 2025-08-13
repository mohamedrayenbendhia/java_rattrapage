package controllers;

import entities.User;
import entities.UserSession;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import services.UserService;
import utils.ValidationUtils;
import utils.PasswordHasher;
import utils.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.util.Optional;
import java.sql.SQLException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ProfileUserController implements Initializable {

    @FXML
    private ImageView profileImage;

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private Label verificationStatusLabel;

    @FXML
    private Label accountStatusLabel;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button changePasswordButton;

    @FXML
    private Button setup2FAButton;

    private User currentUser;
    private UserService userService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser le service utilisateur
        userService = UserService.getInstance();

        // Récupérer l'utilisateur connecté depuis UserSession
        currentUser = UserSession.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Remplir les champs avec les informations de l'utilisateur
            loadUserData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "No user logged in");
        }

        // Configurer les actions des boutons
        setupButtonActions();
    }

    /**
     * Load user data into form fields
     */
    private void loadUserData() {
        // Fill text fields
        nameField.setText(currentUser.getName());
        emailField.setText(currentUser.getEmail());
        phoneField.setText(currentUser.getPhone_number());
        
        // Set verification status
        verificationStatusLabel.setText(currentUser.isVerified() ? "Verified" : "Not Verified");
        verificationStatusLabel.setStyle(currentUser.isVerified() ? 
            "-fx-text-fill: green; -fx-font-weight: bold;" : 
            "-fx-text-fill: red; -fx-font-weight: bold;");
            
        // Set account status
        accountStatusLabel.setText(currentUser.isBlocked() ? "Blocked" : "Active");
        accountStatusLabel.setStyle(currentUser.isBlocked() ? 
            "-fx-text-fill: red; -fx-font-weight: bold;" : 
            "-fx-text-fill: green; -fx-font-weight: bold;");

        // Load profile image if available
        if (currentUser.getImage() != null && !currentUser.getImage().isEmpty()) {
            try {
                File imageFile = new File(currentUser.getImage());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    profileImage.setImage(image);
                } else {
                    // Load default image
                    loadDefaultProfileImage();
                }
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
                loadDefaultProfileImage();
            }
        } else {
            // Charger une image par défaut
            loadDefaultProfileImage();
        }

        // Note: La date de naissance n'est pas dans le modèle User actuel
        // Si vous ajoutez ce champ plus tard, vous pourrez le remplir ici
        // birthDatePicker.setValue(currentUser.getBirthDate());
    }

    /**
     * Charge une image de profil par défaut
     */
    private void loadDefaultProfileImage() {
        try {
            // Charger une image par défaut depuis les ressources
            // Essayer plusieurs chemins possibles
            URL defaultImageUrl = getClass().getResource("/images/default-profile.png");

            if (defaultImageUrl == null) {
                // Essayer un autre chemin
                defaultImageUrl = getClass().getClassLoader().getResource("images/default-profile.png");
            }

            if (defaultImageUrl == null) {
                // Essayer un chemin absolu
                File file = new File("src/main/resources/images/default-profile.png");
                if (file.exists()) {
                    defaultImageUrl = file.toURI().toURL();
                }
            }

            if (defaultImageUrl != null) {
                Image defaultImage = new Image(defaultImageUrl.toString());
                profileImage.setImage(defaultImage);
            } else {
                System.err.println("Image par défaut introuvable");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image par défaut: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configure les actions des boutons
     */
    private void setupButtonActions() {
        // Action du bouton Enregistrer
        saveButton.setOnAction(event -> handleSave());

        // Action du bouton Annuler
        cancelButton.setOnAction(event -> handleCancel());

        // Action du bouton Modifier mot de passe
        changePasswordButton.setOnAction(event -> handleChangePassword());

        // Action du bouton Configurer 2FA
        setup2FAButton.setOnAction(event -> handleSetup2FA());

        // Action pour changer l'image de profil
        profileImage.setOnMouseClicked(event -> handleChangeProfileImage());
    }

    /**
     * Handle Save button action
     */
    @FXML
    private void handleSave() {
        try {
            // Validation des champs
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            
            // Validation du nom
            if (!ValidationUtils.isValidName(name)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", 
                    "Name must be between 3 and 20 characters");
                nameField.requestFocus();
                return;
            }
            
            // Validation de l'email
            if (!ValidationUtils.isValidEmail(email)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", 
                    "Email address is not valid");
                emailField.requestFocus();
                return;
            }
            
            // Validation du téléphone
            if (!ValidationUtils.isValidPhoneNumber(phone)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", 
                    "Phone number must contain exactly 8 digits");
                phoneField.requestFocus();
                return;
            }

            // Update user information
            currentUser.setName(name);
            currentUser.setEmail(email);
            currentUser.setPhone_number(phone);

            try {
                // Check if email already exists for another user
                if (userService.emailExistsForOtherUser(email, currentUser.getId())) {
                    showAlert(Alert.AlertType.ERROR, "Error", "This email is already used by another user");
                    emailField.requestFocus();
                    return;
                }

                // Save changes to database
                userService.updateUser(currentUser);

                // Update user in session
                UserSession.getInstance().setCurrentUser(currentUser);
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error updating profile: " + e.getMessage());
                e.printStackTrace();
                return;
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error updating profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gère l'action du bouton Annuler
     */
    @FXML
    private void handleCancel() {
        // Recharger les données originales
        loadUserData();
    }

    /**
     * Gère l'action du bouton Modifier mot de passe
     */
    @FXML
    private void handleChangePassword() {
        // Créer une boîte de dialogue pour le changement de mot de passe
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter your current password and new password (leave new password empty to keep current)");

        // Configurer les boutons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Créer les champs de saisie
        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Current password");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New password (optional)");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");

        // Créer la mise en page
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Current password:"), 0, 0);
        grid.add(currentPasswordField, 1, 0);
        grid.add(new Label("New password:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(new Label("Confirm password:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Donner le focus au premier champ
        Platform.runLater(currentPasswordField::requestFocus);

        // Attendre la réponse de l'utilisateur
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String currentPassword = currentPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            // Vérifier que le mot de passe actuel est rempli
            if (currentPassword.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Current password is required");
                return;
            }

            // Si un nouveau mot de passe est fourni, vérifier la confirmation
            if (!newPassword.isEmpty()) {
                if (!newPassword.equals(confirmPassword)) {
                    showAlert(Alert.AlertType.ERROR, "Error", "New password and confirmation do not match");
                    return;
                }
            }

            // Vérifier que l'utilisateur a un mot de passe défini
            if (currentUser.getPassword() == null || currentUser.getPassword().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "User password is not set. Please contact administrator.");
                return;
            }

            // Vérifier que le mot de passe actuel est correct
            if (!PasswordHasher.verifyPassword(currentPassword, currentUser.getPassword())) {
                showAlert(Alert.AlertType.ERROR, "Error", "Current password is incorrect");
                return;
            }

            try {
                // Si un nouveau mot de passe est fourni, le mettre à jour
                if (!newPassword.isEmpty()) {
                    // Demander confirmation avant de changer le mot de passe
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Password Change Confirmation");
                    confirmAlert.setHeaderText("Confirm Password Change");
                    confirmAlert.setContentText("Are you sure you want to change your password?");
                    
                    Optional<ButtonType> confirmResult = confirmAlert.showAndWait();
                    if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                        // Mettre à jour le mot de passe dans la base de données
                        userService.updatePassword(currentUser.getId(), newPassword);

                        // Mettre à jour le mot de passe dans l'objet utilisateur avec le hash
                        String hashedNewPassword = PasswordHasher.hashPassword(newPassword);
                        currentUser.setPassword(hashedNewPassword);

                        // Mettre à jour l'utilisateur dans la session
                        UserSession.getInstance().setCurrentUser(currentUser);

                        showAlert(Alert.AlertType.INFORMATION, "Success", "Your password has been successfully changed!");
                    } else {
                        showAlert(Alert.AlertType.INFORMATION, "Information", "Password change cancelled");
                    }
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Information", "No password change requested");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error updating password: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Gère l'action de changement d'image de profil
     */
    @FXML
    private void handleChangeProfileImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // Afficher la boîte de dialogue de sélection de fichier
        File selectedFile = fileChooser.showOpenDialog(profileImage.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Charger l'image sélectionnée
                Image image = new Image(selectedFile.toURI().toString());
                profileImage.setImage(image);

                // Update image path in user object
                currentUser.setImage(selectedFile.getAbsolutePath());

                try {
                    // Enregistrer le chemin de l'image dans la base de données
                    userService.updateUser(currentUser);

                    // Mettre à jour l'utilisateur dans la session
                    UserSession.getInstance().setCurrentUser(currentUser);

                    showAlert(Alert.AlertType.INFORMATION, "Success", "Your profile image has been updated successfully");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Error updating profile image: " + e.getMessage());
                    e.printStackTrace();
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error loading image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Gère l'action du bouton Configurer l'authentification à deux facteurs
     */
    @FXML
    private void handleSetup2FA() {
        try {
            // Charger la vue 2FA
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/2fa.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur et lui passer l'email de l'utilisateur
            controller2fa controller = loader.getController();
            controller.setEmail(currentUser.getEmail());

            // Afficher la fenêtre 2FA
            Stage stage = new Stage();
            stage.setTitle("Two-Factor Authentication Setup");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading 2FA page: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to load 2FA setup page: " + e.getMessage());
        }
    }

    /**
     * Retourne au dashboard
     */
    @FXML
    private void handleBackToDashboard() {
        try {
            Stage stage = (Stage) nameField.getScene().getWindow();
            
            // Vérifier le rôle de l'utilisateur pour rediriger vers le bon dashboard
            User user = UserSession.getInstance().getCurrentUser();
            if (user != null && user.getRole() != null && !user.getRole().isEmpty()) {
                String firstRole = user.getRole().get(0);
                if ("ROLE_ADMIN".equals(firstRole) || "ROLE_SUPER_ADMIN".equals(firstRole)) {
                    SceneManager.navigateToPage(stage, "/fxml/admin/AdminDashboard.fxml", "Admin Dashboard");
                } else {
                    SceneManager.navigateToPage(stage, "/fxml/ClientDashboard.fxml", "Dashboard");
                }
            } else {
                // Par défaut, rediriger vers le dashboard client
                SceneManager.navigateToPage(stage, "/fxml/ClientDashboard.fxml", "Dashboard");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner au dashboard: " + e.getMessage());
        }
    }

    /**
     * Affiche une alerte
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
