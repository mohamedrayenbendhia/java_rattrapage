package controllers.Admin;

import entities.User;
import entities.UserSession;
import services.UserService;
import utils.ValidationUtils;
import utils.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class UserListController implements Initializable {

    @FXML
    private ListView<User> userListView;

    @FXML
    private Button backButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button addUserButton;

    @FXML
    private Label messageLabel;

    private UserService userService;
    private ObservableList<User> userList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser le service utilisateur
        userService = UserService.getInstance();
        userList = FXCollections.observableArrayList();
        
        // Configurer la ListView pour afficher les utilisateurs
        userListView.setItems(userList);
        userListView.setCellFactory(listView -> new UserListCell());
        
        // Charger les utilisateurs
        loadUsers();
    }

    /**
     * Cellule personnalisée pour afficher les utilisateurs
     */
    private class UserListCell extends ListCell<User> {
        @Override
        protected void updateItem(User user, boolean empty) {
            super.updateItem(user, empty);
            
            if (empty || user == null) {
                setGraphic(null);
                setText(null);
            } else {
                // Créer un conteneur pour l'utilisateur
                VBox userContainer = new VBox(5);
                userContainer.setStyle("-fx-padding: 10; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");
                
                // Informations principales
                Label nameLabel = new Label("Name: " + user.getName());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                
                Label emailLabel = new Label("Email: " + user.getEmail());
                emailLabel.setStyle("-fx-font-size: 12px;");
                
                Label phoneLabel = new Label("Phone: " + user.getPhone_number());
                phoneLabel.setStyle("-fx-font-size: 12px;");
                
                // Statuts
                HBox statusBox = new HBox(10);
                
                Label verifiedLabel = new Label(user.isVerified() ? "Verified" : "Not Verified");
                verifiedLabel.setStyle(user.isVerified() ? 
                    "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10;" :
                    "-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10;");
                
                Label blockedLabel = new Label(user.isBlocked() ? "Blocked" : "Active");
                blockedLabel.setStyle(user.isBlocked() ? 
                    "-fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10;" :
                    "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10;");
                
                statusBox.getChildren().addAll(verifiedLabel, blockedLabel);
                
                // Boutons d'action
                HBox actionBox = new HBox(5);
                
                Button editButton = new Button("Edit");
                editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 5 10;");
                editButton.setOnAction(e -> editUser(user));
                
                // Only show block/unblock button for client users (ROLE_USER only)
                boolean isClientOnly = user.getRole().contains("ROLE_USER") && 
                                     !user.getRole().contains("ROLE_ADMIN") && 
                                     !user.getRole().contains("ROLE_SUPER_ADMIN");
                
                if (isClientOnly) {
                    Button toggleBlockButton = new Button(user.isBlocked() ? "Unblock" : "Block");
                    toggleBlockButton.setStyle(user.isBlocked() ? 
                        "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 10;" :
                        "-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 5 10;");
                    toggleBlockButton.setOnAction(e -> toggleBlockUser(user));
                    actionBox.getChildren().addAll(editButton, toggleBlockButton);
                } else {
                    actionBox.getChildren().add(editButton);
                }
                
                userContainer.getChildren().addAll(nameLabel, emailLabel, phoneLabel, statusBox, actionBox);
                setGraphic(userContainer);
            }
        }
    }

    /**
     * Charger la liste des utilisateurs
     */
    private void loadUsers() {
        try {
            // Get current user from session
            User currentUser = UserSession.getInstance().getCurrentUser();
            List<User> users;
            
            if (currentUser != null) {
                // Exclude current user and super admin from list
                users = userService.getAllUsersExcludingCurrentAndSuperAdmin(currentUser.getId());
            } else {
                // Fallback to all users if no session
                users = userService.getAllUsers();
            }
            
            userList.clear();
            userList.addAll(users);
            messageLabel.setText("Number of users: " + users.size());
        } catch (SQLException e) {
            messageLabel.setText("Error loading users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Rafraîchir la liste des utilisateurs
     */
    @FXML
    private void handleRefresh() {
        loadUsers();
    }

    /**
     * Handle add user button click
     */
    @FXML
    private void handleAddUser() {
        // Get current user to check role permissions
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No user logged in");
            return;
        }
        
        // Determine current user's highest role
        String currentUserRole = "ROLE_USER";
        if (currentUser.getRole().contains("ROLE_SUPER_ADMIN")) {
            currentUserRole = "ROLE_SUPER_ADMIN";
        } else if (currentUser.getRole().contains("ROLE_ADMIN")) {
            currentUserRole = "ROLE_ADMIN";
        }
        
        showAddUserDialog(currentUserRole);
    }

    /**
     * Bouton retour
     */
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Stage stage = (Stage) userListView.getScene().getWindow();
            SceneManager.navigateToPage(stage, "/fxml/admin/AdminDashboard.fxml", "Admin Dashboard");
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Edit a user
     */
    private void editUser(User user) {
        // Create dialog to edit user
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit information for " + user.getName());

        // Configurer les boutons
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Créer les champs de formulaire
        VBox formContainer = new VBox(10);
        formContainer.setStyle("-fx-padding: 20;");

        TextField nameField = new TextField(user.getName());
        nameField.setPromptText("Full name");
        
        TextField emailField = new TextField(user.getEmail());
        emailField.setPromptText("Email");
        
        TextField phoneField = new TextField(user.getPhone_number());
        phoneField.setPromptText("Phone");
        
        CheckBox verifiedCheckBox = new CheckBox("Account verified");
        verifiedCheckBox.setSelected(user.isVerified());
        
        CheckBox blockedCheckBox = new CheckBox("Account blocked");
        blockedCheckBox.setSelected(user.isBlocked());

        formContainer.getChildren().addAll(
            new Label("Name:"), nameField,
            new Label("Email:"), emailField,
            new Label("Phone:"), phoneField,
            verifiedCheckBox,
            blockedCheckBox
        );

        dialog.getDialogPane().setContent(formContainer);

        // Validation et conversion du résultat
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            // Validation des champs
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            
            if (!ValidationUtils.isValidName(name)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", 
                    "Name must be between 3 and 20 characters");
                event.consume();
                return;
            }
            
            if (!ValidationUtils.isValidEmail(email)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", 
                    "Email address is not valid");
                event.consume();
                return;
            }
            
            if (!ValidationUtils.isValidPhoneNumber(phone)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", 
                    "Phone number must contain exactly 8 digits");
                event.consume();
                return;
            }

            // Mettre à jour l'utilisateur
            user.setName(name);
            user.setEmail(email);
            user.setPhone_number(phone);
            user.setVerified(verifiedCheckBox.isSelected());
            user.setBlocked(blockedCheckBox.isSelected());

            try {
                userService.updateUser(user);
                loadUsers(); // Rafraîchir la liste
                showAlert(Alert.AlertType.INFORMATION, "Success", "User updated successfully");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error updating user: " + e.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    /**
     * Basculer le statut bloqué/débloqué d'un utilisateur
     */
    private void toggleBlockUser(User user) {
        try {
            // Check if user is a client (only clients can be blocked)
            boolean isClientOnly = user.getRole().contains("ROLE_USER") && 
                                 !user.getRole().contains("ROLE_ADMIN") && 
                                 !user.getRole().contains("ROLE_SUPER_ADMIN");
            
            if (!isClientOnly) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Only client users can be blocked/unblocked.");
                return;
            }
            
            userService.updateUserBlockStatus(user.getId(), !user.isBlocked());
            loadUsers(); // Refresh the list
            
            String message = !user.isBlocked() ? 
                "User blocked successfully" : 
                "User unblocked successfully";
            showAlert(Alert.AlertType.INFORMATION, "Success", message);
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Warning", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error updating user: " + e.getMessage());
        }
    }

    /**
     * Afficher une alerte
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show add user dialog
     */
    private void showAddUserDialog(String currentUserRole) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Create a new user account");

        // Configure buttons
        ButtonType addButtonType = new ButtonType("Add User", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create form fields
        VBox formContainer = new VBox(10);
        formContainer.setStyle("-fx-padding: 20;");

        TextField nameField = new TextField();
        nameField.setPromptText("Full name");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email address");
        
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone number");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (minimum 8 characters)");
        
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Repeat password");
        
        ComboBox<String> roleComboBox = new ComboBox<>();
        if (currentUserRole.equals("ROLE_SUPER_ADMIN")) {
            roleComboBox.getItems().addAll("ROLE_USER", "ROLE_ADMIN");
        } else {
            roleComboBox.getItems().add("ROLE_USER");
        }
        roleComboBox.setValue("ROLE_USER");
        
        CheckBox verifiedCheckBox = new CheckBox("Account verified");
        verifiedCheckBox.setSelected(false);

        formContainer.getChildren().addAll(
            new Label("Full Name:"), nameField,
            new Label("Email:"), emailField,
            new Label("Phone:"), phoneField,
            new Label("Password:"), passwordField,
            new Label("Repeat Password:"), confirmPasswordField,
            new Label("Role:"), roleComboBox,
            verifiedCheckBox
        );

        dialog.getDialogPane().setContent(formContainer);

        // Validation and result conversion
        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.addEventFilter(ActionEvent.ACTION, event -> {
            // Validate fields
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String password = passwordField.getText().trim();
            String confirmPassword = confirmPasswordField.getText().trim();
            String role = roleComboBox.getValue();
            
            if (!ValidationUtils.isValidName(name)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", 
                    "Name must be between 3 and 20 characters");
                event.consume();
                return;
            }
            
            if (!ValidationUtils.isValidEmail(email)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", 
                    "Email address is not valid");
                event.consume();
                return;
            }
            
            if (!ValidationUtils.isValidPhoneNumber(phone)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", 
                    "Phone number must contain exactly 8 digits");
                event.consume();
                return;
            }
            
            if (!ValidationUtils.isValidPassword(password)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", 
                    "Password must be at least 8 characters long");
                event.consume();
                return;
            }
            
            if (!ValidationUtils.doPasswordsMatch(password, confirmPassword)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", 
                    "Passwords do not match");
                event.consume();
                return;
            }

            // Create new user
            User newUser = new User();
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setPhone_number(phone);
            newUser.setPassword(password);
            newUser.setVerified(verifiedCheckBox.isSelected());
            newUser.setBlocked(false);
            
            // Set role
            List<String> roles = new ArrayList<>();
            roles.add(role);
            newUser.setRole(roles);

            try {
                userService.addUser(newUser, currentUserRole);
                loadUsers(); // Refresh the list
                showAlert(Alert.AlertType.INFORMATION, "Success", "User added successfully");
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                event.consume();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error adding user: " + e.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
    }
}
